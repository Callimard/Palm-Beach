package org.palmbeach.basic.network;

import org.palmbeach.basic.network.RandomConnectedNetwork;
import org.palmbeach.core.agent.SimpleAgent;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.palmbeach.core.common.Context;
import org.palmbeach.core.common.SimpleContext;
import org.palmbeach.core.environment.Environment;
import org.palmbeach.core.environment.network.Network;
import org.palmbeach.core.junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("RandomConnectedNetwork tests")
@Tag("RandomConnectedNetwork")
@PalmBeachTest
public class RandomConnectedNetworkTest {

    @Nested
    @DisplayName("RandomConnectedNetwork constructor()")
    @Tag("constructor")
    class Constructor {

        @Test
        @DisplayName("constructor() does not throw exception with null context and default value are used")
        void withNoContext(@Mock SimpleAgent.AgentIdentifier i0, @Mock SimpleAgent.AgentIdentifier i1) {
            Environment env = new Environment("env", null);
            env.addAgent(i0);
            env.addAgent(i1);

            AtomicReference<RandomConnectedNetwork> net = new AtomicReference<>();
            assertDoesNotThrow(() -> net.set(new RandomConnectedNetwork("net", env, null)));
            assertThat(net.get().connectionNumber()).isEqualTo(RandomConnectedNetwork.DEFAULT_CONNECTION_NUMBER);
            assertThat(net.get().minDelay()).isEqualTo(RandomConnectedNetwork.DEFAULT_MIN_DELAY);
            assertThat(net.get().maxDelay()).isEqualTo(RandomConnectedNetwork.DEFAULT_MAX_DELAY);
        }

        @ParameterizedTest
        @ValueSource(ints = {-16545, -65165, -1, 0})
        @DisplayName("constructor() throws IllegalArgumentException if connection number is not correct")
        void withNotCorrectConnectionNumber(int connectionNumber) {
            Environment env = new Environment("env", null);
            Context context = new SimpleContext();
            context.map(RandomConnectedNetwork.CONNECTION_NUMBER, connectionNumber);

            assertThrows(IllegalArgumentException.class, () -> new RandomConnectedNetwork("net", env, context));
        }

        @ParameterizedTest
        @ValueSource(longs = {-16516, -6135, -1, 0})
        @DisplayName("constructor() throws IllegalArgumentException if the maxDelay set in context is less than 1")
        void withWrongMinDelay(long minDelay, @Mock Environment environment) {
            Context context = new SimpleContext();
            context.map(RandomConnectedNetwork.MIN_SENDING_DELAY, minDelay);

            assertThrows(IllegalArgumentException.class, () -> new RandomConnectedNetwork("net", environment, context));
        }

        @ParameterizedTest
        @ValueSource(longs = {-16516, -6135, -1, 0})
        @DisplayName("constructor() throws IllegalArgumentException if the maxDelay set in context is less than 1")
        void withWrongMaxDelay(long maxDelay, @Mock Environment environment) {
            Context context = new SimpleContext();
            context.map(RandomConnectedNetwork.MAX_SENDING_DELAY, maxDelay);

            assertThrows(IllegalArgumentException.class, () -> new RandomConnectedNetwork("net", environment, context));
        }
    }

    @Nested
    @DisplayName("RandomConnectedNetwork environmentAddAgent()")
    @Tag("environmentAddAgent")
    class EnvironmentAddAgent {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 5, 7, 10, 15})
        @DisplayName("environmentAddAgent() connected all agents if the number of agent is less or equal to connection number")
        void withLessOrEqualConnectionNumberAgentInEnvironment(int connectionNumber) {
            Environment env = new Environment("env", null);
            Context context = new SimpleContext();
            context.map(RandomConnectedNetwork.CONNECTION_NUMBER, connectionNumber);
            RandomConnectedNetwork network = new RandomConnectedNetwork("net", env, context);
            Set<SimpleAgent.AgentIdentifier> agents = Sets.newHashSet();
            for (int i = 0; i < connectionNumber; i++) {
                SimpleAgent.AgentIdentifier agent = new SimpleAgent.SimpleAgentIdentifier(String.valueOf(i), i);
                agents.add(agent);
                env.addAgent(agent);
            }

            for (SimpleAgent.AgentIdentifier agent : agents) {
                for (SimpleAgent.AgentIdentifier other : agents) {
                    assertThat(network.hasConnection(agent, other)).isTrue();
                }
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 5, 7, 10, 15})
        @DisplayName("environmentAddAgent() create the specified number or more of connection number")
        void withSpecifiedConnectionNumber(int connectionNumber) {
            Environment env = new Environment("env", null);
            Context context = new SimpleContext();
            context.map(RandomConnectedNetwork.CONNECTION_NUMBER, connectionNumber);
            RandomConnectedNetwork network = new RandomConnectedNetwork("net", env, context);
            Set<SimpleAgent.AgentIdentifier> agents = Sets.newHashSet();
            for (int i = 0; i < connectionNumber + 50; i++) {
                SimpleAgent.AgentIdentifier agent = new SimpleAgent.SimpleAgentIdentifier(String.valueOf(i), i);
                agents.add(agent);
                env.addAgent(agent);
            }

            for (SimpleAgent.AgentIdentifier agent : agents) {
                assertThat(network.directNeighbors(agent)).contains(agent);
                assertThat(network.directNeighbors(agent).size()).isGreaterThanOrEqualTo(connectionNumber + 1);
            }
        }
    }

    @Nested
    @DisplayName("RandomConnectedNetwork environmentRemoveAgent()")
    @Tag("environmentRemoveAgent")
    class EnvironmentRemoveAgent {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 5, 7, 10, 15})
        @DisplayName("environmentRemoveAgent() remove all agents connections")
        void removeAllConnections(int connectionNumber) {
            Environment env = new Environment("env", null);
            Context context = new SimpleContext();
            context.map(RandomConnectedNetwork.CONNECTION_NUMBER, connectionNumber);
            RandomConnectedNetwork network = new RandomConnectedNetwork("net", env, context);
            List<SimpleAgent.AgentIdentifier> agents = Lists.newArrayList();
            for (int i = 0; i < connectionNumber + 50; i++) {
                SimpleAgent.AgentIdentifier agent = new SimpleAgent.SimpleAgentIdentifier(String.valueOf(i), i);
                agents.add(agent);
                env.addAgent(agent);
            }

            Random random = new Random();
            int index = random.nextInt(agents.size());
            SimpleAgent.AgentIdentifier agent = agents.get(index);
            Set<SimpleAgent.AgentIdentifier> connectedAgents = network.directNeighbors(agent);
            for (SimpleAgent.AgentIdentifier cAgent : connectedAgents) {
                assertThat(network.hasConnection(agent, cAgent)).isTrue();
                assertThat(network.hasConnection(cAgent, agent)).isTrue();
            }

            agents.remove(index);
            env.removeAgent(agent);

            for (SimpleAgent.AgentIdentifier cAgent : connectedAgents) {
                assertThat(network.hasConnection(agent, cAgent)).isFalse();
                assertThat(network.hasConnection(cAgent, agent)).isFalse();

                if (cAgent != agent)
                    assertThat(network.directNeighbors(cAgent)).doesNotContain(agent);
            }
        }
    }

    @Nested
    @DisplayName("RandomConnectedNetwork agentDirectConnections()")
    @Tag("agentDirectConnections")
    class AgentDirectConnections {

        @Test
        @DisplayName("agentDirectConnections() throws NotInNetworkException if agent is not in network")
        void throwsException(@Mock SimpleAgent.AgentIdentifier i0) {
            Environment env = new Environment("env", null);
            RandomConnectedNetwork network = new RandomConnectedNetwork("net", env, null);

            assertThrows(Network.NotInNetworkException.class, () -> network.directNeighbors(i0));
        }
    }

    @Nested
    @DisplayName("RandomConnectedNetwork allConnections()")
    @Tag("allConnections")
    class AllConnections {

        @ParameterizedTest
        @ValueSource(ints = {5, 50, 75, 150, 900})
        @DisplayName("allConnections() returns all connections")
        void returnsAllConnections(int n) {
            Environment environment = new Environment("env", null);
            RandomConnectedNetwork network = new RandomConnectedNetwork("net", environment, null);
            for (int i = 0; i < n; i++) {
                environment.addAgent(new SimpleAgent.SimpleAgentIdentifier(String.valueOf(i), i));
            }

            int nbCon = network.connectionNumber();

            Set<Network.Connection> allConnections = network.allConnections();
            assertThat(allConnections.stream().filter(Network.Connection::isSelfConnection)).hasSize(n);
            assertThat(allConnections).isNotNull().hasSize((((nbCon + 1) * (nbCon + 2)) / 2) + (n - (nbCon + 1)) * (nbCon + 1));
        }
    }

    @Nested
    @DisplayName("RandomConnectedNetwork connectionNumber()")
    @Tag("connectionNumber")
    class ConnectionNumber {

        @ParameterizedTest
        @ValueSource(ints = {-12164, -3231, -1, 0})
        @DisplayName("connectionNumber(int) throws IllegalArgumentException if connectionNumber is less than 1")
        void withWrongConnectionNumber(int connectionNumber) {
            Environment env = new Environment("env", null);
            RandomConnectedNetwork network = new RandomConnectedNetwork("net", env, null);

            assertThrows(IllegalArgumentException.class, () -> network.connectionNumber(connectionNumber));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 5, 9, 15, 165})
        @DisplayName("connectionNumber(int) the connection number")
        void withCorrectConnectionNumber(int connectionNumber) {
            Environment env = new Environment("env", null);
            RandomConnectedNetwork network = new RandomConnectedNetwork("net", env, null);

            network.connectionNumber(connectionNumber);
            assertThat(network.connectionNumber()).isEqualByComparingTo(connectionNumber);
        }
    }
}
