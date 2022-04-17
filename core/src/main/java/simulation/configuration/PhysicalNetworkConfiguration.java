package simulation.configuration;

import com.typesafe.config.Config;
import common.Context;
import environment.physical.PhysicalNetwork;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import simulation.configuration.exception.GenerationFailedException;

import static common.Tools.extractClass;

/**
 * Configuration for {@link PhysicalNetwork}.
 * <p>
 * Example of complete {@code PhysicalNetwork} configuration:
 * <pre>
 * physical-network.fullyConnected.class=environment.physical.PhysicalNetwork
 * physical-network.fullyConnected.context.class=context.CustomContext
 * physical-network.fullyConnected.context.key1="value1"
 * physical-network.fullyConnected.context.key2="value2"
 * </pre>
 */
@Getter
@ToString
public class PhysicalNetworkConfiguration extends PalmBeachConfiguration<PhysicalNetwork> {

    // Constants.

    public static final String CLASS_PROPERTY = "class";
    public static final String CONTEXT_PROPERTY = "context";

    // Variables.

    private final String physicalNetworkClass;
    private final String physicalNetworkName;
    private final ContextConfiguration contextConfiguration;

    // Constructors.

    public PhysicalNetworkConfiguration(@NonNull String physicalNetworkName, @NonNull Config baseConfig) {
        super(baseConfig);
        this.physicalNetworkClass = getBaseConfig().getString(CLASS_PROPERTY);
        this.physicalNetworkName = physicalNetworkName;
        this.contextConfiguration = getBaseConfig().hasPath(CONTEXT_PROPERTY) ?
                new ContextConfiguration(getBaseConfig().getConfig(CONTEXT_PROPERTY)) : null;
    }

    // Methods.

    @Override
    public PhysicalNetwork generate() throws GenerationFailedException {
        try {
            Context context = contextConfiguration != null ? contextConfiguration.generate() : null;
            return PhysicalNetwork.initiatePhysicalNetwork(extractClass(physicalNetworkClass), physicalNetworkName, context);
        } catch (Exception e) {
            throw new GenerationFailedException("Cannot generate PhysicalNetwork from configuration " + this, e);
        }
    }
}
