package org.paradise.palmbeach.basic.messaging;

import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.agent.exception.AgentNotStartedException;
import org.paradise.palmbeach.core.environment.Environment;
import org.paradise.palmbeach.core.environment.network.Network;
import org.paradise.palmbeach.core.junit.PalmBeachSimulationTest;
import org.paradise.palmbeach.core.junit.PalmBeachTest;
import org.paradise.palmbeach.basic.network.FullyConnectedNetwork;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.paradise.palmbeach.core.scheduler.Scheduler;
import org.paradise.palmbeach.core.scheduler.exception.ForcedWakeUpException;
import org.paradise.palmbeach.core.simulation.PalmBeachSimulation;
import org.paradise.palmbeach.basic.test_tools.SupplierExecutable;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import static org.paradise.palmbeach.core.junit.PalmBeachSimulationTestExtension.waitSimulationEnd;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Nested
@DisplayName("SimpleMessageSender tests")
@Tag("SimpleMessageSender")
@PalmBeachTest
public class SimpleMessengerTest {

    @Nested
    @DisplayName("SimpleMessageSender sendMessage()")
    @Tag("sendMessage")
    @PalmBeachSimulationTest
    class SendMessage {

        @Test
        @DisplayName("sendMessage() throws AgentNotStartedException if the Agent is not in STARTED state")
        void withNotStartedAgent(@Mock SimpleAgent agent, @Mock Message<? extends Serializable> message, @Mock SimpleAgent.AgentIdentifier target,
                                 @Mock Network network) {
            when(agent.getIdentifier()).thenReturn(target);
            when(agent.isStarted()).thenReturn(false);
            PalmBeachSimulation.addAgent(agent);

            SimpleMessenger messageSender = new SimpleMessenger(agent, null);
            assertThrows(AgentNotStartedException.class, () -> messageSender.sendMessage(message, target, network));
        }

        @Test
        @DisplayName("sendMessage() send the message if in the Network, agents are connected")
        void withConnectedAgent(@Mock SimpleAgent.AgentIdentifier i0, @Mock SimpleAgent.AgentIdentifier i1)
                throws InterruptedException, ForcedWakeUpException {
            SimpleAgent a0 = new SimpleAgent(i0, null);
            SimpleAgent a1 = new SimpleAgent(i1, null);

            SimpleMessenger s0 = new SimpleMessenger(a0, null);
            a0.addProtocol(s0);

            SimpleMessenger s1 = new SimpleMessenger(a1, null);
            a1.addProtocol(s1);

            PalmBeachSimulation.addAgent(a0);
            PalmBeachSimulation.addAgent(a1);

            Environment env = new Environment("envName", null);
            env.addAgent(i0);
            env.addAgent(i1);
            PalmBeachSimulation.addEnvironment(env);

            FullyConnectedNetwork network = new FullyConnectedNetwork("fNetwork", env, null);
            env.addNetwork(network);

            a0.start();
            a1.start();

            Message<String> mString = new Message<>(i0, "msg");
            s0.sendMessage(mString, i1, network);

            PalmBeachSimulation.start();

            waitSimulationEnd();

            assertThat(s1.hasContent()).isTrue();
            assertThat(s1.nextContent()).isNotNull().isSameAs(mString);
        }
    }

    @Nested
    @DisplayName("SimpleMessageSender nextMessage()")
    @Tag("nextMessage")
    @PalmBeachSimulationTest
    class NextMessage {

        @Test
        @DisplayName("nextMessage() block until a message is received and returns the received message")
        void blockUntilMessageReception(@Mock SimpleAgent.AgentIdentifier i0, @Mock SimpleAgent.AgentIdentifier i1) throws InterruptedException {
            SimpleAgent a0 = new SimpleAgent(i0, null);
            SimpleAgent a1 = new SimpleAgent(i1, null);

            SimpleMessenger s0 = new SimpleMessenger(a0, null);
            a0.addProtocol(s0);

            SimpleMessenger s1 = new SimpleMessenger(a1, null);
            a1.addProtocol(s1);

            PalmBeachSimulation.addAgent(a0);
            PalmBeachSimulation.addAgent(a1);

            Environment env = new Environment("envName", null);
            env.addAgent(i0);
            env.addAgent(i1);
            PalmBeachSimulation.addEnvironment(env);

            final FullyConnectedNetwork network = new FullyConnectedNetwork("fNetwork", env, null);
            env.addNetwork(network);

            a0.start();
            a1.start();

            final Message<String> mString = new Message<>(i0, "msg");
            final AtomicReference<Message<String>> receivedMsg = new AtomicReference<>();
            //noinspection unchecked
            SupplierExecutable waitMessageReception = new SupplierExecutable(() -> receivedMsg.set((Message<String>) s1.nextContent()));
            PalmBeachSimulation.scheduler().scheduleOnce(waitMessageReception, Scheduler.NEXT_STEP);
            PalmBeachSimulation.scheduler().scheduleOnce(() -> s0.sendMessage(mString, i1, network), Scheduler.NEXT_STEP + 50L);

            PalmBeachSimulation.start();

            waitSimulationEnd();

            assertThat(waitMessageReception.isExecuted()).isTrue();
            assertThat(receivedMsg.get()).isNotNull().isSameAs(mString);
        }
    }


    // Inner classes.
}
