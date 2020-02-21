/** *****************************************************************************
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
package emlab.gen.role.market;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import emlab.gen.domain.agent.EMLabModel;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.agent.NationalGovernment;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.CO2Auction;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.technology.Interconnector;
import emlab.gen.domain.technology.Substance;
import emlab.gen.engine.Role;
import emlab.gen.engine.Schedule;
import emlab.gen.repository.Reps;
import emlab.gen.role.co2policy.MarketStabilityReserveRole;
import emlab.gen.role.co2policy.RenewableAdaptiveCO2CapRole;
import emlab.gen.role.operating.DetermineFuelMixRole;

/**
 * Creates and clears the {@link ElectricitySpotMarket} for two {@link Zone}s.
 * The market is divided into {@link Segment}s and cleared for each segment. A
 * global CO2 emissions market is cleared. The process is iterative and the
 * target is to let the total emissions match the cap.
 *
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 *
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 * Chmieliauskas</a>
 *
 * @author <a href="mailto:J.C.Richstein@tudelft.nl">Joern C. Richstein</a>
 *
 */
public class ClearIterativeCO2AndElectricitySpotMarketTwoCountryRole extends
        AbstractClearElectricitySpotMarketRole<EMLabModel> implements Role<EMLabModel> {

    SubmitOffersToElectricitySpotMarketRole submitOffersToElectricitySpotMarketRole = new SubmitOffersToElectricitySpotMarketRole(schedule);

    DetermineFuelMixRole determineFuelMixRole = new DetermineFuelMixRole(schedule);

    MarketStabilityReserveRole marketStabilityReserveRole = new MarketStabilityReserveRole(schedule);

    RenewableAdaptiveCO2CapRole renewableAdaptiveCO2CapRole = new RenewableAdaptiveCO2CapRole(schedule);

    public ClearIterativeCO2AndElectricitySpotMarketTwoCountryRole(Schedule schedule) {
        super(schedule);
    }

    public void act(EMLabModel model) {
        Map<Substance, Double> fuelPriceMap = new HashMap<Substance, Double>();
        for (Substance substance : getReps().substances) {
            fuelPriceMap.put(substance, findLastKnownPriceForSubstance(substance));
        }

        if (!model.isCo2BankingIsImplemented() || !model.isCo2TradingImplemented()) {
            clearIterativeCO2AndElectricitySpotMarketTwoCountryForTimestepAndFuelPrices(model, false, getCurrentTick(),
                    fuelPriceMap, null, 0);
        } else {
            clearIterativeCO2ElectricitySpotMarketAndFutureMarketTwoCountryForTimestepAndFuelPrices(model,
                    getCurrentTick());
        }

    }

    public void makeCentralElectricityMarketForecastForTimeStep(long clearingTick) {

        EMLabModel model = getReps().emlabModel;

        Map<Substance, Double> fuelPriceMap = predictFuelPrices(model.getCentralForecastBacklookingYears(),
                clearingTick);

        // logger.warn("Fuel prices: {}", fuelPriceMap);
        Map<ElectricitySpotMarket, Double> demandGrowthMap = predictDemand(model.getCentralForecastBacklookingYears(),
                clearingTick);

        Government government = getReps().government;

        // find national minimum CO2 prices. Initial Map size is 2.
        Map<ElectricitySpotMarket, Double> nationalMinCo2Prices = new HashMap<ElectricitySpotMarket, Double>(2);
        Iterable<NationalGovernment> nationalGovernments = getReps().nationalGovernments;
        for (NationalGovernment nG : nationalGovernments) {
            if (model.isCo2TradingImplemented()) {
                nationalMinCo2Prices.put(getReps().findElectricitySpotMarketByNationalGovernment(nG), nG
                        .getMinNationalCo2PriceTrend().getValue(clearingTick));
            } else {
                nationalMinCo2Prices.put(getReps().findElectricitySpotMarketByNationalGovernment(nG), 0d);
            }

        }

        determineFuelMixRole.determineFuelMixForecastForYearAndFuelPriceMap(clearingTick, fuelPriceMap,
                nationalMinCo2Prices);

        submitOffersToElectricitySpotMarketRole.createOffersForElectricitySpotMarket(null, clearingTick, true,
                fuelPriceMap);

        Iterable<PowerPlantDispatchPlan> ppdps = getReps().powerPlantDispatchPlans;
        for (PowerPlantDispatchPlan ppdp : ppdps) {
            logger.info(ppdp.toString() + " in " + ppdp.getBiddingMarket() + " accepted: " + ppdp.getAcceptedAmount());
        }

        double previouslyBankedCertificates = getReps().determineTotallyBankedCO2Certificates();

        if (clearingTick > model.getCentralForecastingYear()) {
            CO2Auction co2Auction = getReps().co2Auction;
            ClearingPoint forecastedCO2Clearing = getReps().findClearingPointForMarketAndTime(
                    co2Auction, clearingTick - 1, true);
            ClearingPoint lastCO2Clearing = getReps().findClearingPointForMarketAndTime(co2Auction,
                    clearingTick - model.getCentralForecastingYear() - 1, false);

            double targetProducerBanking = calculateTargetCO2EmissionBankingOfEnergyProducers(clearingTick,
                    clearingTick + model.getCentralForecastingYear(), government, model);

            clearIterativeCO2AndElectricitySpotMarketTwoCountryForTimestepAndFuelPrices(model, true, clearingTick,
                    fuelPriceMap, demandGrowthMap,
                    (previouslyBankedCertificates - targetProducerBanking) / model.getCentralForecastingYear());
        } else {
            clearIterativeCO2AndElectricitySpotMarketTwoCountryForTimestepAndFuelPrices(model, true, clearingTick,
                    fuelPriceMap, demandGrowthMap, 0);
        }

    }

    public CO2SecantSearch clearIterativeCO2AndElectricitySpotMarketTwoCountryForTimestepAndFuelPrices(
            EMLabModel model, boolean forecast, long clearingTick, Map<Substance, Double> fuelPriceMap,
            Map<ElectricitySpotMarket, Double> demandGrowthMap, double co2CapAdjustment) {

        if (model == null) {
            model = getReps().emlabModel;
        }

        if (fuelPriceMap == null && forecast) {
            fuelPriceMap = predictFuelPrices(model.getCentralForecastBacklookingYears(), clearingTick);
        }

        if (demandGrowthMap == null && forecast) {
            demandGrowthMap = predictDemand(model.getCentralForecastBacklookingYears(), clearingTick);
        }

        // find all operational power plants and store the ones operational to a
        // list.
        logger.info("Clearing the CO2 and electricity spot markets using iteration for 2 countries ");

        // find all fuel prices
        // find all interconnectors
        Interconnector interconnector = getReps().interconnector;

        // find all segments
        List<Segment> segments = getReps().segments;

        // find the EU government
        Government government = getReps().government;

        // find national minimum CO2 prices. Initial Map size is 2.
        Map<ElectricitySpotMarket, Double> nationalMinCo2Prices = new HashMap<ElectricitySpotMarket, Double>(2);
        Iterable<NationalGovernment> nationalGovernments = getReps().nationalGovernments;
        for (NationalGovernment nG : nationalGovernments) {
            if (model.isCo2TradingImplemented()) {
                nationalMinCo2Prices.put(getReps().findElectricitySpotMarketByNationalGovernment(nG), nG
                        .getMinNationalCo2PriceTrend().getValue(clearingTick));
            } else {
                nationalMinCo2Prices.put(getReps().findElectricitySpotMarketByNationalGovernment(nG), 0d);
            }

        }

        CO2Auction co2Auction = getReps().co2Auction;
        CO2SecantSearch co2SecantSearch = null;

        if (model.isCo2TradingImplemented()) {

            co2SecantSearch = new CO2SecantSearch();
            co2SecantSearch.stable = false;
            co2SecantSearch.twoPricesExistWithBelowAboveEmissions = false;
            co2SecantSearch.co2Price = findLastKnownPriceOnMarket(co2Auction);
            co2SecantSearch.tooHighEmissionsPair = null;
            co2SecantSearch.tooLowEmissionsPair = null;

            // Change Iteration algorithm here, and a few lines below...
            ClearingPoint lastClearingPointOfCo2Market = getReps().findClearingPointForMarketAndTime(co2Auction, getCurrentTick() - 1, false);
            if (lastClearingPointOfCo2Market != null) {
                co2SecantSearch.co2Emissions = lastClearingPointOfCo2Market.getVolume();
            } else {
                co2SecantSearch.co2Emissions = 0d;
            }

            int breakOffIterator = 0;
            while (!co2SecantSearch.stable) {
                logger.info("Clearing secant search iteration " + breakOffIterator);
                
                if (breakOffIterator > 15) {
                    logger.info("Iteration cancelled, last found CO2 Price is used.");
                    break;
                }

                // Clear the electricity markets with the expected co2Price
                clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForSegments(government, clearingTick, forecast,
                        demandGrowthMap, fuelPriceMap, co2SecantSearch.co2Price, nationalMinCo2Prices, segments,
                        interconnector, model);

                co2SecantSearch = co2PriceSecantSearchUpdate(co2SecantSearch, model, government, forecast,
                        clearingTick, co2CapAdjustment);
                breakOffIterator++;
                logger.info("Secant search iteration outcome: " + co2SecantSearch);
            }

            getReps().createOrUpdateClearingPoint(co2Auction, co2SecantSearch.co2Price,
                    co2SecantSearch.co2Emissions, clearingTick, forecast);
        } else {
            if (model.isLongTermContractsImplemented()) {
                determineCommitmentOfPowerPlantsOnTheBasisOfLongTermContracts(segments, forecast);
            }
            for (Segment segment : segments) {
                clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForOneSegment(
                        interconnector.getCapacity(clearingTick),
                        segment, government, clearingTick, forecast, demandGrowthMap);
            }

        }

        return co2SecantSearch;

    }

    /**
     * Clears a time segment of all electricity markets for a given CO2 price.
     *
     * @param powerPlants to be used
     * @param markets to clear
     * @return the total CO2 emissions
     */
    void clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForOneSegment(double interconnectorCapacity,
            Segment segment, Government government, long clearingTick, boolean forecast,
            Map<ElectricitySpotMarket, Double> demandGrowthMap) {

        logger.info("Clearing segment " + segment );
        GlobalSegmentClearingOutcome globalOutcome = new GlobalSegmentClearingOutcome();

        globalOutcome.loads = determineActualDemandForSpotMarkets(segment, demandGrowthMap);

        globalOutcome.globalLoad = determineTotalLoadFromLoadMap(globalOutcome.loads);

        // Keep track of supply per market. Start at 0.
        for (ElectricitySpotMarket m : getReps().electricitySpotMarkets) {
            globalOutcome.supplies.put(m, 0d);
        }

        // empty list of plants that are supplying.
        double marginalPlantMarginalCost = clearGlobalMarketWithNoCapacityConstraints(segment, globalOutcome, forecast,
                clearingTick);

        logger.info("Global loads: " + globalOutcome.loads.entrySet().toString());
        // For each plant in the cost-ordered list
        // Determine the flow over the interconnector.
        ElectricitySpotMarket firstMarket = getReps().electricitySpotMarkets.iterator().next();
        double loadInFirstMarket = globalOutcome.loads.get(firstMarket);
        double supplyInFirstMarket = globalOutcome.supplies.get(firstMarket);

        // Interconnector flow defined as from market A --> market B = positive
        double interconnectorFlow = supplyInFirstMarket - loadInFirstMarket;

        logger.info("Before market coupling interconnector flow: " + interconnectorFlow + " available interconnector capacity " + interconnectorCapacity);

        // if interconnector is not limiting or there is only one market, there
        // is one price
        if (getReps().electricitySpotMarkets.size() < 2
                || Math.abs(interconnectorFlow) + epsilon <= interconnectorCapacity) {
            // Set the price to the bid of the marginal plant.
            for (ElectricitySpotMarket market : getReps().electricitySpotMarkets) {
                double supplyInThisMarket = globalOutcome.supplies.get(market);
                
                // globalOutcome.globalSupply += supplyInThisMarket;
                if (globalOutcome.globalLoad <= globalOutcome.globalSupply + epsilon) {
                    globalOutcome.globalPrice = marginalPlantMarginalCost;
                } else {
                    globalOutcome.globalPrice = market.getValueOfLostLoad();
                }

                double interconenctorFlowForCurrentMarket = market.equals(firstMarket) ? interconnectorFlow * (-1.0)
                        : interconnectorFlow;

                getReps().createOrUpdateSegmentClearingPoint(segment, market,
                        globalOutcome.globalPrice,
                        (supplyInThisMarket + interconenctorFlowForCurrentMarket) * segment.getLengthInHours(),
                        (interconenctorFlowForCurrentMarket * segment.getLengthInHours()), clearingTick, forecast);
                logger.info("Stored a system-uniform price for market " + market + " / segment " + segment
                        + " -- supply " + supplyInThisMarket + " -- price: " + globalOutcome.globalPrice);
            }

        } else {
            
            MarketSegmentClearingOutcome marketOutcomes = new MarketSegmentClearingOutcome();
            for (ElectricitySpotMarket m : getReps().electricitySpotMarkets) {
                marketOutcomes.supplies.put(m, 0d);
                marketOutcomes.prices.put(m, m.getValueOfLostLoad());
            }

            // else there are two prices
            logger.info("There should be multiple prices, but first we should do market coupling.");

            boolean firstImporting = true;
            if (interconnectorFlow > 0) {
                firstImporting = false;
            }

            for (ElectricitySpotMarket market : getReps().electricitySpotMarkets) {
                boolean first = market.equals(firstMarket);
                // Update the load for this market. Which is market's true load
                // +/- the full interconnector capacity, based on direction of
                // the flow
                if ((first && firstImporting) || (!first && !firstImporting)) {
                    marketOutcomes.loads.put(market, globalOutcome.loads.get(market) - interconnectorCapacity);
                } else {
                    marketOutcomes.loads.put(market, globalOutcome.loads.get(market) + interconnectorCapacity);
                }

            }
            logger.info("Loads: " + marketOutcomes.loads.entrySet().toString());

            // For each plant in the cost-ordered list
            clearTwoInterconnectedMarketsGivenAnInterconnectorAdjustedLoad(segment, marketOutcomes, clearingTick,
                    forecast);

            // updatePowerDispatchPlansAfterTwoCountryClearingIsComplete(segment);
            for (ElectricitySpotMarket market : getReps().electricitySpotMarkets) {
                if (marketOutcomes.supplies.get(market) + epsilon < marketOutcomes.loads.get(market)) {
                    marketOutcomes.prices.put(market, market.getValueOfLostLoad());
                }
            }

            for (ElectricitySpotMarket market : getReps().electricitySpotMarkets) {
                double interconenctorFlowForCurrentMarket = (market.equals(firstMarket) && firstImporting)
                        || (!market.equals(firstMarket) && !firstImporting) ? interconnectorCapacity
                        : interconnectorCapacity * (-1.0);
                getReps().createOrUpdateSegmentClearingPoint(segment, market,
                        marketOutcomes.prices.get(market),
                        (marketOutcomes.supplies.get(market) + interconenctorFlowForCurrentMarket)
                        * segment.getLengthInHours(),
                        (interconenctorFlowForCurrentMarket * segment.getLengthInHours()), clearingTick, forecast);
                logger.info("Stored a market specific price for market " + market + " / segment " + segment
                        + " -- supply " + marketOutcomes.supplies.get(market) + " -- demand: "
                        + marketOutcomes.loads.get(market) + " -- price: " + marketOutcomes.prices.get(market));
            }
        }
    }

    void clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForSegments(Government government, long clearingTick,
            boolean forecast, Map<ElectricitySpotMarket, Double> demandGrowthMap, Map<Substance, Double> fuelPriceMap,
            double co2Price, Map<ElectricitySpotMarket, Double> nationalMinCo2Prices, List<Segment> segments,
            Interconnector interconnector, EMLabModel model) {

        submitOffersToElectricitySpotMarketRole.updateMarginalCostInclCO2AfterFuelMixChange(co2Price,
                nationalMinCo2Prices, clearingTick, forecast, fuelPriceMap);

        if (model.isLongTermContractsImplemented()) {
            determineCommitmentOfPowerPlantsOnTheBasisOfLongTermContracts(segments, forecast);
        }

        for (Segment segment : segments) {
            clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForOneSegment(
                    interconnector.getCapacity(clearingTick),
                    segment, government, clearingTick, forecast, demandGrowthMap);
        }
    }

    //TODO Emile feb 2020: what is breaking here?
    void clearTwoInterconnectedMarketsGivenAnInterconnectorAdjustedLoad(Segment segment,
            MarketSegmentClearingOutcome marketOutcomes, long clearingTick, boolean forecast) {

             

      
        for (PowerPlantDispatchPlan plan : getReps().findSortedPowerPlantDispatchPlansForSegmentForTime(segment, clearingTick, forecast)) {
            
            ElectricitySpotMarket myMarket = (ElectricitySpotMarket) plan.getBiddingMarket();//Why on this market?
            
            logger.info("Supply for market: " + myMarket + " equals " + marketOutcomes.supplies.get(myMarket));
            // Make it produce as long as there is load.
            double plantSupply = determineProductionOnSpotMarket(plan, marketOutcomes.supplies.get(myMarket),
                    marketOutcomes.loads.get(myMarket));
            if (plantSupply > 0) {
                // Plant is producing, store the information to
                // determine price and so on.
                marketOutcomes.supplies.put(myMarket, marketOutcomes.supplies.get(myMarket) + plantSupply);
                marketOutcomes.prices.put(myMarket, plan.getPrice());
                logger.info("1Storing price: " + plan.getPrice() + " and supply " + plantSupply +  " for plant " + plan.getPowerPlant() + " in market " + myMarket);
            } else {
                logger.info("2Storing price: " + plan.getPrice() + " and supply " + plantSupply +  " for plant " + plan.getPowerPlant() + " in market " + myMarket);
            }
        }

    }

    void clearCO2AndElectricitySpotMarketTwoCountryWithBanking(EMLabModel model, boolean forecast,
            long clearingTick, Map<Substance, Double> fuelPriceMap, Map<ElectricitySpotMarket, Double> demandGrowthMap) {

        double previouslyBankedCertificates = getReps().determineTotallyBankedCO2Certificates();

        // Check we are not in timestep 0
        if (clearingTick < 1) {
            clearIterativeCO2AndElectricitySpotMarketTwoCountryForTimestepAndFuelPrices(model, forecast, clearingTick,
                    fuelPriceMap, demandGrowthMap, 0);
            return;
        }
        CO2Auction co2Auction = getReps().co2Auction;

        // find all interconnectors
        Interconnector interconnector = getReps().interconnector;

        Government government = getReps().government;

        // find all segments
        List<Segment> segments = getReps().segments;

        ClearingPoint forecastedCO2Clearing = getReps().findClearingPointForMarketAndTime(
                co2Auction, clearingTick + model.getCentralForecastingYear(), true);
        ClearingPoint lastCO2Clearing = getReps().findClearingPointForMarketAndTime(co2Auction,
                clearingTick - 1, false);

        double maximumEnergyProducerBanking = calculateTargetCO2EmissionBankingOfEnergyProducers(clearingTick,
                clearingTick + model.getCentralForecastingYear(), government, model);

        CO2SecantSearch co2SecantSearch = clearIterativeCO2AndElectricitySpotMarketTwoCountryForTimestepAndFuelPrices(
                model, forecast, clearingTick, fuelPriceMap, demandGrowthMap, 0);

        double fundamentalCO2Price = calculateFundamentalCO2PriceForEnergyProducers(clearingTick, model, co2Auction,
                co2SecantSearch);
        logger.info("Fundanmental price: " + fundamentalCO2Price);

        // find national minimum CO2 prices. Initial Map size is 2.
        Map<ElectricitySpotMarket, Double> nationalMinCo2Prices = new HashMap<ElectricitySpotMarket, Double>(2);
        Iterable<NationalGovernment> nationalGovernments = getReps().nationalGovernments;
        for (NationalGovernment nG : nationalGovernments) {
            if (model.isCo2TradingImplemented()) {
                nationalMinCo2Prices.put(getReps().findElectricitySpotMarketByNationalGovernment(nG), nG
                        .getMinNationalCo2PriceTrend().getValue(clearingTick));
            } else {
                nationalMinCo2Prices.put(getReps().findElectricitySpotMarketByNationalGovernment(nG), 0d);
            }
        }

        double adjustedFundamentalCO2Price = fundamentalCO2Price
                * calculateCO2PriceReductionFactor(government, maximumEnergyProducerBanking,
                        previouslyBankedCertificates, 10, clearingTick);

        double previousAdjustedFundamentalCO2Price = adjustedFundamentalCO2Price;
        double co2emissions;
        double deltaBankedEmissionCertificates;

        // adjust fundamental CO2Price
        int i = 0;
        do {

            previousAdjustedFundamentalCO2Price = adjustedFundamentalCO2Price;

            clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForSegments(government, clearingTick, forecast,
                    demandGrowthMap, fuelPriceMap, adjustedFundamentalCO2Price, nationalMinCo2Prices, segments,
                    interconnector, model);

            co2emissions = determineTotalEmissionsBasedOnPowerPlantDispatchPlan(forecast, clearingTick);

            deltaBankedEmissionCertificates = government.getCo2Cap(clearingTick) - co2emissions;
            adjustedFundamentalCO2Price = fundamentalCO2Price
                    * calculateCO2PriceReductionFactor(government, maximumEnergyProducerBanking,
                            previouslyBankedCertificates, 10, clearingTick);

            i++;
            adjustedFundamentalCO2Price = fundamentalCO2Price
                    * calculateCO2PriceReductionFactor(government, maximumEnergyProducerBanking,
                            previouslyBankedCertificates + deltaBankedEmissionCertificates, 10, clearingTick);

            logger.info("CO2 Fundamental Price iteration " + i + ", Old: " + previousAdjustedFundamentalCO2Price + " New: " + adjustedFundamentalCO2Price);

        } while (Math.abs(adjustedFundamentalCO2Price - previousAdjustedFundamentalCO2Price) > 1);

        if (previouslyBankedCertificates + deltaBankedEmissionCertificates > 0) {

            getReps().createOrUpdateClearingPoint(co2Auction, adjustedFundamentalCO2Price,
                    co2emissions, clearingTick, forecast);

            for (EnergyProducer producer : getReps().energyProducers) {
                producer.setLastYearsCo2Allowances(producer.getCo2Allowances());
                double futureEmissionShare = determineTotalEmissionsBasedOnPowerPlantDispatchPlanForEnergyProducer(
                        true, clearingTick + model.getCentralForecastingYear(), producer)
                        / forecastedCO2Clearing.getVolume();
                double bankedEmissionsOfProducer = (deltaBankedEmissionCertificates + previouslyBankedCertificates)
                        * futureEmissionShare;
                producer.setCo2Allowances(bankedEmissionsOfProducer);
            }
        } else {
            clearIterativeCO2AndElectricitySpotMarketTwoCountryForTimestepAndFuelPrices(model, forecast, clearingTick,
                    fuelPriceMap, demandGrowthMap, previouslyBankedCertificates);
            for (EnergyProducer producer : getReps().energyProducers) {
                producer.setLastYearsCo2Allowances(producer.getCo2Allowances());
                producer.setCo2Allowances(0);
            }
        }

    }

    public void clearIterativeCO2ElectricitySpotMarketAndFutureMarketTwoCountryForTimestepAndFuelPrices(
            EMLabModel model, long clearingTick) {

        if (model == null) {
            model = getReps().emlabModel;
        }

        Map<Substance, Double> fuelPriceMap = new HashMap<Substance, Double>();
        for (Substance substance : getReps().substances) {
            fuelPriceMap.put(substance, findLastKnownPriceForSubstance(substance));
        }

        logger.info(fuelPriceMap.toString());

        Map<Substance, Double> futureFuelPriceMap = predictFuelPrices(model.getCentralForecastBacklookingYears(),
                clearingTick + model.getCentralForecastingYear());

        Map<ElectricitySpotMarket, Double> futureDemandGrowthMap = predictDemand(
                model.getCentralForecastBacklookingYears(), clearingTick + model.getCentralForecastingYear());

        // find national minimum CO2 prices. Initial Map size is 2.
        Map<ElectricitySpotMarket, Double> futureNationalMinCo2Prices = new HashMap<ElectricitySpotMarket, Double>(2);
        Iterable<NationalGovernment> nationalGovernments = getReps().nationalGovernments;
        for (NationalGovernment nG : nationalGovernments) {
            if (model.isCo2TradingImplemented()) {
                futureNationalMinCo2Prices.put(getReps().findElectricitySpotMarketByNationalGovernment(nG),
                        nG.getMinNationalCo2PriceTrend().getValue(clearingTick + model.getCentralForecastingYear()));
            } else {
                futureNationalMinCo2Prices.put(getReps().findElectricitySpotMarketByNationalGovernment(nG),
                        0d);
            }

        }

        // find all operational power plants and store the ones operational to a
        // list.
        logger.info("Clearing the CO2 and electricity spot markets using iteration for 2 countries ");

        // find all fuel prices
        // find all interconnectors
        Interconnector interconnector = getReps().interconnector;

        // find all segments
        List<Segment> segments = getReps().segments;

        // find the EU government
        Government government = getReps().government;

        // find national minimum CO2 prices. Initial Map size is 2.
        // find national minimum CO2 prices. Initial Map size is 2.
        Map<ElectricitySpotMarket, Double> nationalMinCo2Prices = new HashMap<ElectricitySpotMarket, Double>(2);
        nationalGovernments = getReps().nationalGovernments;
        for (NationalGovernment nG : nationalGovernments) {
            if (model.isCo2TradingImplemented()) {
                nationalMinCo2Prices.put(getReps().findElectricitySpotMarketByNationalGovernment(nG), nG
                        .getMinNationalCo2PriceTrend().getValue(clearingTick));
            } else {
                nationalMinCo2Prices.put(getReps().findElectricitySpotMarketByNationalGovernment(nG), 0d);
            }

        }

        CO2Auction co2Auction = getReps().co2Auction;
        CO2SecantSearch co2SecantSearch = null;

        double previouslyBankedCertificates = getReps().determineTotallyBankedCO2Certificates();

        double targetEnergyProducerBanking = calculateTargetCO2EmissionBankingOfEnergyProducers(clearingTick,
                clearingTick + model.getCentralForecastingYear(), government, model);
        logger.info("Hedging Target: " + targetEnergyProducerBanking + " Relative Hedging: " + targetEnergyProducerBanking / government.getCo2Cap(getCurrentTick()));

        double deltaBankedEmissionCertificateToReachBankingTarget = (targetEnergyProducerBanking - previouslyBankedCertificates)
                / model.getCentralCO2TargetReversionSpeedFactor();
       double deltaBankedEmissionCertificates;

        co2SecantSearch = new CO2SecantSearch();
        co2SecantSearch.stable = false;
        co2SecantSearch.twoPricesExistWithBelowAboveEmissions = false;
        co2SecantSearch.bankingEffectiveMinimumPrice = calculateBankingEffectiveMinCO2Price(nationalMinCo2Prices,
                futureNationalMinCo2Prices, model);
        co2SecantSearch.co2Price = Math.max(findLastKnownPriceOnMarket(co2Auction),
                co2SecantSearch.bankingEffectiveMinimumPrice);
        co2SecantSearch.tooHighEmissionsPair = null;
        co2SecantSearch.tooLowEmissionsPair = null;

        double futureCO2Price = 0;

        double currentEmissions = 0;
        double futureEmissions = 0;
        double co2CapAdjustment = 0;

        double averageCO2PriceOfLastTwoYears = getReps().calculateAverageClearingPriceForMarketAndTimeRange(co2Auction, getCurrentTick() - 2,
                getCurrentTick() - 1, false);

        determineFuelMixRole.determineFuelMixForecastForYearAndFuelPriceMap(clearingTick, futureFuelPriceMap,
                futureNationalMinCo2Prices);

        submitOffersToElectricitySpotMarketRole.createOffersForElectricitySpotMarket(null,
                clearingTick + model.getCentralForecastingYear(), true, futureFuelPriceMap);

        // Change Iteration algorithm here, and a few lines below...
        ClearingPoint lastClearingPointOfCo2Market = getReps().findClearingPointForMarketAndTime(
                co2Auction, getCurrentTick() - 1, false);
        if (lastClearingPointOfCo2Market != null) {
            co2SecantSearch.co2Emissions = lastClearingPointOfCo2Market.getVolume();
        } else {
            co2SecantSearch.co2Emissions = 0d;
        }

        boolean emergencyPriceTriggerActive = false;
        int breakOffIterator = 0;
        double emergencyAllowancesToBeReleased = 0;
        while (breakOffIterator < 2 || !co2SecantSearch.stable) {

            if (breakOffIterator > 15) {
                logger.info("Iteration cancelled, last found CO2 Price is used.");
                break;
            }

            futureCO2Price = co2SecantSearch.co2Price
                    * Math.pow(1 + model.getCentralPrivateDiscountingRate(), model.getCentralForecastingYear());

            logger.info("Iteration " + breakOffIterator + ", CO2Price for clearing: " + co2SecantSearch.co2Price);
            clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForSegments(government,
                    clearingTick + model.getCentralForecastingYear(), true, futureDemandGrowthMap, futureFuelPriceMap,
                    futureCO2Price, futureNationalMinCo2Prices, segments, interconnector, model);
            futureEmissions = determineTotalEmissionsBasedOnPowerPlantDispatchPlan(true,
                    clearingTick + model.getCentralForecastingYear());

            clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForSegments(government, clearingTick, false, null,
                    fuelPriceMap, co2SecantSearch.co2Price, nationalMinCo2Prices, segments, interconnector, model);
            currentEmissions = determineTotalEmissionsBasedOnPowerPlantDispatchPlan(false, clearingTick);

            deltaBankedEmissionCertificates = government.getCo2Cap(clearingTick) - currentEmissions;

            co2SecantSearch = co2PriceSecantSearchUpdateWithCO2Banking(co2SecantSearch, model, government,
                    clearingTick, -deltaBankedEmissionCertificateToReachBankingTarget, currentEmissions,
                    futureEmissions, previouslyBankedCertificates, averageCO2PriceOfLastTwoYears);

            if (!co2SecantSearch.stable) {
                targetEnergyProducerBanking = calculateTargetCO2EmissionBankingOfEnergyProducers(currentEmissions,
                        futureEmissions, model);

                deltaBankedEmissionCertificateToReachBankingTarget = (targetEnergyProducerBanking - previouslyBankedCertificates)
                        / model.getCentralCO2TargetReversionSpeedFactor();
            }

            if (co2SecantSearch.stable || breakOffIterator >= 15) {
                logger.info("Average price last 2 years: " + averageCO2PriceOfLastTwoYears + " Clearing price " + co2SecantSearch.co2Price);
                logger.info("StabilityReserveActive: "
                        + model.isStabilityReserveIsActive()
                        + ", InOperation: "
                        + (model.getStabilityReserveFirstYearOfOperation() <= clearingTick
                        + model.getCentralForecastingYear()) + ", priceEmergencyTriggerNotActive: "
                        + !emergencyPriceTriggerActive + ", 3x Price: "
                        + (co2SecantSearch.co2Price > 3 * averageCO2PriceOfLastTwoYears));
                if ((model.isStabilityReserveIsActive()
                        && (model.getStabilityReserveFirstYearOfOperation() <= clearingTick) && !emergencyPriceTriggerActive)
                        && (co2SecantSearch.co2Price > 3 * averageCO2PriceOfLastTwoYears)
                        && government.getStabilityReserve() > 0) {
                    breakOffIterator = 0;
                    emergencyPriceTriggerActive = true;
                    emergencyAllowancesToBeReleased = Math.min(government.getStabilityReserve(), government
                            .getStabilityReserveReleaseQuantityTrend().getValue(clearingTick));
                    logger.info(
                            "Stability Reserve releasing " + emergencyAllowancesToBeReleased
                            + " credits since price would be " + co2SecantSearch.co2Price + " which has 3x time higher than "
                            + averageCO2PriceOfLastTwoYears);
                    co2SecantSearch.stable = false;
                    co2SecantSearch.twoPricesExistWithBelowAboveEmissions = false;
                    co2SecantSearch.tooHighEmissionsPair = null;
                    co2SecantSearch.tooLowEmissionsPair = null;
                    government.getCo2CapTrend().setValue(getCurrentTick(),
                            government.getCo2CapTrend().getValue(getCurrentTick()) + emergencyAllowancesToBeReleased);
                    government.setStabilityReserve(government.getStabilityReserve() - emergencyAllowancesToBeReleased);
                }
            }
            breakOffIterator++;

        }

        if (model.getCentralCO2BackSmoothingFactor() != 0) {

            Iterable<ClearingPoint> clearingPoints = getReps().findAllClearingPointsForMarketAndTimeRange(co2Auction,
                    clearingTick - model.getCentralForecastBacklookingYears(), clearingTick, false);

            double averagePastCO2Price = 0;
            int i = 0;
            for (ClearingPoint cp : clearingPoints) {
                averagePastCO2Price = (cp.getPrice()
                        / Math.pow(1 + model.getCentralPrivateDiscountingRate(), clearingTick - cp.getTime()) + i
                        * averagePastCO2Price)
                        / (i + 1);
                i++;
            }

            double oldCO2Price = co2SecantSearch.co2Price;
            co2SecantSearch.co2Price = (1 - model.getCentralCO2BackSmoothingFactor()) * co2SecantSearch.co2Price
                    + model.getCentralCO2BackSmoothingFactor() * averagePastCO2Price;
            if (oldCO2Price != co2SecantSearch.bankingEffectiveMinimumPrice
                    && co2SecantSearch.co2Price > co2SecantSearch.bankingEffectiveMinimumPrice) {
                futureCO2Price = co2SecantSearch.co2Price
                        * Math.pow(1 + model.getCentralPrivateDiscountingRate(), model.getCentralForecastingYear());
                clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForSegments(government,
                        clearingTick + model.getCentralForecastingYear(), true, futureDemandGrowthMap,
                        futureFuelPriceMap, futureCO2Price, futureNationalMinCo2Prices, segments, interconnector, model);
                futureEmissions = determineTotalEmissionsBasedOnPowerPlantDispatchPlan(true,
                        clearingTick + model.getCentralForecastingYear());

                clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForSegments(government, clearingTick, false,
                        null, fuelPriceMap, co2SecantSearch.co2Price, nationalMinCo2Prices, segments, interconnector,
                        model);
                currentEmissions = determineTotalEmissionsBasedOnPowerPlantDispatchPlan(false, clearingTick);
            } else {
                co2SecantSearch.co2Price = oldCO2Price;
            }
        }

        logger.info("CO2Price that is saved: " + co2SecantSearch.co2Price);
        getReps().createOrUpdateCO2MarketClearingPoint(co2Auction, co2SecantSearch.co2Price,
                currentEmissions, emergencyPriceTriggerActive, emergencyAllowancesToBeReleased, clearingTick, false);
        getReps().createOrUpdateCO2MarketClearingPoint(co2Auction,
                Math.max(futureCO2Price, Collections.min(futureNationalMinCo2Prices.values())), futureEmissions, false,
                0,
                clearingTick + model.getCentralForecastingYear(), true);

        if (co2SecantSearch.co2Price > co2SecantSearch.bankingEffectiveMinimumPrice) {
            deltaBankedEmissionCertificates = government.getCo2Cap(clearingTick) - currentEmissions;
        } else {
            deltaBankedEmissionCertificates = Math.min(deltaBankedEmissionCertificateToReachBankingTarget,
                    government.getCo2Cap(clearingTick) - currentEmissions);
        }

        if (previouslyBankedCertificates + deltaBankedEmissionCertificates > 0) {
            for (EnergyProducer producer : getReps().energyProducers) {
                producer.setLastYearsCo2Allowances(producer.getCo2Allowances());
                double emissionShare = determineTotalEmissionsBasedOnPowerPlantDispatchPlanForEnergyProducer(false,
                        clearingTick, producer) / currentEmissions;
                double bankedEmissionsOfProducer = (deltaBankedEmissionCertificates + previouslyBankedCertificates)
                        * emissionShare;
                producer.setCo2Allowances(bankedEmissionsOfProducer);
            }
        } else {
            logger.info("Banking exhausted.");
            clearIterativeCO2AndElectricitySpotMarketTwoCountryForTimestepAndFuelPrices(model, true, clearingTick
                    + model.getCentralForecastingYear(), futureFuelPriceMap, futureDemandGrowthMap, 0);
            clearIterativeCO2AndElectricitySpotMarketTwoCountryForTimestepAndFuelPrices(model, false, clearingTick,
                    fuelPriceMap, null, previouslyBankedCertificates);
            for (EnergyProducer producer : getReps().energyProducers) {
                producer.setLastYearsCo2Allowances(producer.getCo2Allowances());
                producer.setCo2Allowances(0);
            }

        }
    }

    double[] interpolateLinearDiscountedPricesBetweenClearingPoints(ClearingPoint cp1, ClearingPoint cp2,
            double discountRate) {
        int length = (int) Math.abs(cp1.getTime() - cp2.getTime());
        ClearingPoint earlierCp = cp1.getTime() <= cp2.getTime() ? cp1 : cp2;
        ClearingPoint laterCp = cp1.getTime() <= cp2.getTime() ? cp2 : cp1;
        double[] priceInterpolation = new double[length];
        for (int i = 0; i < length; i++) {
            priceInterpolation[i] = earlierCp.getPrice() + ((double) i + 1) / (length)
                    * (laterCp.getPrice() - earlierCp.getPrice()) / Math.pow((1 + discountRate), i);
        }

        return priceInterpolation;
    }

    double[] interpolateLinearVolumesBetweenClearingPoints(ClearingPoint cp1, ClearingPoint cp2) {
        int length = (int) Math.abs(cp1.getTime() - cp2.getTime());
        ClearingPoint earlierCp = cp1.getTime() <= cp2.getTime() ? cp1 : cp2;
        ClearingPoint laterCp = cp1.getTime() <= cp2.getTime() ? cp2 : cp1;
        double[] volumeInterpolation = new double[length];
        for (int i = 0; i < length; i++) {
            volumeInterpolation[i] = earlierCp.getVolume() + ((double) i + 1) / (length)
                    * (laterCp.getVolume() - earlierCp.getVolume());
        }

        return volumeInterpolation;
    }

    double calculateTargetCO2EmissionBankingOfEnergyProducers(long timeFrom, long timeTo, Government government, EMLabModel model) {
        int length = (int) (timeTo - timeFrom);
        double[] volumeInterpolation = new double[length];
        for (int i = 0; i < length; i++) {
            volumeInterpolation[i] = government.getCo2Cap(timeFrom) + ((double) i + 1) / (length)
                    * (government.getCo2Cap(timeTo) - government.getCo2Cap(timeFrom));
        }
        // double targetBankedEmissions = 0.8 * volumeInterpolation[0] + 0.5 *
        // volumeInterpolation[1] + 0.2
        // * volumeInterpolation[2];
        double targetBankedEmissions = model.getStabilityReserveBankingFirstYear() * volumeInterpolation[0]
                + model.getStabilityReserveBankingSecondYear() * volumeInterpolation[1]
                + model.getStabilityReserveBankingThirdYear()
                * volumeInterpolation[2];

        return targetBankedEmissions;
    }

    double calculateTargetCO2EmissionBankingOfEnergyProducers(double currentEmissions, double futureEmissions,
            EMLabModel model) {
        int length = (int) model.getCentralForecastingYear();
        double[] volumeInterpolation = new double[length];
        for (int i = 0; i < length; i++) {
            volumeInterpolation[i] = currentEmissions + ((double) i + 1) / (length)
                    * (futureEmissions - currentEmissions);
        }
        // double targetBankedEmissions = 0.8 * volumeInterpolation[0] + 0.5 * volumeInterpolation[1] + 0.2
        //         * volumeInterpolation[2];
        double targetBankedEmissions = model.getStabilityReserveBankingFirstYear() * volumeInterpolation[0]
                + model.getStabilityReserveBankingSecondYear() * volumeInterpolation[1]
                + model.getStabilityReserveBankingThirdYear()
                * volumeInterpolation[2];

        return targetBankedEmissions;
    }

    double calculateFundamentalCO2PriceForEnergyProducers(long clearingTick, EMLabModel model,
            CO2Auction co2Auction, CO2SecantSearch co2SecantSearch) {

        ClearingPoint forecastedCO2Clearing = getReps().findClearingPointForMarketAndTime(
                co2Auction, clearingTick + model.getCentralForecastingYear(), true);
        ClearingPoint lastCO2Clearing;
        if (co2SecantSearch == null) {
            lastCO2Clearing = getReps().findClearingPointForMarketAndTime(co2Auction,
                    clearingTick - 1, false);
        } else {
            lastCO2Clearing = getReps().createOrUpdateClearingPoint(co2Auction, co2SecantSearch.co2Price, co2SecantSearch.co2Emissions, clearingTick, true);
//            lastCO2Clearing.setPrice(co2SecantSearch.co2Price);
//            lastCO2Clearing.setVolume(co2SecantSearch.co2Emissions);
//            lastCO2Clearing.setTime(clearingTick);
        }
        double[] priceInterpolation = interpolateLinearDiscountedPricesBetweenClearingPoints(lastCO2Clearing,
                forecastedCO2Clearing, model.getCentralPrivateDiscountingRate());
        logger.info("Price interpolation: " + priceInterpolation);
        double[] volumeInterpolation = interpolateLinearVolumesBetweenClearingPoints(lastCO2Clearing,
                forecastedCO2Clearing);
        logger.info("Volume interpolation: " + volumeInterpolation);
        double totalEmissions = 0;
        double fundamentalPrice = 0;
        for (int i = 0; i < volumeInterpolation.length; i++) {
            fundamentalPrice += priceInterpolation[i] * volumeInterpolation[i];
            totalEmissions += volumeInterpolation[i];
        }
        fundamentalPrice = fundamentalPrice / totalEmissions;
        return fundamentalPrice;
    }

    double calculateCO2PriceReductionFactor(Government government, double maximumEnergyProducerBanking,
            double bankedCertificates, long yearsForwardLooking, long clearingTick) {
        double allowedEmissionsInFuture = 0d;
        for (long i = clearingTick; i < (clearingTick + yearsForwardLooking); i++) {
            allowedEmissionsInFuture += government.getCo2Cap(i);
        }
        double co2PriceReductionFactor = 1 - ((bankedCertificates - maximumEnergyProducerBanking) / allowedEmissionsInFuture);
        return co2PriceReductionFactor;
    }

    double calculateBankingEffectiveMinCO2Price(Map<ElectricitySpotMarket, Double> nationalMinCo2Prices,
            Map<ElectricitySpotMarket, Double> futureNationalMinCo2Prices, EMLabModel model) {
        double currentMinimumPrice = Collections.min(nationalMinCo2Prices.values());

        return currentMinimumPrice;
    }

    CO2SecantSearch co2PriceSecantSearchUpdateWithCO2Banking(CO2SecantSearch co2SecantSearch,
            EMLabModel model, Government government, long clearingTick, double co2CapAdjustment,
            double currentEmissions, double futureEmissions, double previouslyBankedCertificates,
            double averageCO2PriceOfLastTwoYears) {
        co2SecantSearch.stable = false;
        double capDeviationCriterion = model.getCapDeviationCriterion();
        double currentCap = government.getCo2Cap(clearingTick);
        double futureCap = government.getCo2Cap(clearingTick + model.getCentralForecastingYear());
        double expectedBankedPermits = calculateExpectedBankedCertificates(currentEmissions, futureEmissions,
                currentCap, futureCap, previouslyBankedCertificates, model.getCentralForecastingYear(), government);
        double effectiveCapInFuture = (model.isStabilityReserveIsActive() && (model
                .getStabilityReserveFirstYearOfOperation() <= clearingTick + model.getCentralForecastingYear())) ? futureCap
                - marketStabilityReserveRole.calculateInflowToMarketReserveForTimeStep(
                        clearingTick + model.getCentralForecastingYear(),
                        expectedBankedPermits,
                        government)
                : futureCap;
        effectiveCapInFuture -= (government.isActivelyAdjustingTheCO2Cap() & getCurrentTick() > 0) ? renewableAdaptiveCO2CapRole
                .calculatedExpectedCapReductionForTimeStep(government, clearingTick,
                        clearingTick + model.getCentralForecastingYear(), currentEmissions, futureEmissions,
                        model.getCentralForecastingYear()) : 0;
        double co2Cap = currentCap + effectiveCapInFuture + co2CapAdjustment;
        co2SecantSearch.co2Emissions = currentEmissions + futureEmissions;

        double deviation = (co2SecantSearch.co2Emissions - co2Cap) / co2Cap;

        if (co2SecantSearch.co2Price == co2SecantSearch.bankingEffectiveMinimumPrice
                && co2SecantSearch.co2Emissions < co2Cap) {
            co2SecantSearch.stable = true;
            return co2SecantSearch;
        }

        // Check if current price leads to emissions close to the cap.
        if (Math.abs(deviation) < capDeviationCriterion
                && co2SecantSearch.co2Price > co2SecantSearch.bankingEffectiveMinimumPrice) {
            // logger.warn("Deviation is less than capDeviationCriterion");
            co2SecantSearch.stable = true;
            return co2SecantSearch;
        }

        // Check and update the twoPricesExistWithBelowAboveEmissions
        if (co2SecantSearch.tooHighEmissionsPair != null && co2SecantSearch.tooLowEmissionsPair != null) {
            co2SecantSearch.twoPricesExistWithBelowAboveEmissions = true;
        } else if (co2SecantSearch.co2Price == government.getMinCo2Price(clearingTick)
                && co2SecantSearch.co2Emissions < co2Cap) {
            // logger.warn("Deviation CO2 price has reached minimum");
            // check if stable enough --> 2. Cap is met with a co2Price
            // equal to the minimum co2 price
            co2SecantSearch.stable = true;
            return co2SecantSearch;
        } else if (co2SecantSearch.co2Price >= government.getCo2Penalty(clearingTick)
                && co2SecantSearch.co2Emissions >= co2Cap) {
            // Only if above the cap...
            // logger.warn("CO2 price ceiling reached {}",
            // co2SecantSearch.co2Price);
            co2SecantSearch.co2Price = government.getCo2Penalty(clearingTick);
            co2SecantSearch.stable = true;
            return co2SecantSearch;
        }

        // Check whether we know two pairs, one with EmissionsAboveCap, one with
        // EmissionsBelowCap
        // in case of yes: calculate new CO2 price via secant calculation. In
        // case of no: Take last known
        // price above or below, or halve/double the price.
        if (co2SecantSearch.twoPricesExistWithBelowAboveEmissions) {

            // Update the emission pairs
            if (deviation > 0) {
                co2SecantSearch.tooHighEmissionsPair.price = co2SecantSearch.co2Price;
                co2SecantSearch.tooHighEmissionsPair.emission = co2SecantSearch.co2Emissions - co2Cap;
            } else {
                co2SecantSearch.tooLowEmissionsPair.price = co2SecantSearch.co2Price;
                co2SecantSearch.tooLowEmissionsPair.emission = co2SecantSearch.co2Emissions - co2Cap;
            }

            double p2 = co2SecantSearch.tooHighEmissionsPair.price;
            double p1 = co2SecantSearch.tooLowEmissionsPair.price;
            double e2 = co2SecantSearch.tooHighEmissionsPair.emission;
            double e1 = co2SecantSearch.tooLowEmissionsPair.emission;

            // Interrupts long iterations by making a binary search step.
            if (co2SecantSearch.iteration < 5) {
                co2SecantSearch.co2Price = p1 - (e1 * (p2 - p1) / (e2 - e1));
                co2SecantSearch.iteration++;
                // logger.warn("New CO2 Secant price {}",
                // co2SecantSearch.co2Price);
            } else {
                co2SecantSearch.co2Price = (p1 + p2) / 2;
                co2SecantSearch.iteration = 0;
                // logger.warn("New CO2 Binary price {}",
                // co2SecantSearch.co2Price);
            }

        } else {

            if (deviation > 0) {
                if (co2SecantSearch.tooHighEmissionsPair == null) {
                    co2SecantSearch.tooHighEmissionsPair = new PriceEmissionPair();
                }

                co2SecantSearch.tooHighEmissionsPair.price = co2SecantSearch.co2Price;
                co2SecantSearch.tooHighEmissionsPair.emission = co2SecantSearch.co2Emissions - co2Cap;

                if (co2SecantSearch.tooLowEmissionsPair == null) {
                    co2SecantSearch.co2Price = (co2SecantSearch.co2Price != 0d) ? ((co2SecantSearch.co2Price * 2 < government
                            .getCo2Penalty(clearingTick)) ? (co2SecantSearch.co2Price * 2) : government
                            .getCo2Penalty(clearingTick)) : 5d;
                    // logger.warn("New doubled CO2 search price {}",
                    // co2SecantSearch.co2Price);
                } else {
                    double p2 = co2SecantSearch.tooHighEmissionsPair.price;
                    double p1 = co2SecantSearch.tooLowEmissionsPair.price;
                    double e2 = co2SecantSearch.tooHighEmissionsPair.emission;
                    double e1 = co2SecantSearch.tooLowEmissionsPair.emission;

                    co2SecantSearch.co2Price = p1 - (e1 * (p2 - p1) / (e2 - e1));
                    co2SecantSearch.iteration++;
                    // logger.warn("New CO2 Secant price {}",
                    // co2SecantSearch.co2Price);
                }

            } else {

                if (co2SecantSearch.tooLowEmissionsPair == null) {
                    co2SecantSearch.tooLowEmissionsPair = new PriceEmissionPair();
                }

                co2SecantSearch.tooLowEmissionsPair.price = co2SecantSearch.co2Price;
                co2SecantSearch.tooLowEmissionsPair.emission = co2SecantSearch.co2Emissions - co2Cap;

                if (co2SecantSearch.tooHighEmissionsPair == null) {
                    co2SecantSearch.co2Price = Math.max((co2SecantSearch.co2Price / 2),
                            co2SecantSearch.bankingEffectiveMinimumPrice);
                    // logger.warn("New halved CO2 search price {}",
                    // co2SecantSearch.co2Price);
                } else {
                    double p2 = co2SecantSearch.tooHighEmissionsPair.price;
                    double p1 = co2SecantSearch.tooLowEmissionsPair.price;
                    double e2 = co2SecantSearch.tooHighEmissionsPair.emission;
                    double e1 = co2SecantSearch.tooLowEmissionsPair.emission;

                    co2SecantSearch.co2Price = p1 - (e1 * (p2 - p1) / (e2 - e1));
                    // logger.warn("New CO2 Secant price {}",
                    // co2SecantSearch.co2Price);
                    co2SecantSearch.iteration++;

                }

                if (co2SecantSearch.co2Price < 0.5
                        || co2SecantSearch.co2Price - government.getMinCo2Price(clearingTick) < 0.5) {
                    co2SecantSearch.stable = true;
                }

            }

        }

        return co2SecantSearch;

    }

    double calculateExpectedBankedCertificates(double currentEmissions, double futureEmissions, double currentCap,
            double futureCap, double currentlyBankedEmissions, long centralForecastingYear, Government government) {
        double expectedBankedCertificates = currentlyBankedEmissions + currentCap - currentEmissions + futureCap
                - futureEmissions;
        expectedBankedCertificates = government.isStabilityReserveHasOneYearDelayInsteadOfTwoYearDelay() ? 2.0 / 3.0
                * expectedBankedCertificates + 1.0 / 3.0 * currentlyBankedEmissions : 1 / 3.0
                * expectedBankedCertificates
                + 2 / 3.0 * currentlyBankedEmissions;
        return expectedBankedCertificates;

    }



}
