package emlab.gen.engine;

import emlab.gen.repository.Reps;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Abstract reporter
 *
 * @author Emile Chappin
 *
 * @param <T>
 */
public abstract class AbstractReporter {

    public Logger logger = Logger.getGlobal();
    public boolean headersWritten = false;
    public final Lock lockMainCSV;
    public final Lock lockPowerPlantCSV;
    public final Lock lockSegmentCSV;
    public final Lock lockMarketInformationCSV;

    private String reporterDirectoryName;
    
    public AbstractReporter() {
        this.lockMainCSV = new ReentrantLock();
        this.lockPowerPlantCSV = new ReentrantLock();
        this.lockSegmentCSV = new ReentrantLock();
        this.lockMarketInformationCSV = new ReentrantLock();
    }

    public void report(Schedule schedule){
    }

	public String getReporterDirectoryName() {
		return reporterDirectoryName;
	}

	public void setReporterDirectoryName(String reporterDirectoryName) {
		this.reporterDirectoryName = reporterDirectoryName;
	}
}
