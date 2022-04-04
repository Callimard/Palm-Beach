package agent.exception;

import agent.BasicAgent;

public class AgentCannotBeStartedException extends AgentException {
    public AgentCannotBeStartedException(BasicAgent basicAgent) {
        super("Agent cannot be started. Agent state = " + basicAgent.getState());
    }
}
