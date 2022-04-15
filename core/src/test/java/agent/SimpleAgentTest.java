package agent;

import agent.behavior.Behavior;
import agent.behavior.BehaviorTest;
import agent.exception.*;
import agent.protocol.ProtocolTest;
import common.Context;
import environment.Environment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import agent.protocol.Protocol;
import event.Event;
import junit.PalmBeachTest;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Nested
@DisplayName("SimpleAgent tests")
@Tag("SimpleAgent")
@PalmBeachTest
public class SimpleAgentTest {

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
                    boolean added = set.add(SimpleAgent.SimpleAgentIdentifier.nextId());
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
                SimpleAgent.SimpleAgentIdentifier identifier =
                        new SimpleAgent.SimpleAgentIdentifier("TEST", SimpleAgent.SimpleAgentIdentifier.nextId());
                Object other = new Object();

                boolean res = identifier.equals(other);
                assertThat(res).isFalse();
            }

            @SuppressWarnings({"EqualsWithItself", "ConstantConditions"})
            @Test
            @DisplayName("Equals returns true with same instance")
            void sameInstance() {
                SimpleAgent.SimpleAgentIdentifier identifier =
                        new SimpleAgent.SimpleAgentIdentifier("TEST", SimpleAgent.SimpleAgentIdentifier.nextId());

                boolean res = identifier.equals(identifier);
                assertThat(res).isTrue();
            }

            @Test
            @DisplayName("Equals returns true with different instances and equal values")
            void equalValues() {
                SimpleAgent.SimpleAgentIdentifier i0 = new SimpleAgent.SimpleAgentIdentifier("TEST", 0L);
                SimpleAgent.SimpleAgentIdentifier i1 = new SimpleAgent.SimpleAgentIdentifier("TEST", 0L);

                boolean res = i0.equals(i1);
                assertThat(res).isTrue();
            }

            @Test
            @DisplayName("Equals returns false with different instances and different values")
            void differentValues() {
                SimpleAgent.SimpleAgentIdentifier i0 = new SimpleAgent.SimpleAgentIdentifier("TEST", 0L);
                SimpleAgent.SimpleAgentIdentifier i1 = new SimpleAgent.SimpleAgentIdentifier("TEST", 1L);
                SimpleAgent.SimpleAgentIdentifier i2 = new SimpleAgent.SimpleAgentIdentifier("TEST1", 0L);
                SimpleAgent.SimpleAgentIdentifier i3 = new SimpleAgent.SimpleAgentIdentifier("TEST2", 1L);

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
                SimpleAgent.SimpleAgentIdentifier i = new SimpleAgent.SimpleAgentIdentifier("TEST", 0L);

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
                SimpleAgent.SimpleAgentIdentifier i = new SimpleAgent.SimpleAgentIdentifier("TEST", 0L);

                assertThat(i.toString()).isNotNull();
            }

        }
    }

    @Nested
    @DisplayName("SimpleAgent constructor")
    @Tag("constructor")
    class Constructor {

        @SuppressWarnings("ConstantConditions")
        @Nested
        @DisplayName("SimpleAgent(AgentIdentifier, Context)")
        class MainConstructor {

            @Test
            @DisplayName("Throws NullPointerException if null identifier")
            void nullIdentifier(@Mock Context context) {
                assertThrows(NullPointerException.class, () -> new SimpleAgent(null, context));
            }
            @Test
            @DisplayName("Does not throw exception with null context and not null context")
            void nullContext(@Mock SimpleAgent.AgentIdentifier identifier, @Mock Context context) {
                assertDoesNotThrow(() -> new SimpleAgent(identifier, null));
                assertDoesNotThrow(() -> new SimpleAgent(identifier, context));
            }

            @Test
            @DisplayName("Constructor set the agent state to CREATED")
            void setCorrectState(@Mock SimpleAgent.AgentIdentifier identifier, @Mock Context context) {
                SimpleAgent simpleAgent = new SimpleAgent(identifier, context);

                assertThat(simpleAgent.getState()).isEqualTo(SimpleAgent.AgentState.CREATED);
            }

        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nested
    @DisplayName("SimpleAgent addObserver()")
    @Tag("addObserver")
    class AddObserver {

        @Test
        @DisplayName("Throws NullPointerException if observer is null")
        void withNullObserver(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            assertThrows(NullPointerException.class, () -> simpleAgent.addObserver(null));
        }

        @Test
        @DisplayName("Does not throw exception if observer is not null")
        void withNotNullObserver(@Mock SimpleAgent.AgentIdentifier identifier, @Mock SimpleAgent.AgentObserver observer) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            assertDoesNotThrow(() -> simpleAgent.addObserver(observer));
        }
    }

    @Nested
    @DisplayName("SimpleAgent start()")
    @Tag("start")
    class Start {

        @Test
        @DisplayName("start() does not throw exception fro created agent and change the state of the agent")
        void startCreatedAgent(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertDoesNotThrow(simpleAgent::start);
            assertThat(simpleAgent.isStarted()).isTrue();
        }

        @Test
        @DisplayName("start() throws AgentCannotBeStartedException for started agent")
        void startStartedAgent(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.start();

            assertThrows(AgentCannotBeStartedException.class, simpleAgent::start);
            assertThat(simpleAgent.isStarted()).isTrue();
        }

        @Test
        @DisplayName("start() does not throw exception for a stopped agent")
        void startStoppedAgent(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.start();

            simpleAgent.stop();

            assertDoesNotThrow(simpleAgent::start);
            assertThat(simpleAgent.isStarted()).isTrue();
        }

        @Test
        @DisplayName("start() throws AgentCannotBeStartedException for killed agent")
        void startKilledAgent(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.kill();

            assertThrows(AgentCannotBeStartedException.class, simpleAgent::start);
            assertThat(simpleAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("start() notify AgentObserver")
        void startNotifyAgentObserver(@Mock SimpleAgent.AgentIdentifier identifier,
                                      @Mock SimpleAgent.AgentObserver observer) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.addObserver(observer);
            simpleAgent.start();

            verify(observer, times(1)).agentStarted();
        }
    }

    @Nested
    @DisplayName("SimpleAgent stop()")
    @Tag("stop")
    class Stop {

        @Test
        @DisplayName("stop() throws AgentCannotBeStoppedException for a created agent")
        void stopCreatedAgent(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertThrows(AgentCannotBeStoppedException.class, simpleAgent::stop);
            assertThat(simpleAgent.getState()).isEqualTo(SimpleAgent.AgentState.CREATED);
        }

        @Test
        @DisplayName("stop() does not throw exception for started agent")
        void stopStartedAgent(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.start();

            assertDoesNotThrow(simpleAgent::stop);
            assertThat(simpleAgent.isStopped()).isTrue();
        }

        @Test
        @DisplayName("stop() throws AgentCannotBeStoppedException for killed agent")
        void stopKilledAgent(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.kill();

            assertThrows(AgentCannotBeStoppedException.class, simpleAgent::stop);
            assertThat(simpleAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("stop() notify AgentObserver")
        void stopNotifyAgentObserver(@Mock SimpleAgent.AgentIdentifier identifier,
                                     @Mock SimpleAgent.AgentObserver observer) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.addObserver(observer);
            simpleAgent.start();
            simpleAgent.stop();

            verify(observer, times(1)).agentStopped();
        }
    }

    @Nested
    @DisplayName("SimpleAgent kill()")
    @Tag("kill")
    class Kill {

        @Test
        @DisplayName("kill() does not throw exception for created agent")
        void killCreatedAgent(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertDoesNotThrow(simpleAgent::kill);
            assertThat(simpleAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("kill() does not throw exception for started agent")
        void killStartedAgent(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.start();

            assertDoesNotThrow(simpleAgent::kill);
            assertThat(simpleAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("kill() does not throw exception for stopped agent")
        void killStoppedAgent(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.start();
            simpleAgent.stop();

            assertDoesNotThrow(simpleAgent::kill);
            assertThat(simpleAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("kill() throws AgentCannotBeKilledException for killed agent")
        void killKilledAgent(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.kill();

            assertThrows(AgentCannotBeKilledException.class, simpleAgent::kill);
            assertThat(simpleAgent.isKilled()).isTrue();
        }

        @Test
        @DisplayName("kill() notify AgentObserver")
        void killNotifyAgentObserver(@Mock SimpleAgent.AgentIdentifier identifier,
                                     @Mock SimpleAgent.AgentObserver observer) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.addObserver(observer);
            simpleAgent.kill();

            verify(observer, times(1)).agentKilled();
        }
    }

    @Nested
    @DisplayName("SimpleAgent addProtocol()")
    @Tag("addProtocol")
    class AddProtocol {

        @Test
        @DisplayName("addProtocol() with non correct Protocol class throws FailToAddProtocolException")
        void nonCorrectProtocolClasses(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertThrows(FailToAddProtocolException.class, () -> simpleAgent.addProtocol(ProtocolTest.NoCorrectConstructorProtocol.class));
            assertThrows(FailToAddProtocolException.class, () -> simpleAgent.addProtocol(ProtocolTest.WrongConstructorVisibilityProtocol.class));
            assertThrows(FailToAddProtocolException.class, () -> simpleAgent.addProtocol(ProtocolTest.ThrowExceptionConstructorProtocol.class));
        }

        @Test
        @DisplayName("addProtocol() with correct Protocol class does not throw exception and add the agent.protocol")
        void withCorrectProtocolClass(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertThat(simpleAgent.hasProtocol(ProtocolTest.BasicProtocol.class)).isFalse();
            assertThat(simpleAgent.getProtocol(ProtocolTest.BasicProtocol.class)).isNull();
            assertDoesNotThrow(() -> simpleAgent.addProtocol(ProtocolTest.BasicProtocol.class));
            assertThat(simpleAgent.hasProtocol(ProtocolTest.BasicProtocol.class)).isTrue();
            assertThat(simpleAgent.getProtocol(ProtocolTest.BasicProtocol.class)).isNotNull();
        }

        @Test
        @DisplayName("addProtocol() with correct Protocol does not throw exception for already added agent.protocol and keep the previous instance of " +
                "protocol")
        void addTheSameProtocol(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.addProtocol(ProtocolTest.BasicProtocol.class);

            Protocol protocol = simpleAgent.getProtocol(ProtocolTest.BasicProtocol.class);

            assertDoesNotThrow(() -> simpleAgent.addProtocol(ProtocolTest.BasicProtocol.class));
            assertThat(simpleAgent.hasProtocol(ProtocolTest.BasicProtocol.class)).isTrue();
            assertThat(simpleAgent.getProtocol(ProtocolTest.BasicProtocol.class)).isNotNull().isSameAs(protocol);
        }
    }

    @Nested
    @DisplayName("SimpleAgent addBehavior")
    @Tag("addBehavior")
    class AddBehavior {

        @Test
        @DisplayName("addBehavior() with non correct Behavior classes throws FailToAddBehaviorException")
        void nonCorrectBehaviorClasses(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertThrows(FailToAddBehaviorException.class, () -> simpleAgent.addBehavior(BehaviorTest.NoCorrectConstructorBehavior.class));
            assertThrows(FailToAddBehaviorException.class, () -> simpleAgent.addBehavior(BehaviorTest.WrongConstructorVisibilityBehavior.class));
            assertThrows(FailToAddBehaviorException.class, () -> simpleAgent.addBehavior(BehaviorTest.ThrowExceptionConstructorBehavior.class));
        }

        @Test
        @DisplayName("addBehavior() does not throw exception with correct Behavior class and add the Behavior")
        void withCorrectBehaviorClass(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertThat(simpleAgent.hasBehavior(BehaviorTest.CorrectConstructorBehavior.class)).isFalse();
            assertThat(simpleAgent.getBehavior(BehaviorTest.CorrectConstructorBehavior.class)).isNull();
            assertDoesNotThrow(() -> simpleAgent.addBehavior(BehaviorTest.CorrectConstructorBehavior.class));
            assertThat(simpleAgent.hasBehavior(BehaviorTest.CorrectConstructorBehavior.class)).isTrue();
            assertThat(simpleAgent.getBehavior(BehaviorTest.CorrectConstructorBehavior.class)).isNotNull();
        }

        @Test
        @DisplayName("addBehavior() with correct Behavior does not throw exception for already added agent.behavior and keep the previous instance of " +
                "behavior")
        void addTheSameProtocol(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.addBehavior(BehaviorTest.CorrectConstructorBehavior.class);

            Behavior behavior = simpleAgent.getBehavior(BehaviorTest.CorrectConstructorBehavior.class);

            assertDoesNotThrow(() -> simpleAgent.addBehavior(BehaviorTest.CorrectConstructorBehavior.class));
            assertThat(simpleAgent.hasBehavior(BehaviorTest.CorrectConstructorBehavior.class)).isTrue();
            assertThat(simpleAgent.getBehavior(BehaviorTest.CorrectConstructorBehavior.class)).isNotNull().isSameAs(behavior);
        }
    }

    @Nested
    @DisplayName("SimpleAgent playBehavior()")
    @Tag("playBehavior")
    class PlayBehavior {

        @Test
        @DisplayName("playBehavior() throws NullPointerException if the SimpleAgent does not have the Behavior")
        void notAddedBehavior(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertThrows(NullPointerException.class, () -> simpleAgent.playBehavior(BehaviorTest.CorrectConstructorBehavior.class));
        }

        @Test
        @DisplayName("playBehavior() does not throws exception if the Behavior has been previously added in the SimpleAgent")
        void addBehavior(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.addBehavior(BehaviorTest.CorrectConstructorBehavior.class);

            assertDoesNotThrow(() -> simpleAgent.playBehavior(BehaviorTest.CorrectConstructorBehavior.class));
        }
    }

    @Nested
    @DisplayName("SimpleAgent stopPlayBehavior()")
    @Tag("stopPlayBehavior")
    class StopPlayBehavior {

        @Test
        @DisplayName("stopPlayBehavior() throws NullPointerException if the SimpleAgent does not have the Behavior")
        void notAddedBehavior(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertThrows(NullPointerException.class, () -> simpleAgent.stopPlayBehavior(BehaviorTest.CorrectConstructorBehavior.class));
        }

        @Test
        @DisplayName("stopPlayBehavior() does not throws exception if the Behavior has been previously added in the SimpleAgent")
        void addBehavior(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.addBehavior(BehaviorTest.CorrectConstructorBehavior.class);

            assertDoesNotThrow(() -> simpleAgent.stopPlayBehavior(BehaviorTest.CorrectConstructorBehavior.class));
        }
    }

    @Nested
    @DisplayName("SimpleAgent toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() does not returns null value")
        void testToString(@Mock SimpleAgent.AgentIdentifier identifier) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertThat(simpleAgent.toString()).isNotNull();
        }

    }

    @Nested
    @DisplayName("SimpleAgent processEvent()")
    @Tag("processEvent")
    class ProcessEvent {

        @Test
        @DisplayName("processEvent() throws AgentNotStartedException if the agent is not started")
        void notStartedAgent(@Mock SimpleAgent.AgentIdentifier identifier, @Mock Event<?> event) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertThrows(AgentNotStartedException.class, () -> simpleAgent.processEvent(event));
        }

        @Test
        @DisplayName("processEvent() throws AgentCannotProcessEventException if the agent has no agent.protocol which can process the Event")
        void noProtocolCanProcessEvent(@Mock SimpleAgent.AgentIdentifier identifier, @Mock Event<?> event) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.start();

            assertThrows(AgentCannotProcessEventException.class, () -> simpleAgent.processEvent(event));
        }

        @Test
        @DisplayName("processEvent() does not throw exception if there is at least one agent.protocol which can process the event and the agent.protocol " +
                "process the event")
        void withProtocolCanProcessEvent(@Mock SimpleAgent.AgentIdentifier identifier, @Mock Event<?> event) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.start();
            simpleAgent.addProtocol(ProtocolTest.BasicProtocol.class);

            assertDoesNotThrow(() -> simpleAgent.processEvent(event));
            ProtocolTest.BasicProtocol protocol = simpleAgent.getProtocol(ProtocolTest.BasicProtocol.class);
            assertThat(protocol.getProcessEventCounter()).isEqualByComparingTo(1);
        }
    }

    @Nested
    @DisplayName("SimpleAgent canProcessEvent()")
    @Tag("canProcessEvent")
    class CanProcessEvent {

        @Test
        @DisplayName("canProcessEvent() returns false if the agent does not have agent.protocol")
        void agentWithoutProtocol(@Mock SimpleAgent.AgentIdentifier identifier, @Mock Event<?> event) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);

            assertThat(simpleAgent.canProcessEvent(event)).isFalse();
        }

        @Test
        @DisplayName("canProcessEvent() returns true if at least one agent.protocol can process the event")
        void agentWithOneProtocol(@Mock SimpleAgent.AgentIdentifier identifier, @Mock Event<?> event) {
            SimpleAgent simpleAgent = new SimpleAgent(identifier, null);
            simpleAgent.addProtocol(ProtocolTest.BasicProtocol.class);

            assertThat(simpleAgent.canProcessEvent(event)).isTrue();
        }
    }
}
