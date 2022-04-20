package messaging;

import agent.SimpleAgent;
import agent.exception.AgentNotStartedException;
import environment.Environment;
import environment.network.Network;
import junit.PalmBeachSimulationTest;
import junit.PalmBeachTest;
import network.FullyConnectedNetwork;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import scheduler.Scheduler;
import scheduler.exception.ForcedWakeUpException;
import simulation.PalmBeachSimulation;
import test_tools.SupplierExecutable;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import static junit.PalmBeachSimulationTestExtension.waitSimulationEnd;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("SimpleMessageSender tests")
@Tag("SimpleMessageSender")
@PalmBeachTest
public class SimpleMessageSenderTest {

    @Nested
    @DisplayName("SimpleMessageSender sendMessage()")
    @Tag("sendMessage")
    @PalmBeachSimulationTest
    class SendMessage {

        @Test
        @DisplayName("sendMessage() throws AgentNotStartedException if the Agent is not in STARTED state")
        void withNotStartedAgent(@Mock SimpleAgent agent, @Mock Message<? extends Serializable> message, @Mock SimpleAgent.AgentIdentifier target,
                                 @Mock Network network) {
            SimpleMessageSender messageSender = new SimpleMessageSender(agent, null);
            assertThrows(AgentNotStartedException.class, () -> messageSender.sendMessage(message, target, network));
        }

        @Test
        @DisplayName("sendMessage() send the message if in the Network, agents are connected")
        void withConnectedAgent(@Mock SimpleAgent.AgentIdentifier i0, @Mock SimpleAgent.AgentIdentifier i1)
                throws InterruptedException, ForcedWakeUpException {
            SimpleAgent a0 = new SimpleAgent(i0, null);
            SimpleAgent a1 = new SimpleAgent(i1, null);

            SimpleMessageSender s0 = new SimpleMessageSender(a0, null);
            a0.addProtocol(s0);

            SimpleMessageSender s1 = new SimpleMessageSender(a1, null);
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

            assertThat(s1.hasMessage()).isTrue();
            assertThat(s1.nextMessage()).isNotNull().isSameAs(mString);
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

            SimpleMessageSender s0 = new SimpleMessageSender(a0, null);
            a0.addProtocol(s0);

            SimpleMessageSender s1 = new SimpleMessageSender(a1, null);
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
            SupplierExecutable waitMessageReception = new SupplierExecutable(() -> receivedMsg.set((Message<String>) s1.nextMessage()));
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
