package messaging;

import agent.SimpleAgent;
import com.google.common.collect.Sets;
import environment.Environment;
import environment.network.Network;
import junit.PalmBeachSimulationTest;
import junit.PalmBeachTest;
import lombok.extern.slf4j.Slf4j;
import network.FullyConnectedNetwork;
import network.RandomConnectedNetwork;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import scheduler.exception.ForcedWakeUpException;
import simulation.PalmBeachSimulation;

import java.util.Set;

import static junit.PalmBeachSimulationTestExtension.waitSimulationEnd;
import static org.assertj.core.api.Assertions.assertThat;

@Nested
@DisplayName("PulsingMessenger tests")
@Tag("PulsingMessenger")
@Slf4j
@PalmBeachTest
public class PulsingMessengerIT {

    @Nested
    @DisplayName("PulsingMessenger sendMessage()")
    @Tag("sendMessage")
    @PalmBeachSimulationTest
    class SendMessage {

        @RepeatedTest(5)
        @DisplayName("sendMessage() send the message and ensure that the receiver receive the message in correct RandomConnectedNetwork")
        void inRandomConnectedNetwork() throws InterruptedException, ForcedWakeUpException {
            Environment env = new Environment("env", null);
            RandomConnectedNetwork network = new RandomConnectedNetwork("net", env, null);
            network.connectionNumber(1);
            PalmBeachSimulation.addEnvironment(env);

            SimpleAgent sender = null;
            SimpleAgent receiver = null;
            Set<SimpleAgent> agents = Sets.newHashSet();
            int numberAgents = 75;
            for (int i = 0; i < numberAgents; i++) {
                SimpleAgent agent = new SimpleAgent(new SimpleAgent.SimpleAgentIdentifier(String.valueOf(i), i), null);
                agent.start();
                PalmBeachSimulation.addAgent(agent);
                env.addAgent(agent.getIdentifier());
                agent.addProtocol(new PulsingMessenger(agent, null));
                if (i == 0) {
                    sender = agent;
                } else if (i == numberAgents - 1) {
                    receiver = agent;
                }
            }

            log.info("Sender and Receiver are directly connected ? {}", directlyConnected(sender.getIdentifier(), receiver.getIdentifier(), network));

            PulsingMessenger pmSender = sender.getProtocol(PulsingMessenger.class);
            PulsingMessenger pmReceiver = receiver.getProtocol(PulsingMessenger.class);

            Message<String> msg = new Message<>(sender.getIdentifier(), "Hello");
            pmSender.sendMessage(msg, receiver.getIdentifier(), network);

            PalmBeachSimulation.start();

            waitSimulationEnd();

            assertThat(pmReceiver.hasMessage()).isTrue();
            assertThat(pmReceiver.nextMessage()).isNotNull().isSameAs(msg);
        }

        @RepeatedTest(5)
        @DisplayName("sendMessage() send the message and ensure that the receiver receive the message in correct FullyConnectedNetwork")
        void inFullyConnectedNetwork() throws InterruptedException, ForcedWakeUpException {
            Environment env = new Environment("env", null);
            FullyConnectedNetwork network = new FullyConnectedNetwork("net", env, null);
            PalmBeachSimulation.addEnvironment(env);

            SimpleAgent sender = null;
            SimpleAgent receiver = null;
            Set<SimpleAgent> agents = Sets.newHashSet();
            int numberAgents = 75;
            for (int i = 0; i < numberAgents; i++) {
                SimpleAgent agent = new SimpleAgent(new SimpleAgent.SimpleAgentIdentifier(String.valueOf(i), i), null);
                agent.start();
                PalmBeachSimulation.addAgent(agent);
                env.addAgent(agent.getIdentifier());
                agent.addProtocol(new PulsingMessenger(agent, null));
                if (i == 0) {
                    sender = agent;
                } else if (i == numberAgents - 1) {
                    receiver = agent;
                }
            }

            log.info("Sender and Receiver are directly connected ? {}", directlyConnected(sender.getIdentifier(), receiver.getIdentifier(), network));

            PulsingMessenger pmSender = sender.getProtocol(PulsingMessenger.class);
            PulsingMessenger pmReceiver = receiver.getProtocol(PulsingMessenger.class);

            Message<String> msg = new Message<>(sender.getIdentifier(), "Hello");
            pmSender.sendMessage(msg, receiver.getIdentifier(), network);

            PalmBeachSimulation.start();

            waitSimulationEnd();

            assertThat(pmReceiver.hasMessage()).isTrue();
            assertThat(pmReceiver.nextMessage()).isNotNull().isSameAs(msg);
        }

        private boolean directlyConnected(SimpleAgent.AgentIdentifier sender, SimpleAgent.AgentIdentifier receiver, Network network) {
            return network.directNeighbors(sender).contains(receiver);
        }
    }

}
