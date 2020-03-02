/** *****************************************************************************
 * Copyright 2014 the original author or authors.
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
package emlab.gen.role.investment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math.stat.regression.SimpleRegression;

import emlab.gen.domain.agent.BigBank;
import emlab.gen.domain.agent.EMLabModel;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.agent.PowerPlantManufacturer;
import emlab.gen.domain.agent.StochasticTargetInvestor;
import emlab.gen.domain.agent.StrategicReserveOperator;
import emlab.gen.domain.agent.TargetInvestor;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.contract.Loan;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.CO2Auction;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.policy.PowerGeneratingTechnologyTarget;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGeneratingTechnologyNodeLimit;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.engine.Role;
import emlab.gen.engine.Schedule;
import emlab.gen.util.GeometricTrendRegression;
import emlab.gen.util.MapValueComparator;

/**
 * {@link EnergyProducer}s decide to invest in new {@link PowerPlant}
 *
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a> @author
 * <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas Chmieliauskas</a>
 * @author JCRichstein
 */
public class InvestWithHistoricalCVar<T extends EnergyProducer>  extends AbstractInvestInPowerGenerationTechnologiesRole<T> implements Role<T> {
	
    public InvestWithHistoricalCVar(Schedule schedule) {
        super(schedule);
    }

    @Override
    public void act(T agent) {
    	
    	// TODO implement with new interface
        Logger.getGlobal().log(Level.SEVERE, "Not yet implemented...");
        throw new UnsupportedOperationException();

//        long futureTimePoint = getCurrentTick() + agent.getInvestmentFutureTimeHorizon();
//        // logger.warn(agent + " is looking at timepoint " + futureTimePoint);
//
//        // ==== Expectations ===
//        Map<Substance, Double> expectedFuelPrices = predictFuelPrices(agent, futureTimePoint);
//
//        // CO2
//        Map<ElectricitySpotMarket, Double> expectedCO2Price = determineExpectedCO2PriceInclTaxAndFundamentalForecast(
//                futureTimePoint,
//                agent.getNumberOfYearsBacklookingForForecasting(), 0, getCurrentTick());
//
//        // logger.warn("{} expects CO2 prices {}", agent.getName(),
//        // expectedCO2Price);
//        Map<ElectricitySpotMarket, Double> expectedCO2PriceOld = determineExpectedCO2PriceInclTax(futureTimePoint,
//                agent.getNumberOfYearsBacklookingForForecasting(), getCurrentTick());
//        // logger.warn("{} used to expect CO2 prices {}", agent.getName(),
//        // expectedCO2PriceOld);
//
//        // logger.warn(expectedCO2Price.toString());
//        //Demand
//        Map<ElectricitySpotMarket, Double> expectedDemand = new HashMap<ElectricitySpotMarket, Double>();
//        for (ElectricitySpotMarket elm : getReps().electricitySpotMarkets) {
//            GeometricTrendRegression gtr = new GeometricTrendRegression();
//            for (long time = getCurrentTick(); time > getCurrentTick() - agent.getNumberOfYearsBacklookingForForecasting() && time >= 0; time = time - 1) {
//                gtr.addData(time, elm.getDemandGrowthTrend().getValue(time));
//            }
//            expectedDemand.put(elm, gtr.predict(futureTimePoint));
//        }
//
//        // Investment decision
//        // for (ElectricitySpotMarket market :
//        // getReps().genericRepository.findAllAtRandom(ElectricitySpotMarket.class))
//        // {
//        ElectricitySpotMarket market = agent.getInvestorMarket();
//        MarketInformation marketInformation = new MarketInformation(market, expectedDemand, expectedFuelPrices, expectedCO2Price.get(market)
//                .doubleValue(), futureTimePoint);
//        /*
//         * if (marketInfoMap.containsKey(market) && marketInfoMap.get(market).time == futureTimePoint) { marketInformation = marketInfoMap.get(market); } else { marketInformation = new
//         * MarketInformation(market, expectedFuelPrices, expectedCO2Price, futureTimePoint); marketInfoMap.put(market, marketInformation); }
//         */
//
//        // logger.warn(agent + " is expecting a CO2 price of " +
//        // expectedCO2Price.get(market) + " Euro/MWh at timepoint "
//        // + futureTimePoint + " in Market " + market);
//        // logger.warn("Agent {}  found the expected prices to be {}", agent,
//        // marketInformation.expectedElectricityPricesPerSegment);
//        // logger.warn("Agent {}  found that the installed capacity in the market {} in future to be "
//        // + marketInformation.capacitySum +
//        // "and expectde maximum demand to be "
//        // + marketInformation.maxExpectedLoad, agent, market);
//        double highestValue = Double.MIN_VALUE;
//        double highestValueWithoutCvar = Double.MIN_VALUE;
//        PowerGeneratingTechnology bestTechnology = null;
//        PowerGeneratingTechnology bestTechnologyWithoutCVar = null;
//        PowerGridNode bestNode = null;
//        PowerGridNode bestNodeWithoutCvar = null;
//
//        for (PowerGeneratingTechnology technology : getReps().powerGeneratingTechnologies) {
//            // logger.warn("Looking at tech: {}", technology.getName());
//
//            EMLabModel model = getReps().emlabModel;
//
//            if (technology.isIntermittent() && model.isNoPrivateIntermittentRESInvestment()) {
//                continue;
//            }
//
//            Iterable<PowerGridNode> possibleInstallationNodes;
//
//            /*
//             * For dispatchable technologies just choose a random node. For
//             * intermittent evaluate all possibilities.
//             */
//            if (technology.isIntermittent()) {
//                possibleInstallationNodes = getReps().findAllPowerGridNodesByZone(market.getZone());
//            } else {
//                possibleInstallationNodes = new LinkedList<PowerGridNode>();
//                ((LinkedList<PowerGridNode>) possibleInstallationNodes).add(getReps().findAllPowerGridNodesByZone(market.getZone()).iterator().next());
//            }
//
//            // logger.warn("Calculating for " + technology.getName() +
//            // ", for Nodes: "
//            // + possibleInstallationNodes.toString());
//            for (PowerGridNode node : possibleInstallationNodes) {
//
//                if (agent.isHistoricalCvarCreateDummyPowerPlantsForNewTechnologies()) {
//                    PowerPlant dummyPowerPlant = getReps().findOneOperationalHistoricalCvarDummyPowerPlantsByOwnerAndTechnology(
//                            technology, getCurrentTick(), agent);
//                    if (dummyPowerPlant != null) { // If dummy
//
//                        dummyPowerPlant.setActualEfficiency(technology.getEfficiency(getCurrentTick()
//                                - technology.getExpectedLeadtime() - technology.getExpectedPermittime()));
//                        dummyPowerPlant.setConstructionStartTime(getCurrentTick() - technology.getExpectedLeadtime()
//                                - technology.getExpectedPermittime());
//                    } else { // else check if power plants exist in the market,
//                        // ignoring dummy plants
//                        if (getReps()
//                                .calculateCapacityOfOperationalPowerPlantsByOwnerAndTechnology(
//                                        technology,
//                                        getCurrentTick(), agent) < 1.0) {
//                            double size = getReps()
//                                    .calculateCapacityOfOperationalPowerPlantsByOwnerAndTechnology(
//                                            technology, getCurrentTick(), agent);
//                            // logger.warn("Did not find dummy plant. Creating it. Found size: "
//                            // + size);
//                            String label = agent.getName() + " - " + technology.getName() + " HistoricalCvarDummy";
//                            PowerPlant dummyPlant = new PowerPlant();
//                            dummyPlant.setName(label);
//                            dummyPlant.setTechnology(technology);
//                            dummyPlant.setOwner(agent);
//                            dummyPlant.setLocation(node);
//                            dummyPlant.setConstructionStartTime(getCurrentTick() - technology.getExpectedLeadtime()
//                                    - technology.getExpectedPermittime());
//                            dummyPlant.setActualLeadtime(0);
//                            dummyPlant.setActualPermittime(0);
//                            dummyPlant.setActualEfficiency(technology.getEfficiency(getCurrentTick()
//                                    - technology.getExpectedLeadtime()));
//                            dummyPlant.setActualNominalCapacity(0.001);
//                            dummyPlant.setDismantleTime(1000);
//                            dummyPlant.calculateAndSetActualInvestedCapital(getCurrentTick()
//                                    - technology.getExpectedLeadtime() - technology.getExpectedPermittime()
//                            );
//                            dummyPlant.calculateAndSetActualFixedOperatingCosts(getCurrentTick()
//                                    - technology.getExpectedLeadtime() - technology.getExpectedPermittime()
//                            );
//                            dummyPlant.setExpectedEndOfLife(1000.0);
//                            dummyPlant.setHistoricalCvarDummyPlant(true);
//                        }
//
//                    }
//                }
//
//                PowerPlant plant = getReps().createAndSpecifyTemporaryPowerPlant(getCurrentTick(), agent, node, technology);
//                // if too much capacity of this technology in the pipeline (not
//                // limited to the 5 years)
//                double expectedInstalledCapacityOfTechnology = getReps()
//                        .calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market, technology, futureTimePoint);
//                PowerGeneratingTechnologyTarget technologyTarget = getReps().findPowerGeneratingTechnologyTargetByTechnologyAndMarket(technology, market);
//                if (technologyTarget != null) {
//                    double technologyTargetCapacity = technologyTarget.getTrend().getValue(futureTimePoint);
//                    expectedInstalledCapacityOfTechnology = (technologyTargetCapacity > expectedInstalledCapacityOfTechnology) ? technologyTargetCapacity
//                            : expectedInstalledCapacityOfTechnology;
//                }
//                double pgtNodeLimit = Double.MAX_VALUE;
//                PowerGeneratingTechnologyNodeLimit pgtLimit = getReps()
//                        .findOneByTechnologyAndNode(technology, plant.getLocation());
//                if (pgtLimit != null) {
//                    pgtNodeLimit = pgtLimit.getUpperCapacityLimit(futureTimePoint);
//                }
//                double expectedInstalledCapacityOfTechnologyInNode = getReps()
//                        .calculateCapacityOfExpectedOperationalPowerPlantsByNodeAndTechnology(plant.getLocation(),
//                                technology, futureTimePoint);
//                double expectedOwnedTotalCapacityInMarket = getReps()
//                        .calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwner(market, futureTimePoint, agent);
//                double expectedOwnedCapacityInMarketOfThisTechnology = getReps()
//                        .calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwnerAndTechnology(market, technology, futureTimePoint,
//                                agent);
//                double capacityOfTechnologyInPipeline = getReps().calculateCapacityOfPowerPlantsByTechnologyInPipeline(
//                        technology, getCurrentTick());
//                double operationalCapacityOfTechnology = getReps().calculateCapacityOfOperationalPowerPlantsByTechnology(
//                        technology, getCurrentTick());
//                double capacityInPipelineInMarket = getReps()
//                        .calculateCapacityOfPowerPlantsByMarketInPipeline(market, getCurrentTick());
//
//                if ((expectedInstalledCapacityOfTechnology + plant.getActualNominalCapacity())
//                        / (marketInformation.maxExpectedLoad + plant.getActualNominalCapacity()) > technology
//                        .getMaximumInstalledCapacityFractionInCountry()) {
//                    // logger.warn(agent +
//                    // " will not invest in {} technology because there's too much of this type in the market",
//                    // technology);
//                } else if ((expectedInstalledCapacityOfTechnologyInNode + plant.getActualNominalCapacity()) > pgtNodeLimit) {
//
//                } else if (expectedOwnedCapacityInMarketOfThisTechnology > expectedOwnedTotalCapacityInMarket
//                        * technology.getMaximumInstalledCapacityFractionPerAgent()) {
//                    // logger.warn(agent +
//                    // " will not invest in {} technology because there's too much capacity planned by him",
//                    // technology);
//                } else if (capacityInPipelineInMarket > 0.2 * marketInformation.maxExpectedLoad) {
//                    // logger.warn("Not investing because more than 20% of demand in pipeline.");
//
//                } else if ((capacityOfTechnologyInPipeline > 2.0 * operationalCapacityOfTechnology)
//                        && capacityOfTechnologyInPipeline > 9000) { // TODO:
//                    // reflects that you cannot expand a technology out of zero.
//                    // logger.warn(agent +
//                    // " will not invest in {} technology because there's too much capacity in the pipeline",
//                    // technology);
//                } else if (plant.getActualInvestedCapital() * (1 - agent.getDebtRatioOfInvestments()) > agent
//                        .getDownpaymentFractionOfCash() * agent.getCash()) {
//                    // logger.warn(agent +
//                    // " will not invest in {} technology as he does not have enough money for downpayment",
//                    // technology);
//                } else {
//
//                    Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
//                    for (Substance fuel : technology.getFuels()) {
//                        myFuelPrices.put(fuel, expectedFuelPrices.get(fuel));
//                    }
//                    Set<SubstanceShareInFuelMix> fuelMix = calculateFuelMix(plant, myFuelPrices, expectedCO2Price.get(market));
//                    plant.setFuelMix(fuelMix);
//
//                    double expectedMarginalCost = determineExpectedMarginalCost(plant, expectedFuelPrices, expectedCO2Price.get(market));
//                    double runningHours = 0d;
//                    double expectedGrossProfit = 0d;
//
//                    long numberOfSegments = getReps().segments.size();
//
//                    // TODO somehow the prices of long-term contracts could also
//                    // be used here to determine the expected profit. Maybe not
//                    // though...
//                    for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {
//                        double expectedElectricityPrice = marketInformation.expectedElectricityPricesPerSegment.get(segmentLoad
//                                .getSegment());
//                        double hours = segmentLoad.getSegment().getLengthInHours();
//                        if (expectedMarginalCost <= expectedElectricityPrice) {
//                            runningHours += hours;
//                            if (technology.isIntermittent()) {
//                                expectedGrossProfit += (expectedElectricityPrice - expectedMarginalCost)
//                                        * hours
//                                        * plant.getActualNominalCapacity()
//                                        * getReps().findIntermittentTechnologyNodeLoadFactorForNodeAndTechnology(node,
//                                                technology).getLoadFactorForSegment(segmentLoad.getSegment());
//                            } else {
//                                expectedGrossProfit += (expectedElectricityPrice - expectedMarginalCost)
//                                        * hours
//                                        * plant.getAvailableCapacity(futureTimePoint, segmentLoad.getSegment(), numberOfSegments);
//                            }
//                        }
//                    }
//
//                    // logger.warn(agent +
//                    // "expects technology {} to have {} running", technology,
//                    // runningHours);
//                    // expect to meet minimum running hours?
//                    if (runningHours < plant.getTechnology().getMinimumRunningHours()) {
//                        // logger.warn(agent+
//                        // " will not invest in {} technology as he expect to have {} running, which is lower then required",
//                        // technology, runningHours);
//                    } else {
//
//                        double fixedOMCost = calculateFixedOperatingCost(plant, getCurrentTick());// /
//                        // plant.getActualNominalCapacity();
//
//                        Double cVarOfHistoricalGrossProfitsResult = getReps().calculateHistoricalCVarRelativePerMWForOperationaPlantsForEnergyProducerAndTechnologyForYearsFromToAndAlphaValue(
//                                getCurrentTick() - agent.getHistoricalCvarBacklookingYears(), getCurrentTick(),
//                                agent, technology,
//                                agent.getHistoricalCVarAlpha());
//
//                        double cVarOfHistoricalGrossProfits = 0;
//                        if (cVarOfHistoricalGrossProfitsResult != null) {
//                            cVarOfHistoricalGrossProfits = cVarOfHistoricalGrossProfitsResult.doubleValue()
//                                    * plant.getActualNominalCapacity();
//                            // logger.warn("Found historical results: {}",
//                            // cVarOfHistoricalGrossProfits);
//                        } else {
//                            // logger.warn("Didn't find any historical results, scaling expected profits down with New Tech Propensity!");
//                            cVarOfHistoricalGrossProfits = agent.getHistoricalCVarPropensityForNewTechnologies()
//                                    * expectedGrossProfit;
//                        }
//
//                        double operatingProfit = expectedGrossProfit - fixedOMCost;
//
//                        double historicalCvarOperatingProfit = cVarOfHistoricalGrossProfits - fixedOMCost;
//
//                        // TODO Alter discount rate on the basis of the amount
//                        // in long-term contracts?
//                        // TODO Alter discount rate on the basis of other stuff,
//                        // such as amount of money, market share, portfolio
//                        // size.
//                        // Calculation of weighted average cost of capital,
//                        // based on the companies debt-ratio
//                        double wacc = (1 - agent.getDebtRatioOfInvestments()) * agent.getEquityInterestRate()
//                                + agent.getDebtRatioOfInvestments() * agent.getLoanInterestRate();
//                        if (cVarOfHistoricalGrossProfitsResult == null) {
//                            wacc += agent.getHistoricalCVarInterestRateIncreaseForNewTechnologies();
//                        }
//
//                        // Creation of out cash-flow during power plant building
//                        // phase (note that the cash-flow is negative!)
//                        TreeMap<Integer, Double> projectCapitalOutflow = calculateSimplePowerPlantInvestmentCashFlow(
//                                technology.getDepreciationTime(), (int) plant.getActualLeadtime(),
//                                plant.getActualInvestedCapital(), 0);
//                        // Creation of in cashflow during operation
//                        TreeMap<Integer, Double> projectCashInflow = calculateSimplePowerPlantInvestmentCashFlow(
//                                technology.getDepreciationTime(), (int) plant.getActualLeadtime(), 0, operatingProfit);
//
//                        TreeMap<Integer, Double> projectCashInflowHistoricalCVar = calculateSimplePowerPlantInvestmentCashFlow(
//                                technology.getDepreciationTime(), (int) plant.getActualLeadtime(), 0,
//                                historicalCvarOperatingProfit);
//
//                        double discountedCapitalCosts = npv(projectCapitalOutflow, wacc);// are
//                        // defined
//                        // negative!!
//                        // plant.getActualNominalCapacity();
//
//                        // logger.warn("Agent {}  found that the discounted capital for technology {} to be "
//                        // + discountedCapitalCosts, agent,
//                        // technology);
//                        double discountedOpProfit = npv(projectCashInflow, wacc);
//
//                        double discountedHistoricalCvarOpProfit = npv(projectCashInflowHistoricalCVar, wacc);
//
//                        // logger.warn("Agent {}  found that the projected discounted inflows for technology {} to be "
//                        // + discountedOpProfit,
//                        // agent, technology);
//                        double projectValue = discountedOpProfit + discountedCapitalCosts;
//                        double oldProjectValue = projectValue;
//
//                        double historicalCvarProjectValue = discountedHistoricalCvarOpProfit + discountedCapitalCosts;
//
//                        if (cVarOfHistoricalGrossProfitsResult != null
//                                || (agent.getHistoricalCVarInterestRateIncreaseForNewTechnologies() == 0 & cVarOfHistoricalGrossProfitsResult == null)) {
//                            projectValue += (agent.getHistoricalCVarBeta() * historicalCvarProjectValue < 0) ? agent
//                                    .getHistoricalCVarBeta() * historicalCvarProjectValue : 0;
//                        }
//                        // if (historicalCvarProjectValue < 0) {
//                        // logger.warn("Adjusting NPV!");
//                        // projectValue += beta * historicalCvarProjectValue;
//                        // }
//
//                        // if (technology.isIntermittent()) {
//                        // logger.warn(technology + "in " + node.getName() +
//                        // ", NPV: " + projectValue
//                        // + ", GrossProfit: " + expectedGrossProfit);
//                        // }
//                        // logger.warn(technology + "in " + node.getName() +
//                        // ", NPV: " + projectValue + ", GrossProfit: "
//                        // + expectedGrossProfit);
//                        //
//                        // logger.warn("CVar: " + historicalCvarProjectValue);
//                        //
//                        // logger.warn("NPV-CVAR: " + projectValue);
//                        // logger.warn(
//                        // "Agent {}  found the project value for technology {} to be "
//                        // + Math.round(projectValue /
//                        // plant.getActualNominalCapacity())
//                        // + " EUR/kW (running hours: " + runningHours + "",
//                        // agent, technology);
//                        // double projectTotalValue = projectValuePerMW *
//                        // plant.getActualNominalCapacity();
//                        // double projectReturnOnInvestment = discountedOpProfit
//                        // / (-discountedCapitalCosts);
//
//                        /*
//                         * Divide by capacity, in order not to favour large power plants (which have the single largest NPV
//                         */
//                        // if (projectValue < 0 && oldProjectValue > 0) {
//                        // logger.warn(
//                        // "Not profitable w CVAR. NPV-CVAR: {}, NPV: {}, CVAR-GP: "
//                        // + cVarOfHistoricalGrossProfits /
//                        // plant.getActualNominalCapacity() + " Tech:"
//                        // + technology + " in "
//                        // + node.getName(), projectValue /
//                        // plant.getActualNominalCapacity(),
//                        // oldProjectValue / plant.getActualNominalCapacity());
//                        // }
//                        // if (projectValue > 0) {
//                        // logger.warn(
//                        // "Is profitable w CVAR. NPV-CVAR: {}, NPV: {}, CVAR-GP: "
//                        // + cVarOfHistoricalGrossProfits /
//                        // plant.getActualNominalCapacity() + " Tech:"
//                        // + technology + " in "
//                        // + node.getName(), projectValue /
//                        // plant.getActualNominalCapacity(),
//                        // oldProjectValue / plant.getActualNominalCapacity());
//                        // }
//                        if (projectValue > 0 && projectValue / plant.getActualNominalCapacity() > highestValue) {
//                            highestValue = projectValue / plant.getActualNominalCapacity();
//                            bestTechnology = plant.getTechnology();
//                            bestNode = node;
//                        }
//
//                        if (oldProjectValue > 0
//                                && oldProjectValue / plant.getActualNominalCapacity() > highestValueWithoutCvar) {
//                            highestValueWithoutCvar = oldProjectValue / plant.getActualNominalCapacity();
//                            bestTechnologyWithoutCVar = plant.getTechnology();
//                            bestNodeWithoutCvar = node;
//                        }
//                    }
//
//                }
//
//            }
//        }
//
//        if (bestTechnology != null && bestTechnologyWithoutCVar != null
//                && !bestTechnologyWithoutCVar.equals(bestTechnology)) {
//            logger.info("Because of CVar investing in " + bestTechnology.getName() + " instead of in "
//                    + bestTechnologyWithoutCVar.getName());
//        }
//        if (bestTechnology == null && bestTechnologyWithoutCVar != null) {
//            logger.info("Not investing. W/o CVar would have invested in "
//                    + bestTechnologyWithoutCVar.getName());
//        }
//
//        if (bestTechnology != null) {
//            // logger.warn("Agent {} invested in technology {} at tick " +
//            // getCurrentTick(), agent, bestTechnology);
//
//            PowerPlant plant = getReps().createAndSpecifyTemporaryPowerPlant(getCurrentTick(), agent, bestNode, bestTechnology);
//            getReps().createPowerPlantFromPlant(plant);
//            
//            Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
//            for (Substance fuel : bestTechnology.getFuels()) {
//                myFuelPrices.put(fuel, expectedFuelPrices.get(fuel));
//            }
//            plant.setFuelMix(calculateFuelMix(plant, myFuelPrices, expectedCO2Price.get(market)));
//            
//            PowerPlantManufacturer manufacturer = getReps().powerPlantManufacturer;
//            BigBank bigbank = getReps().bigBank;
//
//            double investmentCostPayedByEquity = plant.getActualInvestedCapital() * (1 - agent.getDebtRatioOfInvestments());
//            double investmentCostPayedByDebt = plant.getActualInvestedCapital() * agent.getDebtRatioOfInvestments();
//            double downPayment = investmentCostPayedByEquity;
//            createSpreadOutDownPayments(agent, manufacturer, downPayment, plant);
//
//            double amount = determineLoanAnnuities(investmentCostPayedByDebt, plant.getTechnology().getDepreciationTime(),
//                    agent.getLoanInterestRate());
//            // logger.warn("Loan amount is: " + amount);
//            Loan loan = getReps().createLoan(agent, bigbank, amount, plant.getTechnology().getDepreciationTime(),
//                    getCurrentTick(), plant);
//            // Create the loan
//            plant.createOrUpdateLoan(loan);
//
//        } else {
//            // logger.warn("{} found no suitable technology anymore to invest in at tick "
//            // + getCurrentTick(), agent);
//            // agent will not participate in the next round of investment if
//            // he does not invest now
//            setNotWillingToInvest(agent);
//        }
    }

 
//    private class MarketInformation {
//
//        Map<Segment, Double> expectedElectricityPricesPerSegment;
//        double maxExpectedLoad = 0d;
//        Map<PowerPlant, Double> meritOrder;
//        double capacitySum;
//
//        MarketInformation(ElectricitySpotMarket market, Map<ElectricitySpotMarket, Double> expectedDemand, Map<Substance, Double> fuelPrices, double co2price, long time) {
//            // determine expected power prices
//            expectedElectricityPricesPerSegment = new HashMap<Segment, Double>();
//            Map<PowerPlant, Double> marginalCostMap = new HashMap<PowerPlant, Double>();
//            capacitySum = 0d;
//
//            // get merit order for this market
//            for (PowerPlant plant : getReps().findExpectedOperationalPowerPlantsInMarket(market, time)) {
//
//                double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
//                marginalCostMap.put(plant, plantMarginalCost);
//                capacitySum += plant.getActualNominalCapacity();
//            }
//
//            //get difference between technology target and expected operational capacity
//            for (TargetInvestor targetInvestor : getReps().targetInvestors) {
//                if (!(targetInvestor instanceof StochasticTargetInvestor)) {
//                    for (PowerGeneratingTechnologyTarget pggt : targetInvestor.getPowerGenerationTechnologyTargets()) {
//                        double expectedTechnologyCapacity = getReps()
//                                .calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market,
//                                        pggt.getPowerGeneratingTechnology(), time);
//                        double targetDifference = pggt.getTrend().getValue(time) - expectedTechnologyCapacity;
//                        if (targetDifference > 0) {
//                           PowerPlant plant = getReps().createAndSpecifyTemporaryPowerPlant(getCurrentTick(), new EnergyProducer(),
//                                    getReps().findFirstPowerGridNodeByElectricitySpotMarket(market),
//                                    pggt.getPowerGeneratingTechnology());
//                            plant.setActualNominalCapacity(targetDifference);
//                            double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
//                            marginalCostMap.put(plant, plantMarginalCost);
//                            capacitySum += targetDifference;
//                        }
//                    }
//                } else {
//                    for (PowerGeneratingTechnologyTarget pggt : targetInvestor.getPowerGenerationTechnologyTargets()) {
//                        double expectedTechnologyCapacity = getReps()
//                                .calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market,
//                                        pggt.getPowerGeneratingTechnology(), time);
//                        double expectedTechnologyAddition = 0;
//                        long contructionTime = getCurrentTick()
//                                + pggt.getPowerGeneratingTechnology().getExpectedLeadtime()
//                                + pggt.getPowerGeneratingTechnology().getExpectedPermittime();
//                        for (long investmentTimeStep = contructionTime + 1; investmentTimeStep <= time; investmentTimeStep = investmentTimeStep + 1) {
//                            expectedTechnologyAddition += (pggt.getTrend().getValue(investmentTimeStep) - pggt
//                                    .getTrend().getValue(investmentTimeStep - 1));
//                        }
//                        if (expectedTechnologyAddition > 0) {
//                            PowerPlant plant = getReps().createAndSpecifyTemporaryPowerPlant(getCurrentTick(), new EnergyProducer(),
//                                    getReps().findFirstPowerGridNodeByElectricitySpotMarket(market),
//                                    pggt.getPowerGeneratingTechnology());
//                            plant.setActualNominalCapacity(expectedTechnologyAddition);
//                            double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
//                            marginalCostMap.put(plant, plantMarginalCost);
//                            capacitySum += expectedTechnologyAddition;
//                        }
//                    }
//                }
//
//            }
//
//            MapValueComparator comp = new MapValueComparator(marginalCostMap);
//            meritOrder = new TreeMap<PowerPlant, Double>(comp);
//            meritOrder.putAll(marginalCostMap);
//
//            long numberOfSegments = getReps().segments.size();
//
//            double demandFactor = expectedDemand.get(market).doubleValue();
//
//            // find expected prices per segment given merit order
//            for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {
//
//                double expectedSegmentLoad = segmentLoad.getBaseLoad() * demandFactor;
//
//                if (expectedSegmentLoad > maxExpectedLoad) {
//                    maxExpectedLoad = expectedSegmentLoad;
//                }
//
//                double segmentSupply = 0d;
//                double segmentPrice = 0d;
//                double totalCapacityAvailable = 0d;
//
//                for (Entry<PowerPlant, Double> plantCost : meritOrder.entrySet()) {
//                    PowerPlant plant = plantCost.getKey();
//                    double plantCapacity = 0d;
//                    // Determine available capacity in the future in this
//                    // segment
//                    plantCapacity = plant.getExpectedAvailableCapacity(time, segmentLoad.getSegment(), numberOfSegments);
//                    totalCapacityAvailable += plantCapacity;
//                    // logger.warn("Capacity of plant " + plant.toString() +
//                    // " is " +
//                    // plantCapacity/plant.getActualNominalCapacity());
//                    if (segmentSupply < expectedSegmentLoad) {
//                        segmentSupply += plantCapacity;
//                        segmentPrice = plantCost.getValue();
//                    }
//
//                }
//
//                // logger.warn("Segment " +
//                // segmentLoad.getSegment().getSegmentID() + " supply equals " +
//                // segmentSupply + " and segment demand equals " +
//                // expectedSegmentLoad);
//                // Find strategic reserve operator for the market.
//                double reservePrice = 0;
//                double reserveVolume = 0;
//                for (StrategicReserveOperator operator : getReps().strategicReserveOperators) {
//                    ElectricitySpotMarket market1 = getReps().findElectricitySpotMarketForZone(operator
//                            .getZone());
//                    if (market.equals(market1)) {
//                        reservePrice = operator.getReservePriceSR();
//                        reserveVolume = operator.getReserveVolume();
//                    }
//                }
//
//                if (segmentSupply >= expectedSegmentLoad
//                        && ((totalCapacityAvailable - expectedSegmentLoad) <= (reserveVolume))) {
//                    expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), reservePrice);
//                    // logger.warn("Price: "+
//                    // expectedElectricityPricesPerSegment);
//                } else if (segmentSupply >= expectedSegmentLoad
//                        && ((totalCapacityAvailable - expectedSegmentLoad) > (reserveVolume))) {
//                    expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), segmentPrice);
//                    // logger.warn("Price: "+
//                    // expectedElectricityPricesPerSegment);
//                } else {
//                    expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), market.getValueOfLostLoad());
//                }
//
//            }
//        }
//    }

 

}
