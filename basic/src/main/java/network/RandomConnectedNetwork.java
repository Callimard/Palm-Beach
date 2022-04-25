package network;

import agent.SimpleAgent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import common.Context;
import common.validation.Validate;
import environment.Environment;
import environment.network.Network;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Network which is randomly created but ensure one property:
 * <p>
 * Let two agents {@code a0} and {@code a1} in the {@link Network}, always exists a set of connections such as {@code a0} can reach {@code a1}.
 * <p>
 * When an agent is added in the {@link Environment}, the {@code Network} also add the agent to always verify the previous cited property. However,
 * the property is not guaranty anymore if agent leave the {@code Network}.
 * <p>
 * It is possible to configure the number of connection created for a new added agent. More connections are created with the new agent, more the
 * network is connected and resistant to faulty agent. The number of connection must be specified in the {@code Network} {@link Context}. The context
 * key is {@link #CONNECTION_NUMBER} and the default value is {@link #DEFAULT_CONNECTION_NUMBER}.
 *
 * <p>
 * This {@code Network} extends {@link NetworkWithDelay}.
 */
@Slf4j
public class RandomConnectedNetwork extends NetworkWithDelay {

    // Constants.

    public static final int DEFAULT_CONNECTION_NUMBER = 3;

    // Context key.

    public static final String CONNECTION_NUMBER = "agentConnectionNumber";

    // Variables.

    private final ConnectionGroup mainConnectionGroup;

    // Constructors.

    /**
     * Constructs a {@link RandomConnectedNetwork}. At the constructions, browse all already added agent in the specified {@link Environment} to add
     * them in the {@link Network}.
     *
     * @param name        the name of the Network
     * @param environment the Network Environment
     * @param context     the Network context
     *
     * @throws IllegalArgumentException if the value of {@link #connectionNumber()} define in the context is less than 1
     */
    public RandomConnectedNetwork(@NonNull String name, @NonNull Environment environment, Context context) {
        super(name, environment, context);

        // Verifications.
        connectionNumber();

        this.mainConnectionGroup = new ConnectionGroup(Sets.newHashSet());
        for (SimpleAgent.AgentIdentifier agent : environment.evolvingAgents()) {
            this.mainConnectionGroup.addAgent(agent, connectionNumber(), getRandom());
        }
    }

    // Methods.

    @Override
    public void environmentAddAgent(@NonNull SimpleAgent.AgentIdentifier addedAgent) {
        mainConnectionGroup.addAgent(addedAgent, connectionNumber(), getRandom());
    }

    @Override
    public void environmentRemoveAgent(@NonNull SimpleAgent.AgentIdentifier removedAgent) {
        mainConnectionGroup.removeAgent(removedAgent);
    }

    @Override
    public boolean hasConnection(@NonNull SimpleAgent.AgentIdentifier source, @NonNull SimpleAgent.AgentIdentifier target) {
        return mainConnectionGroup.hasConnection(source, target);
    }

    @Override
    public Set<SimpleAgent.AgentIdentifier> directNeighbors(@NonNull SimpleAgent.AgentIdentifier agent) {
        if (mainConnectionGroup.contains(agent)) {
            Set<SimpleAgent.AgentIdentifier> connectedAgent = Sets.newHashSet(mainConnectionGroup.connections.get(agent));
            connectedAgent.add(agent);
            return connectedAgent;
        } else
            throw new NotInNetworkException("Agent " + agent + " is not in the Network " + this);
    }

    @Override
    public Set<Connection> allConnections() {
        Set<SimpleAgent.AgentIdentifier> alreadySeen = Sets.newHashSet();
        Set<Connection> allConnections = Sets.newHashSet();

        for (SimpleAgent.AgentIdentifier a0 : mainConnectionGroup.agents) {
            Set<SimpleAgent.AgentIdentifier> connectedAgents = mainConnectionGroup.connections.get(a0);
            for (SimpleAgent.AgentIdentifier a1 : connectedAgents) {
                if (!alreadySeen.contains(a1)) {
                    allConnections.add(new NonOrientedConnection(a0, a1));
                }
            }
            allConnections.add(new NonOrientedConnection(a0, a0));
            alreadySeen.add(a0);
        }

        return allConnections;
    }

    // Getters and setters.

    public int connectionNumber() {
        if (getContext().getValue(CONNECTION_NUMBER) != null) {
            int connectionNumber = (int) getContext().getValue(CONNECTION_NUMBER);
            Validate.min(connectionNumber, 1, "Connection number cannot be less than 1");
            return connectionNumber;
        } else
            return DEFAULT_CONNECTION_NUMBER;
    }

    public void connectionNumber(int connectionNumber) {
        Validate.min(connectionNumber, 1, "Connection number cannot be less than 1");
        getContext().map(CONNECTION_NUMBER, connectionNumber);
    }

    // Inner classes.

    @RequiredArgsConstructor
    private static class ConnectionGroup {

        @NonNull
        private final Set<SimpleAgent.AgentIdentifier> agents;

        private final Map<SimpleAgent.AgentIdentifier, Set<SimpleAgent.AgentIdentifier>> connections = Maps.newHashMap();

        // Methods.

        /**
         * Add the specified agent by randomly created connections with already added agents. Do nothing if the agent is already in the {@link
         * ConnectionGroup}.
         *
         * @param agent              the agent to add
         * @param numberOfConnection the number of connections created with the agent
         * @param random             the random use to chose randomly which already agents will be chosen to be connected with the new agent
         */
        public void addAgent(@NonNull SimpleAgent.AgentIdentifier agent, int numberOfConnection, Random random) {
            if (!contains(agent)) {
                addNewConnections(agent, Math.min(numberOfConnection, agents.size()), random);
                agents.add(agent);
            } else
                log.debug("Cannot add Agent {} -> already in the Network Connection Group {}", agent, this);
        }

        private void addNewConnections(SimpleAgent.AgentIdentifier agent, int numberOfConnection, Random random) {
            if (numberOfConnection == agents.size()) {
                createConnectionBetween(agent, agents);
            } else {
                createConnectionBetween(agent, selectRandomlyAgents(numberOfConnection, random));
            }
        }

        private void createConnectionBetween(SimpleAgent.AgentIdentifier agent, Set<SimpleAgent.AgentIdentifier> selectedAgents) {
            for (SimpleAgent.AgentIdentifier other : selectedAgents) {
                connections.computeIfAbsent(agent, k -> Sets.newHashSet()).add(other);
                connections.computeIfAbsent(other, k -> Sets.newHashSet()).add(agent);
            }
        }

        private Set<SimpleAgent.AgentIdentifier> selectRandomlyAgents(int number, Random random) {
            List<SimpleAgent.AgentIdentifier> copy = Lists.newArrayList(agents);
            Set<SimpleAgent.AgentIdentifier> selected = Sets.newHashSet();
            for (int i = 0; i < number; i++) {
                int index = random.nextInt(copy.size());
                selected.add(copy.get(index));
                copy.remove(index);
            }

            return selected;
        }

        /**
         * Remove the specified agent from the {@link ConnectionGroup}. If the agent is not in the {@code ConnectionGroup}, do nothing.
         *
         * @param agent the agent to remove
         */
        public void removeAgent(@NonNull SimpleAgent.AgentIdentifier agent) {
            if (contains(agent)) {
                clearAgentConnections(agent);
                agents.remove(agent);
            } else
                log.debug("Cannot remove Agent {} -> not in the Network Connection Group {}", agent, this);

        }

        private void clearAgentConnections(SimpleAgent.AgentIdentifier agent) {
            Set<SimpleAgent.AgentIdentifier> connectedAgents = connections.remove(agent);
            for (SimpleAgent.AgentIdentifier connectedAgent : connectedAgents) {
                connections.get(connectedAgent).remove(agent);
            }
        }

        public boolean contains(SimpleAgent.AgentIdentifier agent) {
            return agents.contains(agent);
        }

        public boolean hasConnection(@NonNull SimpleAgent.AgentIdentifier a0, @NonNull SimpleAgent.AgentIdentifier a1) {
            if (contains(a0) && contains(a1)) {
                if (a0.equals(a1))
                    return true;
                else {
                    return connections.get(a0).contains(a1);
                }
            }
            return false;
        }
    }
}
