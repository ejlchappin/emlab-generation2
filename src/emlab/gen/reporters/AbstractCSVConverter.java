/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.reporters;

import com.googlecode.jcsv.writer.CSVEntryConverter;
import emlab.gen.engine.Schedule;
import emlab.gen.repository.Reps;


/**
 *
 * @author ejlchappin
 */
public class AbstractCSVConverter implements CSVEntryConverter<Schedule> {
    
    AbstractCSVConverter() {
    }

    public String[] convertEntry(Schedule schedule) {
       return new String[0];
    }
}
