package org.paradise.palmbeach.core.agent.exception;

import org.paradise.palmbeach.core.agent.SimpleAgent;

public class AgentCannotBeKilledException extends AgentException {
    public AgentCannotBeKilledException(SimpleAgent simpleAgent) {
        super("Agent cannot be killed. Agent state equals = " + simpleAgent.getState());
    }
}
