package environment.physical;

import event.Event;
import lombok.NonNull;

/**
 * Represent {@link Event} that {@link PhysicalNetwork} can send to simulate {@code PhysicalEvent}.
 */
public abstract class PhysicalEvent extends Event<Event<?>> {
    protected PhysicalEvent(@NonNull Event<?> content) {
        super(content);
    }
}
