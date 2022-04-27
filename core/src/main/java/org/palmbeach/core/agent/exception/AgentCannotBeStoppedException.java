package org.palmbeach.core.agent.exception;

import org.palmbeach.core.agent.SimpleAgent;

public class AgentCannotBeStoppedException extends AgentException {
    public AgentCannotBeStoppedException(SimpleAgent simpleAgent) {
        super("Agent cannot be stopped. Agent state equals = " + simpleAgent.getState());
    }
}
