package simulation.configuration;

import agent.SimpleAgent;
import agent.behavior.Behavior;
import com.typesafe.config.Config;
import common.Context;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import simulation.configuration.exception.GenerationFailedException;

import static common.Tools.extractClass;

@Getter
@ToString
public class BehaviorConfiguration extends PalmBeachConfiguration<Void> {

    // Constants.

    public static final String CLASS_PROPERTY = "class";
    public static final String CONTEXT_PROPERTY = "context";

    // Variables.

    private final String behaviorClass;
    private final ContextConfiguration contextConfiguration;

    // Constructors.

    protected BehaviorConfiguration(@NonNull Config baseConfig) {
        super(baseConfig);
        this.behaviorClass = getBaseConfig().getString(CLASS_PROPERTY);
        this.contextConfiguration = getBaseConfig().hasPath(CONTEXT_PROPERTY) ?
                new ContextConfiguration(getBaseConfig().getConfig(CONTEXT_PROPERTY)) : null;
    }

    // Methods.

    @Override
    public Void generate() throws GenerationFailedException {
        throw new UnsupportedOperationException("Cannot generate Protocol directly without a SimpleAgent instance");
    }

    public Behavior generateBehavior(@NonNull SimpleAgent agent) throws GenerationFailedException {
        try {
            Context context = contextConfiguration != null ? contextConfiguration.generate() : null;
            return Behavior.instantiateBehavior(extractClass(behaviorClass), agent, context);
        } catch (Exception e) {
            throw new GenerationFailedException("Cannot generate Behavior from configuration " + this, e);
        }
    }
}
