/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.reporters;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.writer.CSVEntryConverter;
import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;
import emlab.gen.domain.market.electricity.FinancialPowerPlantReport;
import emlab.gen.engine.AbstractReporter;
import emlab.gen.engine.Schedule;
import emlab.gen.repository.Reps;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 *
 * @author ejlchappin
 */
public class DefaultReporter extends AbstractReporter {

    //TODO implement locks here
    //https://examples.javacodegeeks.com/core-java/util/concurrent/locks-concurrent/reentrantlock/java-reentrantlock-example/
    public DefaultReporter() {
        super();
    }

    public void initiate() {

    }

    @Override
    public void report(Schedule schedule) {

        //Default file of reporters over time
        logger.warning("Writing log for tick " + schedule.getCurrentTick());

        // Main csv
        String outputFileName = schedule.runID + "-" + "main.csv";
        File outputfile = new File(outputFileName);

        //Write header if needed
        if (!outputfile.exists()) {
            logger.warning("File does not exist yet, writing header in csv, for tick: " + schedule.getCurrentTick() + ", and iteration " + schedule.getIteration());

            try {
                schedule.reporter.lockMainCSV.lock();
                FileWriter fileWriter = new FileWriter(outputfile, false);
                CSVWriter<Schedule> csvWriter = new CSVWriterBuilder<Schedule>(fileWriter)
                        .entryConverter(new DefaultCSVConverterHeaders())
                        .strategy(CSVStrategy.DEFAULT)
                        .build();

                csvWriter.write(schedule); //write headers in first tick
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //release writing lock for file
                schedule.reporter.lockMainCSV.unlock();
            }
        }

        //Write current tick data to main csv 
        try {
            // get writing lock
            schedule.reporter.lockMainCSV.lock();
            FileWriter fileWriter = new FileWriter(outputfile, true);
            CSVWriter<Schedule> csvWriter = new CSVWriterBuilder<Schedule>(fileWriter)
                    .entryConverter(new DefaultCSVConverter())
                    .strategy(CSVStrategy.DEFAULT)
                    .build();

            csvWriter.write(schedule);

            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //release writing lock for file
            schedule.reporter.lockMainCSV.unlock();
        }

        //Power plant file               
        String powerplantFileName = schedule.runID + "-" + "Powerplants.csv";
        File powerplantfile = new File(powerplantFileName);

        //Write header if needed
        if (!powerplantfile.exists()) {
            try {
                schedule.reporter.lockPowerPlantCSV.lock();
                FileWriter powerplantfileWriter = new FileWriter(powerplantfile, false);
                CSVWriter<FinancialPowerPlantReport> powerplantCSVWriter = new CSVWriterBuilder<FinancialPowerPlantReport>(powerplantfileWriter)
                        .entryConverter(new FinancialPowerPlantReportCSVConverterHeaders())
                        .strategy(CSVStrategy.DEFAULT)
                        .build();
                powerplantCSVWriter.write(null);
                powerplantfileWriter.flush();
                powerplantfileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //release writing lock for file
                schedule.reporter.lockPowerPlantCSV.unlock();
            }
        }

        try {
            schedule.reporter.lockPowerPlantCSV.lock();
            //TODO change the Command Line parameter to the CSV converer... not the defaultreporter
            FileWriter powerplantfileWriter = new FileWriter(powerplantfile, true);
            CSVWriter<FinancialPowerPlantReport> powerplantCSVWriter = new CSVWriterBuilder<FinancialPowerPlantReport>(powerplantfileWriter)
                    .entryConverter(new FinancialPowerPlantReportCSVConverter())
                    .strategy(CSVStrategy.DEFAULT)
                    .build();

            //write report per power plant
            powerplantCSVWriter.writeAll(schedule.reps.findAllFinancialPowerPlantReportsForTime(schedule.getCurrentTick()));
            powerplantfileWriter.flush();
            powerplantfileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //release writing lock for file
            schedule.reporter.lockPowerPlantCSV.unlock();
        }

    }

}
