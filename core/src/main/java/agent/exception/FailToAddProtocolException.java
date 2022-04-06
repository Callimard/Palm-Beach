package agent.exception;

import agent.protocol.Protocol;

public class FailToAddProtocolException extends AgentException {
    public FailToAddProtocolException(Class<? extends Protocol> protocolClass, Throwable throwable) {
        super("Fail to add agent.protocol " + protocolClass, throwable);
    }
}
