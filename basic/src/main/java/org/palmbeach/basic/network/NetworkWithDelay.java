package org.palmbeach.basic.network;

import org.palmbeach.core.agent.AgentProcessEventExecutable;
import org.palmbeach.core.agent.SimpleAgent;
import org.palmbeach.core.common.Context;
import org.palmbeach.core.common.validation.Validate;
import org.palmbeach.core.environment.Environment;
import org.palmbeach.core.environment.network.Network;
import org.palmbeach.core.event.Event;
import lombok.Getter;
import lombok.NonNull;
import org.palmbeach.core.scheduler.Scheduler;
import org.palmbeach.core.simulation.PalmBeachSimulation;

import java.util.Random;

/**
 * {@link Network} which send {@link Event} with defined min and max delay. Delays are defined in the context of the {@code Network}. Keys to use
 * are:
 * <ul>
 *     <li>{@link #MIN_SENDING_DELAY} with default value {@link #DEFAULT_MIN_DELAY}</li>
 *     <li>{@link #MAX_SENDING_DELAY} with default value {@link #DEFAULT_MAX_DELAY}</li>
 * </ul>
 * <p>
 * When an {@code Event} is sent, the delay is randomly compute and chose between {@link #minDelay()} and {@link #maxDelay()}. The seed of the
 * random can be defined in the context with the key {@link #RANDOM_SEED}. Then {@link Random} is accessible by getter.
 */
public abstract class NetworkWithDelay extends Network {

    // Context keys and default values.

    public static final String MIN_SENDING_DELAY = "minDelay";
    public static final String MAX_SENDING_DELAY = "maxDelay";
    public static final String RANDOM_SEED = "randomSeed";

    public static final long DEFAULT_MIN_DELAY = 50L;
    public static final long DEFAULT_MAX_DELAY = 100L;

    // Variables.

    @Getter
    private final Random random;

    // Constructors.

    protected NetworkWithDelay(@NonNull String name, @NonNull Environment environment, Context context) {
        super(name, environment, context);

        // Verifications.
        minDelay();
        maxDelay();

        if (getContext().getValue(RANDOM_SEED) != null)
            this.random = new Random((Long) getContext().getValue(RANDOM_SEED));
        else
            this.random = new Random();
    }

    /**
     * Just schedule the call of the method {@link SimpleAgent#processEvent(Event)} of the specified target. The sending delay is randomly chosen
     * between {@link #minDelay()} and {@link #maxDelay()}
     *
     * @param source the source agent
     * @param target the target agent
     * @param event  the event
     *
     * @throws IllegalArgumentException if minDelay is greater or equal to maxDelay - 1
     */
    @Override
    protected void simulateSending(SimpleAgent.@NonNull AgentIdentifier source, SimpleAgent.@NonNull AgentIdentifier target,
                                   @NonNull Event<?> event) {
        PalmBeachSimulation.scheduler().scheduleExecutable(new AgentProcessEventExecutable(PalmBeachSimulation.getAgent(target), event),
                                                           random.nextLong(minDelay(), maxDelay() + 1L),
                                                           Scheduler.ScheduleMode.ONCE, Scheduler.IGNORED, Scheduler.IGNORED);
    }

    /**
     * @return the minDelay set inf the context, else {@link #DEFAULT_MIN_DELAY}
     */
    public long minDelay() {
        if (getContext().getValue(MIN_SENDING_DELAY) != null) {
            long minDelay = (long) getContext().getValue(MIN_SENDING_DELAY);
            Validate.min(minDelay, Scheduler.NEXT_STEP, "Min delay cannot be less than 1");
            return minDelay;
        } else
            return DEFAULT_MIN_DELAY;
    }

    /**
     * Set in the context the min sending delay
     *
     * @param minDelay the minDelay
     *
     * @throws IllegalArgumentException if minDelay is less than 1
     */
    public void minDelay(long minDelay) {
        Validate.min(minDelay, Scheduler.NEXT_STEP, "Min delay cannot be less than 1");
        getContext().map(MIN_SENDING_DELAY, minDelay);
    }

    /**
     * @return the maxDelay set inf the context, else {@link #DEFAULT_MAX_DELAY}
     */
    public long maxDelay() {
        if (getContext().getValue(MAX_SENDING_DELAY) != null) {
            long maxDelay = (long) getContext().getValue(MAX_SENDING_DELAY);
            Validate.min(maxDelay, Scheduler.NEXT_STEP, "Max delay cannot be less than 1");
            return maxDelay;
        } else
            return DEFAULT_MAX_DELAY;
    }

    /**
     * Set in the context the max sending delay
     *
     * @param maxDelay the max sending delay
     *
     * @throws IllegalArgumentException if maxDelay is less than 1
     */
    public void maxDelay(long maxDelay) {
        Validate.min(maxDelay, Scheduler.NEXT_STEP, "Max delay cannot be less than 1");
        getContext().map(MAX_SENDING_DELAY, maxDelay);
    }
}
