package emlab.gen.engine;

import emlab.gen.repository.Reps;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.logging.Logger;

/**
 * Abstract role provides utility methods for role implementation
 *
 * @author alfredas
 *
 * @param <T>
 */
public abstract class AbstractRole<T extends Agent> implements Role<T>{

    public Logger logger = Logger.getGlobal();
    public Schedule schedule;
    
    public AbstractRole() {
        
    }
    
    public AbstractRole(Schedule schedule) {
        this.schedule = schedule;
    }

    public Reps getReps() {
        return schedule.reps;
    }

    public long getCurrentTick() {
        return schedule.getCurrentTick();
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
     
    public Class<T> agentClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }
    
    public void act(List<? extends T> agents){
        agents.forEach((t) -> {
//            logger.warning("Doing role " + this.getClass().getSimpleName() + " for " + t);
            act(t);
        });
    }


    

}
