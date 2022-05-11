package org.paradise.palmbeach.basic.messaging.broadcasting;

import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.paradise.palmbeach.basic.messaging.*;
import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.environment.network.Network;
import org.paradise.palmbeach.core.event.Event;
import org.paradise.palmbeach.utils.context.Context;

import java.util.Set;

/**
 * Reliable broadcast protocol. This broadcast protocol guarantees that if a correct agent deliver a {@link Message}, therefore all corrects agents
 * finally deliver the {@code Message}.
 */
@Slf4j
public class ReliableBroadcast extends MessageProtocol<ReliableBroadcast.ReliableBroadcastMessage> implements Broadcaster, MessageReceiver.MessageReceiverObserver {

    // Variables.

    private final ClockManager clockManager;

    private Broadcaster broadcaster;

    // Constructors.

    public ReliableBroadcast(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
        this.clockManager = new ClockManager();
    }

    // Methods.

    @Override
    protected void receive(@NonNull ReliableBroadcastMessage rbMsg) {
        SimpleAgent.AgentIdentifier sender = rbMsg.getSender();

        long msgClock = rbMsg.getClock();

        if (clockManager.notReceivedClock(sender, msgClock)) {
            clockManager.updateAgentClockFromClockReceived(sender, msgClock);
            broadcaster.broadcastMessage(rbMsg, rbMsg.groupMembership, rbMsg.network);
            deliver(rbMsg);
        }
    }

    /**
     * @param message the message to send
     * @param target  the target of the message
     * @param network the network across which the message will be sent
     *
     * @throws UnsupportedOperationException BestEffortBroadcast cannot send message
     */
    @Override
    public void sendMessage(@NonNull Message<?> message, SimpleAgent.@NonNull AgentIdentifier target, @NonNull Network network) {
        throw new UnsupportedOperationException("BestEffortBroadcast cannot send message");
    }

    @Override
    public void broadcastMessage(@NonNull Message<?> message, @NonNull Set<SimpleAgent.AgentIdentifier> groupMembership,
                                 @NonNull Network network) {
        ReliableBroadcastMessage rbMsg =
                new ReliableBroadcastMessage(clockManager.incrementAndGetClock(getAgent().getIdentifier()), getAgent().getIdentifier(),
                                             Sets.newHashSet(groupMembership),
                                             network, message);
        broadcaster.broadcastMessage(rbMsg, groupMembership, network);
        deliver(rbMsg);
    }

    @Override
    public void messageDelivery(@NonNull MessageReceiver msgReceiver, Object msg) {
        if (msgReceiver.equals(broadcaster))
            receive((ReliableBroadcastMessage) msg);
    }

    @Override
    public boolean interestedBy(Object msg) {
        return msg instanceof ReliableBroadcastMessage;
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
     * @param event the event
     *
     * @return always returns false.
     */
    @Override
    public boolean canProcessEvent(Event<?> event) {
        return false;
    }

    // Setters.

    public void setBroadcaster(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
        this.broadcaster.addObserver(this);
    }


    // Inner Classes.

    @EqualsAndHashCode(callSuper = true)
    public static class ReliableBroadcastMessage extends MessageEncapsuler {

        @Getter
        private final long clock;

        @Getter
        @NonNull
        private final SimpleAgent.AgentIdentifier sender;

        private final Set<SimpleAgent.AgentIdentifier> groupMembership;

        private final Network network;

        public ReliableBroadcastMessage(long clock, @NonNull SimpleAgent.AgentIdentifier sender,
                                        @NonNull Set<SimpleAgent.AgentIdentifier> groupMembership,
                                        @NonNull Network network,
                                        Message<?> msg) {
            super(msg);
            this.clock = clock;
            this.sender = sender;
            this.groupMembership = groupMembership;
            this.network = network;
        }
    }
}
