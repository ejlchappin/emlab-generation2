package emlab.gen.engine;

import cern.colt.Timer;
import emlab.gen.repository.Reps;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simulation schedule
 *
 * @author ejlchappin
 *
 */
public class Schedule {

    static Logger logger = Logger.getGlobal();
    public String runID;
    public long iteration;
    private long currentTick = 0;
    private EngineState state = EngineState.STOPPED;
    public Reps reps;
    public Role mainRole;
    public Scenario scenario;
    public Timer timer;
    public AbstractReporter reporter;

    public EngineState getState() {
        return this.state;
    }

    public void build(long iteration, String scenarioName, String modelRole, AbstractReporter reporter) {
        this.iteration = iteration;
        this.reps = new Reps();
        this.reporter = reporter;
        
        Class<?> scenarioClass = null;
        try {
            scenarioClass = Class.forName("emlab.gen.scenarios." + scenarioName);
            this.scenario = (Scenario) scenarioClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Schedule.class.getName()).log(Level.SEVERE, null, ex);
        }

        Class<?> modelRoleClass = null;
        try {
            modelRoleClass = Class.forName("emlab.gen.role." + modelRole);
            //assuming one constructor with Schedule schedule as argument.
            this.mainRole = (Role) modelRoleClass.getConstructors()[0].newInstance(this);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Schedule.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        logger.info("Main model role " + mainRole.getClass());
        this.mainRole.setSchedule(this);

        logger.info("Building scenario " + this.getClass());
        this.scenario.build(this);
        logger.info("Scenario built");

    }

    public void start() {
        logger.info("Starting simulation iteration " + getIteration());

        if (this.state == EngineState.STOPPED || this.state == EngineState.CRASHED) {
            this.state = EngineState.RUNNING;
            while (this.state != EngineState.STOPPING) {
                logger.log(Level.INFO, "***** ITERATION " + getIteration() + " STARTING TICK {0} *****", getCurrentTick());
                timer = new Timer();
                timer.reset();
                timer.start();
                try{
                   mainRole.act(reps.emlabModel);
                } catch (Exception e) {
                    Logger.getGlobal().warning("Error in executing Main Role ");
                    e.printStackTrace();
                }
                timer.stop();
                logger.log(Level.INFO, "Tick took {0} seconds.", timer.seconds());
                reporter.report(this);
                
                if(this.state == EngineState.PAUSING) {
                    this.state = EngineState.PAUSED;
                    while (this.state == EngineState.PAUSED){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Schedule.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                currentTick++;
            }
            this.state = EngineState.STOPPED;
        }

    }

    public void pause() {
        if (this.state == EngineState.RUNNING) {
            this.state = EngineState.PAUSING;
        }
    }

    public void resume() {
        if (this.state == EngineState.PAUSED) {
            this.state = EngineState.RUNNING;
        }
    }

    public void stop() {
        if (this.state == EngineState.RUNNING) {
            this.state = EngineState.STOPPING;
        }
    }

    public void clear() {
        this.currentTick = 0;
        this.reps = null;
        this.mainRole = null;
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public long getIteration() {
        return iteration;
    }

}
