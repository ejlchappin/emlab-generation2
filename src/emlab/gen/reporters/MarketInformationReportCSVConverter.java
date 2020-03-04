/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.reporters;

import com.googlecode.jcsv.writer.CSVEntryConverter;
import emlab.gen.domain.market.electricity.FinancialPowerPlantReport;
import emlab.gen.engine.Schedule;
import emlab.gen.role.investment.MarketInformationReport;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ejlchappin
 */
public class MarketInformationReportCSVConverter implements CSVEntryConverter<MarketInformationReport> {

    MarketInformationReportCSVConverter() {

    }

    public String[] convertEntry(MarketInformationReport report) {
        List<String> row = new ArrayList();
        
        row.add(String.valueOf(report.schedule.iteration));
        row.add(String.valueOf(report.getTime()));
        row.add(String.valueOf(report.getSegment().getSegmentID()));
        row.add(String.valueOf(report.getAgent().getName()));

        row.add(String.valueOf(report.getExpectedSegmentLoad()));
        row.add(String.valueOf(report.getSegmentSupply()));
        row.add(String.valueOf(report.getTotalCapacityAvailable()));
        row.add(String.valueOf(report.getResult()));
        row.add(String.valueOf(report.getExpectedElectricityPrice()));

        return row.toArray(new String[row.size()]);

    }
}
