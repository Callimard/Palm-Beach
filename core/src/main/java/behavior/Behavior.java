package behavior;

import agent.SimpleAgent;
import common.BasicContext;
import common.Context;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import protocol.Protocol;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * A {@code Behavior} represents a part of the arbitrary conduct of a {@link SimpleAgent}. When a {@link SimpleAgent} start to play a {@code Behavior },
 * maybe some {@link Protocol} use by the {@code SimpleAgent} will changes and follow "another" {@link Protocol}. For example, it is possible to define
 * two behavior for a specific {@code Protocol}. One follow the correct {@code Protocol}, the other acts as a Byzantine agent.
 * <p>
 * This class can also allow the simulation of several arbitrary behavior in {@code Protocol} that allow this type of behavior. For example, in
 * Bitcoin blockchain, the transaction selection is totally arbitrary. Therefore, it is possible to define several strategies to know which is the
 * more efficient for a certain goal.
 *
 * <strong>Implementation instructions:</strong> a subclass of {@code Behavior} must add a constructor like this:
 * <pre>
 *     Behavior(SimpleAgent) {
 *        super(...);
 *        ...
 *     }
 * </pre>
 */
@ToString
@Slf4j
public abstract class Behavior {

    // Variables.

    @Getter
    private final SimpleAgent agent;

    @Getter
    private final Context context;

    @Getter
    private volatile boolean played;


    // Constructors.

    /**
     * Constructs a {@link Behavior} with a specified {@link SimpleAgent} which can play the {@code Behavior}.
     * <p>
     * The {@code Behavior} is initiate with an empty {@link Context}. The default {@code Context} class used is {@link BasicContext}.
     *
     * @param agent the agent which can play the behavior
     */
    protected Behavior(@NonNull SimpleAgent agent) {
        this(agent, null);
    }

    /**
     * @param agent   the agent which can play the behavior
     * @param context the behavior initial context
     *
     * @throws NullPointerException if the specified agent is null
     */
    protected Behavior(@NonNull SimpleAgent agent, Context context) {
        this.agent = Optional.of(agent).get();
        this.played = false;
        this.context = context != null ? context : new BasicContext();
    }

    // Methods.

    /**
     * Try to create an instance of the specified {@link Behavior} class with the specified {@link SimpleAgent}.
     *
     * @param behaviorClass the {@code Behavior} class
     * @param agent         the agent with which the {@code Behavior} will be instantiated
     *
     * @return an instance of a {@code Behavior} of the specified {@code Behavior} class.
     *
     * @throws NoSuchMethodException     if the {@code Behavior} class does not have the specific needed constructor
     * @throws InvocationTargetException if the constructor has thrown an exception
     * @throws InstantiationException    if the instantiation failed
     * @throws IllegalAccessException    if the construct is not accessible
     */
    public static Behavior instantiateBehavior(Class<? extends Behavior> behaviorClass, SimpleAgent agent)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Behavior> behaviorConstructor = behaviorClass.getConstructor(SimpleAgent.class);
        return behaviorConstructor.newInstance(agent);
    }

    /**
     * Start to play the {@link Behavior}. Do nothing if the {@code Behavior} is already played.
     */
    public void play() {
        if (!isPlayed()) {
            this.played = true;
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
        if (isPlayed()) {
            this.played = false;
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
