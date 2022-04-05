package agent;

import agent.exception.*;
import behavior.Behavior;
import common.SimpleContext;
import common.Context;
import environment.Environment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import protocol.Protocol;
import protocol.event.Event;
import protocol.event.EventCatcher;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An {@code Agent} of the simulation. An {@code Agent} can be the abstraction of a machine, a processus or even a thread.
 * <p>
 * An {@code SimpleAgent} always evolves in one {@link Environment} and use several {@link Protocol}s. An agent can add several {@link Behavior}s and
 * during the simulation, the {@code Agent} can dynamically start and stop to play the different {@code Behavior} that it has.
 */
@ToString
@Slf4j
public class SimpleAgent implements EventCatcher {

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
     * Construct a {@link SimpleAgent} with a unique {@link AgentIdentifier} and an {@link Environment}. The {@code SimpleAgent} context is not
     * specified that it means the initial context is empty and the default class use for the {@code SimpleAgent} will be {@link SimpleContext}.
     *
     * @param identifier  the unique identifier of the {@code SimpleAgent}
     * @param environment the environment where the {@code SimpleAgent} evolves
     */
    public SimpleAgent(@NonNull AgentIdentifier identifier, @NonNull Environment environment) {
        this(identifier, environment, null);
    }

    /**
     * Constructs a {@link SimpleAgent} with a unique {@link AgentIdentifier}, an {@link Environment} and a {@link Context}. The context parameter is
     * here to allow the {@code SimpleAgent} to begin with an initial context and allow the user to specify any subclass of context. If context is
     * null, the default class use is {@link SimpleContext}.
     *
     * @param identifier  the unique identifier of the {@code SimpleAgent}
     * @param environment the environment where the {@code SimpleAgent} evolves
     * @param context     the context of the {@code SimpleAgent}
     *
     * @throws NullPointerException if specified identifier or environment is/are null.
     */
    public SimpleAgent(@NonNull AgentIdentifier identifier, @NonNull Environment environment, Context context) {
        this.identifier = identifier;
        this.environment = environment;
        this.context = context != null ? context : new SimpleContext();

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
     * Start the {@link SimpleAgent}. This method start the {@code SimpleAgent} only if it is either in the state {@link AgentState#CREATED} either in
     * the state {@link AgentState#STOPPED}, else throws an exception.
     *
     * @throws AgentCannotBeStartedException if the {@code SimpleAgent} is not in a correct state
     * @see AgentState
     */
    public final synchronized void start() {
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
     * @return true if the current state of the {@link SimpleAgent} is {@link AgentState#STARTED}
     */
    public synchronized boolean isStarted() {
        return state.equals(AgentState.STARTED);
    }

    /**
     * Stop the {@link SimpleAgent}. This method stop the {@code SimpleAgent} only if it is in the state {@link AgentState#STARTED}, else throws an
     * exception.
     */
    public final synchronized void stop() {
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
     * @return true if the current state of the {@link SimpleAgent} is {@link AgentState#STOPPED}
     */
    public synchronized boolean isStopped() {
        return state.equals(AgentState.STOPPED);
    }

    /**
     * Kill the {@link SimpleAgent}. This method kill the {@code SimpleAgent} only if the {@code SimpleAgent} is not already in the state {@link
     * AgentState#KILLED}.
     *
     * <strong>If a {@code SimpleAgent} is killed, it cannot be started or stopped anymore.</strong>
     */
    public final synchronized void kill() {
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
     * @return true if the current state of the {@link SimpleAgent} is {@link AgentState#KILLED}
     */
    public synchronized boolean isKilled() {
        return state.equals(AgentState.KILLED);
    }

    /**
     * Try to create an instance of the specified {@link Protocol} class.
     * <p>
     * A {@code Protocol} must have at least a constructor which as this specified form:
     * <pre>
     *     Protocol(SimpleAgent, Context) {
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
     * Verify if the {@link SimpleAgent} has already added a {@link Protocol} for the specified class or not.
     *
     * @param protocolClass the protocol class to verify
     *
     * @return true if the {@code SimpleAgent} has already a {@code Protocol} for this {@code Protocol} class, else false.
     */
    public boolean hasProtocol(Class<? extends Protocol> protocolClass) {
        return protocols.containsKey(protocolClass);
    }

    /**
     * @param protocolClass the protocol class
     *
     * @return the instance of the {@link Protocol} if the {@link SimpleAgent} has added a {@code Protocol} for the specified class, else null.
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

    /**
     * Process the {@link Event} only if the {@link SimpleAgent} is in the {@link AgentState#STARTED} state. Search each {@link Protocol} which can
     * process the {@code Event} with the method {@link EventCatcher#canProcessEvent(Event)}. All {@code Protocols} added in the {@code SimpleAgent}
     * which can process the {@code Event} will process the {@code Event}.
     *
     * @param event the event
     *
     * @throws AgentNotStartedException if the {@code SimpleAgent} is not in the {@link AgentState#STARTED} state.
     */
    @Override
    public final synchronized void processEvent(Event<?> event) {
        if (isStarted()) {
            boolean eventHasBeenProcessed = searchProtocolForEvent(event);
            if (!eventHasBeenProcessed)
                throw new AgentCannotProcessEventException(this, event);
        } else
            throw new AgentNotStartedException(this);
    }

    /**
     * Browses all added {@link Protocol} in the {@link SimpleAgent} and call the method {@link EventCatcher#processEvent(Event)} of each {@code
     * Protocol} which can be process the {@code Event} (which returns true with the call of the method {@link EventCatcher#canProcessEvent(Event)}).
     *
     * @param event the event to process
     */
    private boolean searchProtocolForEvent(Event<?> event) {
        boolean hasBeenProcessed = false;
        for (Protocol p : protocols.values()) {
            if (p.canProcessEvent(event)) {
                p.processEvent(event);
                hasBeenProcessed = true;
            }
        }

        return hasBeenProcessed;
    }

    /**
     * @param event the event
     *
     * @return true if it exists at least one {@link Protocol} which can process the {@link Event}, else false.
     */
    @Override
    public boolean canProcessEvent(Event<?> event) {
        for (Protocol p : protocols.values())
            if (p.canProcessEvent(event))
                return true;

        return false;
    }

    // Inner classes.

    /**
     * Represent the state of a {@link SimpleAgent}.
     * <p>
     * After the construction, a {@code SimpleAgent} is first {@link AgentState#CREATED}. After a call of the method {@link #start()}, the state pass
     * to {@link AgentState#STOPPED}.
     * <p>
     * From any state, with the method {@link #kill()}, the {@code SimpleAgent} pass to {@link AgentState#KILLED}.
     */
    public enum AgentState {
        CREATED, STARTED, STOPPED, KILLED
    }

    /**
     * A class to uniquely identify a {@link SimpleAgent}. This is a sort of primary key of {@code SimpleAgent}.
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
     * A simple implementation of {@link AgentIdentifier}. {@code SimpleIdentifier} identifies a {@code SimpleAgent} with its name and its unique id.
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
         * Generate the next unique id for a {@link SimpleAgent}. If this methos is always used to generate {@code SimpleAgent} identifier unique id, it
         * is guaranty that each {@code SimpleAgent} will have different id.
         *
         * @return the next generated unique id.
         */
        public static synchronized long nextId() {
            return currentId++;
        }
    }

    /**
     * Call back interface to be notified when the {@link SimpleAgent} change its state.
     */
    public interface AgentObserver {

        /**
         * Call back method used when the {@link SimpleAgent} is started with the method {@link SimpleAgent#start()}.
         */
        void agentStarted();

        /**
         * Call back method used when the {@link SimpleAgent} is stopped with the method {@link SimpleAgent#stop()}.
         */
        void agentStopped();

        /**
         * Call back method used when the {@link SimpleAgent} is killed with the method {@link SimpleAgent#kill()}.
         */
        void agentKilled();

    }
}
