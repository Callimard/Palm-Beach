package agent.exception;

import agent.SimpleAgent;

public class AgentNotStartedException extends AgentException {
    public AgentNotStartedException(String s) {
        super(s);
    }

    public AgentNotStartedException(SimpleAgent agent) {
        super("The agent " + agent.isStarted() + " is not in the STARTED state -> current agent state = " + agent.getState());
    }
}
