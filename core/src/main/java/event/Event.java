package event;

import lombok.*;

import java.io.Serializable;

/**
 * Represents an {@code Event} that can occurred in the Simulation and can be treats by an {@link EventCatcher}.
 *
 * @param <T> the type of the content of the {@code Event}
 */
@EqualsAndHashCode
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Event<T extends Serializable> implements Serializable {

    @Getter
    private final T content;
}
