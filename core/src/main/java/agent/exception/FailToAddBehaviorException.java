package agent.exception;

import behavior.Behavior;

public class FailToAddBehaviorException extends AgentException {
    public FailToAddBehaviorException(Class<? extends Behavior> behaviorClass) {
        super("Fail to add behavior " + behaviorClass);
    }
}
