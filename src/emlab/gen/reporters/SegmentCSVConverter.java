/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.reporters;

import cern.colt.Arrays;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.market.CO2MarketClearingPoint;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentClearingPoint;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.engine.Schedule;
import emlab.gen.repository.Reps;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author ejlchappin
 */
public class SegmentCSVConverter extends AbstractCSVConverter {

    SegmentCSVConverter() {
    }

    @Override
    public String[] convertEntry(Schedule schedule) {
        List<String> row = new ArrayList();

        Reps reps = schedule.reps;
        //write values of current tick
        row.add(String.valueOf(schedule.getIteration()));
        row.add(String.valueOf(schedule.getCurrentTick()));
               
        DoubleMatrix1D segmentRow = reps.intermittentMatrix.viewSorted(0).viewColumn(1).viewFlip();
        for(Double d : segmentRow.toArray()){
            int segmentID = d.intValue();
            row.add(String.valueOf(segmentID));
        }
        return row.toArray(new String[row.size()]);
    }
}
