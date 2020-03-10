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
        row.add("agent");
        
        row.add("expectedSegmentLoad");
        row.add("segmentSupply");
        row.add("totalCapacityAvailable");
        row.add("result");
        
        row.add("expectedElectricityPrice");
        row.add("expectedCO2Price");
        
        // TOOD: make working 
//	    for ( Map.Entry<Substance, Double> entry  : report.getFuelPrices().entrySet()) {
//	        row.add(String.valueOf("price." + entry.getKey()));
//	    }
        
        //row.add("expectedDemand");



        return row.toArray(new String[row.size()]);

    }
}
