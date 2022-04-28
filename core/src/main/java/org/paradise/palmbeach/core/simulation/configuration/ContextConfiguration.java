package org.paradise.palmbeach.core.simulation.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import org.paradise.palmbeach.utils.context.Context;
import org.paradise.palmbeach.utils.context.SimpleContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.paradise.palmbeach.core.simulation.configuration.exception.GenerationFailedException;
import org.paradise.palmbeach.utils.reflection.ReflectionTools;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for {@link Context}.
 * <p>
 * Example of complete {@code Context} configuration:
 * <pre>
 * context.class=context.CustomContext
 * context.key1="value1"
 * context.key2="value2"
 * </pre>
 * <p>
 * A {@code ContextConfiguration} is always nested in another configuration.
 */
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

        this.contextClass = parseContextClass();

        this.contextKeyValue = new HashMap<>();
        for (Map.Entry<String, ConfigValue> entry : getBaseConfig().entrySet()) {
            if (!entry.getKey().equals(CLASS_PROPERTY)) {
                this.contextKeyValue.put(entry.getKey(), entry.getValue().unwrapped());
            }
        }
    }

    private String parseContextClass() {
        String cClass;
        if (getBaseConfig().hasPath(CLASS_PROPERTY))
            cClass = getBaseConfig().getString(CLASS_PROPERTY);
        else
            cClass = SimpleContext.class.getName();
        return cClass;
    }

    // Methods.

    @Override
    public Context generate() throws GenerationFailedException {
        try {
            Context context = Context.instantiateContext(ReflectionTools.extractClass(contextClass));
            if (!contextKeyValue.isEmpty()) {
                contextKeyValue.forEach(context::map);
            }
            return context;
        } catch (Exception e) {
            throw new GenerationFailedException("Fail to generate Context from config " + this, e);
        }
    }
}
