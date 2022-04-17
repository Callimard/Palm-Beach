package environment.physical;

import agent.SimpleAgent;
import common.Context;
import environment.Environment;
import event.Event;
import junit.PalmBeachTest;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("PhysicalNetwork tests")
@Tag("PhysicalNetwork")
@PalmBeachTest
public class PhysicalNetworkTest {

    @Nested
    @DisplayName("PhysicalNetwork constructor")
    @Tag("constructor")
    class Constructor {

        @SuppressWarnings("ConstantConditions")
        @Test
        @DisplayName("constructor throws NullPointerException if name or environment is null ")
        void withNullName(@Mock Environment environment) {
            assertThrows(NullPointerException.class, () -> new BasicPhysicalNetwork(null, environment, null));
            assertThrows(NullPointerException.class, () -> new BasicPhysicalNetwork("name", null, null));
            assertThrows(NullPointerException.class, () -> new BasicPhysicalNetwork(null, null, null));
        }

        @Test
        @DisplayName("constructor does not throw exception with non null name")
        void withNonNullName(@Mock Environment environment) {
            assertDoesNotThrow(() -> new BasicPhysicalNetwork("name", environment, null));
        }
    }

    @Nested
    @DisplayName("PhysicalNetwork initiatePhysicalNetwork()")
    @Tag("initiatePhysicalNetwork")
    class initiatePhysicalNetwork {

        @SuppressWarnings("ConstantConditions")
        @Test
        @DisplayName("initiatePhysicalNetwork() throws NullPointerException with null physicalNetworkClass or null physicalNetworkName or null " +
                "environment")
        void withNullParameters(@Mock Environment environment, @Mock Context context) {
            assertThrows(NullPointerException.class, () -> PhysicalNetwork.initiatePhysicalNetwork(null, null, environment, context));
            assertThrows(NullPointerException.class, () -> PhysicalNetwork.initiatePhysicalNetwork(null, "name", null, context));
            assertThrows(NullPointerException.class, () -> PhysicalNetwork.initiatePhysicalNetwork(BasicPhysicalNetwork.class, null, null, context));
            assertThrows(NullPointerException.class, () -> PhysicalNetwork.initiatePhysicalNetwork(null, "name", environment, context));
            assertThrows(NullPointerException.class,
                         () -> PhysicalNetwork.initiatePhysicalNetwork(BasicPhysicalNetwork.class, "name", null, context));
            assertThrows(NullPointerException.class,
                         () -> PhysicalNetwork.initiatePhysicalNetwork(BasicPhysicalNetwork.class, null, environment, context));
            assertThrows(NullPointerException.class, () -> PhysicalNetwork.initiatePhysicalNetwork(null, null, null, context));
            assertDoesNotThrow(
                    () -> PhysicalNetwork.initiatePhysicalNetwork(BasicPhysicalNetwork.class, "physicalNetworkName", environment, context));
        }

        @Test
        @DisplayName("initiatePhysicalNetwork() does not throw exception and create a new instance of Environment")
        void createNewInstanceOfEnvironment(@Mock Environment environment, @Mock Context context) {
            String physicalNetworkName = "physicalNetworkName";
            AtomicReference<PhysicalNetwork> physicalNetwork = new AtomicReference<>();

            assertDoesNotThrow(
                    () -> physicalNetwork.set(PhysicalNetwork.initiatePhysicalNetwork(BasicPhysicalNetwork.class, physicalNetworkName, environment,
                                                                                      context)));
            assertThat(physicalNetwork.get()).isNotNull();
            assertThat(physicalNetwork.get().getClass()).isEqualTo(BasicPhysicalNetwork.class);
            assertThat(physicalNetwork.get().getName()).isEqualTo(physicalNetworkName);
        }
    }

    @Nested
    @DisplayName("PhysicalNetwork toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsValue(@Mock Environment environment) {
            PhysicalNetwork physicalNetwork = new BasicPhysicalNetwork("name", environment, null);

            assertThat(physicalNetwork.toString()).isNotNull();
        }
    }

    @Nested
    @DisplayName("PhysicalNetwork getName()")
    @Tag("getName")
    class GetName {

        @Test
        @DisplayName("getName() never returns null")
        void neverReturnsValue(@Mock Environment environment) {
            PhysicalNetwork physicalNetwork = new BasicPhysicalNetwork("name", environment, null);

            assertThat(physicalNetwork.getName()).isNotNull();
        }
    }

    @Nested
    @DisplayName("PhysicalNetwork send()")
    @Tag("send")
    class Send {

        @Test
        @DisplayName("send() do nothing if source and target are not physically connected")
        void notPhysicallyConnectedAgent(@Mock SimpleAgent.AgentIdentifier source, @Mock SimpleAgent.AgentIdentifier target,
                                         @Mock Environment environment, @Mock Event<?> event) {
            BasicPhysicalNetwork physicalNetwork = new BasicPhysicalNetwork("name", environment, null);
            physicalNetwork.setHasPhysicalConnectionSupplier(() -> false);
            physicalNetwork.send(source, target, event);

            assertThat(physicalNetwork.getPhysicalSendCounter()).isEqualByComparingTo(0);
        }

        @Test
        @DisplayName("send() call physicalSend() one times if source and target are physically connected")
        void physicallyConnectedAgent(@Mock SimpleAgent.AgentIdentifier source, @Mock SimpleAgent.AgentIdentifier target,
                                      @Mock Environment environment, @Mock Event<?> event) {
            BasicPhysicalNetwork physicalNetwork = new BasicPhysicalNetwork("name", environment, null);
            physicalNetwork.setHasPhysicalConnectionSupplier(() -> true);
            physicalNetwork.send(source, target, event);

            assertThat(physicalNetwork.getPhysicalSendCounter()).isEqualByComparingTo(1);
        }
    }

    // Inner classes.

    public static class BasicPhysicalNetwork extends PhysicalNetwork {

        @Getter
        private int physicalSendCounter = 0;

        @Setter
        private Supplier<Boolean> hasPhysicalConnectionSupplier;

        public BasicPhysicalNetwork(@NonNull String name, @NonNull Environment environment, Context context) {
            super(name, environment, context);
        }

        @Override
        public boolean hasPhysicalConnection(SimpleAgent.AgentIdentifier source, SimpleAgent.AgentIdentifier target) {
            return hasPhysicalConnectionSupplier == null || hasPhysicalConnectionSupplier.get();
        }

        @Override
        protected PhysicalEvent preparePhysicalEvent(Event<?> event) {
            return new PhysicalEventTest.BasicPhysicalEvent(event);
        }

        @Override
        protected void physicallySend(SimpleAgent.AgentIdentifier source, SimpleAgent.AgentIdentifier target, PhysicalEvent physicalEvent) {
            physicalSendCounter++;
        }

        @Override
        public void agentAdded(SimpleAgent.AgentIdentifier addedAgent) {
            // Nothing
        }

        @Override
        public void agentRemoved(SimpleAgent.AgentIdentifier removedAgent) {
            // Nothing
        }
    }
}
