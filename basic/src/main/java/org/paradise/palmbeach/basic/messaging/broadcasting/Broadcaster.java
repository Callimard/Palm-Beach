package org.paradise.palmbeach.basic.messaging.broadcasting;

import lombok.NonNull;
import org.paradise.palmbeach.basic.messaging.Message;
import org.paradise.palmbeach.basic.messaging.MessageReceiver;
import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.environment.network.Network;

import java.util.Set;

public interface Broadcaster extends MessageReceiver {

    /**
     * Broadcast the {@link Message} to all agents in the group membership (including itself) across the specified {@link Network}. This method does
     * not guarantee that the {@code Message} will be received by all {@link SimpleAgent} in the {@code Network}. It only depends on the {@code
     * Network} disposition.
     *
     * @param message         the message to broadcast
     * @param groupMembership the group of member for which the message will be broadcast
     * @param network         the network across the message is broadcast
     */
    void broadcastMessage(@NonNull Message<?> message, @NonNull Set<SimpleAgent.AgentIdentifier> groupMembership,
                          @NonNull Network network);

}
