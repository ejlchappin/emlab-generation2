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
package emlab.gen.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math.optimization.linear.Relationship;
import org.apache.commons.math.optimization.linear.SimplexSolver;
import org.apache.commons.math.stat.regression.SimpleRegression;

import emlab.gen.domain.agent.CommoditySupplier;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.market.CO2Auction;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.DecarbonizationMarket;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.FinancialPowerPlantReport;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.engine.AbstractRole;
import emlab.gen.engine.Schedule;
import emlab.gen.trend.GeometricTrend;
import emlab.gen.util.GeometricTrendRegression;
import java.util.logging.Level;

public abstract class AbstractEnergyProducerRole<T extends EnergyProducer> extends AbstractRole<T> {

    public AbstractEnergyProducerRole(Schedule schedule) {
        super(schedule);
    }

    
    public double calculateMarginalCO2Cost(PowerPlant powerPlant, long tick, boolean forecast) {
        double mc = 0d;
        // fuel cost
        mc += calculateCO2TaxMarginalCost(powerPlant, tick);
        mc += calculateCO2MarketMarginalCost(powerPlant, tick, forecast);
//        logger.info("Margincal cost for plant {} is {}", powerPlant.getName(), mc);
        return mc;
    }

    public double calculateMarginalCostExclCO2MarketCost(PowerPlant powerPlant, long clearingTick) {
        double mc = 0d;
        // fuel cost
        mc += calculateMarginalFuelCost(powerPlant, clearingTick);
        mc += calculateCO2TaxMarginalCost(powerPlant, clearingTick);
//        logger.info("Margincal cost excluding CO2 auction/market cost for plant {} is {}", powerPlant.getName(), mc);
        return mc;
    }

    public double calculateExpectedMarginalCostExclCO2MarketCost(PowerPlant powerPlant,
            Map<Substance, Double> forecastedFuelPrices, long tick) {
        double mc = 0d;
        mc += calculateExpectedMarginalFuelCost(powerPlant, forecastedFuelPrices);
        mc += calculateCO2TaxMarginalCost(powerPlant, tick);
//        logger.info("Margincal cost excluding CO2 auction/market cost for plant {} is {}", powerPlant.getName(), mc);
        return mc;
    }

    public double calculateMarginalFuelCost(PowerPlant powerPlant, long clearingTick) {
        double fc = 0d;
        // fuel cost for each fuel
        for (SubstanceShareInFuelMix mix : powerPlant.getFuelMix()) {

            double amount = mix.getShare();
//            logger.info("Calculating need for fuel: {} units of {}", mix.getShare(), mix.getSubstance().getName());
            double fuelPrice = findLastKnownPriceForSubstance(mix.getSubstance(), clearingTick);
            fc += amount * fuelPrice;
//            logger.info("Calculating marginal cost and found a fuel price which is {} per unit of fuel", fuelPrice);
        }

        return fc;
    }

    public double calculateExpectedMarginalFuelCost(PowerPlant powerPlant, Map<Substance, Double> forecastedFuelPrices) {
        double fc = 0d;
        // fuel cost for each fuel
        for (SubstanceShareInFuelMix mix : powerPlant.getFuelMix()) {

            double amount = mix.getShare();
//            logger.info("Calculating need for fuel: {} units of {}", mix.getShare(), mix.getSubstance().getName());
            double fuelPrice = forecastedFuelPrices.get(mix.getSubstance());
            fc += amount * fuelPrice;
//            logger.info("Calculating marginal cost and found a fuel price which is {} per unit of fuel", fuelPrice);
        }

        return fc;
    }

    /**
     * Finds the last known price on a specific market. We try to get it for
     * this tick, previous tick, or from a possible supplier directly. If
     * multiple prices are found, the average is returned. This is the case for
     * electricity spot markets, as they may have segments.
     *
     * @param substance the price we want for
     * @return the (average) price found
     */
    public double findLastKnownPriceOnMarket(DecarbonizationMarket market, long clearingTick) {
        Double average = calculateAverageMarketPriceBasedOnClearingPoints(getReps().findClearingPointsForMarketAndTime(market, clearingTick, false));
        Substance substance = market.getSubstance();

        if (average != null) {
            logger.finer("Average price found on market for this tick for " + substance.getName());
            return average;
        }

        average = calculateAverageMarketPriceBasedOnClearingPoints(getReps().findClearingPointsForMarketAndTime(
                market, clearingTick - 1, false));
        if (average != null) {
            logger.finer("Average price found on market for previous tick for "+ substance.getName());
            return average;
        }

        if (market.getReferencePrice() > 0) {
            logger.finer("Found a reference price found for market for " + substance.getName());
            return market.getReferencePrice();
        }

        for (CommoditySupplier supplier : getReps().commoditySuppliers) {
            if (supplier.getSubstance().equals(substance)) {

                return supplier.getPriceOfCommodity().getValue(clearingTick);
            }
        }

//        logger.info("No price has been found for {}", substance.getName());
        return 0d;
    }

    /**
     * Finds the last known price for a substance. We try to find the market for
     * it and get it get the price on that market for this tick, previous tick,
     * or from a possible supplier directly. If multiple prices are found, the
     * average is returned. This is the case for electricity spot markets, as
     * they may have segments.
     *
     * @param substance the price we want for
     * @return the (average) price found
     */
    public double findLastKnownPriceForSubstance(Substance substance, long clearingTick) {

        DecarbonizationMarket market = getReps().findMarketBySubstance(substance);
        if (market == null) {
            logger.log(Level.WARNING, "No market found for {0} so no price can be found", substance.getName());
            return 0d;
        } else {
            return findLastKnownPriceOnMarket(market, clearingTick);
        }
    }

    /**
     * Calculates the volume-weighted average price on a market based on a set
     * of clearingPoints.
     *
     * @param clearingPoints the clearingPoints with the volumes and prices
     * @return the weighted average
     */
    private Double calculateAverageMarketPriceBasedOnClearingPoints(Iterable<ClearingPoint> clearingPoints) {
        double priceTimesVolume = 0d;
        double volume = 0d;

        for (ClearingPoint point : clearingPoints) {
            priceTimesVolume += point.getPrice() * point.getVolume();
            volume += point.getVolume();
        }
        if (volume > 0) {
            return priceTimesVolume / volume;
        }
        return null;
    }

    public double calculateCO2MarketMarginalCost(PowerPlant powerPlant, long clearingTick, boolean forecast) {
        double co2Intensity = powerPlant.calculateEmissionIntensity();
        CO2Auction auction = getReps().co2Auction;
        double co2Price;
        try {
            co2Price = getReps().findClearingPointForMarketAndTime(auction, clearingTick, forecast)
                    .getPrice();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Couldn''t find clearing point for tick {0} and market {1}", new Object[]{clearingTick, auction});
            co2Price = findLastKnownCO2Price(clearingTick);
        }

        return co2Intensity * co2Price;
    }

    public double calculateCO2MarketCost(PowerPlant powerPlant, boolean forecast, long clearingTick) {
        double co2Intensity = powerPlant.calculateEmissionIntensity();
        CO2Auction auction = getReps().co2Auction;
        double co2Price = findLastKnownPriceOnMarket(auction, clearingTick);
        double electricityOutput = powerPlant.calculateElectricityOutputAtTime(clearingTick, forecast);
        return co2Intensity * co2Price * electricityOutput;
    }

    /**
     * Calculates the payment effective part of the national CO2 price. In this
     * case you only pay the excess over the EU carbon market price to your own
     * government.
     *
     * @param powerPlant
     * @return
     */
    public double calculatePaymentEffictiveCO2NationalMinimumPriceCost(PowerPlant powerPlant, boolean forecast,
            long clearingTick) {
        double co2Intensity = powerPlant.calculateEmissionIntensity();
        CO2Auction auction = getReps().co2Auction;
        double co2Price = findLastKnownPriceOnMarket(auction, clearingTick);
        double electricityOutput = powerPlant.calculateElectricityOutputAtTime(getCurrentTick(), forecast);
        double nationalMinCo2price = getReps().findNationalGovernmentByPowerPlant(powerPlant)
                .getMinNationalCo2PriceTrend().getValue(getCurrentTick());
        double paymentEffectivePartOfNationalCO2;
        if (nationalMinCo2price > co2Price) {
            paymentEffectivePartOfNationalCO2 = nationalMinCo2price - co2Price;
        } else {
            paymentEffectivePartOfNationalCO2 = 0;
        }
        return co2Intensity * paymentEffectivePartOfNationalCO2 * electricityOutput;
    }

    public double calculateCO2TaxMarginalCost(PowerPlant powerPlant, long tick) {
        double co2Intensity = powerPlant.calculateEmissionIntensity();
        Government government = getReps().government;
        double co2Tax = government.getCO2Tax(tick);
        return co2Intensity * co2Tax;
    }

    public double findLastKnownCO2Price(long clearingTick) {
        Government government = getReps().government;
        CO2Auction auction = getReps().co2Auction;
        double co2Price = findLastKnownPriceOnMarket(auction, clearingTick);
        double co2Tax = government.getCO2Tax(clearingTick);
        return co2Price + co2Tax;
    }

    public double calculateCO2Tax(PowerPlant powerPlant, boolean forecast, long clearingTick) {
        double co2Intensity = powerPlant.calculateEmissionIntensity();
        double electricityOutput = powerPlant.calculateElectricityOutputAtTime(clearingTick, forecast);
        Government government = getReps().government;
        double co2Tax = government.getCO2Tax(clearingTick);
        double taxToPay = (co2Intensity * electricityOutput) * co2Tax;
        return taxToPay;
    }

    // TODO: needs to be updated and used somewhere
    public double calculateFixedOperatingCost(PowerPlant powerPlant, long clearingTick) {

        double norm = powerPlant.getActualFixedOperatingCost();
        long timeConstructed = powerPlant.getConstructionStartTime() + powerPlant.calculateActualLeadtime();
        double mod = powerPlant.getTechnology().getFixedOperatingCostModifierAfterLifetime();
        long lifetime = powerPlant.calculateActualLifetime();

        GeometricTrend trend = new GeometricTrend();
        trend.setGrowthRate(mod);
        trend.setStart(norm);

        double currentCost = trend.getValue(clearingTick - (timeConstructed + lifetime));
        return currentCost;
    }

    public double calculateAveragePastOperatingProfit(PowerPlant pp, long horizon) {

        double averagePastOperatingProfit = 0d;
        for (long i = -horizon; i <= 0; i++) { 
            averagePastOperatingProfit += calculatePastOperatingProfitInclFixedOMCost(pp, getCurrentTick() + i) / horizon;
        }
        logger.finer(pp + " has had an average operating profit of " + averagePastOperatingProfit);
        return averagePastOperatingProfit;
    }

    public double calculatePastOperatingProfitInclFixedOMCost(PowerPlant plant, long clearingTick) {
        FinancialPowerPlantReport rep = getReps().findFinancialPowerPlantReportsForPlantForTime(plant, clearingTick);
        if (rep != null) { 
            logger.finer(plant + " report: tick " + clearingTick + " revenue: " + rep.getOverallRevenue() + " var cost: " + rep.getVariableCosts() + " fixed om cost: " + rep.getFixedOMCosts());
            return rep.getOverallRevenue() - rep.getVariableCosts() - rep.getFixedOMCosts();}
        logger.finer("No financial report for " + plant + " for tick " + clearingTick + " so returning 0");
        return Double.MAX_VALUE; //TODO avoid dismantling simply becuase you have no data for the full horizon?
    }
    /**
     * The fuel mix is calculated with a linear optimization model of the
     * possible fuels and the requirements.
     *
     * @param substancePriceMap contains the possible fuels and their market
     * prices
     * @param minimumFuelMixQuality is the minimum fuel quality needed for the
     * power plant to work
     * @param efficiency of the plant determines the need for fuel per MWhe
     * @param co2TaxLevel is part of the cost for CO2
     * @param co2AuctionPrice is part of the cost for CO2
     * @return the fuel mix
     */
    public Set<SubstanceShareInFuelMix> calculateFuelMix(PowerPlant plant, Map<Substance, Double> substancePriceMap, double co2Price) {

        logger.log(Level.FINER, "Calculating fuel mix for {0}", plant);
        double efficiency = plant.getActualEfficiency();

        Set<SubstanceShareInFuelMix> fuelMix = (plant.getFuelMix() == null) ? new HashSet<SubstanceShareInFuelMix>() : plant.getFuelMix();

        int numberOfFuels = substancePriceMap.size();
        if (numberOfFuels == 0) {
        	if(plant.getTechnology().isIntermittent()) {
                logger.log(Level.FINER, "Technology is intermittent, so empty fuel mix is returned");
        	} else {
        		logger.log(Level.WARNING, "No fuels, so no operation mode is set. Empty fuel mix is returned");
        	}        		
            return new HashSet<SubstanceShareInFuelMix>();
        } else if (numberOfFuels == 1) {
            SubstanceShareInFuelMix ssifm = null;
            if (!fuelMix.isEmpty()) {
                ssifm = fuelMix.iterator().next();
            } else {
                ssifm = new SubstanceShareInFuelMix();
                fuelMix.add(ssifm);
            }

            Substance substance = substancePriceMap.keySet().iterator().next();

            ssifm.setShare(calculateFuelConsumptionWhenOnlyOneFuelIsUsed(substance, efficiency));
            ssifm.setSubstance(substance);
//            logger.info("Setting fuel consumption for {} to {}", ssifm.getSubstance().getName(), ssifm.getShare());

            return fuelMix;
        } else {

            double minimumFuelMixQuality = plant.getTechnology().getMinimumFuelQuality();

            double[] fuelAndCO2Costs = new double[numberOfFuels];
            double[] fuelDensities = new double[numberOfFuels];
            double[] fuelQuality = new double[numberOfFuels];

            int i = 0;
            for (Substance substance : substancePriceMap.keySet()) {
                fuelAndCO2Costs[i] = substancePriceMap.get(substance) + substance.getCo2Density() * (co2Price);
                fuelDensities[i] = substance.getEnergyDensity();
                fuelQuality[i] = (substance.getQuality() - minimumFuelMixQuality) * fuelDensities[i];
                i++;
            }

//            logger.info("Fuel prices: {}", fuelAndCO2Costs);
//            logger.info("Fuel densities: {}", fuelDensities);
//            logger.info("Fuel purities: {}", fuelQuality);
            // Objective function = minimize fuel cost (fuel
            // consumption*fuelprices
            // + CO2 intensity*co2 price/tax)
            LinearObjectiveFunction function = new LinearObjectiveFunction(fuelAndCO2Costs, 0d);

            List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();

            // Constraint 1: total fuel density * fuel consumption should match
            // required energy input
            constraints.add(new LinearConstraint(fuelDensities, Relationship.EQ, (1 / efficiency)));

            // Constraint 2&3: minimum fuel quality (times fuel consumption)
            // required
            // The equation is derived from (example for 2 fuels): q1 * x1 / (x1+x2) + q2 * x2 / (x1+x2) >= qmin
            // so that the fuelquality weighted by the mass percentages is greater than the minimum fuel quality.
            constraints.add(new LinearConstraint(fuelQuality, Relationship.GEQ, 0));

            try {
                SimplexSolver solver = new SimplexSolver();
                RealPointValuePair solution = solver.optimize(function, constraints, GoalType.MINIMIZE, true);

//                logger.info("Succesfully solved a linear optimization for fuel mix");
                int f = 0;
                Iterator<SubstanceShareInFuelMix> iterator = plant.getFuelMix().iterator();
                for (Substance substance : substancePriceMap.keySet()) {
                    double share = solution.getPoint()[f];

                    SubstanceShareInFuelMix ssifm;
                    if (iterator.hasNext()) {
                        ssifm = iterator.next();
                    } else {
                        ssifm = new SubstanceShareInFuelMix();
                        fuelMix.add(ssifm);
                    }

                    double fuelConsumptionPerMWhElectricityProduced = convertFuelShareToMassVolume(share);
//                    logger.info("Setting fuel consumption for {} to {}", substance.getName(), fuelConsumptionPerMWhElectricityProduced);
                    ssifm.setShare(fuelConsumptionPerMWhElectricityProduced);
                    ssifm.setSubstance(substance);
                    f++;
                }

                return fuelMix;
            } catch (OptimizationException e) {
//                logger.log(Level.WARNING, "Failed to determine the correct fuel mix. Adding only fuel number 1 in fuel mix out of {0} substances and minimum quality of {1}",substancePriceMap.size(), minimumFuelMixQuality);
//                logger.info("The fuel added is: {}", substancePriceMap.keySet().iterator().next().getName());

                // Override the old one
                fuelMix = new HashSet<SubstanceShareInFuelMix>();
                SubstanceShareInFuelMix ssifm = new SubstanceShareInFuelMix();
                Substance substance = substancePriceMap.keySet().iterator().next();

                ssifm.setShare(calculateFuelConsumptionWhenOnlyOneFuelIsUsed(substance, efficiency));
                ssifm.setSubstance(substance);
//                logger.info("Setting fuel consumption for {} to {}", ssifm.getSubstance().getName(), ssifm.getShare());
                fuelMix.add(ssifm);
                return fuelMix;
            }
        }
    }

    public double convertFuelShareToMassVolume(double share) {
        return share * 3600;
    }

    public double calculateFuelConsumptionWhenOnlyOneFuelIsUsed(Substance substance, double efficiency) {

        double fuelConsumptionPerMWhElectricityProduced = convertFuelShareToMassVolume(1 / (efficiency * substance.getEnergyDensity()));

        return fuelConsumptionPerMWhElectricityProduced;

    }

    /**
     * Calculates the actual investment cost of a power plant per year, by using
     * the exogenous modifier.
     *
     * @param powerPlant
     * @return the actual efficiency
     */
    /*
     * public double determineAnnuitizedInvestmentCost(PowerPlant powerPlant, long time) {
     * 
     * double invNorm = powerPlant.getTechnology().getAnnuitizedInvestmentCost(); double modifierExo = calculateExogenousModifier(powerPlant.getTechnology(). getInvestmentCostModifierExogenous(),
     * time);
     * 
     * double annuitizedInvestmentCost = invNorm * modifierExo; logger.info("Investment cost of plant{} is {}", powerPlant, annuitizedInvestmentCost); return annuitizedInvestmentCost; }
     */
    public double determineLoanAnnuities(double totalLoan, double payBackTime, double interestRate) {

        double q = 1 + interestRate;
        double annuity = totalLoan * (Math.pow(q, payBackTime) * (q - 1)) / (Math.pow(q, payBackTime) - 1);

        return annuity;
    }

    /**
     * Calculates expected CO2 price based on a geometric trend estimation, of
     * the past years
     *
     * @param futureTimePoint
     * @param yearsLookingBackForRegression
     * @return
     */
    protected HashMap<ElectricitySpotMarket, Double> determineExpectedCO2PriceInclTax(long futureTimePoint,
            long yearsLookingBackForRegression, long clearingTick) {
        return determineExpectedCO2PriceInclTax(futureTimePoint, yearsLookingBackForRegression, 0, clearingTick);
    }

    /**
     * Calculates expected CO2 price based on a geometric trend estimation, of
     * the past years. The adjustmentForDetermineFuelMix needs to be set to 1,
     * if this is used in the determine fuel mix role.
     *
     * @param futureTimePoint Year the prediction is made for
     * @param yearsLookingBackForRegression How many years are used as input for
     * the regression, incl. the current tick.
     * @return
     */
    protected HashMap<ElectricitySpotMarket, Double> determineExpectedCO2PriceInclTax(long futureTimePoint,
            long yearsLookingBackForRegression, int adjustmentForDetermineFuelMix, long clearingTick) {
        HashMap<ElectricitySpotMarket, Double> co2Prices = new HashMap<ElectricitySpotMarket, Double>();
        CO2Auction co2Auction = getReps().co2Auction;
        //Find Clearing Points for the last 5 years (counting current year as one of the last 5 years).
        Iterable<ClearingPoint> cps = getReps().findAllClearingPointsForMarketAndTimeRange(
                co2Auction, clearingTick - yearsLookingBackForRegression + 1 - adjustmentForDetermineFuelMix,
                clearingTick - adjustmentForDetermineFuelMix, false);
        // Create regression object and calculate average
        SimpleRegression sr = new SimpleRegression();
        Government government = getReps().government;
        double lastPrice = 0;
        double averagePrice = 0;
        int i = 0;
        for (ClearingPoint clearingPoint : cps) {
            sr.addData(clearingPoint.getTime(), clearingPoint.getPrice());
            lastPrice = clearingPoint.getPrice();
            averagePrice += lastPrice;
            i++;
        }
        averagePrice = averagePrice / i;
        double expectedCO2Price;
        if (i > 1) {
            expectedCO2Price = sr.predict(futureTimePoint);
            expectedCO2Price = Math.max(0, expectedCO2Price);
            expectedCO2Price = Math.min(expectedCO2Price, government.getCo2Penalty(futureTimePoint));
        } else {
            expectedCO2Price = lastPrice;
        }
        // Calculate average of regression and past average:
        expectedCO2Price = (expectedCO2Price + averagePrice) / 2;
        for (ElectricitySpotMarket esm : getReps().electricitySpotMarkets) {
            double nationalCo2MinPriceinFutureTick = getReps().findNationalGovernmentByElectricitySpotMarket(esm)
                    .getMinNationalCo2PriceTrend().getValue(futureTimePoint);
            double co2PriceInCountry = 0d;
            if (expectedCO2Price > nationalCo2MinPriceinFutureTick) {
                co2PriceInCountry = expectedCO2Price;
            } else {
                co2PriceInCountry = nationalCo2MinPriceinFutureTick;
            }
            co2PriceInCountry += getReps().government.getCO2Tax(futureTimePoint);
            co2Prices.put(esm, Double.valueOf(co2PriceInCountry));
        }
        return co2Prices;
    }
    
    
    
    // From Standard role
    
    /**
     * Calculates expected CO2 price based on a geometric trend estimation, of
     * the past years. The adjustmentForDetermineFuelMix needs to be set to 1,
     * if this is used in the determine fuel mix role.
     *
     * @param futureTimePoint Year the prediction is made for
     * @param yearsLookingBackForRegression How many years are used as input for
     * the regression, incl. the current tick.
     * @return
     */
    public HashMap<ElectricitySpotMarket, Double> determineExpectedCO2PriceInclTaxAndFundamentalForecast(
            long futureTimePoint, long yearsLookingBackForRegression, int adjustmentForDetermineFuelMix,
            long clearingTick) {
        HashMap<ElectricitySpotMarket, Double> co2Prices = new HashMap<ElectricitySpotMarket, Double>();
        CO2Auction co2Auction = getReps().co2Auction;
        Iterable<ClearingPoint> cps = getReps().findAllClearingPointsForMarketAndTimeRange(
                co2Auction, clearingTick - yearsLookingBackForRegression + 1 - adjustmentForDetermineFuelMix,
                clearingTick - adjustmentForDetermineFuelMix, false);
        // Create regression object and calculate average
        SimpleRegression sr = new SimpleRegression();
        Government government = getReps().government;
        double lastPrice = 0;
        double averagePrice = 0;
        int i = 0;
        for (ClearingPoint clearingPoint : cps) {
            sr.addData(clearingPoint.getTime(), clearingPoint.getPrice());
            lastPrice = clearingPoint.getPrice();
            averagePrice += lastPrice;
            i++;
        }
        averagePrice = averagePrice / i;
        double expectedCO2Price;
        double expectedRegressionCO2Price;
        if (i > 1) {
            expectedRegressionCO2Price = sr.predict(futureTimePoint);
            expectedRegressionCO2Price = Math.max(0, expectedRegressionCO2Price);
            expectedRegressionCO2Price = Math
                    .min(expectedRegressionCO2Price, government.getCo2Penalty(futureTimePoint));
        } else {
            expectedRegressionCO2Price = lastPrice;
        }
        ClearingPoint expectedCO2ClearingPoint = getReps().findClearingPointForMarketAndTime(co2Auction,
                getCurrentTick()
                + getReps().emlabModel.getCentralForecastingYear(),
                true);
        expectedCO2Price = (expectedCO2ClearingPoint == null) ? 0 : expectedCO2ClearingPoint.getPrice();
        expectedCO2Price = (expectedCO2Price + expectedRegressionCO2Price) / 2;
        for (ElectricitySpotMarket esm : getReps().electricitySpotMarkets) {
            double nationalCo2MinPriceinFutureTick = getReps().findNationalGovernmentByElectricitySpotMarket(esm).getMinNationalCo2PriceTrend()
                    .getValue(futureTimePoint);
            double co2PriceInCountry = 0d;
            if (expectedCO2Price > nationalCo2MinPriceinFutureTick) {
                co2PriceInCountry = expectedCO2Price;
            } else {
                co2PriceInCountry = nationalCo2MinPriceinFutureTick;
            }
            co2PriceInCountry += getReps().government.getCO2Tax(futureTimePoint);
            co2Prices.put(esm, Double.valueOf(co2PriceInCountry));
        }
        return co2Prices;
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
            logger.finer("Predicting price for " + substance);
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


    /**
     * Predicts fuel prices for {@link futureTimePoint} using a geometric trend
     * regression forecast. Only predicts fuels that are traded on a commodity
     * market.
     *
     * @param agent
     * @param futureTimePoint
     * @return Map<Substance, Double> of predicted prices.
     */
    public Map<Substance, Double> predictFuelPrices(long numberOfYearsBacklookingForForecasting, long futureTimePoint,
            long clearingTick) {
        // Fuel Prices
        Map<Substance, Double> expectedFuelPrices = new HashMap<Substance, Double>();
        for (Substance substance : getReps().substancesOnCommodityMarkets) {
            // Find Clearing Points for the last 5 years (counting current year
            // as one of the last 5 years).
            Iterable<ClearingPoint> cps = getReps().findAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(substance, getCurrentTick()
                    - (numberOfYearsBacklookingForForecasting - 1), getCurrentTick(), false);
            // logger.warn("{}, {}",
            // getCurrentTick()-(agent.getNumberOfYearsBacklookingForForecasting()-1),
            // getCurrentTick());
            // Create regression object
            GeometricTrendRegression gtr = new GeometricTrendRegression();
            for (ClearingPoint clearingPoint : cps) {
                // logger.warn("CP {}: {} , in" + clearingPoint.getTime(),
                // substance.getName(), clearingPoint.getPrice());
                gtr.addData(clearingPoint.getTime(), clearingPoint.getPrice());
            }
            double forecast = gtr.predict(futureTimePoint);
            if (Double.isNaN(forecast)) {
                expectedFuelPrices.put(
                        substance,
                        getReps().findClearingPointForMarketAndTime(
                                getReps().findMarketBySubstance(substance), getCurrentTick(), false)
                                .getPrice());
            } else {
                expectedFuelPrices.put(substance, forecast);
            }

            // logger.warn("Forecast {}: {}, in Step " + futureTimePoint,
            // substance, expectedFuelPrices.get(substance));
        }
        return expectedFuelPrices;
    }

}
