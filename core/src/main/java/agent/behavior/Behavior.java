package agent.behavior;

import agent.SimpleAgent;
import agent.protocol.Protocol;
import common.Context;
import common.SimpleContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@code Behavior} represents a part of the arbitrary conduct of a {@link SimpleAgent}. When a {@link SimpleAgent} start to play a {@code
 * Behavior}, maybe some {@link Protocol} use by the {@code SimpleAgent} will changes and follow "another" {@link Protocol}. For example, it is
 * possible to define two {@code Behavior} for a specific {@code Protocol}. One follow the correct {@code Protocol}, the other acts as a Byzantine
 * agent.
 * <p>
 * This class can also allow the simulation of several arbitrary {@code Behaviors} in {@code Protocol} that allow this type of agent.behavior. For
 * example, in Bitcoin blockchain, the transaction selection is totally arbitrary. Therefore, it is possible to define several strategies to know
 * which is the more efficient for a certain goal.
 *
 * <strong>Implementation instructions:</strong> a subclass of {@code Behavior} must have a constructor like this:
 * <pre>
 *     Behavior(SimpleAgent, Context) {
 *        super(...);
 *        ...
 *     }
 * </pre>
 */
@ToString
@Slf4j
public abstract class Behavior {

    // Variables.

    @ToString.Exclude
    @Getter
    private final SimpleAgent agent;

    @Getter
    private final Context context;

    private final AtomicBoolean played;

    // Constructors.

    /**
     * @param agent   the agent which can play the {@code Behavior}
     * @param context the {@code Behavior} initial context
     *
     * @throws NullPointerException if the specified agent is null
     */
    protected Behavior(@NonNull SimpleAgent agent, Context context) {
        this.agent = Optional.of(agent).get();
        this.played = new AtomicBoolean(false);
        this.context = context != null ? context : new SimpleContext();
    }

    // Methods.

    /**
     * Try to create an instance of the specified {@link Behavior} class with the specified {@link SimpleAgent}.
     *
     * @param behaviorClass the {@code Behavior} class
     * @param agent         the agent with which the {@code Behavior} will be instantiated
     * @param context       the behavior context
     *
     * @return an instance of a {@code Behavior} of the specified {@code Behavior} class.
     *
     * @throws NoSuchMethodException     if the {@code Behavior} class does not have the specific needed constructor
     * @throws InvocationTargetException if the constructor has thrown an exception
     * @throws InstantiationException    if the instantiation failed
     * @throws IllegalAccessException    if the construct is not accessible
     * @throws NullPointerException      if behaviorClass or agent is null
     */
    public static Behavior instantiateBehavior(@NonNull Class<? extends Behavior> behaviorClass, @NonNull SimpleAgent agent, Context context)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Behavior> behaviorConstructor = behaviorClass.getConstructor(SimpleAgent.class, Context.class);
        return behaviorConstructor.newInstance(agent, context);
    }

    /**
     * Start to play the {@link Behavior}. Do nothing if the {@code Behavior} is already played.
     */
    public void play() {
        if (played.compareAndSet(false, true)) {
            beginToBePlayed();
            log.info("Agent " + agent.getIdentifier() + " start to played the Behavior " + this.getClass().getSimpleName());
        } else
            log.info("Behavior " + this.getClass().getSimpleName() + " is already played by the agent " + agent.getIdentifier());
    }

    /**
     * Call during the process of the method {@link #play()} if the {@code Behavior} is started to be played.
     */
    protected abstract void beginToBePlayed();

    /**
     * Stop to play the {@link Behavior}. Do nothing if the {@code Behavior} is already not played.
     */
    public void stopPlay() {
        if (played.compareAndSet(true, false)) {
            stopToBePlayed();
            log.info("Agent = " + agent.getIdentifier() + " stop to played the Behavior " + this.getClass().getSimpleName());
        } else
            log.info("Behavior " + this.getClass().getSimpleName() + " is already stopped by the agent " + agent.getIdentifier());
    }

    /**
     * Call during the process of the method {@link #stopPlay()} if the {@code Behavior} is stopped to be played.
     */
    protected abstract void stopToBePlayed();
}
