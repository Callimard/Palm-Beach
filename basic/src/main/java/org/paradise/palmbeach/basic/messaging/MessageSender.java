package org.paradise.palmbeach.basic.messaging;

import lombok.NonNull;
import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.environment.network.Network;

public interface MessageSender {

    /**
     * Sends the {@link Message} from the current agent to the target across the specified {@link Network}. This method does not guarantee that the
     * target will finally receive the message. The message reception depend on the {@code Network} disposition.
     *
     * @param message the message to send
     * @param target  the target of the message
     * @param network the network across which the message will be sent
     *
     * @throws NullPointerException if message, target or network is null
     */
    void sendMessage(@NonNull Message<?> message, @NonNull SimpleAgent.AgentIdentifier target, @NonNull Network network);

}
