package org.paradise.palmbeach.basic.messaging;

import lombok.NonNull;
import org.paradise.palmbeach.core.scheduler.exception.ForcedWakeUpException;

public interface MessageReceiver {

    /**
     * Add the specified {@link MessageReceiverObserver}.
     *
     * @param observer the observer
     */
    void addObserver(MessageReceiverObserver observer);

    /**
     * Returns true if the {@link Messenger} has {@link Message} to read. If this method returns false, therefore the call of the method {@link
     * #nextContent()} will block the execution until a {@code Message} is received.
     *
     * @return true if the {@code Messaging} has messages not already reade, else false.
     */
    boolean hasContent();

    /**
     * Wait until a new message is received. After that, returns and remove the next {@link Message} to read. The {@link SimpleMessenger} is a FIFO
     * list of {@code Message}. First message received is the first message returns by this method.
     *
     * @return the next message to read. Never returns null.
     *
     * @throws ForcedWakeUpException if the Thread is interrupted after or during the wait
     */
    Object nextContent() throws ForcedWakeUpException;

    // Inner classes.

    interface MessageReceiverObserver {

        /**
         * Call back method call when a {@link Message} has been delivered by the specified {@link MessageReceiver}.
         *
         * @param msgReceiver      the {@code MessageReceiver} which has delivered the message
         * @param contentDelivered the object delivered
         *
         * @throws NullPointerException if msgReceiver is null
         */
        void messageDelivery(@NonNull MessageReceiver msgReceiver, Object contentDelivered);

        /**
         * This method ask the {@link MessageReceiverObserver} if it is interested in the content which has been delivered.
         *
         * @param contentDelivered the content delivered
         *
         * @return true if the {@code MessageReceiverObserver} can be interested in the content delivered , else false.
         */
        boolean interestedBy(Object contentDelivered);
    }
}
