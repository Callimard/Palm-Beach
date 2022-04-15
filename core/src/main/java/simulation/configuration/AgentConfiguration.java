package simulation.configuration;

import agent.SimpleAgent;
import com.typesafe.config.Config;
import common.Context;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import simulation.configuration.exception.GenerationFailedException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static common.Tools.extractClass;

@Getter
@ToString
public class AgentConfiguration extends PalmBeachConfiguration<Set<SimpleAgent>> {

    // Constants.

    public static final String CLASS_PROPERTY = "class";
    public static final String NAME_PATTERN_PROPERTY = "name-pattern";
    public static final String AGENT_NUMBER_PROPERTY = "class";
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

    public AgentConfiguration(@NonNull Config baseConfig) {
        super(baseConfig);
        this.agentClass = getBaseConfig().hasPath(CLASS_PROPERTY) ? getBaseConfig().getString(CLASS_PROPERTY) : SimpleAgent.class.getName();
        this.agentNamePattern = getBaseConfig().getString(NAME_PATTERN_PROPERTY);
        this.number = getBaseConfig().getInt(AGENT_NUMBER_PROPERTY);
        this.contextConfiguration = getBaseConfig().hasPath(CONTEXT_PROPERTY) ?
                new ContextConfiguration(getBaseConfig().getConfig(CONTEXT_PROPERTY)) : null;
        this.environments = getBaseConfig().getStringList(ENVIRONMENTS_PROPERTY);
        this.protocols = getBaseConfig().getStringList(PROTOCOLS_PROPERTY);
        this.behaviors = getBaseConfig().getStringList(BEHAVIORS_PROPERTY);
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
