package org.paradise.palmbeach.basic.messaging;

import lombok.*;
import org.paradise.palmbeach.core.agent.SimpleAgent;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Message<T> {

    @Getter
    @NonNull
    private final SimpleAgent.AgentIdentifier sender;

    @Getter
    private final T content;
}
