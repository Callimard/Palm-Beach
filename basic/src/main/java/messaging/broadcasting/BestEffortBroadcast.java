package messaging.broadcasting;

import agent.SimpleAgent;
import agent.protocol.Protocol;
import common.Context;
import environment.network.Network;
import event.Event;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import messaging.*;

import java.io.Serializable;
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
public class BestEffortBroadcast extends MessageProtocol implements Broadcaster, MessageReceiver.MessageReceiverObserver {

    // Variables.

    private Messenger messenger;

    // Constructors.

    public BestEffortBroadcast(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
    }

    // Methods.

    @Override
    protected void receive(@NonNull Message<? extends Serializable> message) {
        BebMessage broadcastMessage = (BebMessage) message;
        deliver(broadcastMessage.getContent());
    }

    @Override
    public void broadcastMessage(@NonNull Message<? extends Serializable> message, @NonNull Set<SimpleAgent.AgentIdentifier> groupMembership,
                                 @NonNull Network network) {
        groupMembership.add(getAgent().getIdentifier());
        for (SimpleAgent.AgentIdentifier agent : groupMembership) {
            messenger.sendMessage(new BebMessage(getAgent().getIdentifier(), message), agent, network);
        }
    }

    /**
     * @throws UnsupportedOperationException Broadcast protocol does not send message, use {@link #broadcastMessage(Message, Set, Network)}
     */
    @Override
    public void sendMessage(@NonNull Message<? extends Serializable> message, @NonNull SimpleAgent.AgentIdentifier target, @NonNull Network network) {
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
    public void messageDelivery(@NonNull MessageReceiver msgReceiver, Message<? extends Serializable> msg) {
        if (msgReceiver.equals(messenger)) {
            receive(msg);
        }
    }

    @Override
    public boolean interestedBy(Message<? extends Serializable> msg) {
        return msg instanceof BebMessage;
    }

    // Setters.

    public void setMessenger(@NonNull Messenger messenger) {
        this.messenger = messenger;
        this.messenger.addObserver(this);
    }

    // Inner classes.

    private static class BebMessage extends Message<Message<? extends Serializable>> {

        public BebMessage(@NonNull SimpleAgent.AgentIdentifier sender, Message<? extends Serializable> content) {
            super(sender, content);
        }
    }
}
