package protocol;

import agent.BasicAgent;
import common.BasicContext;
import common.Context;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * Represents {@code Protocol} that a {@link BasicAgent} can use.
 * <p>
 * A {@code Protocol} is the base of the simulation. It is here where the different algorithms that we want to simulate and to test will be
 * implemented.
 * <p>
 * A {@code Protocol} can for example be a <i>Message Transporter</i> or a more complex algorithm than a <i>Consensus Algorithm</i>.
 *
 * <strong>Implementation instructions:</strong> a subclass of {@code Protocol} must add a constructor like this:
 * <pre>
 *     Protocol(BasicAgent) {
 *      super(...);
 *      ...
 *     }
 * </pre>
 */
@ToString
public abstract class Protocol implements BasicAgent.AgentObserver {

    // Variables.

    @Getter
    private final BasicAgent agent;

    @Getter
    private final Context context;

    // Constructors.

    /**
     * Constructs a {@link Protocol} with a specified {@link BasicAgent} and an empty {@link Context} with the default class {@link BasicContext}.
     *
     * @param agent the agent
     *
     * @throws NullPointerException if the specified agent is null
     */
    protected Protocol(BasicAgent agent) {
        this(agent, null);
    }

    /**
     * Constructs a {@link Protocol} with a specified {@link BasicAgent} and a specified initial {@link Context}. If the {@code Context} is null,
     * create an empty {@code Context} with the default class {@link BasicContext}.
     *
     * @param agent   the agent
     * @param context the context
     *
     * @throws NullPointerException if the specified agent is null
     */
    protected Protocol(BasicAgent agent, Context context) {
        this.agent = Optional.of(agent).get();
        this.context = context != null ? context : new BasicContext();
    }

    // Methods.

    /**
     * Try to create an instance of the specified {@link Protocol} class with the specified {@link BasicAgent}. Create an empty initial context for
     * the {@code Protocol}. The class use for the context is {@link BasicContext}.
     *
     * @param protocolClass the {@code Protocol} class
     * @param agent         the agent with which the {@code Protocol} will be instantiated
     *
     * @return an instance of a {@code Protocol} of the specified {@code Protocol} class.
     *
     * @throws NoSuchMethodException     if the {@code Protocol} class does not have the specific needed constructor
     * @throws InvocationTargetException if the constructor has thrown an exception
     * @throws InstantiationException    if the instantiation failed
     * @throws IllegalAccessException    if the construct is not accessible
     */
    public static Protocol instantiateProtocol(Class<? extends Protocol> protocolClass, BasicAgent agent)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Protocol> protocolConstructor = protocolClass.getConstructor(BasicAgent.class);
        return protocolConstructor.newInstance(agent);
    }
}
