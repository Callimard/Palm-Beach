package org.palmbeach.core.agent.exception;

import org.palmbeach.core.agent.SimpleAgent;

public class AgentCannotBeKilledException extends AgentException {
    public AgentCannotBeKilledException(SimpleAgent simpleAgent) {
        super("Agent cannot be killed. Agent state equals = " + simpleAgent.getState());
    }
}
