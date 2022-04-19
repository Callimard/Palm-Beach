package messaging;

import agent.SimpleAgent;
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
public class SimpleMessageSender extends MessageProtocol implements Messenger {

    // Constructors.

    public SimpleMessageSender(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
    }

    // Methods.

    @Override
    public void agentStarted() {
        // Nothing
    }

    @Override
    public void agentStopped() {
        // Nothing
    }

    @Override
    public void agentKilled() {
        // Nothing
    }

    @Override
    protected ProtocolManipulator defaultProtocolManipulator() {
        return new DefaultProtocolManipulator(this);
    }

    @Override
    public void processEvent(Event<?> event) {
        receive(((MessageReceptionEvent) event).getContent());
    }

    @Override
    protected void receive(@NonNull Message<Serializable> message) {
        deliver(message);
    }

    @Override
    protected void deliver(@NonNull Message<Serializable> message) {
        offerMessage(message);
    }

    @Override
    public boolean canProcessEvent(Event<?> event) {
        return event instanceof MessageReceptionEvent;
    }

    @Override
    public void sendMessage(@NonNull Message<Serializable> message, @NonNull SimpleAgent.AgentIdentifier target, @NonNull Network network) {
        if (getAgent().isStarted())
            network.send(message.getSender(), target, new MessageReceptionEvent(message));
        else
            log.info("Cannot send Message {}, agent {} is not started", message, getAgent());
    }
}
