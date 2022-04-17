package simulation.configuration;

import agent.SimpleAgent;
import com.typesafe.config.Config;
import junit.PalmBeachTest;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import simulation.configuration.exception.GenerationFailedException;
import simulation.configuration.exception.WrongAgentConfigurationException;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Nested
@DisplayName("AgentConfiguration tests")
@Tag("AgentConfiguration")
@PalmBeachTest
public class AgentConfigurationTest {

    private final static String DEFAULT_PATTERN_NAME = "Agent %d";

    @Nested
    @DisplayName("AgentConfiguration constructor()")
    @Tag("constructor")
    class Constructor {

        @Test
        @DisplayName("constructor() throws NullPointerException with null basic config")
        void withNullParameter() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> new AgentConfiguration(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  ", "            ", "\n"})
        @DisplayName("constructor() throws WrongAgentConfigurationException with empty or blank name")
        void withEmptyName(String name, @Mock Config config) {
            when(config.getString(AgentConfiguration.NAME_PATTERN_PROPERTY)).thenReturn(name);

            assertThrows(WrongAgentConfigurationException.class, () -> new AgentConfiguration(config));
        }

        @ParameterizedTest
        @ValueSource(ints = {-105, -1631564, -213, -1, 0})
        @DisplayName("constructor() throws WrongAgentConfigurationException with empty or blank name")
        void withLessThanOneNumber(int number, @Mock Config config) {
            when(config.getString(AgentConfiguration.NAME_PATTERN_PROPERTY)).thenReturn(DEFAULT_PATTERN_NAME);
            when(config.getInt(AgentConfiguration.AGENT_NUMBER_PROPERTY)).thenReturn(number);

            assertThrows(WrongAgentConfigurationException.class, () -> new AgentConfiguration(config));
        }

        @Test
        @DisplayName("constructor() with correct parameters does not throws exception")
        void withCorrectParameters(@Mock Config config) {
            when(config.hasPath(AgentConfiguration.CLASS_PROPERTY)).thenReturn(false);
            when(config.hasPath(AgentConfiguration.CONTEXT_PROPERTY)).thenReturn(false);

            when(config.getString(AgentConfiguration.NAME_PATTERN_PROPERTY)).thenReturn(DEFAULT_PATTERN_NAME);
            when(config.getInt(AgentConfiguration.AGENT_NUMBER_PROPERTY)).thenReturn(1);

            when(config.hasPath(AgentConfiguration.ENVIRONMENTS_PROPERTY)).thenReturn(true);
            when(config.getStringList(AgentConfiguration.ENVIRONMENTS_PROPERTY)).thenReturn(Lists.list("Env1", "Env2"));

            when(config.hasPath(AgentConfiguration.PROTOCOLS_PROPERTY)).thenReturn(true);
            when(config.getStringList(AgentConfiguration.PROTOCOLS_PROPERTY)).thenReturn(Lists.list("P1", "P2"));

            when(config.hasPath(AgentConfiguration.BEHAVIORS_PROPERTY)).thenReturn(true);
            when(config.getStringList(AgentConfiguration.BEHAVIORS_PROPERTY)).thenReturn(Lists.list("B1", "B2"));

            assertDoesNotThrow(() -> new AgentConfiguration(config));
        }

        @Test
        @DisplayName("constructor() with almost empty config does not throws exception")
        void withAlmostEmptyConfig(@Mock Config config) {
            when(config.getString(AgentConfiguration.NAME_PATTERN_PROPERTY)).thenReturn(DEFAULT_PATTERN_NAME);
            when(config.getInt(AgentConfiguration.AGENT_NUMBER_PROPERTY)).thenReturn(1);
            assertDoesNotThrow(() -> new AgentConfiguration(config));

        }
    }

    @Nested
    @DisplayName("AgentConfiguration generate()")
    @Tag("generate")
    class Generate {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 15, 25})
        @DisplayName("generate() generate correctly all agents")
        void generateCorrectlyAllAgents(int number, @Mock Config config) throws WrongAgentConfigurationException {
            when(config.getString(AgentConfiguration.NAME_PATTERN_PROPERTY)).thenReturn(DEFAULT_PATTERN_NAME);
            when(config.getInt(AgentConfiguration.AGENT_NUMBER_PROPERTY)).thenReturn(number);

            AgentConfiguration agentConfiguration = new AgentConfiguration(config);

            final Set<SimpleAgent> allAgents = new HashSet<>();
            assertDoesNotThrow(() -> allAgents.addAll(agentConfiguration.generate()));

            assertThat(allAgents).isNotNull().isNotEmpty().hasSize(number);
        }
    }

    @Nested
    @DisplayName("AgentConfiguration toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull(@Mock Config config) throws WrongAgentConfigurationException {
            when(config.getString(AgentConfiguration.NAME_PATTERN_PROPERTY)).thenReturn(DEFAULT_PATTERN_NAME);
            when(config.getInt(AgentConfiguration.AGENT_NUMBER_PROPERTY)).thenReturn(15);
            AgentConfiguration agentConfiguration = new AgentConfiguration(config);

            assertThat(agentConfiguration.toString()).isNotNull();
        }
    }
}
