package network;

import agent.SimpleAgent;
import common.Context;
import common.SimpleContext;
import environment.Environment;
import event.Event;
import junit.PalmBeachSimulationTest;
import junit.PalmBeachTest;
import lombok.Getter;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import simulation.PalmBeachSimulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@Nested
@DisplayName("FullyConnectedNetwork tests")
@Tag("FullyConnectedNetwork")
@PalmBeachTest
public class FullyConnectedNetworkTest {

    @Nested
    @DisplayName("FullyConnectedNetwork hasConnection()")
    @Tag("hasConnection")
    class HasConnection {

        @Test
        @DisplayName("hasConnection() returns true if both agent are evolving in Environment")
        void withEvolvingAgents(@Mock Environment environment, @Mock SimpleAgent.AgentIdentifier a0, @Mock SimpleAgent.AgentIdentifier a1) {
            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, null);
            when(environment.agentIsEvolving(a0)).thenReturn(true);
            when(environment.agentIsEvolving(a1)).thenReturn(true);

            assertThat(network.hasConnection(a0, a1)).isTrue();
            assertThat(network.hasConnection(a1, a0)).isTrue();
        }

        @Test
        @DisplayName("hasConnection() returns false with one or more agent not evolving in Environment")
        void withNotEvolvingInEnvironment(@Mock Environment environment, @Mock SimpleAgent.AgentIdentifier a0, @Mock SimpleAgent.AgentIdentifier a1) {
            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, null);
            when(environment.agentIsEvolving(a0)).thenReturn(true);
            when(environment.agentIsEvolving(a1)).thenReturn(false);

            assertThat(network.hasConnection(a0, a1)).isFalse();
            assertThat(network.hasConnection(a1, a0)).isFalse();

            when(environment.agentIsEvolving(a0)).thenReturn(false);
            when(environment.agentIsEvolving(a1)).thenReturn(true);

            assertThat(network.hasConnection(a0, a1)).isFalse();
            assertThat(network.hasConnection(a1, a0)).isFalse();

            when(environment.agentIsEvolving(a0)).thenReturn(false);
            when(environment.agentIsEvolving(a1)).thenReturn(false);

            assertThat(network.hasConnection(a0, a1)).isFalse();
            assertThat(network.hasConnection(a1, a0)).isFalse();
        }
    }

    @Nested
    @DisplayName("FullyConnectedNetwork agentDirectConnections()")
    @Tag("agentDirectConnections")
    class AgentDirectConnections {

        @Test
        @DisplayName("agentDirectConnections() returns all agent in the Network Environment")
        void returnsAllAgents(@Mock SimpleAgent.AgentIdentifier a0, @Mock SimpleAgent.AgentIdentifier a1) {
            Environment env = new Environment("envName", null);
            env.addAgent(a0);
            env.addAgent(a1);

            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", env, null);

            assertThat(network.agentDirectConnections(a0)).containsAll(env.evolvingAgents());
            assertThat(network.agentDirectConnections(a1)).containsAll(env.evolvingAgents());
        }
    }

    @Nested
    @DisplayName("FullyConnectedNetwork send()")
    @Tag("send")
    @PalmBeachSimulationTest
    class Send {

        @Test
        @DisplayName("send() throws IllegalArgumentException if minDelay is greater or equal to maxDelay - 1")
        void wrongDelay(@Mock SimpleAgent.AgentIdentifier i0, @Mock SimpleAgent.AgentIdentifier i1,
                        @Mock Event<?> event) {
            SimpleAgent a0 = new SimpleAgent(i0, null);
            SimpleAgent a1 = new SimpleAgent(i1, null);

            Environment environment = new Environment("envName", null);
            prepareAgentInSimulation(environment, a0, i0, a1, i1);

            Context context = new SimpleContext();
            context.map(FullyConnectedNetwork.MIN_SENDING_DELAY, 500L);
            context.map(FullyConnectedNetwork.MAX_SENDING_DELAY, 15L);

            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, context);

            assertThrows(IllegalArgumentException.class, () -> network.send(i0, i1, event));
        }

        @Test
        @DisplayName("send() does not throws exception and schedule the process Event at the specified time")
        void sendIsCorrect(@Mock SimpleAgent.AgentIdentifier i0, @Mock SimpleAgent.AgentIdentifier i1, @Mock Event<?> event)
                throws InterruptedException {
            SimpleAgent a0 = new SimpleAgent(i0, null);

            Environment environment = new Environment("envName", null);
            Agent a1 = new Agent(i1, null);

            prepareAgentInSimulation(environment, a0, i0, a1, i1);

            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, null);

            network.send(i0, i1, event);
            PalmBeachSimulation.start();

            waitSimulationEnd();

            assertThat(a1.getExecutionTime()).isBetween(network.minDelay(), network.maxDelay());
        }

        private void prepareAgentInSimulation(Environment environment, SimpleAgent a0, SimpleAgent.AgentIdentifier i0, SimpleAgent a1,
                                              SimpleAgent.AgentIdentifier i1) {
            environment.addAgent(i0);
            environment.addAgent(i1);
            PalmBeachSimulation.addEnvironment(environment);

            PalmBeachSimulation.addAgent(a0);
            PalmBeachSimulation.addAgent(a1);

            a0.start();
            a1.start();
        }

        private void waitSimulationEnd() throws InterruptedException {
            int counter = 0;
            while (!PalmBeachSimulation.isEnded()) {
                PalmBeachSimulation.waitSimulationEnd(500L);
                counter++;
                if (counter > 5)
                    fail("Too much wait End Simulation");
            }
        }

        // Inner classes.

        private static class Agent extends SimpleAgent {

            @Getter
            private long executionTime = 0L;

            public Agent(@NonNull AgentIdentifier identifier, Context context) {
                super(identifier, context);
            }

            @Override
            protected void inProcessEvent(Event<?> event) {
                executionTime = PalmBeachSimulation.scheduler().getCurrentTime();
            }
        }
    }

    @Nested
    @DisplayName("FullyConnectedNetwork minDelay()")
    @Tag("minDelay")
    class MinDelay {

        @Test
        @DisplayName("minDelay() returns DEFAULT_MIN_DELAY if no min delay has been set in context")
        void withNoContext(@Mock Environment environment) {
            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, null);

            assertThat(network.minDelay()).isEqualTo(FullyConnectedNetwork.DEFAULT_MIN_DELAY);
        }

        @Test
        @DisplayName("minDelay() throws IllegalArgumentException if the minDelay set in context is less than 1")
        void withWrongValue(@Mock Environment environment) {
            Context context = new SimpleContext();
            context.map(FullyConnectedNetwork.MIN_SENDING_DELAY, -1L);

            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, context);

            assertThrows(IllegalArgumentException.class, network::minDelay);
        }

        @Test
        @DisplayName("minDelay() returns the value set in the context")
        void withContext(@Mock Environment environment) {
            long minDelay = 188L;
            Context context = new SimpleContext();
            context.map(FullyConnectedNetwork.MIN_SENDING_DELAY, minDelay);

            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, context);

            assertThat(network.minDelay()).isEqualByComparingTo(minDelay);
        }

        @Test
        @DisplayName("minDelay(int) throws IllegalArgumentException with minDelay less than 1")
        void withWrongSetMinDelay(@Mock Environment environment) {
            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, null);

            assertThrows(IllegalArgumentException.class, () -> network.minDelay(-1));
        }

        @Test
        @DisplayName("minDelay(int) set the new value of minDelay")
        void withCorrectValueSet(@Mock Environment environment) {
            long minDelay = 188L;
            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, null);

            network.minDelay(minDelay);
            assertThat(network.minDelay()).isEqualByComparingTo(minDelay);
        }
    }

    @Nested
    @DisplayName("FullyConnectedNetwork maxDelay()")
    @Tag("maxDelay")
    class MaxDelay {

        @Test
        @DisplayName("maxDelay() returns DEFAULT_MAX_DELAY if no max delay has been set in context")
        void withNoContext(@Mock Environment environment) {
            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, null);

            assertThat(network.maxDelay()).isEqualTo(FullyConnectedNetwork.DEFAULT_MAX_DELAY);
        }

        @Test
        @DisplayName("maxDelay() throws IllegalArgumentException if the maxDelay set in context is less than 1")
        void withWrongValue(@Mock Environment environment) {
            Context context = new SimpleContext();
            context.map(FullyConnectedNetwork.MAX_SENDING_DELAY, -1L);

            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, context);

            assertThrows(IllegalArgumentException.class, network::maxDelay);
        }

        @Test
        @DisplayName("maxDelay() returns the value set in the context")
        void withContext(@Mock Environment environment) {
            long maxDelay = 999L;
            Context context = new SimpleContext();
            context.map(FullyConnectedNetwork.MAX_SENDING_DELAY, maxDelay);

            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, context);

            assertThat(network.maxDelay()).isEqualByComparingTo(maxDelay);
        }

        @Test
        @DisplayName("maxDelay(int) throws IllegalArgumentException with maxDelay less than 1")
        void withWrongSetMaxDelay(@Mock Environment environment) {
            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, null);

            assertThrows(IllegalArgumentException.class, () -> network.maxDelay(-1));
        }

        @Test
        @DisplayName("maxDelay(int) set the new value of maxDelay")
        void withCorrectValueSet(@Mock Environment environment) {
            long maxDelay = 999L;
            FullyConnectedNetwork network = new FullyConnectedNetwork("FullyConnectedNetwork", environment, null);

            network.maxDelay(maxDelay);
            assertThat(network.maxDelay()).isEqualByComparingTo(maxDelay);
        }
    }
}
