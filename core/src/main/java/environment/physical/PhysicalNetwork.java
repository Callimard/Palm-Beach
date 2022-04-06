package environment.physical;

import agent.SimpleAgent;
import event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import agent.protocol.Protocol;

/**
 * Simulate a {@code PhysicalNetwork}. A {@code PhysicalNetwork} represents physical connection between several {@link SimpleAgent}. Via the {@code
 * PhysicalNetwork} it is possible to send {@link Event} and it is the {@code PhysicalNetwork} which manage if yse or not, the source agent can reach
 * the target agent and therefore if yes or not the {@code Event} can be sent.
 * <p>
 * It is advice to create for each {@code PhysicalNetwork} a specific type of {@link PhysicalEvent} which can PhysicalNetwork be managed and processed
 * by specifics {@link Protocol}s.
 */
@ToString
@AllArgsConstructor
@Slf4j
public abstract class PhysicalNetwork {

    // Variables.

    @Getter
    @NonNull
    private final String name;

    // Methods.

    /**
     * Sends the {@link Event} from the source to the target. First check if from the source, the target is reachable with the method {@link
     * #hasPhysicalConnection(SimpleAgent.AgentIdentifier, SimpleAgent.AgentIdentifier)}. If it is the case, simulate the physical send with the
     * method {@link #physicallySend(SimpleAgent.AgentIdentifier, SimpleAgent.AgentIdentifier, PhysicalEvent)}.
     *
     * @param source the source agent
     * @param target the target agent
     * @param event  the event to send
     */
    public void send(@NonNull SimpleAgent.AgentIdentifier source, @NonNull SimpleAgent.AgentIdentifier target, @NonNull Event<?> event) {
        if (hasPhysicalConnection(source, target))
            physicallySend(source, target, preparePhysicalEvent(event));
        else
            log.info("Agent source " + source + " is not physically connected to target " + target + " by the PhysicalNetwork " + this);
    }

    /**
     * Verifies if from the source, the target agent is physically reachable. The order is important and this function is not commutative. It means
     * that if it is true for {@code source -> target}, it can be false for {@code target -> source}.
     *
     * @param source the source agent
     * @param target the target agent
     *
     * @return true if from the source, the target agent is physically reachable, else false.
     */
    public abstract boolean hasPhysicalConnection(SimpleAgent.AgentIdentifier source, SimpleAgent.AgentIdentifier target);

    /**
     * Create the appropriate {@link PhysicalEvent} which encapsulate the specified {@link Event}.
     *
     * @param event the event to encapsulate
     *
     * @return the {@code PhysicalEvent} which encapsulate the specified {@code Event}, never returns null.
     */
    protected abstract PhysicalEvent preparePhysicalEvent(Event<?> event);

    /**
     * Simulate the physical send of the {@link Event} from the source to the target.
     *
     * @param source        the source agent
     * @param target        the target agent
     * @param physicalEvent the physical event
     */
    protected abstract void physicallySend(SimpleAgent.AgentIdentifier source, SimpleAgent.AgentIdentifier target, PhysicalEvent physicalEvent);
}
