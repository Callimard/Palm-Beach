package simulation.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import common.Context;
import environment.Environment;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import simulation.configuration.exception.GenerationFailedException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static common.Tools.extractClass;

@Getter
@ToString
public class EnvironmentConfiguration extends PalmBeachConfiguration<Environment> {

    // Constants.

    public static final String CLASS_PROPERTY = "class";
    public static final String CONTEXT_PROPERTY = "context";
    public static final String PHYSICAL_NETWORKS_PROPERTY = "physicalNetworks";

    // Variables.

    private final String environmentClass;
    private final String environmentName;
    private final Set<String> physicalNetworks;
    private final ContextConfiguration contextConfiguration;
    private final Map<String, Object> customValues;

    // Constructors.

    public EnvironmentConfiguration(@NonNull String environmentName, @NonNull Config baseConfig) {
        super(baseConfig);
        this.environmentClass = getBaseConfig().getString(CLASS_PROPERTY);
        this.environmentName = environmentName;
        this.contextConfiguration =
                getBaseConfig().hasPath(CONTEXT_PROPERTY) ? new ContextConfiguration(getBaseConfig().getConfig(CONTEXT_PROPERTY)) : null;

        this.physicalNetworks = new HashSet<>();
        if (getBaseConfig().hasPath(PHYSICAL_NETWORKS_PROPERTY)) {
            this.physicalNetworks.addAll(getBaseConfig().getStringList(PHYSICAL_NETWORKS_PROPERTY));
        }

        this.customValues = new HashMap<>();
        for (Map.Entry<String, ConfigValue> entry : getBaseConfig().entrySet()) {
            if (!entry.getKey().equals(CLASS_PROPERTY) && !entry.getKey().equals(CONTEXT_PROPERTY)) {
                this.customValues.put(entry.getKey(), entry.getValue().unwrapped());
            }
        }
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
