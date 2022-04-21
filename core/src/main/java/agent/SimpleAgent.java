package agent;

import agent.behavior.Behavior;
import agent.exception.*;
import agent.protocol.Protocol;
import com.google.common.collect.Maps;
import common.Context;
import common.SimpleContext;
import environment.Environment;
import event.Event;
import event.EventCatcher;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import simulation.configuration.BehaviorConfiguration;
import simulation.configuration.ProtocolConfiguration;
import simulation.configuration.exception.GenerationFailedException;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An {@code Agent} of the simulation. An {@code Agent} can be the abstraction of a machine, a processus or even a thread.
 * <p>
 * An {@code SimpleAgent} always evolves in one {@link Environment} and use several {@link Protocol}s. An agent can add several {@link Behavior}s and
 * during the simulation, the {@code Agent} can dynamically start and stop to play the different {@code Behavior} that it has.
 * <p>
 * All {@code SimpleAgent} subclasses must have constructor:
 * <pre>
 *     SimpleAgent(AgentIdentifier, Context) {
 *         ...
 *     }
 * </pre>
 */
@ToString
@Slf4j
public class SimpleAgent implements EventCatcher {

    // Variables.

    @Getter
    private final AgentIdentifier identifier;

    @Getter
    private final Context context;

    private final Map<Class<? extends Protocol>, Protocol> protocols;
    private final Map<Class<? extends Behavior>, Behavior> behaviors;

    private final AtomicReference<AgentState> state;

    private final List<AgentObserver> observers;

    // Constructors.

    /**
     * Constructs a {@link SimpleAgent} with a unique {@link AgentIdentifier} and a {@link Context} (which can be null). The context parameter is here
     * to allow the {@code SimpleAgent} to begin with an initial context and allow the user to specify any subclass of context. If context is null,
     * the default class use is {@link SimpleContext}.
     *
     * @param identifier the unique identifier of the {@code SimpleAgent}
     * @param context    the context of the {@code SimpleAgent}
     *
     * @throws NullPointerException if specified identifier is null.
     */
    public SimpleAgent(@NonNull AgentIdentifier identifier, Context context) {
        this.identifier = identifier;

        this.context = context != null ? context : new SimpleContext();

        this.protocols = Maps.newConcurrentMap();
        this.behaviors = Maps.newConcurrentMap();

        this.state = new AtomicReference<>(AgentState.CREATED);

        this.observers = new Vector<>();

        log.info(identifier + " has been created");
    }

    // Methods.

    /**
     * Create an instance of the specified {@link SimpleAgent} class. The specified class must have a construct as described in the general doc of
     * {@code SimpleAgent}.
     *
     * @param agentClass the agent class to instantiate
     * @param identifier the identifier of the agent
     * @param context    the context of the agent
     *
     * @return a new instance of the specified {@code SimpleAgent} class.
     *
     * @throws NoSuchMethodException     if the {@code SimpleAgent} class does not have the specific needed constructor
     * @throws InvocationTargetException if the constructor has thrown an exception
     * @throws InstantiationException    if the instantiation failed
     * @throws IllegalAccessException    if the construct is not accessible
     * @throws NullPointerException      if agentClass or identifier is null
     * @see #SimpleAgent(AgentIdentifier, Context)
     */
    public static SimpleAgent initiateAgent(@NonNull Class<? extends SimpleAgent> agentClass, @NonNull AgentIdentifier identifier, Context context)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends SimpleAgent> constructor = agentClass.getConstructor(AgentIdentifier.class, Context.class);
        return constructor.newInstance(identifier, context);
    }

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
    public final void start() {
        if (state.compareAndSet(AgentState.CREATED, AgentState.STARTED) || state.compareAndSet(AgentState.STOPPED, AgentState.STARTED)) {
            onStart();
            log.info(this.identifier + " started");
        } else
            throw new AgentCannotBeStartedException(this);
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
    public boolean isStarted() {
        return state.get().equals(AgentState.STARTED);
    }

    /**
     * Stop the {@link SimpleAgent}. This method stop the {@code SimpleAgent} only if it is in the state {@link AgentState#STARTED}, else throws an
     * exception.
     */
    public final void stop() {
        if (state.compareAndSet(AgentState.STARTED, AgentState.STOPPED)) {
            onStop();
            log.info(this.identifier + " stopped");
        } else
            throw new AgentCannotBeStoppedException(this);
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
    public boolean isStopped() {
        return state.get().equals(AgentState.STOPPED);
    }

    /**
     * Kill the {@link SimpleAgent}. This method kill the {@code SimpleAgent} only if the {@code SimpleAgent} is not already in the state {@link
     * AgentState#KILLED}.
     *
     * <strong>If a {@code SimpleAgent} is killed, it cannot be started or stopped anymore.</strong>
     */
    public final void kill() {
        if (state.compareAndSet(AgentState.CREATED, AgentState.KILLED)
                || state.compareAndSet(AgentState.STARTED, AgentState.KILLED)
                || state.compareAndSet(AgentState.STOPPED, AgentState.KILLED)) {
            onKill();
            log.info(this.identifier + " killed");
        } else
            throw new AgentCannotBeKilledException(this);
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
    public boolean isKilled() {
        return state.get().equals(AgentState.KILLED);
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
     * @param protocolClass the {@code Protocol} class
     *
     * @throws FailToAddProtocolException if the {@code Protocol} has not been added
     * @throws NullPointerException       if the protocolClass is null
     */
    public void addProtocol(@NonNull Class<? extends Protocol> protocolClass) {
        try {
            Protocol protocol = Protocol.instantiateProtocol(protocolClass, this, null);
            addProtocol(protocol);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new FailToAddProtocolException(protocolClass, e);
        }
    }

    /**
     * Add the protocol to the {@link SimpleAgent} by generated one base on the specified {@link ProtocolConfiguration}.
     *
     * @param protocolConfiguration the protocol configuration to instantiate and add
     *
     * @throws FailToAddProtocolException if the {@code Protocol} has not been added
     * @throws NullPointerException       if the protocolConfiguration is null
     */
    public void addProtocol(@NonNull ProtocolConfiguration protocolConfiguration) {
        try {
            Protocol protocol = protocolConfiguration.generateProtocol(this);
            addProtocol(protocol);
        } catch (GenerationFailedException e) {
            throw new FailToAddProtocolException(e);
        }
    }

    /**
     * Add the specified {@link Protocol} to the agent. The add is done onl if there is not already an added {@code Protocol} with the same class.
     *
     * <strong>WARNINGS!</strong> it is very important the specified instance of {@code Protocol} added is only added to only one agent and that it
     * is just the agent which add the {@code Protocol} which use it and there is no concurrency uses.
     *
     * @param protocol the protocol to add
     */
    public void addProtocol(Protocol protocol) {
        if (protocols.putIfAbsent(protocol.getClass(), protocol) == null) {
            // Protocol added
            addObserver(protocol);
            log.info("Protocol " + protocol.getClass().getSimpleName() + " added to the Agent " + identifier);
        } else
            log.info(identifier + " try to add an already added Protocol " + protocol.getClass());
    }

    /**
     * Verify if the {@link SimpleAgent} has already added a {@link Protocol} for the specified class or not.
     *
     * @param protocolClass the {@code Protocol} class to verify
     *
     * @return true if the {@code SimpleAgent} has already a {@code Protocol} for this {@code Protocol} class, else false.
     */
    public boolean hasProtocol(Class<? extends Protocol> protocolClass) {
        return protocols.containsKey(protocolClass);
    }

    /**
     * @param protocolClass the {@link Protocol} class
     *
     * @return the instance of the {@link Protocol} if the {@link SimpleAgent} has added a {@code Protocol} for the specified class, else null.
     *
     * @see #hasProtocol(Class)
     */
    public <T extends Protocol> T getProtocol(Class<T> protocolClass) {
        //noinspection unchecked
        return (T) protocols.get(protocolClass);
    }

    /**
     * Add to the {@link SimpleAgent} the specified {@link Behavior} by instantiate the {@code behavior} and add it.
     *
     * @param behaviorClass the behavior class to instantiate and add
     *
     * @throws FailToAddBehaviorException if the {@code Behavior} has not been added
     */
    public void addBehavior(Class<? extends Behavior> behaviorClass) {
        try {
            Behavior behavior = Behavior.instantiateBehavior(behaviorClass, this, null);
            addBehavior(behavior);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new FailToAddBehaviorException(behaviorClass);
        }
    }

    /**
     * Create a new instance of {@link Behavior} base on the specified {@link BehaviorConfiguration} and add it the {@link SimpleAgent}.
     *
     * @param behaviorConfiguration the behavior configuration to instantiate and add
     *
     * @throws FailToAddBehaviorException if the {@code Behavior} has not been added
     * @throws NullPointerException       if behaviorConfiguration is null
     */
    public void addBehavior(@NonNull BehaviorConfiguration behaviorConfiguration) {
        try {
            Behavior behavior = behaviorConfiguration.generateBehavior(this);
            addBehavior(behavior);
        } catch (GenerationFailedException e) {
            throw new FailToAddBehaviorException(e);
        }

    }

    /**
     * Add the specified {@link Behavior} to the agent. The add is done onl if there is not already an added {@code Behavior} with the same class.
     *
     * <strong>WARNINGS!</strong> it is very important the specified instance of {@code Behavior} added is only added to only one agent and that it
     * is just the agent which add the {@code Behavior} which use it and there is no concurrency uses.
     *
     * @param behavior the Behavior to add
     */
    public void addBehavior(@NonNull Behavior behavior) {
        if (behaviors.putIfAbsent(behavior.getClass(), behavior) == null)
            // Added Behavior
            log.info(identifier + " add the Behavior " + behavior.getClass());
        else
            // Already added Behavior
            log.info(identifier + " try to add an already added Behavior " + behavior.getClass());
    }

    /**
     * @param behaviorClass the behavior class
     *
     * @return true if the agent has an added {@link Behavior} which has the specified class, else false.
     */
    public boolean hasBehavior(Class<? extends Behavior> behaviorClass) {
        return behaviors.containsKey(behaviorClass);
    }

    /**
     * @param behaviorClass the behavior class
     * @param <T>           the type of the {@code Behavior}
     *
     * @return the instance of {@link Behavior} which has been added to the {@link SimpleAgent} and which has the specified class, else null.
     */
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
            inProcessEvent(event);
        } else
            throw new AgentNotStartedException(this);
    }

    /**
     * Call in the method {@link #processEvent(Event)} only if the {@link SimpleAgent} is started. Because {@link #processEvent(Event)} is final. In
     * is this method that you can override to update the implementation of {@link #processEvent(Event)}
     *
     * @param event the event to process
     *
     * @see #processEvent(Event)
     */
    protected void inProcessEvent(Event<?> event) {
        boolean eventHasBeenProcessed = searchProtocolForEvent(event);
        if (!eventHasBeenProcessed)
            throw new AgentCannotProcessEventException(this, event);
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

    // Getters.

    public AgentState getState() {
        return state.get();
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
    public abstract static class AgentIdentifier implements Serializable {

        @Override
        public abstract int hashCode();

        @Override
        public abstract boolean equals(Object obj);
    }

    /**
     * A simple implementation of {@link AgentIdentifier}. {@code SimpleIdentifier} identifies a {@code SimpleAgent} with its name and its unique id.
     */
    @EqualsAndHashCode(callSuper = false)
    @ToString
    @Getter
    @AllArgsConstructor
    public static class SimpleAgentIdentifier extends AgentIdentifier {

        // Static.

        /**
         * Counter to generate unique id.
         */
        private static final AtomicLong currentId = new AtomicLong(0L);

        // Variables.

        private final @NonNull String agentName;
        private final long uniqueId;

        // Methods.

        /**
         * Generate the next unique id for a {@link SimpleAgent}. If this method is always used to generate {@code SimpleAgent} identifier unique id,
         * it is guaranty that each {@code SimpleAgent} will have different id.
         *
         * @return the next generated unique id.
         */
        public static long nextId() {
            return currentId.getAndIncrement();
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
