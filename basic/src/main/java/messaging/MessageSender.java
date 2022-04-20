package messaging;

import agent.SimpleAgent;
import environment.network.Network;
import lombok.NonNull;

import java.io.Serializable;

public interface MessageSender {

    /**
     * Send the {@link Message} to the target across the specified {@link Network}. This method does not guarantee that the target will finally
     * receive the message. The message reception depend on the {@code Network} disposition.
     *
     *
     * @param message the message to send
     * @param target  the target of the message
     * @param network the network across which the message will be sent
     *
     * @throws NullPointerException if message, target or network is null
     */
    void sendMessage(@NonNull Message<? extends Serializable> message, @NonNull SimpleAgent.AgentIdentifier target, @NonNull Network network);

}
