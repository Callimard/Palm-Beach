package org.paradise.palmbeach.core.agent.exception;

import org.paradise.palmbeach.core.agent.SimpleAgent;

public class AgentCannotBeStartedException extends AgentException {
    public AgentCannotBeStartedException(SimpleAgent simpleAgent) {
        super("Agent cannot be started. Agent state = " + simpleAgent.getState());
    }
}
