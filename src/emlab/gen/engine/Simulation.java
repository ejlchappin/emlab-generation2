package emlab.gen.engine;



/**
 * Simulation interface. Describes methods to start/stop/pause the simulation
 * 
 * @author alfredas
 * 
 */
public interface Simulation  {

    public void runSimulation();

    public void pauseSimulation();

    public void stopSimulation();

    public void resumeSimulation();

    public EngineState getState();

    public long getCurrentTick();

    public void wake();

    public void listen(SimulationListener listener);

}
