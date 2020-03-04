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
public class DefaultCSVConverterHeaders extends AbstractCSVConverter {

    DefaultCSVConverterHeaders() {
    }

    @Override
    public String[] convertEntry(Schedule schedule) {
        List<String> row = new ArrayList();

        Reps reps = schedule.reps;
        //write headers
        row.add("iteration");
        row.add("tick");
        row.add("duration");
        row.add("nr.of.powerplants");
        row.add("operational.capacity.powerplants");
        for (ElectricitySpotMarket market : schedule.reps.electricitySpotMarkets) {
            for (PowerGeneratingTechnology tech : schedule.reps.powerGeneratingTechnologies) {
                row.add("operational.capacity." + market + "." + tech);
                row.add("production." + market + "." + tech);
            }
        }
        for (ElectricitySpotMarket market : schedule.reps.electricitySpotMarkets) {
            row.add("capacity." + market);
        }
        for (ElectricitySpotMarket market : schedule.reps.electricitySpotMarkets) {
            row.add("pipeline.capacity." + market);
        }
        for (EnergyProducer agent : schedule.reps.energyProducers) {
            row.add("cash." + agent.getName());
        }
        for (Substance substance : schedule.reps.substancesOnCommodityMarkets) {
            row.add("price." + substance);
            row.add("volume." + substance);
        }
        row.add("price.co2");
        row.add("volume.traded.co2.emissions");
        row.add("volume.total.co2.emissions");
        row.add("average.start.constructing.operational.plants");
        for (ElectricitySpotMarket market : schedule.reps.electricitySpotMarkets) {
            for (Segment segment : schedule.reps.segments) {
                row.add("price." + market + "." + segment);
                row.add("volume." + market + "." + segment);
            }
            row.add("average price." + market);
            row.add("volume." + market);
        }
        row.add("cashflow.ELECTRICITYSPOT");
        row.add("cashflow.COMMODITY");
        row.add("cashflow.FIXEDOMCOST");
        row.add("cashflow.LOAN");
        row.add("cashflow.CO2AUCTION");
        row.add("cashflow.CO2TAX");
        row.add("cashflow.NATIONALMINCO2");

        return row.toArray(new String[row.size()]);
    }
}
