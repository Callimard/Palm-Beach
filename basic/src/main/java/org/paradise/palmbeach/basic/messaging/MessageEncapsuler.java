package org.paradise.palmbeach.basic.messaging;

import lombok.NonNull;
import org.paradise.palmbeach.core.agent.SimpleAgent;

public class MessageEncapsuler extends Message<Message<?>> {

    // Constructor.

    public MessageEncapsuler(@NonNull SimpleAgent.AgentIdentifier sender, Message<?> msg) {
        super(sender, msg);
    }
}
