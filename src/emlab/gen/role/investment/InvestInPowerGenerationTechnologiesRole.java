/** *****************************************************************************
 * Copyright 2013 the original author or authors.
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

import emlab.gen.domain.agent.BigBank;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.PowerPlantManufacturer;
import emlab.gen.domain.agent.StrategicReserveOperator;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.contract.Loan;
import emlab.gen.domain.gis.Zone;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * {@link EnergyProducer}s decide to invest in new {@link PowerPlant}
 *
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a> @author
 * <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas Chmieliauskas</a>
 * @author JCRichstein
 */
public class InvestInPowerGenerationTechnologiesRole<T extends EnergyProducer> extends GenericInvestmentRole<T> implements Role<T> {

    Map<ElectricitySpotMarket, MarketInformation> marketInfoMap = new HashMap<ElectricitySpotMarket, MarketInformation>();

    public InvestInPowerGenerationTechnologiesRole(Schedule schedule) {
        super(schedule);
    }

    @Override
    public void act(T agent) {
        logger.info(agent + " in considering investment with horizon " + agent.getInvestmentFutureTimeHorizon());
        long futureTimePoint = getCurrentTick() + agent.getInvestmentFutureTimeHorizon();

        // ==== Expectations ===
        Map<Substance, Double> expectedFuelPrices = predictFuelPrices(agent, futureTimePoint);

        // CO2
        Map<ElectricitySpotMarket, Double> expectedCO2Price = determineExpectedCO2PriceInclTax(futureTimePoint,
                agent.getNumberOfYearsBacklookingForForecasting(), getCurrentTick());

        // logger.warn(expectedCO2Price.toString());
        //Demand
        Map<ElectricitySpotMarket, Double> expectedDemand = new HashMap<ElectricitySpotMarket, Double>();
        for (ElectricitySpotMarket elm : getReps().electricitySpotMarkets) {
            GeometricTrendRegression gtr = new GeometricTrendRegression();
            for (long time = getCurrentTick(); time > getCurrentTick() - agent.getNumberOfYearsBacklookingForForecasting() && time >= 0; time = time - 1) {
                gtr.addData(time, elm.getDemandGrowthTrend().getValue(time));
            }
            expectedDemand.put(elm, gtr.predict(futureTimePoint));
        }

        ElectricitySpotMarket market = agent.getInvestorMarket();
        MarketInformation marketInformation = new MarketInformation(market, expectedDemand, expectedFuelPrices, expectedCO2Price.get(market)
                .doubleValue(), futureTimePoint);

        double highestValue = Double.MIN_VALUE;
        //PowerGeneratingTechnology bestTechnology = null;
        PowerPlant bestPlant = null;

        for (PowerGeneratingTechnology technology : getReps().powerGeneratingTechnologies) {

            PowerPlant plant = getReps().createAndSpecifyTemporaryPowerPlant(getCurrentTick(), agent, getNodeForZone(market.getZone()), technology);
//            plant.specifyNotPersist(getCurrentTick(), agent, getNodeForZone(market.getZone()), technology);
            // if too much capacity of this technology in the pipeline (not
            // limited to the 5 years)
            double expectedInstalledCapacityOfTechnology = getReps().calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market, technology, futureTimePoint);
            PowerGeneratingTechnologyTarget technologyTarget = getReps().findPowerGeneratingTechnologyTargetByTechnologyAndMarket(technology, market);
            if (technologyTarget != null) {
                double technologyTargetCapacity = technologyTarget.getTrend().getValue(futureTimePoint);
                expectedInstalledCapacityOfTechnology = (technologyTargetCapacity > expectedInstalledCapacityOfTechnology) ? technologyTargetCapacity : expectedInstalledCapacityOfTechnology;
            }
            double pgtNodeLimit = Double.MAX_VALUE;
            PowerGeneratingTechnologyNodeLimit pgtLimit = getReps()
                    .findOneByTechnologyAndNode(technology, plant.getLocation());
            if (pgtLimit != null) {
                pgtNodeLimit = pgtLimit.getUpperCapacityLimit(futureTimePoint);
            }
            double expectedInstalledCapacityOfTechnologyInNode = getReps().calculateCapacityOfExpectedOperationalPowerPlantsByNodeAndTechnology(plant.getLocation(),
                    technology, futureTimePoint);
            double expectedOwnedTotalCapacityInMarket = getReps().calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwner(market, futureTimePoint, agent);
            double expectedOwnedCapacityInMarketOfThisTechnology = getReps()
                    .calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwnerAndTechnology(market, technology, futureTimePoint,
                            agent);
            double capacityOfTechnologyInPipeline = getReps().calculateCapacityOfPowerPlantsByTechnologyInPipeline(
                    technology, getCurrentTick());
            double operationalCapacityOfTechnology = getReps().calculateCapacityOfOperationalPowerPlantsByTechnology(
                    technology, getCurrentTick());
            double capacityInPipelineInMarket = getReps()
                    .calculateCapacityOfPowerPlantsByMarketInPipeline(market, getCurrentTick());

            if ((expectedInstalledCapacityOfTechnology + plant.getActualNominalCapacity())
                    / (marketInformation.maxExpectedLoad + plant.getActualNominalCapacity()) > technology
                    .getMaximumInstalledCapacityFractionInCountry()) {
                // logger.warn(agent +
                // " will not invest in {} technology because there's too much of this type in the market",
                // technology);
            } else if ((expectedInstalledCapacityOfTechnologyInNode + plant.getActualNominalCapacity()) > pgtNodeLimit) {

            } else if (expectedOwnedCapacityInMarketOfThisTechnology > expectedOwnedTotalCapacityInMarket
                    * technology.getMaximumInstalledCapacityFractionPerAgent()) {
                // logger.warn(agent +
                // " will not invest in {} technology because there's too much capacity planned by him",
                // technology);
            } else if (capacityInPipelineInMarket > 0.2 * marketInformation.maxExpectedLoad) {
                // logger.warn("Not investing because more than 20% of demand in pipeline.");

            } else if ((capacityOfTechnologyInPipeline > 2.0 * operationalCapacityOfTechnology)
                    && capacityOfTechnologyInPipeline > 9000) { // TODO:
                // reflects that you cannot expand a technology out of zero.
                // logger.warn(agent +
                // " will not invest in {} technology because there's too much capacity in the pipeline",
                // technology);
            } else if (plant.getActualInvestedCapital() * (1 - agent.getDebtRatioOfInvestments()) > agent
                    .getDownpaymentFractionOfCash() * agent.getCash()) {
                // logger.warn(agent +
                // " will not invest in {} technology as he does not have enough money for downpayment",
                // technology);
            } else {

                // Passes all hard limits. Financial consideration.
                Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
                for (Substance fuel : technology.getFuels()) {
                    myFuelPrices.put(fuel, expectedFuelPrices.get(fuel));
                }
                //TODO: all investment: change to an empty fuel mix default.
                Set<SubstanceShareInFuelMix> fuelMix = new HashSet<SubstanceShareInFuelMix>();
                if (myFuelPrices.size() > 0) {
                    fuelMix = calculateFuelMix(plant, myFuelPrices, expectedCO2Price.get(market));
                }
                plant.setFuelMix(fuelMix);

                double expectedMarginalCost = determineExpectedMarginalCost(plant, expectedFuelPrices, expectedCO2Price.get(market));
                double runningHours = 0d;
                double expectedGrossProfit = 0d;

                long numberOfSegments = getReps().segments.size();

                // TODO somehow the prices of long-term contracts could also
                // be used here to determine the expected profit. Maybe not
                // though...
                for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {
                    double expectedElectricityPrice = marketInformation.expectedElectricityPricesPerSegment.get(segmentLoad
                            .getSegment());
                    double hours = segmentLoad.getSegment().getLengthInHours();
                    if (expectedMarginalCost <= expectedElectricityPrice) {
                        runningHours += hours;
                        expectedGrossProfit += (expectedElectricityPrice - expectedMarginalCost) * hours
                                * plant.getAvailableCapacity(futureTimePoint, segmentLoad.getSegment(), numberOfSegments);
                    }
                }

                //logger.warning(agent + "expects technology " + technology + " to have " + runningHours + " hours running");
                //expect to meet minimum running hours?
                if (runningHours < plant.getTechnology().getMinimumRunningHours()) {
                    logger.info(agent + " will not invest in " + technology + " technology as he expect to have " + runningHours + " running hours, which is lower then required");
                } else {

                    double fixedOMCost = calculateFixedOperatingCost(plant, getCurrentTick());// /
                    // plant.getActualNominalCapacity();

                    double operatingProfit = expectedGrossProfit - fixedOMCost;

                    // TODO Alter discount rate on the basis of the amount
                    // in long-term contracts?
                    // TODO Alter discount rate on the basis of other stuff,
                    // such as amount of money, market share, portfolio
                    // size.
                    // Calculation of weighted average cost of capital,
                    // based on the companies debt-ratio
                    double wacc = (1 - agent.getDebtRatioOfInvestments()) * agent.getEquityInterestRate()
                            + agent.getDebtRatioOfInvestments() * agent.getLoanInterestRate();

                    // Creation of out cash-flow during power plant building
                    // phase (note that the cash-flow is negative!)
                    TreeMap<Integer, Double> discountedProjectCapitalOutflow = calculateSimplePowerPlantInvestmentCashFlow(
                            technology.getDepreciationTime(), (int) plant.getActualLeadtime(),
                            plant.getActualInvestedCapital(), 0);
                    // Creation of in cashflow during operation
                    TreeMap<Integer, Double> discountedProjectCashInflow = calculateSimplePowerPlantInvestmentCashFlow(
                            technology.getDepreciationTime(), (int) plant.getActualLeadtime(), 0, operatingProfit);

                    double discountedCapitalCosts = npv(discountedProjectCapitalOutflow, wacc);// are
                    // defined
                    // negative!!
                    // plant.getActualNominalCapacity();

                    // logger.warn("Agent {}  found that the discounted capital for technology {} to be "
                    // + discountedCapitalCosts, agent,
                    // technology);
                    double discountedOpProfit = npv(discountedProjectCashInflow, wacc);

                    //logger.warning(agent + " found that the projected discounted inflows for technology " + technology + " to be " + discountedOpProfit);
                    double projectValue = discountedOpProfit + discountedCapitalCosts;

                    logger.info(agent + " found the project value for technology " + technology + " to be " + Math.round(projectValue / (plant.getActualNominalCapacity() * 1e3)) / 1e3 + " million EUR/kW (running hours: " + runningHours + ")");
                    // double projectTotalValue = projectValuePerMW *
                    // plant.getActualNominalCapacity();
                    // double projectReturnOnInvestment = discountedOpProfit
                    // / (-discountedCapitalCosts);

                    /*
                     * Divide by capacity, in order not to favour large power
                     * plants (which have the single largest NPV
                     */
                    if (projectValue > 0 && projectValue / plant.getActualNominalCapacity() > highestValue) {
                        highestValue = projectValue / plant.getActualNominalCapacity();
                        //bestTechnology = plant.getTechnology();
                        bestPlant = plant;
                    }
                }

            }
        }

        if (bestPlant != null) {
            logger.log(Level.INFO, "{0} invested in technology {1} at tick {2}", new Object[]{agent, bestPlant.getTechnology(), getCurrentTick()});
//            PowerPlant plant = getReps().createAndSpecifyTemporaryPowerPlant(getCurrentTick(), agent, getNodeForZone(market.getZone()), bestTechnology);
            getReps().createPowerPlantFromPlant(bestPlant);
            //TODO recalculate fuelmix in other investment roles!
            Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
            for (Substance fuel : bestPlant.getTechnology().getFuels()) {
                myFuelPrices.put(fuel, expectedFuelPrices.get(fuel));
            }
            bestPlant.setFuelMix(calculateFuelMix(bestPlant, myFuelPrices, expectedCO2Price.get(market)));

            PowerPlantManufacturer manufacturer = getReps().powerPlantManufacturer;
            BigBank bigbank = getReps().bigBank;

            double investmentCostPayedByEquity = bestPlant.getActualInvestedCapital() * (1 - agent.getDebtRatioOfInvestments());
            double investmentCostPayedByDebt = bestPlant.getActualInvestedCapital() * agent.getDebtRatioOfInvestments();
            double downPayment = investmentCostPayedByEquity;
            createSpreadOutDownPayments(agent, manufacturer, downPayment, bestPlant);

            double amount = determineLoanAnnuities(investmentCostPayedByDebt, bestPlant.getTechnology().getDepreciationTime(),
                    agent.getLoanInterestRate());
            // logger.warn("Loan amount is: " + amount);
            Loan loan = getReps().createLoan(agent, bigbank, amount, bestPlant.getTechnology().getDepreciationTime(),
                    getCurrentTick(), bestPlant);
            // Create the loan
            bestPlant.createOrUpdateLoan(loan);

        } else {
            // logger.warn("{} found no suitable technology anymore to invest in at tick "
            // + getCurrentTick(), agent);
            // agent will not participate in the next round of investment if
            // he does not invest now
            setNotWillingToInvest(agent);
        }
        logger.info("Investment done for " + agent);
    }

    // }
    // Creates n downpayments of equal size in each of the n building years of a
    // power plant
    private void createSpreadOutDownPayments(EnergyProducer agent, PowerPlantManufacturer manufacturer, double totalDownPayment,
            PowerPlant plant) {
        int buildingTime = (int) plant.getActualLeadtime();
        getReps().createCashFlow(agent, manufacturer, totalDownPayment / buildingTime,
                CashFlow.DOWNPAYMENT, getCurrentTick(), plant);
        Loan downpayment = getReps().createLoan(agent, manufacturer, totalDownPayment / buildingTime,
                buildingTime - 1, getCurrentTick(), plant);
        plant.createOrUpdateDownPayment(downpayment);
    }

    private void setNotWillingToInvest(EnergyProducer agent) {
        agent.setWillingToInvest(false);
    }

    /**
     * Predicts fuel prices for {@link futureTimePoint} using a geometric trend
     * regression forecast. Only predicts fuels that are traded on a commodity
     * market.
     *
     * @param agent
     * @param futureTimePoint
     * @return Map<Substance, Double> of predicted prices.
     */
    public Map<Substance, Double> predictFuelPrices(EnergyProducer agent, long futureTimePoint) {
        // Fuel Prices
        Map<Substance, Double> expectedFuelPrices = new HashMap<Substance, Double>();
        for (Substance substance : getReps().substancesOnCommodityMarkets) {
            logger.info("Predicting price for " + substance);
            //Find Clearing Points for the last 5 years (counting current year as one of the last 5 years).
            Iterable<ClearingPoint> cps = getReps().findAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(substance, getCurrentTick()
                    - (agent.getNumberOfYearsBacklookingForForecasting() - 1), getCurrentTick(), false);
            //logger.warn("{}, {}", getCurrentTick()-(agent.getNumberOfYearsBacklookingForForecasting()-1), getCurrentTick());
            //Create regression object
            GeometricTrendRegression gtr = new GeometricTrendRegression();
            for (ClearingPoint clearingPoint : cps) {
                //logger.warn("CP {}: {} , in" + clearingPoint.getTime(), substance.getName(), clearingPoint.getPrice());
                gtr.addData(clearingPoint.getTime(), clearingPoint.getPrice());
            }
            expectedFuelPrices.put(substance, gtr.predict(futureTimePoint));
            //logger.warn("Forecast {}: {}, in Step " +  futureTimePoint, substance, expectedFuelPrices.get(substance));
        }
        return expectedFuelPrices;
    }

    // Create a powerplant investment and operation cash-flow in the form of a
    // map. If only investment, or operation costs should be considered set
    // totalInvestment or operatingProfit to 0
    private TreeMap<Integer, Double> calculateSimplePowerPlantInvestmentCashFlow(int depriacationTime, int buildingTime,
            double totalInvestment, double operatingProfit) {
        TreeMap<Integer, Double> investmentCashFlow = new TreeMap<Integer, Double>();
        double equalTotalDownPaymentInstallement = totalInvestment / buildingTime;
        for (int i = 0; i < buildingTime; i++) {
            investmentCashFlow.put(new Integer(i), -equalTotalDownPaymentInstallement);
        }
        for (int i = buildingTime; i < depriacationTime + buildingTime; i++) {
            investmentCashFlow.put(new Integer(i), operatingProfit);
        }

        return investmentCashFlow;
    }

    private double npv(TreeMap<Integer, Double> netCashFlow, double wacc) {
        double npv = 0;
        for (Integer iterator : netCashFlow.keySet()) {
            npv += netCashFlow.get(iterator).doubleValue() / Math.pow(1 + wacc, iterator.intValue());
        }
        return npv;
    }

    public double determineExpectedMarginalCost(PowerPlant plant, Map<Substance, Double> expectedFuelPrices, double expectedCO2Price) {
        double mc = determineExpectedMarginalFuelCost(plant, expectedFuelPrices);
        double co2Intensity = plant.calculateEmissionIntensity();
        mc += co2Intensity * expectedCO2Price;
        return mc;
    }

    public double determineExpectedMarginalFuelCost(PowerPlant powerPlant, Map<Substance, Double> expectedFuelPrices) {
        double fc = 0d;
        logger.info("Fuel mix of plant: " + powerPlant + " of owner " + powerPlant.getOwner() + " is " + powerPlant.getFuelMix());
        for (SubstanceShareInFuelMix mix : powerPlant.getFuelMix()) {
            double amount = mix.getShare();
            logger.info("amount of fuel: " + amount);
            logger.info("fuel prices: " + expectedFuelPrices.size());
            double fuelPrice = expectedFuelPrices.get(mix.getSubstance());
            fc += amount * fuelPrice;
        }
        return fc;
    }

    private PowerGridNode getNodeForZone(Zone zone) {
        for (PowerGridNode node : getReps().powerGridNodes) {
            if (node.getZone().equals(zone)) {
                return node;
            }
        }
        return null;
    }

    private class MarketInformation {

        Map<Segment, Double> expectedElectricityPricesPerSegment;
        double maxExpectedLoad = 0d;
        Map<PowerPlant, Double> meritOrder;
        double capacitySum;

        MarketInformation(ElectricitySpotMarket market, Map<ElectricitySpotMarket, Double> expectedDemand, Map<Substance, Double> fuelPrices, double co2price, long time) {
            // determine expected power prices
            expectedElectricityPricesPerSegment = new HashMap<Segment, Double>();
            Map<PowerPlant, Double> marginalCostMap = new HashMap<PowerPlant, Double>();
            capacitySum = 0d;

            // get merit order for this market
            for (PowerPlant plant : getReps().findExpectedOperationalPowerPlantsInMarket(market, time)) {

                double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
                marginalCostMap.put(plant, plantMarginalCost);
                capacitySum += plant.getActualNominalCapacity();
            }

            //get difference between technology target and expected operational capacity
            for (PowerGeneratingTechnologyTarget pggt : getReps().findAllPowerGeneratingTechnologyTargetsByMarket(market)) {
                double expectedTechnologyCapacity = getReps().calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market, pggt.getPowerGeneratingTechnology(), time);
                double targetDifference = pggt.getTrend().getValue(time) - expectedTechnologyCapacity;
                if (targetDifference > 0) {
                    PowerPlant plant = getReps().createAndSpecifyTemporaryPowerPlant(getCurrentTick(), new EnergyProducer(), getReps().findFirstPowerGridNodeByElectricitySpotMarket(market), pggt.getPowerGeneratingTechnology());
                    plant.setActualNominalCapacity(targetDifference);
                    Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
                    for (Substance fuel : plant.getTechnology().getFuels()) {
                        myFuelPrices.put(fuel, fuelPrices.get(fuel));
                    }
                    plant.setFuelMix(calculateFuelMix(plant, myFuelPrices, co2price));
                    double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
                    marginalCostMap.put(plant, plantMarginalCost);
                    capacitySum += targetDifference;
                }
            }

            MapValueComparator comp = new MapValueComparator(marginalCostMap);
            meritOrder = new TreeMap<PowerPlant, Double>(comp);
            meritOrder.putAll(marginalCostMap);

            long numberOfSegments = getReps().segments.size();

            double demandFactor = expectedDemand.get(market).doubleValue();

            // find expected prices per segment given merit order
            for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {

                double expectedSegmentLoad = segmentLoad.getBaseLoad() * demandFactor;

                if (expectedSegmentLoad > maxExpectedLoad) {
                    maxExpectedLoad = expectedSegmentLoad;
                }

                double segmentSupply = 0d;
                double segmentPrice = 0d;
                double totalCapacityAvailable = 0d;

                for (Entry<PowerPlant, Double> plantCost : meritOrder.entrySet()) {
                    PowerPlant plant = plantCost.getKey();
                    double plantCapacity = 0d;
                    // Determine available capacity in the future in this
                    // segment
                    plantCapacity = plant.getExpectedAvailableCapacity(time, segmentLoad.getSegment(), numberOfSegments);
                    totalCapacityAvailable += plantCapacity;
                    // logger.warn("Capacity of plant " + plant.toString() +
                    // " is " +
                    // plantCapacity/plant.getActualNominalCapacity());
                    if (segmentSupply < expectedSegmentLoad) {
                        segmentSupply += plantCapacity;
                        segmentPrice = plantCost.getValue();
                    }

                }

                // logger.warn("Segment " +
                // segmentLoad.getSegment().getSegmentID() + " supply equals " +
                // segmentSupply + " and segment demand equals " +
                // expectedSegmentLoad);
                // Find strategic reserve operator for the market.
                double reservePrice = 0;
                double reserveVolume = 0;
                for (StrategicReserveOperator operator : getReps().strategicReserveOperators) {
                    ElectricitySpotMarket market1 = getReps().findElectricitySpotMarketForZone(operator
                            .getZone());
                    if (market.equals(market1)) {
                        reservePrice = operator.getReservePriceSR();
                        reserveVolume = operator.getReserveVolume();
                    }
                }

                if (segmentSupply >= expectedSegmentLoad
                        && ((totalCapacityAvailable - expectedSegmentLoad) <= (reserveVolume))) {
                    expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), reservePrice);
                    // logger.warn("Price: "+
                    // expectedElectricityPricesPerSegment);
                } else if (segmentSupply >= expectedSegmentLoad
                        && ((totalCapacityAvailable - expectedSegmentLoad) > (reserveVolume))) {
                    expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), segmentPrice);
                    // logger.warn("Price: "+
                    // expectedElectricityPricesPerSegment);
                } else {
                    expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), market.getValueOfLostLoad());
                }

            }
        }
    }

}
