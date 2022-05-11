package org.paradise.palmbeach.basic.messaging;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.environment.network.Network;
import org.paradise.palmbeach.core.event.Event;
import org.paradise.palmbeach.utils.context.Context;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

    private long currentClock;

    private final Map<SimpleAgent.AgentIdentifier, Long> agentClock;

    private final Map<SimpleAgent.AgentIdentifier, TreeSet<Long>> agentClockReceived;

    // Constructors.

    public PulsingMessenger(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
        this.agentClock = Maps.newHashMap();
        this.agentClockReceived = Maps.newHashMap();
    }

    // Methods.

    @Override
    protected void receive(@NonNull PulseMessage pulseMsg) {
        SimpleAgent.AgentIdentifier sender = pulseMsg.getSender();
        agentClock.putIfAbsent(sender, 0L);
        agentClockReceived.putIfAbsent(sender, Sets.newTreeSet());

        long agentCurrentClock = agentClock.get(sender);
        long msgClock = pulseMsg.getClock();
        TreeSet<Long> clockReceived = agentClockReceived.get(sender);

        if (msgClock > agentCurrentClock && !clockReceived.contains(msgClock)) {
            clockReceived.add(msgClock);
            if (pulseMsg.getReceiver().equals(getAgent().getIdentifier())) {
                deliver(pulseMsg);
            } else {
                pulse(pulseMsg);
            }

            updateAgentClock(sender, agentCurrentClock, clockReceived);
        }
    }

    private void updateAgentClock(SimpleAgent.AgentIdentifier sender, final long agentCurrentClock, TreeSet<Long> clockReceived) {
        List<Long> clockToRemove = Lists.newArrayList();
        long aClock = agentCurrentClock;
        for (long clock : clockReceived) {
            if (clock == aClock + 1) {
                aClock += 1;
                clockToRemove.add(clock);
            }
        }

        agentClock.put(sender, aClock);
        clockToRemove.forEach(clockReceived::remove);
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
            network.send(getAgent().getIdentifier(), neighbor, new PulseMessageReception(new PulseMessage(++currentClock,
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
