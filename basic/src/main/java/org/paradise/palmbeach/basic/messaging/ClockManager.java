package org.paradise.palmbeach.basic.messaging;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.assertj.core.util.Lists;
import org.paradise.palmbeach.core.agent.SimpleAgent;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class ClockManager {

    // Variables.

    private final Map<SimpleAgent.AgentIdentifier, Long> agentClock;
    private final Map<SimpleAgent.AgentIdentifier, TreeSet<Long>> agentClockReceived;

    // Constructors.

    public ClockManager() {
        this.agentClock = Maps.newHashMap();
        this.agentClockReceived = Maps.newHashMap();
    }

    // Methods.

    public long incrementAndGetClock(SimpleAgent.AgentIdentifier agent) {
        return agentClock.merge(agent, 1L, Long::sum);
    }

    public long getAgentClock(SimpleAgent.AgentIdentifier agent) {
        return agentClock.computeIfAbsent(agent, k -> 0L);
    }

    public boolean notReceivedClock(SimpleAgent.AgentIdentifier agent, long clock) {
        return clock > getAgentClock(agent) && !isInAlreadyReceivedClock(agent, clock);

    }

    private boolean isInAlreadyReceivedClock(SimpleAgent.AgentIdentifier agent, long clock) {
        return agentClockReceived.computeIfAbsent(agent, k -> Sets.newTreeSet()).contains(clock);
    }

    public void updateAgentClockFromClockReceived(SimpleAgent.AgentIdentifier agent, long clockReceived) {
        TreeSet<Long> allClockReceived = Sets.newTreeSet();
        allClockReceived.add(clockReceived);
        agentClockReceived.merge(agent, allClockReceived, (oldV, v) -> {
            oldV.addAll(v);
            return oldV;
        });
        updateClock(agent);
    }

    private void updateClock(SimpleAgent.AgentIdentifier agent) {
        if (agentClock.containsKey(agent)) {
            List<Long> clockToRemove = Lists.newArrayList();
            long aClock = agentClock.get(agent);
            TreeSet<Long> clockReceived = agentClockReceived.get(agent);
            if (clockReceived != null) {
                // clock Received is sorted is why we can break if the condition
                // is not respect because all others will also not satisfy
                // the condition
                for (long clock : clockReceived) {
                    if (clock == aClock + 1) {
                        aClock += 1;
                        clockToRemove.add(clock);
                    } else {
                        break;
                    }
                }
                agentClock.put(agent, aClock);
                clockToRemove.forEach(clockReceived::remove);
            }
        }
    }
}
