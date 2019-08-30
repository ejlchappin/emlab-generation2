/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.engine;

import java.awt.Color;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author ejlchappin
 */
public class ScheduleWorker extends SwingWorker<Void, Integer> {
    
    public long runID;
    public long iteration;
    public String scenarioName;
    public String modelRole;
    public AbstractReporter reporter;
    public Schedule schedule;
            
    public ScheduleWorker(long runID, long iteration, String scenarioName, String modelRole, AbstractReporter reporter) {
        this.runID = runID;
        this.iteration = iteration;
        this.scenarioName = scenarioName;
        this.modelRole = modelRole;
        this.reporter = reporter;
    }

    @Override
    protected Void doInBackground() {
        schedule = new Schedule();
        schedule.runID = runID + "-" + scenarioName + "-" + modelRole + "-" + reporter.getClass().getSimpleName();
        schedule.build(iteration, scenarioName, modelRole, reporter);
        schedule.start();
        Logger.getGlobal().warning("Worker for iteration " + iteration + " is now completed.");
        return null;
    }


}
