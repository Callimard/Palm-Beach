package simulation.configuration;

import agent.SimpleAgent;
import agent.protocol.Protocol;
import com.typesafe.config.Config;
import common.Context;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import simulation.configuration.exception.GenerationFailedException;

import static common.Tools.extractClass;

@Getter
@ToString
public class ProtocolConfiguration extends PalmBeachConfiguration<Void> {

    // Constants.

    public static final String CLASS_PROPERTY = "class";
    public static final String CONTEXT_PROPERTY = "context";

    // Variables.

    public final String protocolClass;
    private final ContextConfiguration contextConfiguration;

    // Constructors.

    protected ProtocolConfiguration(@NonNull Config baseConfig) {
        super(baseConfig);
        this.protocolClass = getBaseConfig().getString(CLASS_PROPERTY);
        this.contextConfiguration = getBaseConfig().hasPath(CONTEXT_PROPERTY) ?
                new ContextConfiguration(getBaseConfig().getConfig(CONTEXT_PROPERTY)) : null;
    }

    // Methods.

    @Override
    public Void generate() throws GenerationFailedException {
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
