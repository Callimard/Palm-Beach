package environment.network;

import agent.SimpleAgent;
import common.Context;
import common.SimpleContext;
import environment.Environment;
import event.Event;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import simulation.PalmBeachSimulation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Simulate a {@code Network}. A {@code Network} represents connection between several {@link SimpleAgent}. Via the {@code Network} it is possible to
 * send {@link Event} and it is the {@code Network} which manage if yse or not, the source agent can reach the target agent and therefore if yes or
 * not the {@code Event} can be sent.
 * <p>
 * All {@code Network} subclasses must have this constructor:
 * <pre>
 *     Network(String name, Environment environment, Context context) {
 *         ...
 *     }
 * </pre>
 */
@ToString
@Slf4j
public abstract class Network implements Environment.EnvironmentObserver {

    // Variables.

    @Getter
    @NonNull
    private final String name;

    @Getter
    private final Context context;

    @ToString.Exclude
    @Getter
    private final Environment environment;

    // Constructors.

    protected Network(@NonNull String name, @NonNull Environment environment, Context context) {
        this.name = name;
        this.environment = environment;
        this.environment.addObserver(this);
        this.context = context != null ? context : new SimpleContext();
    }

    // Methods.

    /**
     * Create an instance of the specified {@link Network} class. The specified class must have a construct as described in the general doc of {@code
     * Network}.
     *
     * @param networkClass the Network class name
     * @param networkName  the Network name
     * @param environment  the Network associated Environment
     * @param context      the Network context
     *
     * @return a new instance of the specified {@code Network} class
     *
     * @throws NoSuchMethodException     if the {@code Network} class does not have the specific needed constructor
     * @throws InvocationTargetException if the constructor has thrown an exception
     * @throws InstantiationException    if the instantiation failed
     * @throws IllegalAccessException    if the construct is not accessible
     * @throws NullPointerException      if networkClass, networkName or environment is null
     */
    public static Network initiateNetwork(@NonNull Class<? extends Network> networkClass,
                                          @NonNull String networkName,
                                          @NonNull Environment environment,
                                          Context context)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Network> constructor = networkClass.getConstructor(String.class, Environment.class, Context.class);
        return constructor.newInstance(networkName, environment, context);
    }

    /**
     * Sends the {@link Event} from the source to the target. First check if from the source, the target is reachable with the method {@link
     * #hasConnection(SimpleAgent.AgentIdentifier, SimpleAgent.AgentIdentifier)}. If it is the case, simulate sending with the method {@link
     * #simulateSending(SimpleAgent.AgentIdentifier, SimpleAgent.AgentIdentifier, Event)}.
     *
     * @param source the source agent
     * @param target the target agent
     * @param event  the event to send
     *
     * @throws NullPointerException if source, target or event is null
     */
    public final synchronized void send(@NonNull SimpleAgent.AgentIdentifier source, @NonNull SimpleAgent.AgentIdentifier target,
                                        @NonNull Event<?> event) {
        if (PalmBeachSimulation.getAgent(source).isStarted()) {
            if (hasConnection(source, target))
                simulateSending(source, target, event);
            else
                log.debug("Agent source " + source + " is not connected to target " + target + " by the Network " + this);
        } else
            log.debug("Agent source is not STARTED -> cannot send via the Network {}", this);
    }

    /**
     * Verifies if from the source, the target agent is reachable. The order is important and this function is not commutative. It means that if it is
     * true for {@code source -> target}, it can be false for {@code target -> source}.
     *
     * @param source the source agent
     * @param target the target agent
     *
     * @return true if from the source, the target agent is reachable, else false.
     *
     * @throws NullPointerException if source or target is null
     */
    public abstract boolean hasConnection(@NonNull SimpleAgent.AgentIdentifier source, @NonNull SimpleAgent.AgentIdentifier target);

    /**
     * @param agent the agent to verify the connection
     *
     * @return a set which contains all {@link SimpleAgent.AgentIdentifier} directly connected to the specified agent. The set is never null and
     * contains at least the specified agent itself.
     *
     * @throws NotInNetworkException if the agent is not in network
     *
     * @throws NullPointerException if agent is null
     */
    public abstract Set<SimpleAgent.AgentIdentifier> agentDirectConnections(@NonNull SimpleAgent.AgentIdentifier agent);

    /**
     * Simulate the sending of the {@link Event} from the source to the target.
     *
     * @param source the source agent
     * @param target the target agent
     * @param event  the event
     *
     * @throws NullPointerException if source, target or event is null
     */
    protected abstract void simulateSending(@NonNull SimpleAgent.AgentIdentifier source, @NonNull SimpleAgent.AgentIdentifier target,
                                            @NonNull Event<?> event);

    // Exceptions.

    public static class NotInNetworkException extends RuntimeException {
        public NotInNetworkException(String s) {
            super(s);
        }
    }
}
