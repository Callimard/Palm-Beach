package org.paradise.palmbeach.core.agent.exception;

import org.paradise.palmbeach.core.agent.SimpleAgent;

public class AgentNotStartedException extends AgentException {
    public AgentNotStartedException(String s) {
        super(s);
    }

    public AgentNotStartedException(SimpleAgent agent) {
        super("The agent " + agent.isStarted() + " is not in the STARTED state -> current agent state = " + agent.getState());
    }
}
