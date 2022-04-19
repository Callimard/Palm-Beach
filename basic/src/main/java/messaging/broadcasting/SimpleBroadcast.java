package messaging.broadcasting;

import agent.SimpleAgent;
import agent.exception.AgentNotStartedException;
import agent.protocol.Protocol;
import common.Context;
import environment.network.Network;
import event.Event;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import messaging.Message;
import messaging.MessageProtocol;
import messaging.MessageSender;
import messaging.Messenger;

import java.io.Serializable;
import java.util.Set;

@Slf4j
public class SimpleBroadcast extends MessageProtocol implements Broadcaster {

    // Variables.

    @Setter
    private Messenger messenger;

    // Constructors.

    public SimpleBroadcast(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
    }

    // Methods.

    @Override
    protected void receive(@NonNull Message<? extends Serializable> message) {
        deliver(message);
    }

    @Override
    protected void deliver(@NonNull Message<? extends Serializable> message) {
        offerMessage(message);
    }

    /**
     * @param message the message to broadcast
     * @param network the network across the message is broadcast
     *
     * @throws AgentNotStartedException if the Agent is not in the STARTED state
     */
    @Override
    public void broadcastMessage(@NonNull Message<? extends Serializable> message, Network network) {
        if (getAgent().isStarted()) {
            Set<SimpleAgent.AgentIdentifier> agents = network.getEnvironment().evolvingAgents();
            for (SimpleAgent.AgentIdentifier agent : agents) {
                messenger.sendMessage(new Message<>(getAgent().getIdentifier(), message), agent, network);
            }
        } else
            throw new AgentNotStartedException("Cannot broadcast Message, Agent " + getAgent().getIdentifier() + " is not in STARTED state");
    }

    /**
     * @throws UnsupportedOperationException Broadcast protocol does not send message, use {@link #broadcastMessage(Message, Network)}
     */
    @Override
    public void sendMessage(@NonNull Message<? extends Serializable> message, SimpleAgent.@NonNull AgentIdentifier target, @NonNull Network network) {
        throw new UnsupportedOperationException("Broadcast protocol cannot send message");
    }

    @Override
    protected ProtocolManipulator defaultProtocolManipulator() {
        return new DefaultProtocolManipulator(this);
    }

    @Override
    public void processEvent(Event<?> event) {
        MessageDeliveryEvent deliveryEvent = (MessageDeliveryEvent) event;
        Message<? extends Serializable> msg = deliveryEvent.getContent();
        receive((Message<? extends Serializable>) msg.getContent());
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
        return event instanceof MessageDeliveryEvent;
    }
}
