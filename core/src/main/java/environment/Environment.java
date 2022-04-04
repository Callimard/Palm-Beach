package environment;

import common.BasicContext;
import common.Context;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public abstract class Environment {

    // Variables.

    @Getter
    private final String name;

    @Getter
    private final Context context;

    // Constructors.

    protected Environment(String name) {
        this(name, null);
    }

    protected Environment(String name, Context context) {
        this.name = Optional.of(name).get();
        this.context = context != null ? context : new BasicContext();
    }

}
