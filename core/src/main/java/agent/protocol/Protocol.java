package agent.protocol;

import agent.SimpleAgent;
import common.SimpleContext;
import common.Context;
import lombok.*;
import event.EventCatcher;
import agent.protocol.exception.NullDefaultProtocolManipulatorException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Represents {@code Protocol} that a {@link SimpleAgent} can use.
 * <p>
 * A {@code Protocol} is the base of the simulation. It is here where the different algorithms that we want to simulate and to test will be
 * implemented.
 * <p>
 * A {@code Protocol} can for example be a <i>Message Transporter</i> or a more complex algorithm than a <i>Consensus Algorithm</i>.
 *
 * <strong>Implementation instructions:</strong> a subclass of {@code Protocol} must add a constructor like this:
 * <pre>
 *     Protocol(SimpleAgent) {
 *      super(...);
 *      ...
 *     }
 * </pre>
 */
@ToString
public abstract class Protocol implements SimpleAgent.AgentObserver, EventCatcher {

    // Variables.

    @Getter
    private final SimpleAgent agent;

    @Getter
    private final Context context;

    @Getter
    @Setter
    @NonNull
    private ProtocolManipulator manipulator;

    // Constructors.

    /**
     * Constructs a {@link Protocol} with a specified {@link SimpleAgent} and an empty {@link Context} with the default class {@link SimpleContext}.
     *
     * @param agent the agent
     *
     * @throws NullPointerException if the specified agent is null
     */
    protected Protocol(@NonNull SimpleAgent agent) {
        this(agent, null);
    }

    /**
     * Constructs a {@link Protocol} with a specified {@link SimpleAgent} and a specified initial {@link Context}. If the {@code Context} is null,
     * create an empty {@code Context} with the default class {@link SimpleContext}.
     *
     * @param agent   the agent
     * @param context the context
     *
     * @throws NullPointerException if the specified agent is null
     */
    protected Protocol(@NonNull SimpleAgent agent, Context context) {
        this.agent = agent;
        this.context = context != null ? context : new SimpleContext();
        this.manipulator = defaultProtocolManipulator();
        if (manipulator == null)
            throw new NullDefaultProtocolManipulatorException("The default ProtocolManipulator of the Protocol class " + this.getClass() + " is " +
                                                                      "null (re implement the method defaultProtocolManipulator() to avoid that it " +
                                                                      "returns null value");
    }

    // Methods.

    /**
     * Try to create an instance of the specified {@link Protocol} class with the specified {@link SimpleAgent}. Create an empty initial context for
     * the {@code Protocol}. The class use for the context is {@link SimpleContext}.
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
    public static Protocol instantiateProtocol(Class<? extends Protocol> protocolClass, SimpleAgent agent)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Protocol> protocolConstructor = protocolClass.getConstructor(SimpleAgent.class);
        return protocolConstructor.newInstance(agent);
    }

    /**
     * Returns an instance of the default {@link ProtocolManipulator} of the {@link Protocol}. It is to the user to decide if at each call, a new
     * instance is created or an instance is created once and reuse after. However, the class of the instance returned must always be same at any
     * invocation.
     *
     * @return an instance of the default {@code ProtocolManipulator} of the current {@code Protocol}.
     */
    protected abstract ProtocolManipulator defaultProtocolManipulator();

    /**
     * Use the method {@link #defaultProtocolManipulator()} to set the {@link ProtocolManipulator} to the default {@code ProtocolManipulator} of the
     * {@link Protocol}.
     */
    public void resetDefaultProtocolManipulator() {
        manipulator = defaultProtocolManipulator();
    }

    // Inner classes.

    /**
     * Represents a manipulator which can control a {@link Protocol}. A {@code Protocol} get have a part of its code that can be change. Therefore,
     * the {@code Protocol} can define an Interface that several {@code ProtocolManipulator} can implement with different implementation. In that way,
     * when a {@code Protocol} change its manipulator, the implementation will also change and depends on the new {@code ProtocolManipulator.
     * <p>
     * This feature can be use by {@link agent.behavior.Behavior}. Indeed, when a {@code Behavior} is started to be played, the {@code Behavior} can for
     * example change {@code ProtocolManipulator} of several {@code Protocols}.
     *
     * <strong>WARNING! A {@code ProtocolManipulator} is destined to ONE instance of a {@code Protocol}. The manipulated agent.protocol instance is always
     * the same and cannot change.</strong>
     */
    @Getter
    @AllArgsConstructor
    public abstract static class ProtocolManipulator {

        // Variables.

        @Getter
        @NonNull
        private final Protocol manipulatedProtocol;
    }

    /**
     * Default implementation of {@link ProtocolManipulator}. This {@code ProtocolManipulator} do nothing and allow {@link Protocol} which does not
     * need to have a {@code ProtocolManipulator} to be instantiated.
     * <p>
     * It must be instantiated and returned by the method {@link #defaultProtocolManipulator()}.
     */
    public static class DefaultProtocolManipulator extends ProtocolManipulator {

        public DefaultProtocolManipulator(@NonNull Protocol manipulatedProtocol) {
            super(manipulatedProtocol);
        }
    }
}
