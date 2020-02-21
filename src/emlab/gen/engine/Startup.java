/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.engine;

import static emlab.gen.engine.Schedule.logger;
import emlab.gen.gui.GUIServer;
import emlab.gen.gui.SimpleGUI2;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author ejlchappin
 */
public class Startup {

    

    public static void main(String[] args) {
        long runID = System.currentTimeMillis();
        
        /* Default parameters */
        long numberOfIterations = 10;
        long numberOfParallelJobs = 4;
        
        String scenarioName = "DefaultScenario";
        //String scenarioName = "Scenario_NL";
        //String scenarioName = "Scenario_NL_intermittent";
        //String scenarioName = "Scenario_NL_DE";
        //String scenarioName = "Scenario_NL_hourly";
        //String scenarioName = "Scenario_NL_DE_toy";

        String modelRole = "EMlabModelRole";
        String reporterClassName = "DefaultReporter";
        
        String reporterDirectoryName = "results/";
        
        boolean haveGUI = false;


        Logger.getGlobal().setLevel(Level.WARNING);
        try {
            FileHandler handler = new FileHandler(reporterDirectoryName + runID + "-log.txt");
            handler.setFormatter(new SimpleFormatter());
            Logger.getGlobal().addHandler(handler);//.java.util.logging.FileHandler.pattern   = 
        } catch (IOException ex) {
            Logger.getLogger(Startup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Startup.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logger.getGlobal().warning("Starting EMLab-Generation 2");
        Logger.getGlobal().warning("Run id is " + runID);

        for (int i = 0; i < args.length; i++) {
            if (args[i].contains("=")) {
                String[] theArg = args[i].split("=");
                switch (theArg[0]) {
                    case "scenario":
                        scenarioName = theArg[1];
                        break;
                    case "iterations":
                        numberOfIterations = Long.parseLong(theArg[1]);
                        break;
                    case "parallel":
                        numberOfParallelJobs = Long.parseLong(theArg[1]);
                        break;
                    case "role":
                        modelRole = theArg[1];
                        break;
                    case "reporter":
                        reporterClassName = theArg[1];
                        break;
                    case "gui":
                        haveGUI = Boolean.parseBoolean(theArg[1]);
                    default:
                        Logger.getGlobal().warning("Argument not known: " + theArg);
                }
            } else {
                Logger.getGlobal().warning("Argument not correct:" + args[i]);
            }
        }

        
        Class<?> reporterClass = null;
        AbstractReporter reporter = null;
        try {
            reporterClass = Class.forName("emlab.gen.reporters." + reporterClassName);
            //assuming one constructor with Schedule schedule as argument.
            reporter = (AbstractReporter) reporterClass.getConstructors()[0].newInstance(null);
            reporter.setReporterDirectoryName(reporterDirectoryName);
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Schedule.class.getName()).log(Level.SEVERE, null, ex);
        }

        logger.warning("Reporter enabled: " + reporter.getClass());

        
        
        
        List<ScheduleWorker> activeWorkers = new ArrayList<ScheduleWorker>();
        List<ScheduleWorker> doneWorkers = new ArrayList<ScheduleWorker>();

        SimpleGUI2 gui = new SimpleGUI2(activeWorkers, doneWorkers);

        if (haveGUI) {
            GUIServer server = new GUIServer(gui);
            server.execute();
        }

        long iteration = 0;

        while (!gui.exit && !gui.stop && (iteration < numberOfIterations || activeWorkers.size() > 0)) {

            gui.body = "<p>Run: " + String.valueOf(runID) + " iteration " + String.valueOf(iteration) + " out of " + String.valueOf(numberOfIterations) + "</p>";

            //Check whether we need workers for new iterations and whether we have space for a new worker
            if (!gui.stop && (activeWorkers.size() < numberOfParallelJobs && iteration < numberOfIterations)) {
                iteration++;
                Logger.getGlobal().warning("Starting worker for iteration " + iteration);
                ScheduleWorker worker = new ScheduleWorker(runID, iteration, scenarioName, modelRole, reporter);
                activeWorkers.add(worker);
                    worker.execute();
            }

            //Remove workers that are done.
            for (ScheduleWorker worker : activeWorkers) {
               
                if (worker.isDone()) {
                    Logger.getGlobal().warning("Worker was found completed.");
                    doneWorkers.add(worker);
                }
            }
            activeWorkers.removeAll(doneWorkers);

            //Wait a little bit
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Startup.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //Out of the loop, all iterations have been performed and all workers are done
        Logger.getGlobal().warning("All is done.");
        if (haveGUI) {
            while (!gui.exit) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Startup.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                gui.getServer().stop();
            } catch (Exception ex) {
                Logger.getLogger(Startup.class.getName()).log(Level.SEVERE, null, ex);
            }
            gui.destroy();
        }
    }
}
