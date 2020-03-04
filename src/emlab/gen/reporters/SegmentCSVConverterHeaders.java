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
public class SegmentCSVConverterHeaders extends AbstractCSVConverter {

    SegmentCSVConverterHeaders() {
    }

    @Override
    public String[] convertEntry(Schedule schedule) {
        List<String> row = new ArrayList();

        Reps reps = schedule.reps;
        //write headers
        row.add("iteration");
        row.add("tick");
        for (int i = 1; i <= 8760; i++) {
            row.add(String.valueOf(i));
        }

        return row.toArray(new String[row.size()]);
    }
}
