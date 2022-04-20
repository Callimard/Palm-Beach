package simulation.configuration;

import com.typesafe.config.Config;
import common.Context;
import environment.Environment;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import simulation.configuration.exception.GenerationFailedException;

import java.util.HashSet;
import java.util.Set;

import static common.Tools.extractClass;

/**
 * Configuration for {@link Environment}.
 * <p>
 * Example of complete {@code Environment} configuration:
 * <pre>
 * environment.simpleEnvironment.class=environment.SimpleEnvironment
 * environment.simpleEnvironment.context.class=context.CustomContext
 * environment.simpleEnvironment.Networks=[fullyConnected]
 * environment.simpleEnvironment.context.key1="value1"
 * environment.simpleEnvironment.context.key2="value2"
 * </pre>
 */
@Getter
@ToString
@Slf4j
public class EnvironmentConfiguration extends PalmBeachConfiguration<Environment> {

    // Constants.

    public static final String CLASS_PROPERTY = "class";
    public static final String CONTEXT_PROPERTY = "context";
    public static final String PHYSICAL_NETWORKS_PROPERTY = "Networks";

    // Variables.

    private final String environmentClass;
    private final String environmentName;
    private final Set<String> networks;
    private final ContextConfiguration contextConfiguration;

    // Constructors.

    public EnvironmentConfiguration(@NonNull String environmentName, @NonNull Config baseConfig) {
        super(baseConfig);
        this.environmentClass = getBaseConfig().getString(CLASS_PROPERTY);
        this.environmentName = environmentName;
        this.contextConfiguration =
                getBaseConfig().hasPath(CONTEXT_PROPERTY) ? new ContextConfiguration(getBaseConfig().getConfig(CONTEXT_PROPERTY)) : null;

        this.networks = new HashSet<>();
        if (getBaseConfig().hasPath(PHYSICAL_NETWORKS_PROPERTY)) {
            this.networks.addAll(getBaseConfig().getStringList(PHYSICAL_NETWORKS_PROPERTY));
            if (this.networks.isEmpty())
                log.info("Environment without Network");
        } else
            log.info("Environment without Network");
    }

    // Methods.

    @Override
    public Environment generate() throws GenerationFailedException {
        try {
            Context context = contextConfiguration != null ? contextConfiguration.generate() : null;
            return Environment.instantiateEnvironment(extractClass(environmentClass), environmentName, context);
        } catch (Exception e) {
            throw new GenerationFailedException("Cannot generate Environment from config " + this, e);
        }
    }
}
