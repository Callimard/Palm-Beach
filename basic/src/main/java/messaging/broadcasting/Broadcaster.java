package messaging.broadcasting;

import environment.network.Network;
import lombok.NonNull;
import messaging.Message;
import messaging.MessageReceiver;

import java.io.Serializable;

public interface Broadcaster extends MessageReceiver {

    /**
     * Broadcast the {@link Message} across the specified {@link Network} (including itself). This method does not guarantee that the {@code Message}
     * will be received by all {@link agent.SimpleAgent} in the {@code Network}. It only depends on the {@code Network} disposition.
     *
     * @param message the message to broadcast
     * @param network the network across the message is broadcast
     */
    void broadcastMessage(@NonNull Message<? extends Serializable> message, Network network);

}
