package messaging.broadcasting;

import agent.SimpleAgent;
import com.google.common.collect.Sets;
import common.Context;
import environment.network.Network;
import event.Event;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import messaging.Message;
import messaging.MessageProtocol;
import messaging.MessageReceiver;

import java.io.Serializable;
import java.util.Set;

/**
 * Reliable broadcast protocol. This broadcast protocol guarantees that if a correct agent deliver a {@link Message}, therefore all corrects agents
 * finally deliver the {@code Message}.
 */
@Slf4j
public class ReliableBroadcast extends MessageProtocol implements Broadcaster, MessageReceiver.MessageReceiverObserver {

    // Variables.

    private long msgId;

    private final Set<ReliableBroadcastMessage> bebMsgReceived;

    private Broadcaster broadcaster;

    // Constructors.

    public ReliableBroadcast(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
        this.bebMsgReceived = Sets.newHashSet();
    }

    // Methods.

    @Override
    protected void receive(@NonNull Message<? extends Serializable> message) {
        ReliableBroadcastMessage rbMsg = (ReliableBroadcastMessage) message;
        if (!alreadyReceived(rbMsg)) {
            bebMsgReceived.add(rbMsg);
            broadcaster.broadcastMessage(rbMsg, rbMsg.groupMembership, rbMsg.network);
            deliver(rbMsg.getContent());
        }
    }

    private boolean alreadyReceived(ReliableBroadcastMessage bebMsg) {
        return bebMsgReceived.contains(bebMsg);
    }

    /**
     * @param message the message to send
     * @param target  the target of the message
     * @param network the network across which the message will be sent
     *
     * @throws UnsupportedOperationException BestEffortBroadcast cannot send message
     */
    @Override
    public void sendMessage(@NonNull Message<? extends Serializable> message, SimpleAgent.@NonNull AgentIdentifier target, @NonNull Network network) {
        throw new UnsupportedOperationException("BestEffortBroadcast cannot send message");
    }

    @Override
    public void broadcastMessage(@NonNull Message<? extends Serializable> message, @NonNull Set<SimpleAgent.AgentIdentifier> groupMembership,
                                 @NonNull Network network) {
        broadcaster.broadcastMessage(
                new ReliableBroadcastMessage(getAgent().getIdentifier(), msgId++, Sets.newHashSet(groupMembership), network, message),
                groupMembership, network);
    }

    @Override
    public void messageDelivery(@NonNull MessageReceiver msgReceiver, Message<? extends Serializable> msg) {
        if (msgReceiver.equals(broadcaster))
            receive(msg);
    }

    @Override
    public boolean interestedBy(Message<? extends Serializable> msg) {
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
    public static class ReliableBroadcastMessage extends Message<Message<? extends Serializable>> {

        private final long id;

        private final Set<SimpleAgent.AgentIdentifier> groupMembership;

        private final transient Network network;

        public ReliableBroadcastMessage(@NonNull SimpleAgent.AgentIdentifier sender, long id,
                                        @NonNull Set<SimpleAgent.AgentIdentifier> groupMembership, @NonNull Network network,
                                        Message<? extends Serializable> content) {
            super(sender, content);
            this.id = id;
            this.groupMembership = groupMembership;
            this.network = network;
        }
    }
}
