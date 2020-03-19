/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.reporters;

import com.googlecode.jcsv.writer.CSVEntryConverter;

import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.FinancialPowerPlantReport;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.Substance;
import emlab.gen.engine.Schedule;
import emlab.gen.role.investment.MarketInformationReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ejlchappin
 * @author marcmel
 */
public class MarketInformationReportCSVConverterHeaders implements CSVEntryConverter<MarketInformationReport> {

    MarketInformationReportCSVConverterHeaders() {

    }

    public String[] convertEntry(MarketInformationReport report) {
        List<String> row = new ArrayList();
        
        row.add("iteration");
        row.add("tick");
        row.add("segment");
        row.add("market");
        row.add("producer");
        
        row.add("segment.load");
        row.add("segment.supply");
        row.add("capacity.available");
        row.add("case.id");
        
        row.add("price.electricity");
        row.add("price.co2");
        
        // TOOD: make working
        // TODO: add different markets
//	    for ( Map.Entry<Substance, Double> entry  : report.getFuelPrices().entrySet()) {
//	        row.add(String.valueOf("price." + entry.getKey()));
//	    }
        
        //row.add("expectedDemand");



        return row.toArray(new String[row.size()]);

    }
}
