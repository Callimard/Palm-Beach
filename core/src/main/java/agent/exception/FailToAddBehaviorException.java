package agent.exception;

import agent.behavior.Behavior;

public class FailToAddBehaviorException extends AgentException {
    public FailToAddBehaviorException(Class<? extends Behavior> behaviorClass) {
        super("Fail to add agent.behavior " + behaviorClass);
    }

    public FailToAddBehaviorException(Throwable cause) {
        super(cause);
    }
}
