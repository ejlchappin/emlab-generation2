package emlab.gen.engine;

/**
 * Role encapsulates agent's behavior. 
 * Roles are modular pieces of behavior that can be chained and combined to produce more sophisticated behaviors.
 * @author alfredas
 *
 * @param <T>
 */
public interface Role<T extends Agent>  {

    public void act(T agent);

    public Class<T> agentClass();

    public void setSchedule(Schedule schedule);

}
