package agent;

import agent.exception.*;
import behavior.Behavior;
import common.Context;
import environment.Environment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import protocol.Protocol;
import tools.junit.PalmBeachTest;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Nested
@DisplayName("BasicAgent tests")
@Tag("BasicAgent")
@PalmBeachTest
public class BasicAgentTest {

    @Nested
    @DisplayName("SimpleAgentIdentifier tests")
    @Tag("SimpleAgentIdentifier")
    class SimpleAgentIdentifierTest {

        @Nested
        @DisplayName("Static nextId()")
        @Tag("nextId")
        class NextId {

            @Test
            @DisplayName("Several call of nextId returns different values")
            void severalCallOfNextIdReturnsSeveralValue() {
                Set<Long> set = new HashSet<>();
                for (int i = 0; i < 500; i++) {
                    boolean added = set.add(BasicAgent.SimpleAgentIdentifier.nextId());
                    assertThat(added).isTrue();
                }
            }
        }

        @Nested
        @DisplayName("equals()")
        @Tag("equals")
        class Equals {

            @Test
            @DisplayName("Equals returns false with not SimpleAgentIdentifier class")
            void differentTypeWithEquals() {
                BasicAgent.SimpleAgentIdentifier identifier = new BasicAgent.SimpleAgentIdentifier("TEST", BasicAgent.SimpleAgentIdentifier.nextId());
                Object other = new Object();

                boolean res = identifier.equals(other);
                assertThat(res).isFalse();
            }

            @SuppressWarnings({"EqualsWithItself", "ConstantConditions"})
            @Test
            @DisplayName("Equals returns true with same instance")
            void sameInstance() {
                BasicAgent.SimpleAgentIdentifier identifier = new BasicAgent.SimpleAgentIdentifier("TEST", BasicAgent.SimpleAgentIdentifier.nextId());

                boolean res = identifier.equals(identifier);
                assertThat(res).isTrue();
            }

            @Test
            @DisplayName("Equals returns true with different instances and equal values")
            void equalValues() {
                BasicAgent.SimpleAgentIdentifier i0 = new BasicAgent.SimpleAgentIdentifier("TEST", 0L);
                BasicAgent.SimpleAgentIdentifier i1 = new BasicAgent.SimpleAgentIdentifier("TEST", 0L);

                boolean res = i0.equals(i1);
                assertThat(res).isTrue();
            }

            @Test
            @DisplayName("Equals returns false with different instances and different values")
            void differentValues() {
                BasicAgent.SimpleAgentIdentifier i0 = new BasicAgent.SimpleAgentIdentifier("TEST", 0L);
                BasicAgent.SimpleAgentIdentifier i1 = new BasicAgent.SimpleAgentIdentifier("TEST", 1L);
                BasicAgent.SimpleAgentIdentifier i2 = new BasicAgent.SimpleAgentIdentifier("TEST1", 0L);
                BasicAgent.SimpleAgentIdentifier i3 = new BasicAgent.SimpleAgentIdentifier("TEST2", 1L);

                boolean res = i0.equals(i1);
                assertThat(res).isFalse();

                res = i0.equals(i2);
                assertThat(res).isFalse();

                res = i0.equals(i3);
                assertThat(res).isFalse();
            }
        }

        @Nested
        @DisplayName("hashCode()")
        @Tag("hashCode")
        class HashCode {

            @Test
            @DisplayName("Returns a it without thrown exception")
            void returnsCorrectValue() {
                BasicAgent.SimpleAgentIdentifier i = new BasicAgent.SimpleAgentIdentifier("TEST", 0L);

                assertDoesNotThrow(i::hashCode);
            }

        }

        @Nested
        @DisplayName("toString()")
        @Tag("toString")
        class ToString {

            @Test
            @DisplayName("Returns non null String")
            void returnsCorrectValue() {
                BasicAgent.SimpleAgentIdentifier i = new BasicAgent.SimpleAgentIdentifier("TEST", 0L);

                assertThat(i.toString()).isNotNull();
            }

        }
    }

    @Nested
    @DisplayName("BasicAgent constructor")
    @Tag("constructor")
    class Constructor {

        @SuppressWarnings("ConstantConditions")
        @Nested
        @DisplayName("BasicAgent(AgentIdentifier, Environment, Context)")
        class MainConstructor {

            @Test
            @DisplayName("Throws NullPointerException if null identifier")
            void nullIdentifier(@Mock Environment environment, @Mock Context context) {
                assertThrows(NullPointerException.class, () -> new BasicAgent(null, environment, context));
            }

            @Test
            @DisplayName("Throws NullPointerException if null environment")
            void nullEnvironment(@Mock BasicAgent.AgentIdentifier identifier, @Mock Context context) {
                assertThrows(NullPointerException.class, () -> new BasicAgent(identifier, null, context));
            }

            @Test
            @DisplayName("Does not throw exception with null context and not null context")
            void nullContext(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment, @Mock Context context) {
                assertDoesNotThrow(() -> new BasicAgent(identifier, environment, null));
                assertDoesNotThrow(() -> new BasicAgent(identifier, environment, context));
            }

            @Test
            @DisplayName("Constructor set the agent state to CREATED")
            void setCorrectState(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment, @Mock Context context) {
                BasicAgent basicAgent = new BasicAgent(identifier, environment, context);

                assertThat(basicAgent.getState()).isEqualTo(BasicAgent.AgentState.CREATED);
            }

        }

        @Nested
        @DisplayName("BasicAgent(AgentIdentifier, Environment)")
        class SecondaryConstructor {

            @Test
            @DisplayName("Throws NullPointerException if null identifier")
            void nullIdentifier(@Mock Environment environment) {
                assertThrows(NullPointerException.class, () -> new BasicAgent(null, environment));
            }

            @Test
            @DisplayName("Throws NullPointerException if null environment")
            void nullEnvironment(@Mock BasicAgent.AgentIdentifier identifier) {
                assertThrows(NullPointerException.class, () -> new BasicAgent(identifier, null));
            }

            @Test
            @DisplayName("Does not throw exception with non null parameter and BasicAgent context is not null after construction")
            void nullEnvironment(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
                final AtomicReference<BasicAgent> basicAgent = new AtomicReference<>();

                assertDoesNotThrow(() -> basicAgent.set(new BasicAgent(identifier, environment)));
                assertThat(basicAgent.get().getContext()).isNotNull();
            }

            @Test
            @DisplayName("Constructor set the agent state to CREATED")
            void setCorrectState(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
                BasicAgent basicAgent = new BasicAgent(identifier, environment);
                assertThat(basicAgent.getState()).isEqualTo(BasicAgent.AgentState.CREATED);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nested
    @DisplayName("BasicAgent addObserver()")
    @Tag("addObserver")
    class AddObserver {

        @Test
        @DisplayName("Throws NullPointerException if observer is null")
        void withNullObserver(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            assertThrows(NullPointerException.class, () -> basicAgent.addObserver(null));
        }

        @Test
        @DisplayName("Does not throw exception if observer is not null")
        void withNotNullObserver(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment,
                                 @Mock BasicAgent.AgentObserver observer) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            assertDoesNotThrow(() -> basicAgent.addObserver(observer));
        }
    }

    @Nested
    @DisplayName("BasicAgent start()")
    @Tag("start")
    class Start {

        @Test
        @DisplayName("start() does not throw exception fro created agent and change the state of the agent")
        void startCreatedAgent(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);

            assertDoesNotThrow(basicAgent::start);
            assertThat(basicAgent.isStarted()).isTrue();
        }

        @Test
        @DisplayName("start() throws AgentCannotBeStartedException for started agent")
        void startStartedAgent(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.start();

            assertThrows(AgentCannotBeStartedException.class, basicAgent::start);
            assertThat(basicAgent.isStarted()).isTrue();
        }

        @Test
        @DisplayName("start() does not throw exception for a stopped agent")
        void startStoppedAgent(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.start();

            basicAgent.stop();

            assertDoesNotThrow(basicAgent::start);
            assertThat(basicAgent.isStarted()).isTrue();
        }

        @Test
        @DisplayName("start() throws AgentCannotBeStartedException for killed agent")
        void startKilledAgent(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.kill();

            assertThrows(AgentCannotBeStartedException.class, basicAgent::start);
            assertThat(basicAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("start() notify AgentObserver")
        void startNotifyAgentObserver(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment,
                                      @Mock BasicAgent.AgentObserver observer) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.addObserver(observer);
            basicAgent.start();

            verify(observer, times(1)).agentStarted();
        }
    }

    @Nested
    @DisplayName("BasicAgent stop()")
    @Tag("stop")
    class Stop {

        @Test
        @DisplayName("stop() throws AgentCannotBeStoppedException for a created agent")
        void stopCreatedAgent(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);

            assertThrows(AgentCannotBeStoppedException.class, basicAgent::stop);
            assertThat(basicAgent.getState()).isEqualTo(BasicAgent.AgentState.CREATED);
        }

        @Test
        @DisplayName("stop() does not throw exception for started agent")
        void stopStartedAgent(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.start();

            assertDoesNotThrow(basicAgent::stop);
            assertThat(basicAgent.isStopped()).isTrue();
        }

        @Test
        @DisplayName("stop() throws AgentCannotBeStoppedException for killed agent")
        void stopKilledAgent(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.kill();

            assertThrows(AgentCannotBeStoppedException.class, basicAgent::stop);
            assertThat(basicAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("stop() notify AgentObserver")
        void stopNotifyAgentObserver(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment,
                                     @Mock BasicAgent.AgentObserver observer) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.addObserver(observer);
            basicAgent.start();
            basicAgent.stop();

            verify(observer, times(1)).agentStopped();
        }
    }

    @Nested
    @DisplayName("BasicAgent kill()")
    @Tag("kill")
    class Kill {

        @Test
        @DisplayName("kill() does not throw exception for created agent")
        void killCreatedAgent(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);

            assertDoesNotThrow(basicAgent::kill);
            assertThat(basicAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("kill() does not throw exception for started agent")
        void killStartedAgent(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.start();

            assertDoesNotThrow(basicAgent::kill);
            assertThat(basicAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("kill() does not throw exception for stopped agent")
        void killStoppedAgent(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.start();
            basicAgent.stop();

            assertDoesNotThrow(basicAgent::kill);
            assertThat(basicAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("kill() throws AgentCannotBeKilledException for killed agent")
        void killKilledAgent(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.kill();

            assertThrows(AgentCannotBeKilledException.class, basicAgent::kill);
            assertThat(basicAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("kill() notify AgentObserver")
        void killNotifyAgentObserver(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment,
                                     @Mock BasicAgent.AgentObserver observer) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.addObserver(observer);
            basicAgent.kill();

            verify(observer, times(1)).agentKilled();
        }
    }

    @Nested
    @DisplayName("BasicAgent addProtocol()")
    @Tag("addProtocol")
    class AddProtocol {

        @Test
        @DisplayName("addProtocol() with non correct Protocol class throws FailToAddProtocolException")
        void nonCorrectProtocolClasses(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);

            assertThrows(FailToAddProtocolException.class, () -> basicAgent.addProtocol(NoCorrectConstructorProtocol.class));
            assertThrows(FailToAddProtocolException.class, () -> basicAgent.addProtocol(WrongConstructorVisibilityProtocol.class));
            assertThrows(FailToAddProtocolException.class, () -> basicAgent.addProtocol(ThrowExceptionConstructorProtocol.class));
        }

        @Test
        @DisplayName("addProtocol() with correct Protocol class does not throw exception and add the protocol")
        void withCorrectProtocolClass(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);

            assertThat(basicAgent.hasProtocol(CorrectConstructorProtocol.class)).isFalse();
            assertThat(basicAgent.getProtocol(CorrectConstructorProtocol.class)).isNull();
            assertDoesNotThrow(() -> basicAgent.addProtocol(CorrectConstructorProtocol.class));
            assertThat(basicAgent.hasProtocol(CorrectConstructorProtocol.class)).isTrue();
            assertThat(basicAgent.getProtocol(CorrectConstructorProtocol.class)).isNotNull();
        }

        @Test
        @DisplayName("addProtocol() with correct Protocol does not throw exception for already added protocol and keep the previous instance of " +
                "protocol")
        void addTheSameProtocol(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.addProtocol(CorrectConstructorProtocol.class);

            Protocol protocol = basicAgent.getProtocol(CorrectConstructorProtocol.class);

            assertDoesNotThrow(() -> basicAgent.addProtocol(CorrectConstructorProtocol.class));
            assertThat(basicAgent.hasProtocol(CorrectConstructorProtocol.class)).isTrue();
            assertThat(basicAgent.getProtocol(CorrectConstructorProtocol.class)).isNotNull().isSameAs(protocol);
        }

        // Inner classes.

        public static abstract class BasicProtocol extends Protocol {

            protected BasicProtocol(BasicAgent agent) {
                super(agent);
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
        }

        public static class CorrectConstructorProtocol extends BasicProtocol {

            public CorrectConstructorProtocol(BasicAgent agent) {
                super(agent);
            }
        }

        public static class NoCorrectConstructorProtocol extends BasicProtocol {

            public NoCorrectConstructorProtocol(BasicAgent agent, @SuppressWarnings("unused") String toMuchArgs) {
                super(agent);
            }
        }

        public static class WrongConstructorVisibilityProtocol extends BasicProtocol {

            protected WrongConstructorVisibilityProtocol(BasicAgent agent) {
                super(agent);
            }
        }

        public static class ThrowExceptionConstructorProtocol extends BasicProtocol {

            public ThrowExceptionConstructorProtocol(BasicAgent agent) {
                super(agent);
                throw new RuntimeException();
            }
        }
    }

    @Nested
    @DisplayName("BasicAgent addBehavior")
    @Tag("addBehavior")
    class AddBehavior {

        @Test
        @DisplayName("addBehavior() with non correct Behavior classes throws FailToAddBehaviorException")
        void nonCorrectBehaviorClasses(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);

            assertThrows(FailToAddBehaviorException.class, () -> basicAgent.addBehavior(NoCorrectConstructorBehavior.class));
            assertThrows(FailToAddBehaviorException.class, () -> basicAgent.addBehavior(WrongConstructorVisibilityBehavior.class));
            assertThrows(FailToAddBehaviorException.class, () -> basicAgent.addBehavior(ThrowExceptionConstructorBehavior.class));
        }

        @Test
        @DisplayName("addBehavior() does not throw exception with correct Behavior class and add the Behavior")
        void withCorrectBehaviorClass(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);

            assertThat(basicAgent.hasBehavior(CorrectConstructorBehavior.class)).isFalse();
            assertThat(basicAgent.getBehavior(CorrectConstructorBehavior.class)).isNull();
            assertDoesNotThrow(() -> basicAgent.addBehavior(CorrectConstructorBehavior.class));
            assertThat(basicAgent.hasBehavior(CorrectConstructorBehavior.class)).isTrue();
            assertThat(basicAgent.getBehavior(CorrectConstructorBehavior.class)).isNotNull();
        }

        @Test
        @DisplayName("addBehavior() with correct Behavior does not throw exception for already added behavior and keep the previous instance of " +
                "behavior")
        void addTheSameProtocol(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.addBehavior(CorrectConstructorBehavior.class);

            Behavior behavior = basicAgent.getBehavior(CorrectConstructorBehavior.class);

            assertDoesNotThrow(() -> basicAgent.addBehavior(CorrectConstructorBehavior.class));
            assertThat(basicAgent.hasBehavior(CorrectConstructorBehavior.class)).isTrue();
            assertThat(basicAgent.getBehavior(CorrectConstructorBehavior.class)).isNotNull().isSameAs(behavior);
        }

        // Inner class

        public static abstract class BasicBehavior extends Behavior {

            protected BasicBehavior(BasicAgent agent) {
                super(agent);
            }

            @Override
            protected void beginToBePlayed() {
                // Nothing
            }

            @Override
            protected void stopToBePlayed() {
                // Nothing
            }
        }

        public static class CorrectConstructorBehavior extends BasicBehavior {

            public CorrectConstructorBehavior(BasicAgent agent) {
                super(agent);
            }
        }

        public static class NoCorrectConstructorBehavior extends BasicBehavior {

            public NoCorrectConstructorBehavior(BasicAgent agent, @SuppressWarnings("unused") String toMuchArgs) {
                super(agent);
            }
        }

        public static class WrongConstructorVisibilityBehavior extends BasicBehavior {

            protected WrongConstructorVisibilityBehavior(BasicAgent agent) {
                super(agent);
            }
        }

        public static class ThrowExceptionConstructorBehavior extends BasicBehavior {

            public ThrowExceptionConstructorBehavior(BasicAgent agent) {
                super(agent);
                throw new RuntimeException();
            }
        }
    }

    @Nested
    @DisplayName("BasicAgent playBehavior()")
    @Tag("playBehavior")
    class PlayBehavior {

        @Test
        @DisplayName("playBehavior() throws NullPointerException if the BasicAgent does not have the Behavior")
        void notAddedBehavior(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);

            assertThrows(NullPointerException.class, () -> basicAgent.playBehavior(AddBehavior.CorrectConstructorBehavior.class));
        }

        @Test
        @DisplayName("playBehavior() does not throws exception if the Behavior has been previously added in the BasicAgent")
        void addBehavior(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.addBehavior(AddBehavior.CorrectConstructorBehavior.class);

            assertDoesNotThrow(() -> basicAgent.playBehavior(AddBehavior.CorrectConstructorBehavior.class));
        }
    }

    @Nested
    @DisplayName("BasicAgent stopPlayBehavior()")
    @Tag("stopPlayBehavior")
    class StopPlayBehavior {

        @Test
        @DisplayName("stopPlayBehavior() throws NullPointerException if the BasicAgent does not have the Behavior")
        void notAddedBehavior(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);

            assertThrows(NullPointerException.class, () -> basicAgent.stopPlayBehavior(AddBehavior.CorrectConstructorBehavior.class));
        }

        @Test
        @DisplayName("stopPlayBehavior() does not throws exception if the Behavior has been previously added in the BasicAgent")
        void addBehavior(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);
            basicAgent.addBehavior(AddBehavior.CorrectConstructorBehavior.class);

            assertDoesNotThrow(() -> basicAgent.stopPlayBehavior(AddBehavior.CorrectConstructorBehavior.class));
        }
    }

    @Nested
    @DisplayName("BasicAgent toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() does not returns null value")
        void testToString(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);

            assertThat(basicAgent.toString()).isNotNull();
        }

    }

    @Nested
    @DisplayName("BasicAgent getEnvironment()")
    @Tag("getEnvironment")
    class GetEnvironment {

        @Test
        @DisplayName("getEnvironment() never returns null")
        void neverReturnsNull(@Mock BasicAgent.AgentIdentifier identifier, @Mock Environment environment) {
            BasicAgent basicAgent = new BasicAgent(identifier, environment);

            assertThat(basicAgent.getEnvironment()).isNotNull();
        }
    }
}
