package environment;

import agent.SimpleAgent;
import common.Context;
import environment.physical.PhysicalNetwork;
import junit.PalmBeachTest;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
                assertThrows(NullPointerException.class, () -> new BasicEnvironment(null, context));
            }

            @Test
            @DisplayName("constructor does not throw exception with null context and create a non null empty context")
            void withNullContext() {
                AtomicReference<Environment> environment = new AtomicReference<>();

                assertDoesNotThrow(() -> environment.set(new BasicEnvironment("name", null)));
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
                assertThrows(NullPointerException.class, () -> Environment.instantiateEnvironment(BasicEnvironment.class, null, context));
                assertThrows(NullPointerException.class, () -> Environment.instantiateEnvironment(null, null, context));
                assertThrows(NullPointerException.class, () -> Environment.instantiateEnvironment(null, null, null));
                assertDoesNotThrow(() -> Environment.instantiateEnvironment(BasicEnvironment.class, "environmentName", null));
            }

            @Test
            @DisplayName("instantiateEnvironment() does not throw exception and create a new instance of Environment")
            void createNewInstanceOfEnvironment(@Mock Context context) {
                String environmentName = "environmentName";
                AtomicReference<Environment> environment = new AtomicReference<>();

                assertDoesNotThrow(() -> environment.set(Environment.instantiateEnvironment(BasicEnvironment.class, environmentName, context)));
                assertThat(environment.get()).isNotNull();
                assertThat(environment.get().getClass()).isEqualTo(BasicEnvironment.class);
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
                assertThrows(NullPointerException.class, () -> new BasicEnvironment(null, null));
            }

            @Test
            @DisplayName("constructor create a non null empty context")
            void withNullContext() {
                AtomicReference<Environment> environment = new AtomicReference<>();

                assertDoesNotThrow(() -> environment.set(new BasicEnvironment("name", null)));
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
            Environment environment = new BasicEnvironment("name", null);

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
            Environment environment = new BasicEnvironment("name", null);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> environment.addAgent(null));
        }

        @Test
        @DisplayName("addAgent() returns true if the agent has never been added before")
        void firstAdd(@Mock SimpleAgent.AgentIdentifier agent, @Mock Environment.EnvironmentObserver observer) {
            Environment environment = new BasicEnvironment("name", null);
            environment.addObserver(observer);

            boolean added = environment.addAgent(agent);
            assertThat(added).isTrue();
            verify(observer, times(1)).agentAdded(agent);
        }

        @Test
        @DisplayName("addAgent() returns false if the agent has already been added")
        void alreadyAdded(@Mock SimpleAgent.AgentIdentifier agent, @Mock Environment.EnvironmentObserver observer) {
            Environment environment = new BasicEnvironment("name", null);
            environment.addObserver(observer);

            environment.addAgent(agent);
            boolean added = environment.addAgent(agent);
            assertThat(added).isFalse();
            verify(observer, times(1)).agentAdded(agent);
        }
    }

    @Nested
    @DisplayName("Environment removeAgent()")
    @Tag("removeAgent")
    class RemoveAgent {

        @Test
        @DisplayName("removeAgent() remove agent if agent is evolving in Environment")
        void withEvolvingAgent(@Mock SimpleAgent.AgentIdentifier agent, @Mock Environment.EnvironmentObserver observer) {
            Environment environment = new BasicEnvironment("name", null);
            environment.addObserver(observer);
            environment.addAgent(agent);

            environment.removeAgent(agent);
            assertThat(environment.agentIsEvolving(agent)).isFalse();
            verify(observer, times(1)).agentRemoved(agent);
        }

        @Test
        @DisplayName("removeAgent() remove do nothing if agent is not evolving in Environment")
        void withNotEvolvingAgent(@Mock SimpleAgent.AgentIdentifier agent, @Mock Environment.EnvironmentObserver observer) {
            Environment environment = new BasicEnvironment("name", null);
            environment.addObserver(observer);

            environment.removeAgent(agent);
            assertThat(environment.agentIsEvolving(agent)).isFalse();
            verify(observer, times(0)).agentRemoved(agent);
        }
    }

    @Nested
    @DisplayName("Environment agentIsEvolving()")
    @Tag("agentIsEvolving")
    class AgentIsEvolving {

        @Test
        @DisplayName("agentIsEvolving returns false if it is not added in Environment")
        void notAddedAgent(@Mock SimpleAgent.AgentIdentifier agent) {
            Environment environment = new BasicEnvironment("name", null);

            assertThat(environment.agentIsEvolving(agent)).isFalse();
        }

        @Test
        @DisplayName("agentIsEvolving throws NullPointerException with null agent")
        void withNullAgent() {
            Environment environment = new BasicEnvironment("name", null);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> environment.agentIsEvolving(null));
        }

        @Test
        @DisplayName("agentIsEvolving returns true if it is added in Environment")
        void addedAgent(@Mock SimpleAgent.AgentIdentifier agent) {
            Environment environment = new BasicEnvironment("name", null);
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
            Environment environment = new BasicEnvironment("name", null);

            assertThat(environment.evolvingAgents()).isEmpty();
        }

        @Test
        @DisplayName("evolvingAgents() returns non empty set if agent has been added and added agent are contains in this set")
        void withAddedAgent(@Mock SimpleAgent.AgentIdentifier a0, @Mock SimpleAgent.AgentIdentifier a1) {
            Environment environment = new BasicEnvironment("name", null);
            environment.addAgent(a0);
            environment.addAgent(a1);

            Set<SimpleAgent.AgentIdentifier> agents = environment.evolvingAgents();
            assertThat(agents).contains(a0, a1);
        }
    }

    @Nested
    @DisplayName("Environment addPhysicalNetwork()")
    @Tag("addPhysicalNetwork")
    class AddPhysicalNetwork {

        @Test
        @DisplayName("addPhysicalNetwork() throws NullPointerException with null PhysicalNetwork")
        void withNullPhysicalNetwork() {
            Environment environment = new BasicEnvironment("name", null);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> environment.addPhysicalNetwork(null));
        }

        @Test
        @DisplayName("addPhysicalNetwork() does not throw exception with non null PhysicalNetwork")
        void withNonNullPhysicalNetwork(@Mock PhysicalNetwork physicalNetwork) {
            Environment environment = new BasicEnvironment("name", null);

            assertDoesNotThrow(() -> environment.addPhysicalNetwork(physicalNetwork));
        }

        @Test
        @DisplayName("addPhysicalNetwork() erase the previous value if added PhysicalNetwork has the same name")
        void withSameName(@Mock PhysicalNetwork pN0, @Mock PhysicalNetwork pN1) {
            Environment environment = new BasicEnvironment("name", null);

            String physicalNetworkName = "PN name";
            when(pN0.getName()).thenReturn(physicalNetworkName);
            when(pN1.getName()).thenReturn(physicalNetworkName);
            environment.addPhysicalNetwork(pN0);
            environment.addPhysicalNetwork(pN1);

            assertThat(environment.getPhysicalNetwork(physicalNetworkName)).isNotNull().isSameAs(pN1);
        }
    }

    @Nested
    @DisplayName("Environment getPhysicalNetwork()")
    @Tag("getPhysicalNetwork")
    class GetPhysicalNetwork {

        @Test
        @DisplayName("getPhysicalNetwork() returns null with null name")
        void withNullName() {
            Environment environment = new BasicEnvironment("name", null);

            assertThat(environment.getPhysicalNetwork(null)).isNull();
        }

        @Test
        @DisplayName("getPhysicalNetwork() returns null with non added PhysicalNetwork with the specified name")
        void notAddedPhysicalNetworkName() {
            Environment environment = new BasicEnvironment("name", null);

            assertThat(environment.getPhysicalNetwork("not added name")).isNull();
        }

        @Test
        @DisplayName("getPhysicalNetwork() returns the correct PhysicalNetwork if it has been added")
        void withAddedPhysicalNetwork(@Mock PhysicalNetwork physicalNetwork) {
            Environment environment = new BasicEnvironment("name", null);

            String physicalNetworkName = "pN Name";
            when(physicalNetwork.getName()).thenReturn(physicalNetworkName);
            environment.addPhysicalNetwork(physicalNetwork);

            assertThat(environment.getPhysicalNetwork(physicalNetworkName)).isNotNull().isSameAs(physicalNetwork);
        }
    }

    @Nested
    @DisplayName("Environment toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull() {
            Environment environment = new BasicEnvironment("name", null);

            assertThat(environment.toString()).isNotNull();
        }
    }

    // Inner class.

    public static class BasicEnvironment extends Environment {

        public BasicEnvironment(@NonNull String name, Context context) {
            super(name, context);
        }
    }
}
