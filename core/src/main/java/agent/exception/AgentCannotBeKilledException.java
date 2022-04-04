package agent.exception;

import agent.BasicAgent;

public class AgentCannotBeKilledException extends AgentException {
    public AgentCannotBeKilledException(BasicAgent basicAgent) {
        super("Agent cannot be killed. Agent state equals = " + basicAgent.getState());
    }
}
