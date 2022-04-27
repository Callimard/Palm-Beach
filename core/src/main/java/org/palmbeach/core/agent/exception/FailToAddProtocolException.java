package org.palmbeach.core.agent.exception;

import org.palmbeach.core.agent.protocol.Protocol;

public class FailToAddProtocolException extends AgentException {
    public FailToAddProtocolException(Class<? extends Protocol> protocolClass, Throwable throwable) {
        super("Fail to add agent.protocol " + protocolClass, throwable);
    }

    public FailToAddProtocolException(Throwable cause) {
        super(cause);
    }
}
