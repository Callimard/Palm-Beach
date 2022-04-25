package messaging;

import agent.SimpleAgent;
import com.google.common.collect.Sets;
import common.Context;
import environment.network.Network;
import event.Event;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Set;

/**
 * This class is a {@link Messenger} which is able to send {@link Message} to agent even if the {@link Network} is not fully connected but garanties
 * that for both agents in the network, it exists a path of connections between both.
 * <p>
 * The algorithm is simple. The sender send the message to all its direct neighbors and when an agent received a message, if it is not the receiver of
 * the message, it sends it to all its direct neighbors. The message will be broadcast in all the network and eventually arrived to the receiver.
 * <p>
 * This algorithm can be crash fault tolerant, but it only depends on the network form.
 */
@Slf4j
public class PulsingMessenger extends MessageProtocol {

    // Variables.

    @NonNull
    private final Set<PulseMessage> alreadyReceived;

    // Constructors.

    public PulsingMessenger(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
        this.alreadyReceived = Sets.newHashSet();
    }

    // Methods.

    @Override
    protected void receive(@NonNull Message<? extends Serializable> message) {
        PulseMessage pulseMsg = (PulseMessage) message;
        if (!alreadyReceived.contains(pulseMsg)) {
            alreadyReceived.add(pulseMsg);
            if (pulseMsg.getReceiver().equals(getAgent().getIdentifier())) {
                deliver(pulseMsg.getContent());
            } else {
                pulse(pulseMsg);
            }
        }
    }

    private void pulse(PulseMessage pulseMsg) {
        Network network = pulseMsg.getNetwork();
        Set<SimpleAgent.AgentIdentifier> directNeighbors = network.directNeighbors(getAgent().getIdentifier());
        directNeighbors.remove(getAgent().getIdentifier());
        if (!directNeighbors.contains(pulseMsg.getReceiver())) {
            for (SimpleAgent.AgentIdentifier neighbor : directNeighbors) {
                network.send(getAgent().getIdentifier(), neighbor, new PulseMessageReception(pulseMsg));
            }
        } else {
            network.send(getAgent().getIdentifier(), pulseMsg.getReceiver(), new PulseMessageReception(pulseMsg));
        }
    }

    @Override
    public void sendMessage(@NonNull Message<? extends Serializable> message, SimpleAgent.@NonNull AgentIdentifier target, @NonNull Network network) {
        Set<SimpleAgent.AgentIdentifier> directNeighbors = network.directNeighbors(getAgent().getIdentifier());
        directNeighbors.remove(getAgent().getIdentifier());
        for (SimpleAgent.AgentIdentifier neighbor : directNeighbors) {
            network.send(getAgent().getIdentifier(), neighbor, new PulseMessageReception(new PulseMessage(getAgent().getIdentifier(),
                                                                                                          target, network, message)));
        }
    }

    @Override
    protected ProtocolManipulator defaultProtocolManipulator() {
        return new DefaultProtocolManipulator(this);
    }

    @Override
    public void processEvent(Event<?> event) {
        receive(((PulseMessageReception) event).getContent());
    }

    @Override
    public boolean canProcessEvent(Event<?> event) {
        return event instanceof PulseMessageReception;
    }

    // Inner classes.

    @EqualsAndHashCode(callSuper = true)
    public static class PulseMessage extends Message<Message<? extends Serializable>> {

        // Variables.

        @NonNull
        @Getter
        private final transient Network network;

        @NonNull
        @Getter
        private final SimpleAgent.AgentIdentifier receiver;

        // Constructors.

        public PulseMessage(@NonNull SimpleAgent.AgentIdentifier sender, @NonNull SimpleAgent.AgentIdentifier receiver, @NonNull Network network,
                            Message<? extends Serializable> content) {
            super(sender, content);
            this.receiver = receiver;
            this.network = network;
        }
    }

    public static class PulseMessageReception extends Event<PulseMessage> {
        public PulseMessageReception(@NonNull PulseMessage msg) {
            super(msg);
        }
    }
}
