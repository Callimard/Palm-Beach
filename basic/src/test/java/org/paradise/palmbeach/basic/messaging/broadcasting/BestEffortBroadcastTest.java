package org.paradise.palmbeach.basic.messaging.broadcasting;

import org.paradise.palmbeach.basic.test_tools.SupplierExecutable;
import org.paradise.palmbeach.basic.messaging.Messenger;
import org.paradise.palmbeach.basic.messaging.SimpleMessenger;
import org.paradise.palmbeach.basic.network.FullyConnectedNetwork;
import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.agent.exception.AgentNotStartedException;
import com.google.common.collect.Sets;
import org.paradise.palmbeach.core.environment.Environment;
import org.paradise.palmbeach.core.environment.network.Network;
import org.paradise.palmbeach.core.junit.PalmBeachSimulationTest;
import org.paradise.palmbeach.core.junit.PalmBeachTest;
import org.paradise.palmbeach.basic.messaging.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.paradise.palmbeach.core.scheduler.Scheduler;
import org.paradise.palmbeach.core.scheduler.exception.ForcedWakeUpException;
import org.paradise.palmbeach.core.simulation.PalmBeachSimulation;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.paradise.palmbeach.core.junit.PalmBeachSimulationTestExtension.waitSimulationEnd;
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
        void withNotStartedAgent() {
            SimpleAgent agent = new SimpleAgent(new SimpleAgent.AgentIdentifier("Agent", 0), null);
            Message<String> message = new Message<>( "Hello");
            Environment env = new Environment("env", null);
            Network network = new FullyConnectedNetwork("net", env, null);
            Messenger messenger = new SimpleMessenger(agent, null);

            PalmBeachSimulation.addAgent(agent);

            BestEffortBroadcast broadcast = new BestEffortBroadcast(agent, null);
            broadcast.setMessenger(messenger);
            Set<SimpleAgent.AgentIdentifier> groupMembership = Sets.newHashSet();
            assertThrows(AgentNotStartedException.class, () -> broadcast.broadcastMessage(message, groupMembership, network));
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

            Message<String> mString = new Message<>("msg");
            b0.broadcastMessage(mString, env.evolvingAgents(), network);

            PalmBeachSimulation.start();

            waitSimulationEnd();

            assertThat(b0.hasContent()).isTrue();
            assertThat(b0.nextContent()).isNotNull().isSameAs(mString);
            assertThat(b1.hasContent()).isTrue();
            assertThat(b1.nextContent()).isNotNull().isSameAs(mString);
            assertThat(b2.hasContent()).isTrue();
            assertThat(b2.nextContent()).isNotNull().isSameAs(mString);
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

            final Message<String> mString = new Message<>("msg");
            final AtomicReference<Message<String>> receivedMsg1 = new AtomicReference<>();
            final AtomicReference<Message<String>> receivedMsg2 = new AtomicReference<>();
            //noinspection unchecked
            SupplierExecutable waitMessageReception1 = new SupplierExecutable(() -> receivedMsg1.set((Message<String>) b1.nextContent()));
            SupplierExecutable waitMessageReception2 = new SupplierExecutable(() -> receivedMsg2.set((Message<String>) b2.nextContent()));
            PalmBeachSimulation.scheduler().scheduleOnce(waitMessageReception1, Scheduler.NEXT_STEP);
            PalmBeachSimulation.scheduler().scheduleOnce(waitMessageReception2, Scheduler.NEXT_STEP + 50L);
            PalmBeachSimulation.scheduler().scheduleOnce(() -> b0.broadcastMessage(mString, env.evolvingAgents(), network),
                                                         Scheduler.NEXT_STEP + 255L);

            PalmBeachSimulation.start();

            waitSimulationEnd();

            assertThat(waitMessageReception1.isExecuted()).isTrue();
            assertThat(receivedMsg1.get()).isNotNull().isSameAs(mString);
            assertThat(waitMessageReception2.isExecuted()).isTrue();
            assertThat(receivedMsg2.get()).isNotNull().isSameAs(mString);
        }
    }
}
