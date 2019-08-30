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
public class FinancialPowerPlantReportCSVConverterHeaders implements CSVEntryConverter<FinancialPowerPlantReport> {

    FinancialPowerPlantReportCSVConverterHeaders() {

    }

    public String[] convertEntry(FinancialPowerPlantReport report) {
        List<String> row = new ArrayList();
        row.add("iteration");
        row.add("tick");
        row.add("name");
        row.add("spotMarketRevenue");
        row.add("longTermMarketRevenue");
        row.add("capacityMarketRevenue");
        row.add("strategicReserveRevenue");
        row.add("co2HedgingRevenue");
        row.add("overallRevenue");
        row.add("commodityCosts");
        row.add("co2Costs");
        row.add("variableCosts");
        row.add("fixedCosts");
        row.add("fullLoadHours");
        row.add("production");
        row.add("technology");
        row.add("location");
        row.add("age");
        row.add("owner");
        row.add("capacity");
        row.add("efficiency");

        return row.toArray(new String[row.size()]);

    }
}
