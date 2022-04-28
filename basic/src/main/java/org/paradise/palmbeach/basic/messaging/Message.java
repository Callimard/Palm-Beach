package org.paradise.palmbeach.basic.messaging;

import org.paradise.palmbeach.core.agent.SimpleAgent;
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
}