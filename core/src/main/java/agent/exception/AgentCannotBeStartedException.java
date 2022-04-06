package agent.exception;

import agent.SimpleAgent;

public class AgentCannotBeStartedException extends AgentException {
    public AgentCannotBeStartedException(SimpleAgent simpleAgent) {
        super("Agent cannot be started. Agent state = " + simpleAgent.getState());
    }
}
