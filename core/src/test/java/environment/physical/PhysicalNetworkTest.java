package environment.physical;

import agent.SimpleAgent;
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

        @Test
        @DisplayName("constructor throws NullPointerException if name is null")
        void withNullName() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> new BasicPhysicalNetwork(null));
        }

        @Test
        @DisplayName("constructor does not throw exception with non null name")
        void withNonNullName() {
            assertDoesNotThrow(() -> new BasicPhysicalNetwork("name"));
        }
    }

    @Nested
    @DisplayName("PhysicalNetwork toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsValue() {
            PhysicalNetwork physicalNetwork = new BasicPhysicalNetwork("name");

            assertThat(physicalNetwork.toString()).isNotNull();
        }
    }

    @Nested
    @DisplayName("PhysicalNetwork getName()")
    @Tag("getName")
    class GetName {

        @Test
        @DisplayName("getName() never returns null")
        void neverReturnsValue() {
            PhysicalNetwork physicalNetwork = new BasicPhysicalNetwork("name");

            assertThat(physicalNetwork.getName()).isNotNull();
        }
    }

    @Nested
    @DisplayName("PhysicalNetwork send()")
    @Tag("send")
    class Send {

        @Test
        @DisplayName("send() do nothing if source and target are not physically connected")
        void notPhysicallyConnectedAgent(@Mock SimpleAgent.AgentIdentifier source, @Mock SimpleAgent.AgentIdentifier target, @Mock Event<?> event) {
            BasicPhysicalNetwork physicalNetwork = new BasicPhysicalNetwork("name");
            physicalNetwork.setHasPhysicalConnectionSupplier(() -> false);
            physicalNetwork.send(source, target, event);

            assertThat(physicalNetwork.getPhysicalSendCounter()).isEqualByComparingTo(0);
        }

        @Test
        @DisplayName("send() call physicalSend() one times if source and target are physically connected")
        void physicallyConnectedAgent(@Mock SimpleAgent.AgentIdentifier source, @Mock SimpleAgent.AgentIdentifier target, @Mock Event<?> event) {
            BasicPhysicalNetwork physicalNetwork = new BasicPhysicalNetwork("name");
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

        public BasicPhysicalNetwork(@NonNull String name) {
            super(name);
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
    }
}
