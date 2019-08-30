package emlab.gen.engine;

import emlab.gen.repository.Reps;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Abstract role provides utility methods for role implementation
 *
 * @author alfredas
 *
 * @param <T>
 */
public abstract class AbstractReporter {

    public Logger logger = Logger.getGlobal();
    public boolean headersWritten = false;
    public final Lock lockMainCSV;
    public final Lock lockPowerPlantCSV;
    
    public AbstractReporter() {
        this.lockMainCSV = new ReentrantLock();
        this.lockPowerPlantCSV = new ReentrantLock();
    }

    public void report(Schedule schedule){
    }
}
