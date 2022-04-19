package messaging;

import agent.SimpleAgent;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Message<T extends Serializable> implements Serializable {

    @Getter
    @NonNull
    private final SimpleAgent.AgentIdentifier sender;

    @Getter
    private final T content;

    /**
     * Copy constructor.
     *
     * @param other the message to copy
     */
    public Message(Message<T> other) {
        this.sender = other.sender;
        this.content = other.content;
    }
}
