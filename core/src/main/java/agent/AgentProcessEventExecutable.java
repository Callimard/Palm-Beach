package agent;

import event.Event;
import lombok.NonNull;
import scheduler.executor.Executable;

/**
 * Simple {@link Executable} which call the method {@link SimpleAgent#processEvent(Event)} for the specified {@link Event}. The particularity is,
 * because the method {@link SimpleAgent#processEvent(Event)} is synchronized on the {@link SimpleAgent} instance, the method {@link
 * Executable#getLockMonitor()} must return the instance of the specified agent to avoir problem. This {@code Executable} is already configured for
 * this and the user has no conscience of that.
 * <p>
 * It avoid creating an Executable like this:
 * <pre>
 *     SimpleAgent agent = ...
 *     Event event = ...
 *     ...
 *     new Executable() {
 *
 *          public void execute() {
 *              agent.processEvent(event);
 *          }
 *
 *          public Object getLockMonitor() {
 *              return agent;
 *          }
 *     }
 * </pre>
 */
public record AgentProcessEventExecutable(@NonNull SimpleAgent agent, @NonNull Event<?> event) implements Executable {

    @Override
    public void execute() throws Exception {
        agent.processEvent(event);
    }

    @Override
    public Object getLockMonitor() {
        return agent;
    }
}
