package messaging;

import agent.SimpleAgent;
import agent.exception.AgentNotStartedException;
import common.Context;
import environment.network.Network;
import event.Event;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Protocol used to send and receive {@link Message}.
 */
@Slf4j
public class SimpleMessageSender extends MessageProtocol {

    // Constructors.

    public SimpleMessageSender(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
    }

    // Methods.

    @Override
    protected ProtocolManipulator defaultProtocolManipulator() {
        return new DefaultProtocolManipulator(this);
    }

    @Override
    public void processEvent(Event<?> event) {
        receive(((MessageReceptionEvent) event).getContent());
    }

    @Override
    protected void receive(@NonNull Message<? extends Serializable> message) {
        deliver(message);
    }

    @Override
    protected void deliver(@NonNull Message<? extends Serializable> message) {
        offerMessage(message);
        notifyMessageDelivery(message);
    }

    @Override
    public boolean canProcessEvent(Event<?> event) {
        return event instanceof MessageReceptionEvent;
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
        if (getAgent().isStarted())
            network.send(message.getSender(), target, new MessageReceptionEvent(message));
        else
            throw new AgentNotStartedException("Cannot send Message, Agent " + getAgent().getIdentifier() + " is not in STARTED state");
    }
}
