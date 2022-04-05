package agent.exception;

import agent.SimpleAgent;

public class AgentCannotBeKilledException extends AgentException {
    public AgentCannotBeKilledException(SimpleAgent simpleAgent) {
        super("Agent cannot be killed. Agent state equals = " + simpleAgent.getState());
    }
}
