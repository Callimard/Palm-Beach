package org.paradise.palmbeach.basic.messaging;

import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.agent.exception.AgentNotStartedException;
import org.paradise.palmbeach.utils.context.Context;
import org.paradise.palmbeach.core.environment.network.Network;
import org.paradise.palmbeach.core.event.Event;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Protocol used to send and receive {@link Message}. This protocol can only send message to agent which are directly connected.
 */
@Slf4j
public class SimpleMessenger extends MessageProtocol {

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
    protected void receive(@NonNull Message<? extends Serializable> message) {
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
    public void sendMessage(@NonNull Message<? extends Serializable> message, @NonNull SimpleAgent.AgentIdentifier target, @NonNull Network network) {
        network.send(getAgent().getIdentifier(), target, new SimpleMessageReception(message));
    }

    public static class SimpleMessageReception extends Event<Message<? extends Serializable>> {
        public SimpleMessageReception(@NonNull Message<? extends Serializable> msg) {
            super(msg);
        }
    }
}
