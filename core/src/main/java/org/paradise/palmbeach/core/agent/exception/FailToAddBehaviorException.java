package org.paradise.palmbeach.core.agent.exception;

import org.paradise.palmbeach.core.agent.behavior.Behavior;

public class FailToAddBehaviorException extends AgentException {
    public FailToAddBehaviorException(Class<? extends Behavior> behaviorClass) {
        super("Fail to add agent.behavior " + behaviorClass);
    }

    public FailToAddBehaviorException(Throwable cause) {
        super(cause);
    }
}
