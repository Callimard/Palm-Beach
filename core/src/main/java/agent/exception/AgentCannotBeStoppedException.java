package agent.exception;

import agent.BasicAgent;

public class AgentCannotBeStoppedException extends AgentException {
    public AgentCannotBeStoppedException(BasicAgent basicAgent) {
        super("Agent cannot be stopped. Agent state equals = " + basicAgent.getState());
    }
}
