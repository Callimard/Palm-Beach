package org.palmbeach.core.simulation.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.palmbeach.core.junit.PalmBeachTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.palmbeach.core.simulation.PalmBeachSimulation;
import org.palmbeach.core.simulation.configuration.AgentConfiguration;
import org.palmbeach.core.simulation.configuration.SimulationConfiguration;
import org.palmbeach.core.simulation.configuration.exception.WrongSimulationConfigurationException;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Nested
@DisplayName("SimulationConfiguration tests")
@Tag("SimulationConfiguration")
@PalmBeachTest
@Slf4j
public class SimulationConfigurationTest {

    @Nested
    @DisplayName("SimulationConfiguration constructor()")
    @Tag("constructor")
    class Constructor {

        @Test
        @DisplayName("constructor() throws NullPointerException with null basic config")
        void withNullParameter() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> new SimulationConfiguration(null));
        }

        @ParameterizedTest
        @ValueSource(ints = {-131654, -974, -6346, -1, 0})
        @DisplayName("constructor() with wrong threads throws WrongSimulationConfigurationException")
        void withWrongThreads(int threads, @Mock Config config, @Mock Config simulationConfig) {
            when(config.getConfig(SimulationConfiguration.SIMULATION_PROPERTY)).thenReturn(simulationConfig);
            when(simulationConfig.getInt(SimulationConfiguration.THREADS_PROPERTY)).thenReturn(threads);

            assertThrows(WrongSimulationConfigurationException.class, () -> new SimulationConfiguration(config));
        }

        @ParameterizedTest
        @ValueSource(ints = {-131654, -974, -6346, -1, 0})
        @DisplayName("constructor() with wrong max duration throws WrongSimulationConfigurationException")
        void withWrongMaxDuration(int maxDuration, @Mock Config config, @Mock Config simulationConfig) {
            when(config.getConfig(SimulationConfiguration.SIMULATION_PROPERTY)).thenReturn(simulationConfig);
            when(simulationConfig.getInt(SimulationConfiguration.THREADS_PROPERTY)).thenReturn(1);
            when(simulationConfig.getInt(SimulationConfiguration.MAX_DURATION_PROPERTY)).thenReturn(maxDuration);

            assertThrows(WrongSimulationConfigurationException.class, () -> new SimulationConfiguration(config));
        }

        @Test
        @DisplayName("constructor() with wrong agent configuration throws WrongSimulationConfigurationException")
        void withWrongAgentConfiguration(@Mock Config config, @Mock Config simulationConfig, @Mock Config agentConfig) {
            when(config.getConfig(SimulationConfiguration.SIMULATION_PROPERTY)).thenReturn(simulationConfig);
            when(simulationConfig.getInt(SimulationConfiguration.THREADS_PROPERTY)).thenReturn(1);
            when(simulationConfig.getInt(SimulationConfiguration.MAX_DURATION_PROPERTY)).thenReturn(1);

            when(config.hasPath(SimulationConfiguration.AGENT_PROPERTY)).thenReturn(true);
            when(config.getConfig(SimulationConfiguration.AGENT_PROPERTY)).thenReturn(agentConfig);
            when(agentConfig.getString(AgentConfiguration.NAME_PATTERN_PROPERTY)).thenReturn("");

            assertThrows(WrongSimulationConfigurationException.class, () -> new SimulationConfiguration(config));
        }

        @Test
        @DisplayName("constructor() with empty configuration does not throw exception")
        void withEmptyName(@Mock Config config, @Mock Config simulationConfig) {
            when(config.getConfig(SimulationConfiguration.SIMULATION_PROPERTY)).thenReturn(simulationConfig);
            when(simulationConfig.getInt(SimulationConfiguration.THREADS_PROPERTY)).thenReturn(1);
            when(simulationConfig.getInt(SimulationConfiguration.MAX_DURATION_PROPERTY)).thenReturn(1);

            assertDoesNotThrow(() -> new SimulationConfiguration(config));
        }

        @Test
        @DisplayName("constructor() with complete configuration does not throw exception")
        void withCompleteConfig() {
            Config mainConfig = ConfigFactory.load(SimulationConfiguration.DEFAULT_SIMULATION_CONFIG_NAME);
            assertDoesNotThrow(() -> new SimulationConfiguration(mainConfig));
        }
    }

    @Nested
    @DisplayName("SimulationConfiguration generate()")
    @Tag("generate")
    class Generate {

        @Test
        @DisplayName("generate() generate correct PalmBeachSimulation")
        void generateCorrectPalmBeachSimulation() throws WrongSimulationConfigurationException {
            Config mainConfig = ConfigFactory.load(SimulationConfiguration.DEFAULT_SIMULATION_CONFIG_NAME);
            SimulationConfiguration simulationConfiguration = new SimulationConfiguration(mainConfig);

            AtomicReference<PalmBeachSimulation> palmBeachSimulation = new AtomicReference<>();
            assertDoesNotThrow(() -> palmBeachSimulation.set(simulationConfiguration.generate()));
            assertThat(palmBeachSimulation.get()).isNotNull();
        }
    }
}
