package simulation.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import common.Context;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import simulation.configuration.exception.GenerationFailedException;

import java.util.HashMap;
import java.util.Map;

import static common.Tools.extractClass;

@Getter
@ToString
public class ContextConfiguration extends PalmBeachConfiguration<Context> {

    // Constants.

    public static final String CLASS_PROPERTY = "class";

    // Variables.

    private final String contextClass;
    private final Map<String, Object> contextKeyValue;

    // Constructors.

    public ContextConfiguration(@NonNull Config baseConfig) {
        super(baseConfig);
        this.contextClass = getBaseConfig().getString(CLASS_PROPERTY);

        this.contextKeyValue = new HashMap<>();
        for (Map.Entry<String, ConfigValue> entry : getBaseConfig().entrySet()) {
            if (!entry.getKey().equals(CLASS_PROPERTY)) {
                this.contextKeyValue.put(entry.getKey(), entry.getValue().unwrapped());
            }
        }
    }

    // Methods.

    @Override
    public Context generate() throws GenerationFailedException {
        try {
            Context context = Context.instantiateContext(extractClass(contextClass));
            if (!contextKeyValue.isEmpty()) {
                contextKeyValue.forEach(context::map);
            }
            return context;
        } catch (Exception e) {
            throw new GenerationFailedException("Fail to generate Context from config " + this, e);
        }
    }
}
