/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.reporters;

import com.googlecode.jcsv.writer.CSVEntryConverter;
import emlab.gen.domain.market.electricity.FinancialPowerPlantReport;
import emlab.gen.engine.Schedule;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ejlchappin
 */
public class FinancialPowerPlantReportCSVConverter implements CSVEntryConverter<FinancialPowerPlantReport> {

    FinancialPowerPlantReportCSVConverter() {

    }

    public String[] convertEntry(FinancialPowerPlantReport report) {
        List<String> row = new ArrayList();

        row.add(String.valueOf(report.schedule.iteration));
        row.add(String.valueOf(report.getTime()));
        row.add(report.getPowerPlant().getName());
        row.add(String.valueOf(report.getSpotMarketRevenue()));
        row.add(String.valueOf(report.getLongTermMarketRevenue()));
        row.add(String.valueOf(report.getCapacityMarketRevenue()));
        row.add(String.valueOf(report.getStrategicReserveRevenue()));
        row.add(String.valueOf(report.getCo2HedgingRevenue()));
        row.add(String.valueOf(report.getOverallRevenue()));
        row.add(String.valueOf(report.getCommodityCosts()));
        row.add(String.valueOf(report.getCo2Costs()));
        row.add(String.valueOf(report.getVariableCosts()));
        row.add(String.valueOf(report.getFixedCosts()));
        row.add(String.valueOf(report.getFullLoadHours()));
        row.add(String.valueOf(report.getProduction()));
        row.add(String.valueOf(report.getPowerPlant().getTechnology().getName()));
        row.add(String.valueOf(report.getPowerPlant().getLocation().getName()));
        long age = report.getTime() - report.getPowerPlant().getConstructionStartTime() + report.getPowerPlant().getActualLeadtime();
        row.add(String.valueOf(age));
        row.add(String.valueOf(report.getPowerPlant().getOwner().getName()));
        row.add(String.valueOf(report.getPowerPlant().getActualNominalCapacity()));
        row.add(String.valueOf(report.getPowerPlant().getActualEfficiency()));

        return row.toArray(new String[row.size()]);

    }
}
