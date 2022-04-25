package messaging.broadcasting;

import agent.SimpleAgent;
import environment.network.Network;
import lombok.NonNull;
import messaging.Message;
import messaging.MessageReceiver;

import java.io.Serializable;
import java.util.Set;

public interface Broadcaster extends MessageReceiver {

    /**
     * Broadcast the {@link Message} to all agents in the group membership (including itself) across the specified {@link Network}. This method does
     * not guarantee that the {@code Message} will be received by all {@link agent.SimpleAgent} in the {@code Network}. It only depends on the {@code
     * Network} disposition.
     *
     * @param message         the message to broadcast
     * @param groupMembership the group of member for which the message will be broadcast
     * @param network         the network across the message is broadcast
     */
    void broadcastMessage(@NonNull Message<? extends Serializable> message, @NonNull Set<SimpleAgent.AgentIdentifier> groupMembership,
                          @NonNull Network network);

}
