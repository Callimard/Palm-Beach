package network;

import agent.SimpleAgent;
import common.Context;
import common.validation.Validate;
import environment.Environment;
import environment.network.Network;
import event.Event;
import lombok.NonNull;
import scheduler.Scheduler;
import simulation.PalmBeachSimulation;

import java.util.Random;

public class FullyConnectedNetwork extends Network {

    // Constants.

    public static final String MIN_SENDING_DELAY = "minDelay";
    public static final String MAX_SENDING_DELAY = "minDelay";

    public static final long DEFAULT_MIN_DELAY = 50L;
    public static final long DEFAULT_MAX_DELAY = 100L;

    // Variables.

    private final Random r = new Random();

    // Constructors.

    public FullyConnectedNetwork(@NonNull String name, @NonNull Environment environment, Context context) {
        super(name, environment, context);
    }

    // Methods.

    @Override
    public void environmentAddAgent(SimpleAgent.AgentIdentifier addedAgent) {
        // Nothing
    }

    @Override
    public void environmentRemoveAgent(SimpleAgent.AgentIdentifier removedAgent) {
        // Nothing
    }

    /**
     * Only verify if both {@link SimpleAgent} are evolving in the {@link Environment} of the {@link FullyConnectedNetwork}.
     *
     * @param source the source agent
     * @param target the target agent
     *
     * @return true if both agent are in the environment, else false.
     */
    @Override
    public boolean hasConnection(SimpleAgent.AgentIdentifier source, SimpleAgent.AgentIdentifier target) {
        return getEnvironment().agentIsEvolving(source) && getEnvironment().agentIsEvolving(target);
    }

    /**
     * Just schedule the call of the method {@link SimpleAgent#processEvent(Event)} of the specified target. The sending delay is randomly chosen
     * between {@link #minDelay()} and {@link #maxDelay()}
     *
     * @param source the source agent
     * @param target the target agent
     * @param event  the event
     */
    @Override
    protected void simulateSending(SimpleAgent.AgentIdentifier source, SimpleAgent.AgentIdentifier target, Event<?> event) {
        PalmBeachSimulation.scheduler().scheduleExecutable(() -> PalmBeachSimulation.getAgent(target).processEvent(event),
                                                           r.nextLong(minDelay(), maxDelay() + 1L),
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
