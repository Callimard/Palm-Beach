package messaging;

import event.Event;
import scheduler.exception.ForcedWakeUpException;

import java.io.Serializable;

public interface MessageReceiver {

    /**
     * Returns true if the {@link Messenger} has {@link Message} to read. If this method returns false, therefore the call of the method {@link
     * #nextMessage()} will block the execution until a {@code Message} is received.
     *
     * @return true if the {@code Messaging} has messages not already reade, else false.
     */
    boolean hasMessage();

    /**
     * Wait until a new message is received. After that, returns and remove the next {@link Message} to read. The {@link SimpleMessageSender} is a
     * FIFO list of {@code Message}. First message received is the first message returns by this method.
     *
     * @return the next message to read. Never returns null.
     *
     * @throws ForcedWakeUpException if the Thread is interrupted after or during the wait
     */
    Message<? extends Serializable> nextMessage() throws ForcedWakeUpException;

    // Inner classes.

    class MessageReceptionEvent extends Event<Message<? extends Serializable>> {
        public MessageReceptionEvent(Message<? extends Serializable> message) {
            super(message);
        }
    }

    class MessageDeliveryEvent extends Event<Message<? extends Serializable>> {
        public MessageDeliveryEvent(Message<? extends Serializable> content) {
            super(content);
        }
    }
}
