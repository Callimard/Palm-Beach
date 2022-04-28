package org.paradise.palmbeach.core.simulation.configuration;

import com.typesafe.config.Config;
import org.paradise.palmbeach.utils.context.Context;
import org.paradise.palmbeach.core.environment.Environment;
import org.paradise.palmbeach.core.environment.network.Network;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.paradise.palmbeach.core.simulation.configuration.exception.GenerationFailedException;
import org.paradise.palmbeach.utils.reflection.ReflectionTools;

/**
 * Configuration for {@link Network}.
 * <p>
 * Example of complete {@code Network} configuration:
 * <pre>
 * network.fullyConnected.class=environment.network.Network
 * network.fullyConnected.context.class=context.CustomContext
 * network.fullyConnected.context.key1="value1"
 * network.fullyConnected.context.key2="value2"
 * </pre>
 */
@Getter
@ToString
public class NetworkConfiguration extends PalmBeachConfiguration<Network> {

    // Constants.

    public static final String CLASS_PROPERTY = "class";
    public static final String CONTEXT_PROPERTY = "context";

    // Variables.

    private final String networkClass;
    private final String networkName;
    private final ContextConfiguration contextConfiguration;

    // Constructors.

    public NetworkConfiguration(@NonNull String networkName, @NonNull Config baseConfig) {
        super(baseConfig);
        this.networkClass = getBaseConfig().getString(CLASS_PROPERTY);
        this.networkName = networkName;
        this.contextConfiguration = getBaseConfig().hasPath(CONTEXT_PROPERTY) ?
                new ContextConfiguration(getBaseConfig().getConfig(CONTEXT_PROPERTY)) : null;
    }

    // Methods.

    /**
     * @return Nothing, unsupported operation
     *
     * @throws UnsupportedOperationException see {@link #generateNetwork(Environment)}
     * @see #generateNetwork(Environment)
     */
    @Override
    public Network generate() {
        throw new UnsupportedOperationException();
    }

    public Network generateNetwork(Environment environment) throws GenerationFailedException {
        try {
            Context context = contextConfiguration != null ? contextConfiguration.generate() : null;
            return Network.initiateNetwork(ReflectionTools.extractClass(networkClass), networkName, environment, context);
        } catch (Exception e) {
            throw new GenerationFailedException("Cannot generate Network from configuration " + this, e);
        }
    }
}
