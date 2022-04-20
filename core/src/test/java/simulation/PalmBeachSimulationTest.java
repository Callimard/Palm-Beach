package simulation;

import agent.SimpleAgent;
import com.google.common.collect.Sets;
import environment.Environment;
import junit.PalmBeachTest;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import scheduler.Scheduler;
import simulation.exception.PalmBeachSimulationSingletonAlreadyCreateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Nested
@DisplayName("PalmBeachSimulation tests")
@Tag("PalmBeachSimulation")
@PalmBeachTest
public class PalmBeachSimulationTest {

    @BeforeEach
    void setup() {
        PalmBeachSimulation.clear();
    }

    @Nested
    @DisplayName("PalmBeachSimulation constructor()")
    @Tag("constructor")
    class GenerateSingletonPalmBeachSimulation {

        @SuppressWarnings("ConstantConditions")
        @Test
        @DisplayName("constructor() throws NullPointerException with null SimulationSetup or null Scheduler")
        void withNullParameters(@Mock SimulationSetup simulationSetup, @Mock Scheduler scheduler) {
            assertThrows(NullPointerException.class,
                         () -> new PalmBeachSimulation(null, scheduler, null, null, null));
            assertThrows(NullPointerException.class,
                         () -> new PalmBeachSimulation(simulationSetup, null, null, null, null));
            assertThrows(NullPointerException.class,
                         () -> new PalmBeachSimulation(null, null, null, null, null));
            assertDoesNotThrow(() -> new PalmBeachSimulation(simulationSetup, scheduler, null, null, null));
        }
    }

    @Nested
    @DisplayName("PalmBeachSimulation setSingletonInstance()")
    @Tag("setSingletonInstance")
    class SetSingletonInstance {

        @Test
        @DisplayName("setSingletonInstance() throws NullPointerException with null PalmBeachSimulation")
        void withNullParameter() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> PalmBeachSimulation.setSingletonInstance(null));
        }

        @Test
        @DisplayName("setSingletonInstance() does not throw exception for the first call")
        void firstCall(@Mock PalmBeachSimulation palmBeachSimulation) {
            assertDoesNotThrow(() -> PalmBeachSimulation.setSingletonInstance(palmBeachSimulation));
        }

        @Test
        @DisplayName("setSingletonInstance() throws PalmBeachSimulationSingletonAlreadyCreateException for the second call")
        void secondCall(@Mock PalmBeachSimulation palmBeachSimulation) {
            PalmBeachSimulation.setSingletonInstance(palmBeachSimulation);
            assertThrows(PalmBeachSimulationSingletonAlreadyCreateException.class,
                         () -> PalmBeachSimulation.setSingletonInstance(palmBeachSimulation));
        }
    }

    @Nested
    @DisplayName("PalmBeachSimulation start()")
    @Tag("start")
    class Start {

        @Test
        @DisplayName("start() start does not throw exception")
        void doesNotThrowException(@Mock SimulationSetup simulationSetup, @Mock Scheduler scheduler,
                                   @Mock Environment environment, @Mock SimpleAgent agent,
                                   @Mock SimpleAgent.AgentIdentifier identifier, @Mock Controller controller) {
            when(controller.getScheduleTime()).thenReturn(1);
            when(controller.getScheduleMode()).thenReturn(Scheduler.ScheduleMode.ONCE);
            when(controller.getRepetitions()).thenReturn(1);
            when(controller.getExecutionsStep()).thenReturn(1);

            when(environment.getName()).thenReturn("envName");
            when(agent.getIdentifier()).thenReturn(identifier);
            PalmBeachSimulation.setSingletonInstance(
                    new PalmBeachSimulation(simulationSetup, scheduler, Sets.newHashSet(environment), Sets.newHashSet(agent),
                                            Sets.newHashSet(controller)));
            assertThat(PalmBeachSimulation.isEnded()).isFalse();
            assertDoesNotThrow(PalmBeachSimulation::start);
            assertDoesNotThrow(PalmBeachSimulation::start);
            assertDoesNotThrow(PalmBeachSimulation::start);
            assertDoesNotThrow(PalmBeachSimulation::start);
            verify(scheduler, times(1)).scheduleExecutable(any(), anyLong(), any(), anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("PalmBeachSimulation addEnvironment()")
    @Tag("addEnvironment")
    class AddEnvironment {

        @Test
        @DisplayName("addEnvironment() throws NullPointerException with null environment")
        void withNullEnvironment(@Mock SimulationSetup simulationSetup, @Mock Scheduler scheduler) {
            PalmBeachSimulation.setSingletonInstance(new PalmBeachSimulation(simulationSetup, scheduler, null, null, null));

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> PalmBeachSimulation.addEnvironment(null));
        }

        @Test
        @DisplayName("addEnvironment() returns true with an never added Environment")
        void notAlreadyAdded(@Mock SimulationSetup simulationSetup, @Mock Scheduler scheduler, @Mock Environment environment) {
            String envName = "envName";
            PalmBeachSimulation.setSingletonInstance(new PalmBeachSimulation(simulationSetup, scheduler, null, null, null));
            when(environment.getName()).thenReturn(envName);

            assertThat(PalmBeachSimulation.addEnvironment(environment)).isTrue();
            assertThat(PalmBeachSimulation.getEnvironment(envName)).isNotNull().isSameAs(environment);
            assertThat(PalmBeachSimulation.allEnvironments()).isNotEmpty().hasSize(1);
        }

        @Test
        @DisplayName("addEnvironment() returns with already added Environment")
        void alreadyAdded(@Mock SimulationSetup simulationSetup, @Mock Scheduler scheduler, @Mock Environment environment) {
            String envName = "envName";
            PalmBeachSimulation.setSingletonInstance(new PalmBeachSimulation(simulationSetup, scheduler, null, null, null));
            when(environment.getName()).thenReturn(envName);
            PalmBeachSimulation.addEnvironment(environment);

            assertThat(PalmBeachSimulation.addEnvironment(environment)).isFalse();
        }
    }

    @Nested
    @DisplayName("PalmBeachSimulation addAgent()")
    @Tag("addAgent")
    class AddAgent {

        @Test
        @DisplayName("AddAgent() throws NullPointerException with null SimpleAgent")
        void withNullEnvironment(@Mock SimulationSetup simulationSetup, @Mock Scheduler scheduler) {
            PalmBeachSimulation.setSingletonInstance(new PalmBeachSimulation(simulationSetup, scheduler, null, null, null));

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> PalmBeachSimulation.addAgent(null));
        }

        @Test
        @DisplayName("AddAgent() returns true with an never added SimpleAgent")
        void returnsTrue(@Mock SimulationSetup simulationSetup, @Mock Scheduler scheduler, @Mock SimpleAgent agent,
                         @Mock SimpleAgent.AgentIdentifier identifier) {
            PalmBeachSimulation.setSingletonInstance(new PalmBeachSimulation(simulationSetup, scheduler, null, null, null));
            when(agent.getIdentifier()).thenReturn(identifier);

            assertThat(PalmBeachSimulation.addAgent(agent)).isTrue();
            assertThat(PalmBeachSimulation.getAgent(identifier)).isNotNull().isSameAs(agent);
            assertThat(PalmBeachSimulation.allAgents()).isNotEmpty().hasSize(1);
        }

        @Test
        @DisplayName("AddAgent() returns with already added SimpleAgent")
        void alreadyAdded(@Mock SimulationSetup simulationSetup, @Mock Scheduler scheduler, @Mock SimpleAgent agent,
                          @Mock SimpleAgent.AgentIdentifier identifier) {
            PalmBeachSimulation.setSingletonInstance(new PalmBeachSimulation(simulationSetup, scheduler, null, null, null));
            when(agent.getIdentifier()).thenReturn(identifier);
            PalmBeachSimulation.addAgent(agent);

            assertThat(PalmBeachSimulation.addAgent(agent)).isFalse();
        }
    }
}
