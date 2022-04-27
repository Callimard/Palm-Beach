package org.palmbeach.core.agent.exception;

import org.palmbeach.core.agent.SimpleAgent;
import org.palmbeach.core.event.Event;

public class AgentCannotProcessEventException extends AgentException {
    public AgentCannotProcessEventException(SimpleAgent agent, Event<?> event) {
        super("Agent " + agent.getIdentifier() + " cannot process the Event " + event.getClass().getSimpleName());
    }
}
