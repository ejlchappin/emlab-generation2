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

import java.util.HashMap;
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
import emlab.gen.domain.agent.StrategicReserveOperator;
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
public class InvestInPowerGenerationTechnologiesWithCO2ForecastRole<T extends EnergyProducer>  extends AbstractInvestInPowerGenerationTechnologiesRole<T> implements Role<T> {

    public InvestInPowerGenerationTechnologiesWithCO2ForecastRole(Schedule schedule) {
        super(schedule);
    }

    @Override
    public void act(T agent) {
    	
    	// TODO implement with new interface, probably same as Standard Role anyway?
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
//        PowerGeneratingTechnology bestTechnology = null;
//
//        for (PowerGeneratingTechnology technology : getReps().powerGeneratingTechnologies) {
//
//            PowerPlant plant = getReps().createAndSpecifyTemporaryPowerPlant(getCurrentTick(), agent, getNodeForZone(market.getZone()), technology);
//            // if too much capacity of this technology in the pipeline (not
//            // limited to the 5 years)
//            double expectedInstalledCapacityOfTechnology = getReps()
//                    .calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market, technology, futureTimePoint);
//            PowerGeneratingTechnologyTarget technologyTarget = getReps().findPowerGeneratingTechnologyTargetByTechnologyAndMarket(technology, market);
//            if (technologyTarget != null) {
//                double technologyTargetCapacity = technologyTarget.getTrend().getValue(futureTimePoint);
//                expectedInstalledCapacityOfTechnology = (technologyTargetCapacity > expectedInstalledCapacityOfTechnology) ? technologyTargetCapacity : expectedInstalledCapacityOfTechnology;
//            }
//            double pgtNodeLimit = Double.MAX_VALUE;
//            PowerGeneratingTechnologyNodeLimit pgtLimit = getReps().findOneByTechnologyAndNode(technology, plant.getLocation());
//            if (pgtLimit != null) {
//                pgtNodeLimit = pgtLimit.getUpperCapacityLimit(futureTimePoint);
//            }
//            double expectedInstalledCapacityOfTechnologyInNode = getReps()
//                    .calculateCapacityOfExpectedOperationalPowerPlantsByNodeAndTechnology(plant.getLocation(),
//                            technology, futureTimePoint);
//            double expectedOwnedTotalCapacityInMarket = getReps()
//                    .calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwner(market, futureTimePoint, agent);
//            double expectedOwnedCapacityInMarketOfThisTechnology = getReps()
//                    .calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwnerAndTechnology(market, technology, futureTimePoint,
//                            agent);
//            double capacityOfTechnologyInPipeline = getReps().calculateCapacityOfPowerPlantsByTechnologyInPipeline(
//                    technology, getCurrentTick());
//            double operationalCapacityOfTechnology = getReps().calculateCapacityOfOperationalPowerPlantsByTechnology(
//                    technology, getCurrentTick());
//            double capacityInPipelineInMarket = getReps()
//                    .calculateCapacityOfPowerPlantsByMarketInPipeline(market, getCurrentTick());
//
//            if ((expectedInstalledCapacityOfTechnology + plant.getActualNominalCapacity())
//                    / (marketInformation.maxExpectedLoad + plant.getActualNominalCapacity()) > technology
//                    .getMaximumInstalledCapacityFractionInCountry()) {
//                // logger.warn(agent +
//                // " will not invest in {} technology because there's too much of this type in the market",
//                // technology);
//            } else if ((expectedInstalledCapacityOfTechnologyInNode + plant.getActualNominalCapacity()) > pgtNodeLimit) {
//
//            } else if (expectedOwnedCapacityInMarketOfThisTechnology > expectedOwnedTotalCapacityInMarket
//                    * technology.getMaximumInstalledCapacityFractionPerAgent()) {
//                // logger.warn(agent +
//                // " will not invest in {} technology because there's too much capacity planned by him",
//                // technology);
//            } else if (capacityInPipelineInMarket > 0.2 * marketInformation.maxExpectedLoad) {
//                // logger.warn("Not investing because more than 20% of demand in pipeline.");
//
//            } else if ((capacityOfTechnologyInPipeline > 2.0 * operationalCapacityOfTechnology)
//                    && capacityOfTechnologyInPipeline > 9000) { // TODO:
//                // reflects that you cannot expand a technology out of zero.
//                // logger.warn(agent +
//                // " will not invest in {} technology because there's too much capacity in the pipeline",
//                // technology);
//            } else if (plant.getActualInvestedCapital() * (1 - agent.getDebtRatioOfInvestments()) > agent
//                    .getDownpaymentFractionOfCash() * agent.getCash()) {
//                // logger.warn(agent +
//                // " will not invest in {} technology as he does not have enough money for downpayment",
//                // technology);
//            } else {
//
//                Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
//                for (Substance fuel : technology.getFuels()) {
//                    myFuelPrices.put(fuel, expectedFuelPrices.get(fuel));
//                }
//                Set<SubstanceShareInFuelMix> fuelMix = calculateFuelMix(plant, myFuelPrices, expectedCO2Price.get(market));
//                plant.setFuelMix(fuelMix);
//
//                double expectedMarginalCost = determineExpectedMarginalCost(plant, expectedFuelPrices, expectedCO2Price.get(market));
//                double runningHours = 0d;
//                double expectedGrossProfit = 0d;
//
//                long numberOfSegments = getReps().segments.size();
//
//                // TODO somehow the prices of long-term contracts could also
//                // be used here to determine the expected profit. Maybe not
//                // though...
//                for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {
//                    double expectedElectricityPrice = marketInformation.expectedElectricityPricesPerSegment.get(segmentLoad
//                            .getSegment());
//                    double hours = segmentLoad.getSegment().getLengthInHours();
//                    if (expectedMarginalCost <= expectedElectricityPrice) {
//                        runningHours += hours;
//                        expectedGrossProfit += (expectedElectricityPrice - expectedMarginalCost) * hours
//                                * plant.getAvailableCapacity(futureTimePoint, segmentLoad.getSegment(), numberOfSegments);
//                    }
//                }
//
//                // logger.warn(agent +
//                // "expects technology {} to have {} running", technology,
//                // runningHours);
//                // expect to meet minimum running hours?
//                if (runningHours < plant.getTechnology().getMinimumRunningHours()) {
//                    // logger.warn(agent+
//                    // " will not invest in {} technology as he expect to have {} running, which is lower then required",
//                    // technology, runningHours);
//                } else {
//
//                    double fixedOMCost = calculateFixedOperatingCost(plant, getCurrentTick());// /
//                    // plant.getActualNominalCapacity();
//
//                    double operatingProfit = expectedGrossProfit - fixedOMCost;
//
//                    // TODO Alter discount rate on the basis of the amount
//                    // in long-term contracts?
//                    // TODO Alter discount rate on the basis of other stuff,
//                    // such as amount of money, market share, portfolio
//                    // size.
//                    // Calculation of weighted average cost of capital,
//                    // based on the companies debt-ratio
//                    double wacc = (1 - agent.getDebtRatioOfInvestments()) * agent.getEquityInterestRate()
//                            + agent.getDebtRatioOfInvestments() * agent.getLoanInterestRate();
//
//                    // Creation of out cash-flow during power plant building
//                    // phase (note that the cash-flow is negative!)
//                    TreeMap<Integer, Double> discountedProjectCapitalOutflow = calculateSimplePowerPlantInvestmentCashFlow(
//                            technology.getDepreciationTime(), (int) plant.getActualLeadtime(),
//                            plant.getActualInvestedCapital(), 0);
//                    // Creation of in cashflow during operation
//                    TreeMap<Integer, Double> discountedProjectCashInflow = calculateSimplePowerPlantInvestmentCashFlow(
//                            technology.getDepreciationTime(), (int) plant.getActualLeadtime(), 0, operatingProfit);
//
//                    double discountedCapitalCosts = npv(discountedProjectCapitalOutflow, wacc);// are
//                    // defined
//                    // negative!!
//                    // plant.getActualNominalCapacity();
//
//                    // logger.warn("Agent {}  found that the discounted capital for technology {} to be "
//                    // + discountedCapitalCosts, agent,
//                    // technology);
//                    double discountedOpProfit = npv(discountedProjectCashInflow, wacc);
//
//                    // logger.warn("Agent {}  found that the projected discounted inflows for technology {} to be "
//                    // + discountedOpProfit,
//                    // agent, technology);
//                    double projectValue = discountedOpProfit + discountedCapitalCosts;
//
//                    // logger.warn(
//                    // "Agent {}  found the project value for technology {} to be "
//                    // + Math.round(projectValue /
//                    // plant.getActualNominalCapacity()) +
//                    // " EUR/kW (running hours: "
//                    // + runningHours + "", agent, technology);
//                    // double projectTotalValue = projectValuePerMW *
//                    // plant.getActualNominalCapacity();
//                    // double projectReturnOnInvestment = discountedOpProfit
//                    // / (-discountedCapitalCosts);
//
//                    /*
//                     * Divide by capacity, in order not to favour large power plants (which have the single largest NPV
//                     */
//                    if (projectValue > 0 && projectValue / plant.getActualNominalCapacity() > highestValue) {
//                        highestValue = projectValue / plant.getActualNominalCapacity();
//                        bestTechnology = plant.getTechnology();
//                    }
//                }
//
//            }
//        }
//
//        if (bestTechnology != null) {
//            // logger.warn("Agent {} invested in technology {} at tick " + getCurrentTick(), agent, bestTechnology);
//
//            PowerPlant plant = getReps().createAndSpecifyTemporaryPowerPlant(getCurrentTick(), agent, getNodeForZone(market.getZone()), bestTechnology);
//            getReps().createPowerPlantFromPlant(plant);
//            
//            Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
//            for (Substance fuel : bestTechnology.getFuels()) {
//                myFuelPrices.put(fuel, expectedFuelPrices.get(fuel));
//            }
//            plant.setFuelMix(calculateFuelMix(plant, myFuelPrices, expectedCO2Price.get(market)));
//            
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

}
