package environment.network;

import agent.SimpleAgent;
import com.google.common.collect.Sets;
import common.Context;
import environment.Environment;
import event.Event;
import junit.PalmBeachSimulationTest;
import junit.PalmBeachTest;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import simulation.PalmBeachSimulation;

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
                "environment")
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
        @DisplayName("send() do nothing if the source is not stared even they are connected")
        void notStartedSource(@Mock SimpleAgent.AgentIdentifier source, @Mock SimpleAgent.AgentIdentifier target,
                              @Mock Environment environment, @Mock Event<?> event) {
            SimpleAgent aSource = new SimpleAgent(source, null);
            SimpleAgent aTarget = new SimpleAgent(target, null);

            PalmBeachSimulation.addAgent(aSource);
            PalmBeachSimulation.addAgent(aTarget);

            BasicNetwork network = new BasicNetwork("name", environment, null);
            network.setHasConnectionSupplier(() -> true);
            network.send(source, target, event);

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
        protected void simulateSending(SimpleAgent.@NonNull AgentIdentifier source, SimpleAgent.@NonNull AgentIdentifier target, @NonNull Event<?> event) {
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
        public Set<SimpleAgent.AgentIdentifier> agentDirectConnections(SimpleAgent.@NonNull AgentIdentifier agent) {
            return Sets.newHashSet(agent);
        }
    }
}
