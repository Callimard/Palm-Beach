package agent;

import agent.exception.*;
import behavior.Behavior;
import common.BasicContext;
import common.Context;
import environment.Environment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import protocol.Protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An {@code Agent} of the simulation. An {@code Agent} can be the abstraction of a machine, a processus or even a thread.
 * <p>
 * An {@code BasicAgent} always evolves in one {@link Environment} and use several {@link Protocol}s. An agent can add several {@link Behavior}s and
 * during the simulation, the {@code Agent} can dynamically start and stop to play the different {@code Behavior} that it has.
 */
@ToString
@Slf4j
public class BasicAgent {

    // Variables.

    @Getter
    private final AgentIdentifier identifier;

    @Getter
    private final Environment environment;

    @Getter
    private final Context context;

    private final Map<Class<? extends Protocol>, Protocol> protocols;
    private final Map<Class<? extends Behavior>, Behavior> behaviors;

    @Getter
    private AgentState state;

    private final List<AgentObserver> observers;

    // Constructors.

    /**
     * Construct a {@link BasicAgent} with a unique {@link AgentIdentifier} and an {@link Environment}. The {@code BasicAgent} context is not
     * specified that it means the initial context is empty and the default class use for the {@code BasicAgent} will be {@link BasicContext}.
     *
     * @param identifier  the unique identifier of the {@code BasicAgent}
     * @param environment the environment where the {@code BasicAgent} evolves
     */
    public BasicAgent(AgentIdentifier identifier, Environment environment) {
        this(identifier, environment, null);
    }

    /**
     * Constructs a {@link BasicAgent} with a unique {@link AgentIdentifier}, an {@link Environment} and a {@link Context}. The context parameter is
     * here to allow the {@code BasicAgent} to begin with an initial context and allow the user to specify any subclass of context. If context is
     * null, the default class use is {@link BasicContext}.
     *
     * @param identifier  the unique identifier of the {@code BasicAgent}
     * @param environment the environment where the {@code BasicAgent} evolves
     * @param context     the context of the {@code BasicAgent}
     *
     * @throws NullPointerException if specified identifier or environment is/are null.
     */
    public BasicAgent(@NonNull AgentIdentifier identifier, @NonNull Environment environment, Context context) {
        this.identifier = identifier;
        this.environment = environment;
        this.context = context != null ? context : new BasicContext();

        this.protocols = new ConcurrentHashMap<>();
        this.behaviors = new ConcurrentHashMap<>();

        this.state = AgentState.CREATED;

        this.observers = new Vector<>();

        log.info(identifier + " has been created");
    }

    // Methods.

    /**
     * Add observer. Observer must be not null.
     *
     * @param observer the observer
     *
     * @throws NullPointerException if observer is null
     */
    public void addObserver(@NonNull AgentObserver observer) {
        observers.add(observer);
    }

    /**
     * Start the {@link BasicAgent}. This method start the {@code BasicAgent} only if it is either in the state {@link AgentState#CREATED} either in
     * the state {@link AgentState#STOPPED}, else throws an exception.
     *
     * @throws AgentCannotBeStartedException if the {@code BasicAgent} is not in a correct state
     * @see AgentState
     */
    public synchronized void start() {
        if (canBeStarted()) {
            setStarted();
            onStart();
            log.info(this.identifier + " started");
        } else
            throw new AgentCannotBeStartedException(this);
    }

    private boolean canBeStarted() {
        return state.equals(AgentState.CREATED) || state.equals(AgentState.STOPPED);
    }

    private void setStarted() {
        state = AgentState.STARTED;
    }

    protected void onStart() {
        notifyAgentStarted();
    }

    private void notifyAgentStarted() {
        for (AgentObserver observer : observers) {
            observer.agentStarted();
        }
    }

    /**
     * @return true if the current state of the {@link BasicAgent} is {@link AgentState#STARTED}
     */
    public synchronized boolean isStarted() {
        return state.equals(AgentState.STARTED);
    }

    /**
     * Stop the {@link BasicAgent}. This method stop the {@code BasicAgent} only if it is in the state {@link AgentState#STARTED}, else throws an
     * exception.
     */
    public synchronized void stop() {
        if (canBeStopped()) {
            setStopped();
            onStop();
            log.info(this.identifier + " stopped");
        } else
            throw new AgentCannotBeStoppedException(this);
    }

    private boolean canBeStopped() {
        return state.equals(AgentState.STARTED);
    }

    private void setStopped() {
        state = AgentState.STOPPED;
    }

    protected void onStop() {
        notifyAgentStopped();
    }

    private void notifyAgentStopped() {
        for (AgentObserver observer : observers) {
            observer.agentStopped();
        }
    }

    /**
     * @return true if the current state of the {@link BasicAgent} is {@link AgentState#STOPPED}
     */
    public synchronized boolean isStopped() {
        return state.equals(AgentState.STOPPED);
    }

    /**
     * Kill the {@link BasicAgent}. This method kill the {@code BasicAgent} only if the {@code BasicAgent} is not already in the state {@link
     * AgentState#KILLED}.
     *
     * <strong>If a {@code BasicAgent} is killed, it cannot be started or stopped anymore.</strong>
     */
    public synchronized void kill() {
        if (canBeKilled()) {
            setKilled();
            onKill();
            log.info(this.identifier + " killed");
        } else
            throw new AgentCannotBeKilledException(this);
    }

    private boolean canBeKilled() {
        return state.equals(AgentState.CREATED) || state.equals(AgentState.STARTED) || state.equals(AgentState.STOPPED);
    }

    private void setKilled() {
        state = AgentState.KILLED;
    }

    private void onKill() {
        notifyAgentKilled();
    }

    private void notifyAgentKilled() {
        for (AgentObserver observer : observers) {
            observer.agentKilled();
        }
    }

    /**
     * @return true if the current state of the {@link BasicAgent} is {@link AgentState#KILLED}
     */
    public synchronized boolean isKilled() {
        return state.equals(AgentState.KILLED);
    }

    /**
     * Try to create an instance of the specified {@link Protocol} class.
     * <p>
     * A {@code Protocol} must have at least a constructor which as this specified form:
     * <pre>
     *     Protocol(BasicAgent, Context) {
     *       super(...);
     *       ...
     *     }
     * </pre>
     *
     * @param protocolClass the protocol class
     */
    public void addProtocol(Class<? extends Protocol> protocolClass) {
        try {
            Protocol protocol = Protocol.instantiateProtocol(protocolClass, this);
            if (protocols.putIfAbsent(protocolClass, protocol) == null) {
                // Protocol added
                addObserver(protocol);
                log.info("Protocol " + protocolClass.getSimpleName() + " added to the Agent " + identifier);
            } else
                // Already added protocol
                log.info(identifier + " try to add an already added Protocol " + protocolClass);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new FailToAddProtocolException(protocolClass, e);
        }
    }

    /**
     * Verify if the {@link BasicAgent} has already added a {@link Protocol} for the specified class or not.
     *
     * @param protocolClass the protocol class to verify
     *
     * @return true if the {@code BasicAgent} has already a {@code Protocol} for this {@code Protocol} class, else false.
     */
    public boolean hasProtocol(Class<? extends Protocol> protocolClass) {
        return protocols.containsKey(protocolClass);
    }

    /**
     * @param protocolClass the protocol class
     *
     * @return the instance of the {@link Protocol} if the {@link BasicAgent} has added a {@code Protocol} for the specified class, else null.
     *
     * @see #hasProtocol(Class)
     */
    public <T extends Protocol> T getProtocol(Class<T> protocolClass) {
        //noinspection unchecked
        return (T) protocols.get(protocolClass);
    }

    public void addBehavior(Class<? extends Behavior> behaviorClass) {
        try {
            if (behaviors.putIfAbsent(behaviorClass, Behavior.instantiateBehavior(behaviorClass, this)) == null)
                // Added Behavior
                log.info(identifier + " add the Behavior " + behaviorClass);
            else
                // Already added Behavior
                log.info(identifier + " try to add an already added Behavior " + behaviorClass);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new FailToAddBehaviorException(behaviorClass);
        }
    }

    public boolean hasBehavior(Class<? extends Behavior> behaviorClass) {
        return behaviors.containsKey(behaviorClass);
    }

    public <T extends Behavior> T getBehavior(Class<T> behaviorClass) {
        //noinspection unchecked
        return (T) behaviors.get(behaviorClass);
    }

    public void playBehavior(Class<? extends Behavior> behaviorClass) {
        behaviors.get(behaviorClass).play();
    }

    public void stopPlayBehavior(Class<? extends Behavior> behaviorClass) {
        behaviors.get(behaviorClass).stopPlay();
    }

    // Inner classes.

    /**
     * Represent the state of a {@link BasicAgent}.
     * <p>
     * After the construction, a {@code BasicAgent} is first {@link AgentState#CREATED}. After a call of the method {@link #start()}, the state pass
     * to {@link AgentState#STOPPED}.
     * <p>
     * From any state, with the method {@link #kill()}, the {@code BasicAgent} pass to {@link AgentState#KILLED}.
     */
    public enum AgentState {
        CREATED, STARTED, STOPPED, KILLED
    }

    /**
     * A class to uniquely identify a {@link BasicAgent}. This is a sort of primary key of {@code BasicAgent}.
     * <p>
     * This class is abstract to force subclasses to implement {@link Object#hashCode()} and {@link Object#equals(Object)}.
     * <p>
     * A subclass which implements {@code AgentIdentifier} must verify this assertion: for all two agents <i>a0</i> and <i>a1</i> in the simulation,
     * {@code a0.getIdentifier() != a1.getIdentifier()} is always true.
     */
    public abstract static class AgentIdentifier {

        @Override
        public abstract int hashCode();

        @Override
        public abstract boolean equals(Object obj);

    }

    /**
     * A simple implementation of {@link AgentIdentifier}. {@code SimpleIdentifier} identifies a {@code BasicAgent} with its name and its unique id.
     */
    @ToString
    @Getter
    @AllArgsConstructor
    public static class SimpleAgentIdentifier extends AgentIdentifier {

        // Static.

        /**
         * Counter to generate unique id.
         */
        private static long currentId = 0L;

        // Variables.

        private final @NonNull String agentName;
        private final long uniqueId;

        // Methods.

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SimpleAgentIdentifier that)) return false;
            return uniqueId == that.uniqueId && agentName.equals(that.agentName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(agentName, uniqueId);
        }

        /**
         * Generate the next unique id for a {@link BasicAgent}. If this methos is always used to generate {@code BasicAgent} identifier unique id, it
         * is guaranty that each {@code BasicAgent} will have different id.
         *
         * @return the next generated unique id.
         */
        public static synchronized long nextId() {
            return currentId++;
        }
    }

    /**
     * Call back interface to be notified when the {@link BasicAgent} change its state.
     */
    public interface AgentObserver {

        /**
         * Call back method used when the {@link BasicAgent} is started with the method {@link BasicAgent#start()}.
         */
        void agentStarted();

        /**
         * Call back method used when the {@link BasicAgent} is stopped with the method {@link BasicAgent#stop()}.
         */
        void agentStopped();

        /**
         * Call back method used when the {@link BasicAgent} is killed with the method {@link BasicAgent#kill()}.
         */
        void agentKilled();

    }
}
