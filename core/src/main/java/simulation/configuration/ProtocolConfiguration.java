package simulation.configuration;

import agent.SimpleAgent;
import agent.protocol.Protocol;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import common.Context;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import simulation.configuration.exception.GenerationFailedException;

import java.util.Map;

import static common.Tools.extractClass;

/**
 * Configuration for {@link Protocol}.
 * <p>
 * Example of complete {@code Protocol} configuration:
 * <pre>
 * protocol.transport.class=protocol.TransportProtocol
 * protocol.transport.context.class=context.CustomContext
 * protocol.transport.context.key1="value1"
 * protocol.transport.context.key2="value2"
 *
 * protocol.tendermint.class=protocol.TendermintProtocol
 * protocol.tendermint.context.class=context.CustomContext
 * protocol.tendermint.transport=transport
 *
 * protocol.hackProtocol.class=protocol.HackProtocol
 * </pre>
 */
@Getter
@ToString
@Slf4j
public class ProtocolConfiguration extends PalmBeachConfiguration<Void> {

    // Constants.

    public static final String CLASS_PROPERTY = "class";
    public static final String CONTEXT_PROPERTY = "context";

    // Variables.

    public final String protocolClass;
    private final ContextConfiguration contextConfiguration;
    private final Map<String, String> protocolDependencies;

    // Constructors.

    protected ProtocolConfiguration(@NonNull Config baseConfig) {
        super(baseConfig);
        this.protocolClass = getBaseConfig().getString(CLASS_PROPERTY);
        this.contextConfiguration = getBaseConfig().hasPath(CONTEXT_PROPERTY) ?
                new ContextConfiguration(getBaseConfig().getConfig(CONTEXT_PROPERTY)) : null;
        this.protocolDependencies = Maps.newHashMap();
        fillProtocolDependencies();
    }

    private void fillProtocolDependencies() {
        for (Map.Entry<String, ConfigValue> entry : getBaseConfig().entrySet()) {
            String key = entry.getKey();
            if (!key.equals(CLASS_PROPERTY) && !key.equals(CONTEXT_PROPERTY) && !key.contains(".")) {
                String value = (String) entry.getValue().unwrapped();
                this.protocolDependencies.put(key, value);
            }
        }
    }

    // Methods.

    @Override
    public Void generate() {
        throw new UnsupportedOperationException("Cannot generate Protocol directly without a SimpleAgent instance");
    }

    public Protocol generateProtocol(@NonNull SimpleAgent agent) throws GenerationFailedException {
        try {
            Context context = contextConfiguration != null ? contextConfiguration.generate() : null;
            return Protocol.instantiateProtocol(extractClass(protocolClass), agent, context);
        } catch (Exception e) {
            throw new GenerationFailedException("Cannot generate Protocol from configuration " + this, e);
        }
    }
}
