package org.paradise.palmbeach.core.environment;

import org.junit.jupiter.api.*;
import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.utils.context.Context;
import org.paradise.palmbeach.core.environment.network.Network;
import org.paradise.palmbeach.core.junit.PalmBeachTest;
import org.mockito.Mock;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Nested
@DisplayName("Environment tests")
@Tag("Environment")
@PalmBeachTest
public class EnvironmentTest {

    @Nested
    @DisplayName("Environment constructor")
    @Tag("constructor")
    class Constructor {

        @Nested
        @DisplayName("Environment(String, Context)")
        class PrimaryConstructor {

            @Test
            @DisplayName("constructor throws NullPointerException if name is null")
            void withNullName(@Mock Context context) {
                //noinspection ConstantConditions
                assertThrows(NullPointerException.class, () -> new Environment(null, context));
            }

            @Test
            @DisplayName("constructor does not throw exception with null context and create a non null empty context")
            void withNullContext() {
                AtomicReference<Environment> environment = new AtomicReference<>();

                assertDoesNotThrow(() -> environment.set(new Environment("name", null)));
                assertThat(environment.get().getContext()).isNotNull();
                assertThat(environment.get().getContext().isEmpty()).isTrue();
            }
        }

        @Nested
        @DisplayName("Environment instantiateEnvironment()")
        @Tag("instantiateEnvironment")
        class InstantiateEnvironment {

            @SuppressWarnings("ConstantConditions")
            @Test
            @DisplayName("instantiateEnvironment() throws NullPointerException with null environmentClass or null environmentName")
            void withNullParameters(@Mock Context context) {
                assertThrows(NullPointerException.class, () -> Environment.instantiateEnvironment(null, "environmentName", context));
                assertThrows(NullPointerException.class, () -> Environment.instantiateEnvironment(Environment.class, null, context));
                assertThrows(NullPointerException.class, () -> Environment.instantiateEnvironment(null, null, context));
                assertThrows(NullPointerException.class, () -> Environment.instantiateEnvironment(null, null, null));
                Assertions.assertDoesNotThrow(() -> Environment.instantiateEnvironment(Environment.class, "environmentName", null));
            }

            @Test
            @DisplayName("instantiateEnvironment() does not throw exception and create a new instance of Environment")
            void createNewInstanceOfEnvironment(@Mock Context context) {
                String environmentName = "environmentName";
                AtomicReference<Environment> environment = new AtomicReference<>();

                assertDoesNotThrow(() -> environment.set(Environment.instantiateEnvironment(Environment.class, environmentName, context)));
                assertThat(environment.get()).isNotNull();
                assertThat(environment.get().getClass()).isEqualTo(Environment.class);
                assertThat(environment.get().getName()).isEqualTo(environmentName);
            }
        }

        @Nested
        @DisplayName("Environment(String)")
        class SecondaryConstructor {

            @Test
            @DisplayName("constructor throws NullPointerException if name is null")
            void withNullName() {
                //noinspection ConstantConditions
                assertThrows(NullPointerException.class, () -> new Environment(null, null));
            }

            @Test
            @DisplayName("constructor create a non null empty context")
            void withNullContext() {
                AtomicReference<Environment> environment = new AtomicReference<>();

                assertDoesNotThrow(() -> environment.set(new Environment("name", null)));
                assertThat(environment.get().getContext()).isNotNull();
                assertThat(environment.get().getContext().isEmpty()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Environment getName()")
    @Tag("getName")
    class GetName {

        @Test
        @DisplayName("getName() never returns null")
        void neverReturnsNull() {
            Environment environment = new Environment("name", null);

            assertThat(environment.getName()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Environment addAgent()")
    @Tag("addAgent")
    class AddAgent {

        @Test
        @DisplayName("addAgent() throws NullPointerException if the agent is null")
        void withNullAgent() {
            Environment environment = new Environment("name", null);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> environment.addAgent(null));
        }

        @Test
        @DisplayName("addAgent() returns true if the agent has never been added before")
        void firstAdd(@Mock SimpleAgent.AgentIdentifier agent, @Mock Environment.EnvironmentObserver observer) {
            Environment environment = new Environment("name", null);
            environment.addObserver(observer);

            boolean added = environment.addAgent(agent);
            assertThat(added).isTrue();
            verify(observer, times(1)).environmentAddAgent(agent);
        }

        @Test
        @DisplayName("addAgent() returns false if the agent has already been added")
        void alreadyAdded(@Mock SimpleAgent.AgentIdentifier agent, @Mock Environment.EnvironmentObserver observer) {
            Environment environment = new Environment("name", null);
            environment.addObserver(observer);

            environment.addAgent(agent);
            boolean added = environment.addAgent(agent);
            assertThat(added).isFalse();
            verify(observer, times(1)).environmentAddAgent(agent);
        }
    }

    @Nested
    @DisplayName("Environment removeAgent()")
    @Tag("removeAgent")
    class RemoveAgent {

        @Test
        @DisplayName("removeAgent() remove agent if agent is evolving in Environment")
        void withEvolvingAgent(@Mock SimpleAgent.AgentIdentifier agent, @Mock Environment.EnvironmentObserver observer) {
            Environment environment = new Environment("name", null);
            environment.addObserver(observer);
            environment.addAgent(agent);

            environment.removeAgent(agent);
            assertThat(environment.agentIsEvolving(agent)).isFalse();
            verify(observer, times(1)).environmentRemoveAgent(agent);
        }

        @Test
        @DisplayName("removeAgent() remove do nothing if agent is not evolving in Environment")
        void withNotEvolvingAgent(@Mock SimpleAgent.AgentIdentifier agent, @Mock Environment.EnvironmentObserver observer) {
            Environment environment = new Environment("name", null);
            environment.addObserver(observer);

            environment.removeAgent(agent);
            assertThat(environment.agentIsEvolving(agent)).isFalse();
            verify(observer, times(0)).environmentRemoveAgent(agent);
        }
    }

    @Nested
    @DisplayName("Environment agentIsEvolving()")
    @Tag("agentIsEvolving")
    class AgentIsEvolving {

        @Test
        @DisplayName("agentIsEvolving returns false if it is not added in Environment")
        void notAddedAgent(@Mock SimpleAgent.AgentIdentifier agent) {
            Environment environment = new Environment("name", null);

            assertThat(environment.agentIsEvolving(agent)).isFalse();
        }

        @Test
        @DisplayName("agentIsEvolving throws NullPointerException with null agent")
        void withNullAgent() {
            Environment environment = new Environment("name", null);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> environment.agentIsEvolving(null));
        }

        @Test
        @DisplayName("agentIsEvolving returns true if it is added in Environment")
        void addedAgent(@Mock SimpleAgent.AgentIdentifier agent) {
            Environment environment = new Environment("name", null);
            environment.addAgent(agent);

            assertThat(environment.agentIsEvolving(agent)).isTrue();
        }
    }

    @Nested
    @DisplayName("Environment evolvingAgents()")
    @Tag("evolvingAgents")
    class EvolvingAgents {

        @Test
        @DisplayName("evolvingAgents() returns empty set if no agent has been added")
        void noAgentAdded() {
            Environment environment = new Environment("name", null);

            assertThat(environment.evolvingAgents()).isEmpty();
        }

        @Test
        @DisplayName("evolvingAgents() returns non empty set if agent has been added and added agent are contains in this set")
        void withAddedAgent(@Mock SimpleAgent.AgentIdentifier a0, @Mock SimpleAgent.AgentIdentifier a1) {
            Environment environment = new Environment("name", null);
            environment.addAgent(a0);
            environment.addAgent(a1);

            Set<SimpleAgent.AgentIdentifier> agents = environment.evolvingAgents();
            assertThat(agents).contains(a0, a1);
        }
    }

    @Nested
    @DisplayName("Environment addNetwork()")
    @Tag("addNetwork")
    class AddNetwork {

        @Test
        @DisplayName("addNetwork() throws NullPointerException with null Network")
        void withNullNetwork() {
            Environment environment = new Environment("name", null);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> environment.addNetwork(null));
        }

        @Test
        @DisplayName("addNetwork() does not throw exception with non null Network")
        void withNonNullNetwork(@Mock Network network) {
            Environment environment = new Environment("name", null);

            assertDoesNotThrow(() -> environment.addNetwork(network));
        }

        @Test
        @DisplayName("addNetwork() erase the previous value if added Network has the same name")
        void withSameName(@Mock Network n0, @Mock Network n1) {
            Environment environment = new Environment("name", null);

            String networkName = "PN name";
            when(n0.getName()).thenReturn(networkName);
            when(n1.getName()).thenReturn(networkName);
            environment.addNetwork(n0);
            environment.addNetwork(n1);

            assertThat(environment.getNetwork(networkName)).isNotNull().isSameAs(n1);
        }
    }

    @Nested
    @DisplayName("Environment getNetwork()")
    @Tag("getNetwork")
    class GetNetwork {

        @Test
        @DisplayName("getNetwork() returns null with null name")
        void withNullName() {
            Environment environment = new Environment("name", null);

            assertThat(environment.getNetwork(null)).isNull();
        }

        @Test
        @DisplayName("getNetwork() returns null with non added Network with the specified name")
        void notAddedNetworkName() {
            Environment environment = new Environment("name", null);

            assertThat(environment.getNetwork("not added name")).isNull();
        }

        @Test
        @DisplayName("getNetwork() returns the correct Network if it has been added")
        void withAddedNetwork(@Mock Network network) {
            Environment environment = new Environment("name", null);

            String networkName = "pN Name";
            when(network.getName()).thenReturn(networkName);
            environment.addNetwork(network);

            assertThat(environment.getNetwork(networkName)).isNotNull().isSameAs(network);
        }
    }

    @Nested
    @DisplayName("Environment toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull() {
            Environment environment = new Environment("name", null);

            assertThat(environment.toString()).isNotNull();
        }
    }
}
