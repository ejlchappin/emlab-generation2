package emlab.gen.engine;


/**
 * Provides utility methods for agent implementation. Implements the act method using the visitor pattern.
 * @author alfredas
 *
 */
public abstract class AbstractAgent implements Agent {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void act(Role role) {
        role.act(this);
    }

}
