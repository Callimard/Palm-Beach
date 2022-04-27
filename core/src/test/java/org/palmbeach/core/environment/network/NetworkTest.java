package org.palmbeach.core.environment.network;

import org.palmbeach.core.agent.SimpleAgent;
import org.palmbeach.core.agent.exception.AgentNotStartedException;
import com.google.common.collect.Sets;
import org.palmbeach.core.common.Context;
import org.palmbeach.core.environment.Environment;
import org.palmbeach.core.event.Event;
import org.palmbeach.core.junit.PalmBeachSimulationTest;
import org.palmbeach.core.junit.PalmBeachTest;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.palmbeach.core.simulation.PalmBeachSimulation;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("Network tests")
@Tag("Network")
@PalmBeachTest
public class NetworkTest {

    @Nested
    @DisplayName("Network NonOrientedConnection tests")
    @Tag("NonOrientedConnection")
    class NonOrientedConnectionTest {

        @Nested
        @DisplayName("NonOrientedConnection isSelfConnection()")
        @Tag("isSelfConnection")
        class IsSelfConnection {

            @Test
            @DisplayName("isSelfConnection() returns true with null or equal a1")
            void withNullOrEqualAgentOne(@Mock SimpleAgent.AgentIdentifier a0) {
                Network.NonOrientedConnection c0 = new Network.NonOrientedConnection(a0, null);
                Network.NonOrientedConnection c1 = new Network.NonOrientedConnection(a0, a0);

                assertThat(c0.isSelfConnection()).isTrue();
                assertThat(c1.isSelfConnection()).isTrue();
            }
        }

        @Nested
        @DisplayName("NonOrientedConnection equals()")
        @Tag("equals")
        class Equals {

            @Test
            @DisplayName("equals() returns true with two equal connections")
            void withSameAgents(@Mock SimpleAgent.AgentIdentifier a0, @Mock SimpleAgent.AgentIdentifier a1) {
                Network.NonOrientedConnection c0 = new Network.NonOrientedConnection(a0, a1);
                Network.NonOrientedConnection c1 = new Network.NonOrientedConnection(a0, a1);

                assertThat(c0).isEqualTo(c1);
            }

            @Test
            @DisplayName("equals() returns true with two connection with equals a0 and a1 reversed")
            void withReversedConnection(@Mock SimpleAgent.AgentIdentifier a0, @Mock SimpleAgent.AgentIdentifier a1) {
                Network.NonOrientedConnection c0 = new Network.NonOrientedConnection(a0, a1);
                Network.NonOrientedConnection c1 = new Network.NonOrientedConnection(a1, a0);

                assertThat(c0).isEqualTo(c1);
            }

            @Test
            @DisplayName("equals() returns false with connection with different agens")
            void withDifferentAgent(@Mock SimpleAgent.AgentIdentifier a0, @Mock SimpleAgent.AgentIdentifier a1,
                                    @Mock SimpleAgent.AgentIdentifier a2) {
                Network.NonOrientedConnection c0 = new Network.NonOrientedConnection(a0, a1);
                Network.NonOrientedConnection c1 = new Network.NonOrientedConnection(a0, a2);

                assertThat(c0).isNotEqualTo(c1);
            }
        }

        @Nested
        @DisplayName("NonOrientedConnection hashCode()")
        @Tag("hashCode")
        class HashCode {

            @Test
            @DisplayName("hashCode() returns same value for equals connection")
            void withEqualsConnections(@Mock SimpleAgent.AgentIdentifier a0, @Mock SimpleAgent.AgentIdentifier a1) {
                Network.NonOrientedConnection c0 = new Network.NonOrientedConnection(a0, a1);
                Network.NonOrientedConnection c1 = new Network.NonOrientedConnection(a1, a0);

                assertThat(c0.hashCode()).isEqualByComparingTo(c1.hashCode());
            }
        }
    }

    @Nested
    @DisplayName("Network constructor")
    @Tag("constructor")
    class Constructor {

        @SuppressWarnings("ConstantConditions")
        @Test
        @DisplayName("constructor throws NullPointerException if name or environment is null ")
        void withNullName(@Mock Environment environment) {
            assertThrows(NullPointerException.class, () -> new BasicNetwork(null, environment, null));
            assertThrows(NullPointerException.class, () -> new BasicNetwork("name", null, null));
            assertThrows(NullPointerException.class, () -> new BasicNetwork(null, null, null));
        }

        @Test
        @DisplayName("constructor does not throw exception with non null name")
        void withNonNullName(@Mock Environment environment) {
            assertDoesNotThrow(() -> new BasicNetwork("name", environment, null));
        }
    }

    @Nested
    @DisplayName("Network initiateNetwork()")
    @Tag("initiateNetwork")
    class initiateNetwork {

        @SuppressWarnings("ConstantConditions")
        @Test
        @DisplayName("initiateNetwork() throws NullPointerException with null NetworkClass or null NetworkName or null " +
                "org/palmbeach/core/environment")
        void withNullParameters(@Mock Environment environment, @Mock Context context) {
            assertThrows(NullPointerException.class, () -> Network.initiateNetwork(null, null, environment, context));
            assertThrows(NullPointerException.class, () -> Network.initiateNetwork(null, "name", null, context));
            assertThrows(NullPointerException.class, () -> Network.initiateNetwork(BasicNetwork.class, null, null, context));
            assertThrows(NullPointerException.class, () -> Network.initiateNetwork(null, "name", environment, context));
            assertThrows(NullPointerException.class,
                         () -> Network.initiateNetwork(BasicNetwork.class, "name", null, context));
            assertThrows(NullPointerException.class,
                         () -> Network.initiateNetwork(BasicNetwork.class, null, environment, context));
            assertThrows(NullPointerException.class, () -> Network.initiateNetwork(null, null, null, context));
            assertDoesNotThrow(
                    () -> Network.initiateNetwork(BasicNetwork.class, "NetworkName", environment, context));
        }

        @Test
        @DisplayName("initiateNetwork() does not throw exception and create a new instance of Environment")
        void createNewInstanceOfEnvironment(@Mock Environment environment, @Mock Context context) {
            String networkName = "NetworkName";
            AtomicReference<Network> network = new AtomicReference<>();

            assertDoesNotThrow(
                    () -> network.set(Network.initiateNetwork(BasicNetwork.class, networkName, environment,
                                                              context)));
            assertThat(network.get()).isNotNull();
            assertThat(network.get().getClass()).isEqualTo(BasicNetwork.class);
            assertThat(network.get().getName()).isEqualTo(networkName);
        }
    }

    @Nested
    @DisplayName("Network toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsValue(@Mock Environment environment) {
            Network network = new BasicNetwork("name", environment, null);

            assertThat(network.toString()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Network getName()")
    @Tag("getName")
    class GetName {

        @Test
        @DisplayName("getName() never returns null")
        void neverReturnsValue(@Mock Environment environment) {
            Network network = new BasicNetwork("name", environment, null);

            assertThat(network.getName()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Network send()")
    @Tag("send")
    @PalmBeachSimulationTest
    class Send {

        @Test
        @DisplayName("send() throws AgentNotStartedException if source is not started")
        void notStartedSource(@Mock SimpleAgent.AgentIdentifier source, @Mock SimpleAgent.AgentIdentifier target,
                              @Mock Environment environment, @Mock Event<?> event) {
            SimpleAgent aSource = new SimpleAgent(source, null);
            SimpleAgent aTarget = new SimpleAgent(target, null);

            PalmBeachSimulation.addAgent(aSource);
            PalmBeachSimulation.addAgent(aTarget);

            BasicNetwork network = new BasicNetwork("name", environment, null);
            network.setHasConnectionSupplier(() -> true);
            assertThrows(AgentNotStartedException.class, () -> network.send(source, target, event));
            assertThat(network.getSendingCounter()).isEqualByComparingTo(0);
        }

        @Test
        @DisplayName("send() do nothing if source and target are not connected but source is started")
        void notConnectedAgent(@Mock SimpleAgent.AgentIdentifier source, @Mock SimpleAgent.AgentIdentifier target,
                               @Mock Environment environment, @Mock Event<?> event) {
            SimpleAgent aSource = new SimpleAgent(source, null);
            SimpleAgent aTarget = new SimpleAgent(target, null);

            PalmBeachSimulation.addAgent(aSource);
            PalmBeachSimulation.addAgent(aTarget);
            aSource.start();

            BasicNetwork network = new BasicNetwork("name", environment, null);
            network.setHasConnectionSupplier(() -> false);
            network.send(source, target, event);

            assertThat(network.getSendingCounter()).isEqualByComparingTo(0);
        }

        @Test
        @DisplayName("send() call simulateSending() one times if source and target are connected and source is started")
        void connectedAgent(@Mock SimpleAgent.AgentIdentifier source, @Mock SimpleAgent.AgentIdentifier target,
                            @Mock Environment environment, @Mock Event<?> event) {
            SimpleAgent aSource = new SimpleAgent(source, null);
            SimpleAgent aTarget = new SimpleAgent(target, null);

            PalmBeachSimulation.addAgent(aSource);
            PalmBeachSimulation.addAgent(aTarget);

            aSource.start();

            BasicNetwork network = new BasicNetwork("name", environment, null);
            network.setHasConnectionSupplier(() -> true);
            network.send(source, target, event);

            assertThat(network.getSendingCounter()).isEqualByComparingTo(1);
        }
    }

    // Inner classes.

    public static class BasicNetwork extends Network {

        @Getter
        private int sendingCounter = 0;

        @Setter
        private Supplier<Boolean> hasConnectionSupplier;

        public BasicNetwork(@NonNull String name, @NonNull Environment environment, Context context) {
            super(name, environment, context);
        }

        @Override
        public boolean hasConnection(SimpleAgent.@NonNull AgentIdentifier source, SimpleAgent.@NonNull AgentIdentifier target) {
            return hasConnectionSupplier == null || hasConnectionSupplier.get();
        }

        @Override
        protected void simulateSending(SimpleAgent.@NonNull AgentIdentifier source, SimpleAgent.@NonNull AgentIdentifier target,
                                       @NonNull Event<?> event) {
            sendingCounter++;
        }

        @Override
        public void environmentAddAgent(SimpleAgent.AgentIdentifier addedAgent) {
            // Nothing
        }

        @Override
        public void environmentRemoveAgent(SimpleAgent.AgentIdentifier removedAgent) {
            // Nothing
        }

        @Override
        public Set<Connection> allConnections() {
            return Sets.newHashSet();
        }

        @Override
        public Set<SimpleAgent.AgentIdentifier> directNeighbors(SimpleAgent.@NonNull AgentIdentifier agent) {
            return Sets.newHashSet(agent);
        }
    }
}
