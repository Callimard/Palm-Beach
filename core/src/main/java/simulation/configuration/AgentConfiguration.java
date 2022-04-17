package simulation.configuration;

import agent.SimpleAgent;
import com.typesafe.config.Config;
import common.Context;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import simulation.configuration.exception.GenerationFailedException;
import simulation.configuration.exception.WrongAgentConfigurationException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static common.Tools.extractClass;

/**
 * Configuration for {@link SimpleAgent}.
 * <p>
 * Example of complete agent configuration:
 * <pre>
 * agent.correctAgent.name-pattern="CorrectAgent %d"
 * agent.correctAgent.number=15
 * agent.correctAgent.context.class=context.CustomContext
 * agent.correctAgent.context.key1="value1"
 * agent.correctAgent.context.key2="value2"
 * agent.correctAgent.environments=[simpleEnvironment]
 * agent.correctAgent.protocols=[transport, tendermint]
 * agent.correctAgent.behaviors=[correctBehavior]
 *
 * agent.byzantineAgent.class=agent.ByzantineAgent
 * agent.byzantineAgent.name-pattern="ByzantineAgent %d"
 * agent.byzantineAgent.number=3
 * agent.byzantineAgent.environments=[simpleEnvironment]
 * agent.byzantineAgent.protocols=[transport, tendermint, hackProtocol]
 * agent.byzantineAgent.behaviors=[byzantineBehavior]
 * </pre>
 */
@Getter
@ToString
@Slf4j
public class AgentConfiguration extends PalmBeachConfiguration<Set<SimpleAgent>> {

    // Constants.

    public static final String CLASS_PROPERTY = "class";
    public static final String NAME_PATTERN_PROPERTY = "name-pattern";
    public static final String AGENT_NUMBER_PROPERTY = "number";
    public static final String CONTEXT_PROPERTY = "context";
    public static final String ENVIRONMENTS_PROPERTY = "environments";
    public static final String PROTOCOLS_PROPERTY = "protocols";
    public static final String BEHAVIORS_PROPERTY = "behaviors";

    // Variables.

    private final String agentClass;
    private final String agentNamePattern;
    private final int number;
    private final ContextConfiguration contextConfiguration;
    private final List<String> environments;
    private final List<String> protocols;
    private final List<String> behaviors;

    // Constructors.

    /**
     * @param baseConfig the base configuration
     *
     * @throws WrongAgentConfigurationException if agentPattern is empty or blank or if agent number is less than 1
     */
    public AgentConfiguration(@NonNull Config baseConfig) throws WrongAgentConfigurationException {
        super(baseConfig);
        this.agentClass = getBaseConfig().hasPath(CLASS_PROPERTY) ? getBaseConfig().getString(CLASS_PROPERTY) : SimpleAgent.class.getName();

        this.agentNamePattern = getBaseConfig().getString(NAME_PATTERN_PROPERTY);
        if (this.agentNamePattern.isEmpty() || this.agentNamePattern.isBlank())
            throw new WrongAgentConfigurationException("AgentNamePattern cannot be empty or blank");

        this.number = getBaseConfig().getInt(AGENT_NUMBER_PROPERTY);
        if (this.number < 1)
            throw new WrongAgentConfigurationException("Agent number cannot be less than 1");

        this.contextConfiguration = getBaseConfig().hasPath(CONTEXT_PROPERTY) ?
                new ContextConfiguration(getBaseConfig().getConfig(CONTEXT_PROPERTY)) : null;

        this.environments = parseEnvironments();
        this.protocols = parseProtocols();
        this.behaviors = parseBehaviors();
    }

    private List<String> parseEnvironments() {
        List<String> allEnvironments;
        if (getBaseConfig().hasPath(ENVIRONMENTS_PROPERTY)) {
            allEnvironments = getBaseConfig().getStringList(ENVIRONMENTS_PROPERTY);
            if (allEnvironments.isEmpty())
                log.info("Agent configuration WITHOUT Environment");
        } else {
            allEnvironments = Collections.emptyList();
            log.info("Agent configuration WITHOUT Environment");
        }
        return allEnvironments;
    }

    private List<String> parseProtocols() {
        List<String> allProtocols;
        if (getBaseConfig().hasPath(PROTOCOLS_PROPERTY)) {
            allProtocols = getBaseConfig().getStringList(PROTOCOLS_PROPERTY);
            if (allProtocols.isEmpty())
                log.info("Agent configuration WITHOUT Protocol");
        } else {
            allProtocols = Collections.emptyList();
            log.info("Agent configuration WITHOUT Protocol");
        }
        return allProtocols;
    }

    private List<String> parseBehaviors() {
        List<String> allBehaviors;
        if (getBaseConfig().hasPath(BEHAVIORS_PROPERTY)) {
            allBehaviors = getBaseConfig().getStringList(BEHAVIORS_PROPERTY);
            if (allBehaviors.isEmpty())
                log.info("Agent configuration WITHOUT Behavior");
        } else {
            allBehaviors = Collections.emptyList();
            log.info("Agent configuration WITHOUT Behavior");
        }
        return allBehaviors;
    }

    // Methods.

    @Override
    public Set<SimpleAgent> generate() throws GenerationFailedException {
        try {
            Set<SimpleAgent> agents = new HashSet<>();
            for (int i = 0; i < number; i++) {
                Context context = contextConfiguration != null ? contextConfiguration.generate() : null;
                SimpleAgent agent = SimpleAgent.initiateAgent(extractClass(agentClass),
                                                              new SimpleAgent.SimpleAgentIdentifier(agentNamePattern.formatted(i),
                                                                                                    SimpleAgent.SimpleAgentIdentifier.nextId()),
                                                              context);
                agents.add(agent);
            }
            return agents;
        } catch (Exception e) {
            throw new GenerationFailedException("fail to generate all Agents from the configuration " + this, e);
        }
    }
}
