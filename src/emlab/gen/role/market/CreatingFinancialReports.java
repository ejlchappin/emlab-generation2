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
package emlab.gen.role.market;

import java.util.HashMap;
import java.util.Map;

import emlab.gen.domain.agent.EMLabModel;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.market.electricity.FinancialPowerPlantReport;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.engine.Schedule;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Creating finanical reports for each power plant. Currently implemented for
 * use with spot markets only. Long-term contracts are ignored since costs can
 * not easily be assigned to individual power plants.
 *
 * @author Joern
 *
 */
public class CreatingFinancialReports extends AbstractClearElectricitySpotMarketRole<EMLabModel> {

    public CreatingFinancialReports(Schedule schedule) {
        super(schedule);
    }

    public void act(EMLabModel model) {

        Map<Substance, Double> fuelPriceMap = new HashMap<Substance, Double>();
        for (Substance substance : getReps().substances) {
            fuelPriceMap.put(substance, findLastKnownPriceForSubstance(substance));
        }
        logger.info(fuelPriceMap.toString());

        createFinancialReportsForPowerPlantsAndTick(
                getReps().findAllPowerPlantsWhichAreNotDismantledBeforeTick(getCurrentTick() - 2),
                getCurrentTick());

    }

    public void createFinancialReportsForNewInvestments(EMLabModel model) {
        createFinancialReportsForPowerPlantsAndTick(
                getReps().findAllPowerPlantsWithConstructionStartTimeInTick(getCurrentTick()),
                getCurrentTick());
    }

    void createFinancialReportsForPowerPlantsAndTick(Iterable<PowerPlant> plants, long tick) {

        for (PowerPlant plant : plants) {

            FinancialPowerPlantReport financialPowerPlantReport = new FinancialPowerPlantReport();
            getReps().financialPowerPlantReports.add(financialPowerPlantReport);
            financialPowerPlantReport.schedule = schedule; //TODO move this to reps?

            financialPowerPlantReport.setTime(tick);
            financialPowerPlantReport.setFullLoadHours(0);
            financialPowerPlantReport.setPowerPlant(plant);
            financialPowerPlantReport.setCommodityCosts(0);

            // Determining variable and CO2 costs in current time step.
            double totalSupply = plant.calculateElectricityOutputAtTime(tick, false);
            financialPowerPlantReport.setProduction(totalSupply);
            //logger.warning("Plant fuel mix: " + plant + " mix " + plant.getFuelMix());
            //TODO fix this:
            if (plant.getFuelMix() == null) {
                plant.setFuelMix(new HashSet<SubstanceShareInFuelMix>());
            }
            for (SubstanceShareInFuelMix share : plant.getFuelMix()) {

                double amount = share.getShare() * totalSupply;
                Substance substance = share.getSubstance();
                double substanceCost = findLastKnownPriceForSubstance(substance) * amount;
                financialPowerPlantReport.setCommodityCosts(financialPowerPlantReport.getCommodityCosts()
                        + substanceCost);

            }
            logger.info(" CO2 costs for " + plant);

            List<CashFlow> cashFlows = getReps().getCashFlowsForPowerPlant(plant, tick);

            financialPowerPlantReport.setCo2Costs(calculateCO2CostsOfPowerPlant(cashFlows));
            financialPowerPlantReport.setVariableCosts(financialPowerPlantReport.getCommodityCosts() + financialPowerPlantReport.getCo2Costs());

            //Determine fixed costs
            financialPowerPlantReport.setFixedCosts(calculateFixedCostsOfPowerPlant(cashFlows));

            //Calculate overall revenue
            financialPowerPlantReport.setSpotMarketRevenue(calculateSpotMarketRevenueOfPowerPlant(cashFlows));

            financialPowerPlantReport.setStrategicReserveRevenue(calculateStrategicReserveRevenueOfPowerPlant(cashFlows));

            financialPowerPlantReport.setCapacityMarketRevenue(calculateCapacityMarketRevenueOfPowerPlant(cashFlows));

            financialPowerPlantReport.setCo2HedgingRevenue(calculateCO2HedgingRevenueOfPowerPlant(cashFlows));

            financialPowerPlantReport.setOverallRevenue(financialPowerPlantReport.getCapacityMarketRevenue() + financialPowerPlantReport.getCo2HedgingRevenue() + financialPowerPlantReport.getSpotMarketRevenue() + financialPowerPlantReport
                    .getStrategicReserveRevenue());

            // Calculate Full load hours
            financialPowerPlantReport.setFullLoadHours(getReps().calculateFullLoadHoursOfPowerPlant(
                    plant, tick));

            int operationalStatus;
            if (plant.isOperational(tick)) {
                operationalStatus = 1;
            } else if (plant.isInPipeline(tick)) {
                operationalStatus = 0;
            } else {
                operationalStatus = 2;
            }

            financialPowerPlantReport.setPowerPlantStatus(operationalStatus);

        }

    }

    public double calculateSpotMarketRevenueOfPowerPlant(List<CashFlow> cashFlows) {
        double toReturn = cashFlows.stream().filter(p -> p.getType() == CashFlow.ELECTRICITY_SPOT).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum();
        Logger.getGlobal().info("Income Spot " + toReturn);
        return toReturn;
    }

    public double calculateLongTermContractRevenueOfPowerPlant(List<CashFlow> cashFlows) {
        double toReturn = cashFlows.stream().filter(p -> p.getType() == CashFlow.ELECTRICITY_LONGTERM).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum();
        Logger.getGlobal().info("Income LT " + toReturn);
        return toReturn;
    }

    public double calculateStrategicReserveRevenueOfPowerPlant(List<CashFlow> cashFlows) {
        double toReturn = cashFlows.stream().filter(p -> p.getType() == CashFlow.STRRESPAYMENT).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum();
        Logger.getGlobal().info("Income strategic reserve " + toReturn);
        return toReturn;
    }

    public double calculateCapacityMarketRevenueOfPowerPlant(List<CashFlow> cashFlows) {
        double toReturn = cashFlows.stream().filter(p -> p.getType() == CashFlow.CAPMARKETPAYMENT).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum();
        Logger.getGlobal().info("Income Capacity market " + toReturn);
        return toReturn;
    }

    public double calculateCO2HedgingRevenueOfPowerPlant(List<CashFlow> cashFlows) {
        double toReturn = cashFlows.stream().filter(p -> p.getType() == CashFlow.CO2HEDGING).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum();
        Logger.getGlobal().info("Income CO2 Hedging" + toReturn);
        return toReturn;
    }

    public double calculateCO2CostsOfPowerPlant(List<CashFlow> list) {
        return list.stream().filter(p -> (p.getType() == CashFlow.CO2TAX) || (p.getType() == CashFlow.CO2AUCTION) || (p.getType() == CashFlow.NATIONALMINCO2)).mapToDouble(p -> p.getMoney()).sum();
    }

    public double calculateFixedCostsOfPowerPlant(List<CashFlow> list) {
        return list.stream().filter(p -> (p.getType() == CashFlow.FIXEDOMCOST) || (p.getType() == CashFlow.LOAN) || (p.getType() == CashFlow.DOWNPAYMENT)).mapToDouble(p -> p.getMoney()).sum();
    }
}
