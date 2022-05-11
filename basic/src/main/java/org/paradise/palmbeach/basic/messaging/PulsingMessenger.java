package org.paradise.palmbeach.basic.messaging;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.environment.network.Network;
import org.paradise.palmbeach.core.event.Event;
import org.paradise.palmbeach.utils.context.Context;

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
public class PulsingMessenger extends MessageProtocol<PulsingMessenger.PulseMessage> {

    // Variables.

    private final ClockManager clockManager;


    // Constructors.

    public PulsingMessenger(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
        this.clockManager = new ClockManager();
    }

    // Methods.

    @Override
    protected void receive(@NonNull PulseMessage pulseMsg) {
        SimpleAgent.AgentIdentifier sender = pulseMsg.getSender();

        long msgClock = pulseMsg.getClock();

        if (clockManager.notReceivedClock(sender, msgClock)) {
            clockManager.updateAgentClockFromClockReceived(sender, msgClock);
            if (pulseMsg.getReceiver().equals(getAgent().getIdentifier())) {
                deliver(pulseMsg);
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
                if (!neighbor.equals(pulseMsg.getSender()))
                    network.send(getAgent().getIdentifier(), neighbor, new PulseMessageReception(pulseMsg));
            }
        } else {
            network.send(getAgent().getIdentifier(), pulseMsg.getReceiver(), new PulseMessageReception(pulseMsg));
        }
    }

    @Override
    public void sendMessage(@NonNull Message<?> message, SimpleAgent.@NonNull AgentIdentifier target, @NonNull Network network) {
        Set<SimpleAgent.AgentIdentifier> directNeighbors = network.directNeighbors(getAgent().getIdentifier());
        directNeighbors.remove(getAgent().getIdentifier());
        for (SimpleAgent.AgentIdentifier neighbor : directNeighbors) {
            network.send(getAgent().getIdentifier(),
                         neighbor,
                         new PulseMessageReception(new PulseMessage(clockManager.incrementAndGetClock(getAgent().getIdentifier()),
                                                                    getAgent().getIdentifier(), target, network,
                                                                    message)));
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
    public static class PulseMessage extends MessageEncapsuler {

        // Variables.

        @NonNull
        @Getter
        private final Network network;

        @NonNull
        @Getter
        private final SimpleAgent.AgentIdentifier sender;

        @NonNull
        @Getter
        private final SimpleAgent.AgentIdentifier receiver;

        @Getter
        private final long clock; // Indispensable to avoid problem if equal messages are sent

        // Constructors.

        public PulseMessage(long clock, @NonNull SimpleAgent.AgentIdentifier sender, @NonNull SimpleAgent.AgentIdentifier receiver,
                            @NonNull Network network, Message<?> msg) {
            super(msg);
            this.sender = sender;
            this.receiver = receiver;
            this.network = network;
            this.clock = clock;
        }
    }

    public static class PulseMessageReception extends Event<PulseMessage> {

        public PulseMessageReception(@NonNull PulseMessage msg) {
            super(msg);
        }
    }
}
