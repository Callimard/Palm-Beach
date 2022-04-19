package messaging.broadcasting;

import agent.SimpleAgent;
import agent.protocol.Protocol;
import common.Context;
import environment.network.Network;
import event.Event;
import lombok.NonNull;
import lombok.Setter;
import messaging.Message;
import messaging.MessageSender;
import messaging.Messenger;
import messaging.MessageProtocol;

import java.io.Serializable;
import java.util.Set;

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
    protected void receive(@NonNull Message<Serializable> message) {
        deliver(message);
    }

    @Override
    protected void deliver(@NonNull Message<Serializable> message) {
        offerMessage(message);
    }

    @Override
    public void broadcastMessage(@NonNull Message<Serializable> message, Network network) {
        if (getAgent().isStarted()) {
            Set<SimpleAgent.AgentIdentifier> agents = network.getEnvironment().evolvingAgents();
            for (SimpleAgent.AgentIdentifier agent : agents) {
                messenger.sendMessage(new Message<>(getAgent().getIdentifier(), message), agent, network);
            }
        }
    }

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
}
