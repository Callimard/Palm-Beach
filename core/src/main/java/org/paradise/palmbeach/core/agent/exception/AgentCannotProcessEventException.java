package org.paradise.palmbeach.core.agent.exception;

import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.event.Event;

public class AgentCannotProcessEventException extends AgentException {
    public AgentCannotProcessEventException(SimpleAgent agent, Event<?> event) {
        super("Agent " + agent.getIdentifier() + " cannot process the Event " + event.getClass().getSimpleName());
    }
}
