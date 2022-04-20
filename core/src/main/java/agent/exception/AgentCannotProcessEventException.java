package agent.exception;

import agent.SimpleAgent;
import event.Event;

public class AgentCannotProcessEventException extends AgentException {
    public AgentCannotProcessEventException(SimpleAgent agent, Event<?> event) {
        super("Agent " + agent.getIdentifier() + " cannot process the Event " + event.getClass().getSimpleName());
    }
}
