package messaging.broadcasting;

import agent.SimpleAgent;
import agent.exception.AgentNotStartedException;
import environment.Environment;
import environment.network.Network;
import junit.PalmBeachSimulationTest;
import junit.PalmBeachTest;
import messaging.Message;
import messaging.SimpleMessenger;
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
@DisplayName("BestEffortBroadcast tests")
@Tag("BestEffortBroadcast")
@PalmBeachTest
public class BestEffortBroadcastTest {

    @Nested
    @DisplayName("BestEffortBroadcast sendMessage()")
    @Tag("sendMessage")
    class SendMessage {

        @Test
        @DisplayName("sendMessage() throws UnsupportedOperationException")
        void unsupportedOperation(@Mock SimpleAgent agent, @Mock Message<? extends Serializable> message, @Mock SimpleAgent.AgentIdentifier target,
                                  @Mock Network network) {
            BestEffortBroadcast broadcast = new BestEffortBroadcast(agent, null);
            assertThrows(UnsupportedOperationException.class, () -> broadcast.sendMessage(message, target, network));
        }
    }

    @Nested
    @DisplayName("BestEffortBroadcast broadcastMessage()")
    @Tag("broadcastMessage")
    @PalmBeachSimulationTest
    class BroadcastMessage {

        @Test
        @DisplayName("broadcastMessage() throws AgentNotStartedException if the Agent is not in STARTED state")
        void withNotStartedAgent(@Mock SimpleAgent agent, @Mock Message<? extends Serializable> message, @Mock Network network) {
            BestEffortBroadcast broadcast = new BestEffortBroadcast(agent, null);
            assertThrows(AgentNotStartedException.class, () -> broadcast.broadcastMessage(message, network));
        }

        @Test
        @DisplayName("broadcastMessage() send the message if in the Network, agents are connected")
        void withConnectedAgent(@Mock SimpleAgent.AgentIdentifier i0, @Mock SimpleAgent.AgentIdentifier i1, @Mock SimpleAgent.AgentIdentifier i2)
                throws InterruptedException, ForcedWakeUpException {
            SimpleAgent a0 = new SimpleAgent(i0, null);
            SimpleAgent a1 = new SimpleAgent(i1, null);
            SimpleAgent a2 = new SimpleAgent(i2, null);

            SimpleMessenger m0 = new SimpleMessenger(a0, null);
            BestEffortBroadcast b0 = new BestEffortBroadcast(a0, null);
            b0.setMessenger(m0);
            a0.addProtocol(m0);
            a0.addProtocol(b0);

            SimpleMessenger m1 = new SimpleMessenger(a1, null);
            BestEffortBroadcast b1 = new BestEffortBroadcast(a1, null);
            b1.setMessenger(m1);
            a1.addProtocol(m1);
            a1.addProtocol(b1);

            SimpleMessenger m2 = new SimpleMessenger(a2, null);
            BestEffortBroadcast b2 = new BestEffortBroadcast(a2, null);
            b2.setMessenger(m2);
            a2.addProtocol(m2);
            a2.addProtocol(b2);

            PalmBeachSimulation.addAgent(a0);
            PalmBeachSimulation.addAgent(a1);
            PalmBeachSimulation.addAgent(a2);

            Environment env = new Environment("envName", null);
            env.addAgent(i0);
            env.addAgent(i1);
            env.addAgent(i2);
            PalmBeachSimulation.addEnvironment(env);

            FullyConnectedNetwork network = new FullyConnectedNetwork("fNetwork", env, null);
            env.addNetwork(network);

            a0.start();
            a1.start();
            a2.start();

            Message<String> mString = new Message<>(i0, "msg");
            b0.broadcastMessage(mString, network);

            PalmBeachSimulation.start();

            waitSimulationEnd();

            assertThat(b0.hasMessage()).isTrue();
            assertThat(b0.nextMessage()).isNotNull().isSameAs(mString);
            assertThat(b1.hasMessage()).isTrue();
            assertThat(b1.nextMessage()).isNotNull().isSameAs(mString);
            assertThat(b2.hasMessage()).isTrue();
            assertThat(b2.nextMessage()).isNotNull().isSameAs(mString);
        }
    }

    @Nested
    @DisplayName("BestEffortBroadcast nextMessage()")
    @Tag("nextMessage")
    @PalmBeachSimulationTest
    class NextMessage {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("nextMessage() block until a message is received and returns the received message")
        void blockUntilMessageReception(@Mock SimpleAgent.AgentIdentifier i0, @Mock SimpleAgent.AgentIdentifier i1,
                                        @Mock SimpleAgent.AgentIdentifier i2) throws InterruptedException {
            SimpleAgent a0 = new SimpleAgent(i0, null);
            SimpleAgent a1 = new SimpleAgent(i1, null);
            SimpleAgent a2 = new SimpleAgent(i2, null);

            SimpleMessenger m0 = new SimpleMessenger(a0, null);
            BestEffortBroadcast b0 = new BestEffortBroadcast(a0, null);
            b0.setMessenger(m0);
            a0.addProtocol(m0);
            a0.addProtocol(b0);

            SimpleMessenger m1 = new SimpleMessenger(a1, null);
            BestEffortBroadcast b1 = new BestEffortBroadcast(a1, null);
            b1.setMessenger(m1);
            a1.addProtocol(m1);
            a1.addProtocol(b1);

            SimpleMessenger m2 = new SimpleMessenger(a2, null);
            BestEffortBroadcast b2 = new BestEffortBroadcast(a2, null);
            b2.setMessenger(m2);
            a2.addProtocol(m2);
            a2.addProtocol(b2);

            PalmBeachSimulation.addAgent(a0);
            PalmBeachSimulation.addAgent(a1);
            PalmBeachSimulation.addAgent(a2);

            Environment env = new Environment("envName", null);
            env.addAgent(i0);
            env.addAgent(i1);
            env.addAgent(i2);
            PalmBeachSimulation.addEnvironment(env);

            FullyConnectedNetwork network = new FullyConnectedNetwork("fNetwork", env, null);
            env.addNetwork(network);

            a0.start();
            a1.start();
            a2.start();

            final Message<String> mString = new Message<>(i0, "msg");
            final AtomicReference<Message<String>> receivedMsg1 = new AtomicReference<>();
            final AtomicReference<Message<String>> receivedMsg2 = new AtomicReference<>();
            //noinspection unchecked
            SupplierExecutable waitMessageReception1 = new SupplierExecutable(() -> receivedMsg1.set((Message<String>) b1.nextMessage()));
            SupplierExecutable waitMessageReception2 = new SupplierExecutable(() -> receivedMsg2.set((Message<String>) b2.nextMessage()));
            PalmBeachSimulation.scheduler().scheduleOnce(waitMessageReception1, Scheduler.NEXT_STEP);
            PalmBeachSimulation.scheduler().scheduleOnce(waitMessageReception2, Scheduler.NEXT_STEP + 50L);
            PalmBeachSimulation.scheduler().scheduleOnce(() -> b0.broadcastMessage(mString, network), Scheduler.NEXT_STEP + 255L);

            PalmBeachSimulation.start();

            waitSimulationEnd();

            assertThat(waitMessageReception1.isExecuted()).isTrue();
            assertThat(receivedMsg1.get()).isNotNull().isSameAs(mString);
            assertThat(waitMessageReception2.isExecuted()).isTrue();
            assertThat(receivedMsg2.get()).isNotNull().isSameAs(mString);
        }
    }
}
