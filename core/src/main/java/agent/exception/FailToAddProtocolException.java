package agent.exception;

import protocol.Protocol;

public class FailToAddProtocolException extends AgentException {
    public FailToAddProtocolException(Class<? extends Protocol> protocolClass, Throwable throwable) {
        super("Fail to add protocol " + protocolClass, throwable);
    }
}
