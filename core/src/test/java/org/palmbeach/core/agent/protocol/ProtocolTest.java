package org.palmbeach.core.agent.protocol;

import org.palmbeach.core.agent.SimpleAgent;
import org.palmbeach.core.agent.protocol.Protocol;
import org.palmbeach.core.agent.protocol.exception.NullDefaultProtocolManipulatorException;
import org.palmbeach.core.common.Context;
import org.palmbeach.core.event.Event;
import org.palmbeach.core.junit.PalmBeachTest;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("Protocol tests")
@Tag("Protocol")
@PalmBeachTest
public class ProtocolTest {

    @Nested
    @DisplayName("ProtocolManipulator tests")
    @Tag("ProtocolManipulator")
    class ProtocolManipulatorTest {

        @Nested
        @DisplayName("ProtocolManipulator getManipulatedProtocol()")
        @Tag("getManipulatedProtocol")
        class GetManipulatedProtocol {

            @Test
            @DisplayName("getManipulatedProtocol() never returns null")
            void neverReturnsNull(@Mock Protocol protocol) {
                Protocol.ProtocolManipulator manipulator = new Protocol.DefaultProtocolManipulator(protocol);

                assertThat(manipulator.getManipulatedProtocol()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Protocol constructor")
    @Tag("constructor")
    class Constructor {

        @Nested
        @DisplayName("Protocol(SimpleAgent, Context)")
        class MainConstructor {

            @Test
            @DisplayName("Throws NullPointerException if the agent is null")
            void withNullAgent(@Mock Context context) {
                //noinspection ConstantConditions
                assertThrows(NullPointerException.class, () -> new BasicProtocol(null, context));
            }

            @Test
            @DisplayName("Does not throw exception with null context and the context is not null and empty")
            void withNullContext(@Mock SimpleAgent agent) {
                AtomicReference<Protocol> p = new AtomicReference<>();

                assertDoesNotThrow(() -> p.set(new BasicProtocol(agent, null)));
                assertThat(p.get().getContext()).isNotNull();
                assertThat(p.get().getContext().isEmpty()).isTrue();
            }

            @Test
            @DisplayName("Throws NullDefaultProtocolManipulatorException if the method defaultProtocolManipulator() implementation returns null")
            void withNullDefaultProtocolManipulator(@Mock SimpleAgent agent, @Mock Context context) {
                assertThrows(NullDefaultProtocolManipulatorException.class, () -> new WrongProtocolManipulatorProtocol(agent, context));
            }

        }
    }

    @Nested
    @DisplayName("Static instantiateProtocol()")
    @Tag("instantiateProtocol")
    class InstantiateProtocol {

        @Test
        @DisplayName("instantiateProtocol() throws Exception with non correct Protocol classes")
        void nonCorrectProtocolClasses(@Mock SimpleAgent agent) {
            assertThrows(Exception.class, () -> Protocol.instantiateProtocol(NoCorrectConstructorProtocol.class, agent, null));
            assertThrows(Exception.class, () -> Protocol.instantiateProtocol(WrongConstructorVisibilityProtocol.class, agent, null));
            assertThrows(Exception.class, () -> Protocol.instantiateProtocol(ThrowExceptionConstructorProtocol.class, agent, null));
        }

        @Test
        @DisplayName("instantiateProtocol() create an instance with a correct Protocol class")
        void withCorrectProtocolClass(@Mock SimpleAgent agent) {
            AtomicReference<Protocol> p = new AtomicReference<>();

            assertDoesNotThrow(() -> p.set(Protocol.instantiateProtocol(BasicProtocol.class, agent, null)));
            assertThat(p.get()).isNotNull();
        }

    }

    @Nested
    @DisplayName("Protocol resetDefaultProtocolManipulator()")
    @Tag("resetDefaultProtocolManipulator")
    class ResetDefaultProtocolManipulator {

        @Test
        @DisplayName("resetDefaultProtocolManipulator() reset ProtocolManipulator to the default ProtocolManipulator class")
        void resetDefaultProtocolManipulator(@Mock SimpleAgent agent) {
            Protocol p = new BasicProtocol(agent, null);
            Protocol.ProtocolManipulator manipulator = p.getManipulator();
            p.resetDefaultProtocolManipulator();
            Protocol.ProtocolManipulator newManipulator = p.getManipulator();

            assertThat(manipulator.getClass()).isEqualTo(newManipulator.getClass());
        }
    }

    @Nested
    @DisplayName("Protocol toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() does not returns null value")
        void notReturnsNull(@Mock SimpleAgent agent) {
            Protocol p = new BasicProtocol(agent, null);

            assertThat(p.toString()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Protocol setManipulator()")
    @Tag("setManipulator")
    class SetManipulator {

        @Test
        @DisplayName("setManipulator() throws NullPointerException if specified ProtocolManipulator is null")
        void withNullProtocolManipulator(@Mock SimpleAgent agent) {
            Protocol p = new BasicProtocol(agent, null);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> p.setManipulator(null));
        }

        @Test
        @DisplayName("setManipulator() does not throw exception with non null ProtocolManipulator")
        void withNonNullProtocolManipulator(@Mock SimpleAgent agent, @Mock Protocol.ProtocolManipulator manipulator) {
            Protocol p = new BasicProtocol(agent, null);

            assertDoesNotThrow(() -> p.setManipulator(manipulator));
            assertThat(p.getManipulator()).isNotNull().isSameAs(manipulator);
        }
    }

    public static class BasicProtocol extends Protocol {

        @Getter
        @Setter
        private int processEventCounter = 0;

        public BasicProtocol(@NonNull SimpleAgent agent, Context context) {
            super(agent, context);
        }

        @Override
        public void agentStarted() {
            // Nothing
        }

        @Override
        public void agentStopped() {
            // Nothing
        }

        @Override
        public void agentKilled() {
            // Nothing
        }

        @Override
        protected ProtocolManipulator defaultProtocolManipulator() {
            return new DefaultProtocolManipulator(this);
        }

        @Override
        public void processEvent(Event<?> event) {
            processEventCounter++;
        }

        @Override
        public boolean canProcessEvent(Event<?> event) {
            return true;
        }
    }

    public static class NoCorrectConstructorProtocol extends BasicProtocol {

        public NoCorrectConstructorProtocol(@NonNull SimpleAgent agent) {
            super(agent, null);
        }

        @Override
        protected ProtocolManipulator defaultProtocolManipulator() {
            return new DefaultProtocolManipulator(this);
        }
    }

    public static class WrongConstructorVisibilityProtocol extends BasicProtocol {

        protected WrongConstructorVisibilityProtocol(@NonNull SimpleAgent agent, Context context) {
            super(agent, context);
        }

        @Override
        protected ProtocolManipulator defaultProtocolManipulator() {
            return new DefaultProtocolManipulator(this);
        }
    }

    public static class WrongProtocolManipulatorProtocol extends BasicProtocol {

        public WrongProtocolManipulatorProtocol(@NonNull SimpleAgent agent, Context context) {
            super(agent, context);
        }

        @Override
        protected ProtocolManipulator defaultProtocolManipulator() {
            return null;
        }
    }

    public static class ThrowExceptionConstructorProtocol extends BasicProtocol {

        public ThrowExceptionConstructorProtocol(@NonNull SimpleAgent agent, Context context) {
            super(agent, context);
            throw new RuntimeException();
        }

        @Override
        protected ProtocolManipulator defaultProtocolManipulator() {
            return new DefaultProtocolManipulator(this);
        }
    }

    public static class ProtocolWithDependencies extends BasicProtocol {

        @Setter
        private BasicProtocol basicProtocol;

        public ProtocolWithDependencies(@NonNull SimpleAgent agent, Context context) {
            super(agent, context);
        }
    }

}
