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
public class MarketInformationReportCSVConverterHeaders implements CSVEntryConverter<MarketInformationReport> {

    MarketInformationReportCSVConverterHeaders() {

    }

    public String[] convertEntry(MarketInformationReport report) {
        List<String> row = new ArrayList();
        
        row.add("iteration");
        row.add("tick");
        row.add("segment");
        row.add("agent");
        row.add("expectedSegmentLoad");
        row.add("segmentSupply");
        row.add("totalCapacityAvailable");
        row.add("result");
        row.add("expectedElectricityPrice");


        return row.toArray(new String[row.size()]);

    }
}
