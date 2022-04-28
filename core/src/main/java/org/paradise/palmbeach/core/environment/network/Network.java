package org.paradise.palmbeach.core.environment.network;

import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.agent.exception.AgentNotStartedException;
import org.paradise.palmbeach.utils.context.Context;
import org.paradise.palmbeach.utils.context.SimpleContext;
import org.paradise.palmbeach.core.environment.Environment;
import org.paradise.palmbeach.core.event.Event;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.paradise.palmbeach.core.simulation.PalmBeachSimulation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
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
            throw new AgentNotStartedException("Cannot send Message, Agent " + source + " is not in STARTED state");
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
     * @throws NullPointerException  if agent is null
     */
    public abstract Set<SimpleAgent.AgentIdentifier> directNeighbors(@NonNull SimpleAgent.AgentIdentifier agent);

    /**
     * @return the set of all {@link Connection}, never returns null.
     */
    public abstract Set<Connection> allConnections();

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

    // Inner classes.

    @ToString
    @RequiredArgsConstructor
    public abstract static class Connection {

        // Variables.

        @NonNull
        @Getter
        private final SimpleAgent.AgentIdentifier a0;

        @Getter
        private final SimpleAgent.AgentIdentifier a1;

        // Constructors.

        protected Connection(@NonNull SimpleAgent.AgentIdentifier a0) {
            this(a0, null);
        }

        // Methods.

        @Override
        public abstract boolean equals(Object o);

        @Override
        public abstract int hashCode();

        public boolean isSelfConnection() {
            return a0.equals(a1) || a1 == null;
        }
    }

    public static class NonOrientedConnection extends Connection {

        // Constructors.

        public NonOrientedConnection(@NonNull SimpleAgent.AgentIdentifier a0, SimpleAgent.AgentIdentifier a1) {
            super(a0, a1);
        }

        // Methods.

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NonOrientedConnection that)) return false;
            return getA0().equals(that.getA0()) && Objects.equals(getA1(), that.getA1()) ||
                    getA0().equals(that.getA1()) && getA1().equals(that.getA0());
        }

        @Override
        public int hashCode() {
            int h0 = Objects.hash(getA0());
            int h1 = Objects.hash(getA1());

            if (h0 < h1)
                return Objects.hash(getA0(), getA1());
            else
                return Objects.hash(getA1(), getA0());
        }
    }

    // Exceptions.

    public static class NotInNetworkException extends RuntimeException {
        public NotInNetworkException(String s) {
            super(s);
        }
    }
}
