/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.reporters;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.market.CO2MarketClearingPoint;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.engine.Schedule;
import emlab.gen.repository.Reps;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author ejlchappin
 */
public class DefaultCSVConverter extends AbstractCSVConverter {

    DefaultCSVConverter() {
    }

    @Override
    public String[] convertEntry(Schedule schedule) {
        List<String> row = new ArrayList();

        Reps reps = schedule.reps;
        //write values of current tick
        row.add(String.valueOf(schedule.getIteration()));
        row.add(String.valueOf(schedule.getCurrentTick()));
        row.add(String.valueOf(schedule.timer.seconds()));
        row.add(String.valueOf(reps.powerPlants.size()));
        row.add(String.valueOf(reps.calculateCapacityOfOperationalPowerPlants(schedule.getCurrentTick())));
        for (ElectricitySpotMarket market : schedule.reps.electricitySpotMarkets) {
            for (PowerGeneratingTechnology tech : schedule.reps.powerGeneratingTechnologies) {
                row.add(String.valueOf(reps.calculateCapacityOfOperationalPowerPlantsByTechnologyInMarket(tech, market, schedule.getCurrentTick())));
            }
        }
        for (ElectricitySpotMarket market : reps.electricitySpotMarkets) {
            row.add(String.valueOf(reps.calculateCapacityOfOperationalPowerPlantsInMarket(market, schedule.getCurrentTick())));
        }
        for (ElectricitySpotMarket market : reps.electricitySpotMarkets) {
            row.add(String.valueOf(reps.calculateCapacityOfPowerPlantsByMarketInPipeline(market, schedule.getCurrentTick())));
        }
        for (EnergyProducer agent : reps.energyProducers) {
            row.add(String.valueOf(agent.getCash()));
        }
        for (Substance substance : reps.substancesOnCommodityMarkets) {
            ClearingPoint point = reps.findAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(substance, schedule.getCurrentTick(), schedule.getCurrentTick(), false).get(0);
            row.add(String.valueOf(point.getPrice()));
            row.add(String.valueOf(point.getVolume()));
        }
        ClearingPoint point = reps.findClearingPointForMarketAndTime(reps.co2Auction, schedule.getCurrentTick(), false);
        row.add(String.valueOf(point.getPrice()));
        row.add(String.valueOf(point.getVolume()));

        row.add(String.valueOf(reps.calculateTotalEmissionsBasedOnPowerPlantDispatchPlans(false, schedule.getCurrentTick())));

        row.add(String.valueOf(reps.findOperationalPowerPlants(schedule.getCurrentTick()).stream().collect(Collectors.summarizingDouble(PowerPlant::getConstructionStartTime)).getAverage()));
        for (ElectricitySpotMarket market : reps.electricitySpotMarkets) {
            double totalVolume = 0d;
            double totalAveragePrice = 0d;
            double totalHours = 0d;
            for (Segment segment : reps.segments) {
                double amount = reps.findAllAcceptedPowerPlantDispatchPlansForMarketSegmentAndTime(market, segment, schedule.getCurrentTick(), false).stream().collect(Collectors.summarizingDouble(PowerPlantDispatchPlan::getAcceptedAmount)).getSum();
                double price = reps.findOneSegmentClearingPointForMarketSegmentAndTime(schedule.getCurrentTick(), segment, market, false).getPrice();
                double hours = segment.getLengthInHours();
                totalAveragePrice += price * hours;
                totalHours += hours;
                row.add(String.valueOf(price));
                row.add(String.valueOf(segment.getLengthInHours() * amount));
                totalVolume += segment.getLengthInHours() * amount;
            }
            row.add(String.valueOf(totalAveragePrice/totalHours));
            row.add(String.valueOf(totalVolume));

        }
        row.add(String.valueOf(reps.cashFlows.stream().filter(p -> p.getType() == CashFlow.ELECTRICITY_SPOT).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum()));
        row.add(String.valueOf(reps.cashFlows.stream().filter(p -> p.getType() == CashFlow.COMMODITY).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum()));
        row.add(String.valueOf(reps.cashFlows.stream().filter(p -> p.getType() == CashFlow.FIXEDOMCOST).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum()));
        row.add(String.valueOf(reps.cashFlows.stream().filter(p -> p.getType() == CashFlow.LOAN).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum()));
        row.add(String.valueOf(reps.cashFlows.stream().filter(p -> p.getType() == CashFlow.CO2AUCTION).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum()));
        row.add(String.valueOf(reps.cashFlows.stream().filter(p -> p.getType() == CashFlow.CO2TAX).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum()));
        row.add(String.valueOf(reps.cashFlows.stream().filter(p -> p.getType() == CashFlow.NATIONALMINCO2).collect(Collectors.summarizingDouble(CashFlow::getMoney)).getSum()));

        return row.toArray(new String[row.size()]);
    }
}
