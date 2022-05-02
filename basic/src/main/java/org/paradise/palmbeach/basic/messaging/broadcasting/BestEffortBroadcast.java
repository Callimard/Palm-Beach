package org.paradise.palmbeach.basic.messaging.broadcasting;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.paradise.palmbeach.basic.messaging.*;
import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.agent.protocol.Protocol;
import org.paradise.palmbeach.core.environment.network.Network;
import org.paradise.palmbeach.core.event.Event;
import org.paradise.palmbeach.utils.context.Context;

import java.util.Set;

/**
 * Simple broadcast protocol. This algorithm try to broadcast to all {@link SimpleAgent} the specified {@link Message}.
 * <p>
 * However, this type of broadcast does not guaranty that all correct agent will receive the {@code Message}. Indeed, if the agent crash during the
 * broadcast, the broadcast will not be done to all agents in the {@link Network}.
 * <p>
 * In addition to this, if the {@code Network} is not fully connected or the {@link Messenger} use by the {@code BestEffortBroadcast} is not able to
 * send {@code Message} to agent which is not directly connected to the source agent, the {@code Message} will only be received by agent directly
 * connected to the agent sender.
 */
@Slf4j
public class BestEffortBroadcast extends MessageProtocol<BestEffortBroadcast.BestEffortBroadcastMessage> implements Broadcaster, MessageReceiver.MessageReceiverObserver {

    // Variables.

    private Messenger messenger;

    // Constructors.

    public BestEffortBroadcast(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
    }

    // Methods.

    @Override
    protected void receive(@NonNull BestEffortBroadcast.BestEffortBroadcastMessage bebMessage) {
        deliver(bebMessage);
    }

    @Override
    public void broadcastMessage(@NonNull Message<?> message, @NonNull Set<SimpleAgent.AgentIdentifier> groupMembership,
                                 @NonNull Network network) {
        groupMembership.add(getAgent().getIdentifier());
        for (SimpleAgent.AgentIdentifier agent : groupMembership) {
            messenger.sendMessage(new BestEffortBroadcastMessage(message), agent, network);
        }
    }

    /**
     * @throws UnsupportedOperationException Broadcast protocol does not send message, use {@link #broadcastMessage(Message, Set, Network)}
     */
    @Override
    public void sendMessage(@NonNull Message<?> message, @NonNull SimpleAgent.AgentIdentifier target, @NonNull Network network) {
        throw new UnsupportedOperationException("Broadcast protocol cannot send message");
    }

    @Override
    protected ProtocolManipulator defaultProtocolManipulator() {
        return new DefaultProtocolManipulator(this);
    }

    @Override
    public void processEvent(Event<?> event) {
        // Nothing
    }

    /**
     * Does not process {@link Event}. This {@link Protocol} use a {@link MessageSender} to send and receive {@link Message}. Therefore, it is the
     * {@code Messenger} which processed {@code Event}.
     *
     * @param event the event
     *
     * @return always false.
     */
    @Override
    public boolean canProcessEvent(Event<?> event) {
        return false;
    }

    @Override
    public void messageDelivery(@NonNull MessageReceiver msgReceiver, Object msg) {
        if (msgReceiver.equals(messenger)) {
            receive((BestEffortBroadcastMessage) msg);
        }
    }

    @Override
    public boolean interestedBy(Object msg) {
        return msg instanceof BestEffortBroadcastMessage;
    }

    // Setters.

    public void setMessenger(@NonNull Messenger messenger) {
        this.messenger = messenger;
        this.messenger.addObserver(this);
    }

    // Inner classes.

    public static class BestEffortBroadcastMessage extends MessageEncapsuler {

        public BestEffortBroadcastMessage(Message<?> msg) {
            super(msg);
        }
    }
}
