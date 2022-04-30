package org.paradise.palmbeach.basic.messaging;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.agent.exception.AgentNotStartedException;
import org.paradise.palmbeach.core.environment.network.Network;
import org.paradise.palmbeach.core.event.Event;
import org.paradise.palmbeach.utils.context.Context;

/**
 * Protocol used to send and receive {@link Message}. This protocol can only send message to agent which are directly connected.
 */
@Slf4j
public class SimpleMessenger extends MessageProtocol<SimpleMessenger.SimpleMessage> {

    // Constructors.

    public SimpleMessenger(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
    }

    // Methods.

    @Override
    protected ProtocolManipulator defaultProtocolManipulator() {
        return new DefaultProtocolManipulator(this);
    }

    @Override
    public void processEvent(Event<?> event) {
        receive(((SimpleMessageReception) event).getContent());
    }

    @Override
    protected void receive(@NonNull SimpleMessage message) {
        deliver(message);
    }

    @Override
    public boolean canProcessEvent(Event<?> event) {
        return event instanceof SimpleMessageReception;
    }

    /**
     * @param message the message to send
     * @param target  the target of the message
     * @param network the network across which the message will be sent
     *
     * @throws AgentNotStartedException if the Agent is not in STARTED state
     */
    @Override
    public void sendMessage(@NonNull Message<?> message, @NonNull SimpleAgent.AgentIdentifier target, @NonNull Network network) {
        network.send(getAgent().getIdentifier(), target, new SimpleMessageReception(new SimpleMessage(getAgent().getIdentifier(), message)));
    }

    // Inner classes.

    public static class SimpleMessage extends MessageEncapsuler {
        public SimpleMessage(SimpleAgent.@NonNull AgentIdentifier sender, Message<?> msg) {
            super(sender, msg);
        }
    }

    public static class SimpleMessageReception extends Event<SimpleMessage> {
        public SimpleMessageReception(@NonNull SimpleMessage msg) {
            super(msg);
        }
    }
}
