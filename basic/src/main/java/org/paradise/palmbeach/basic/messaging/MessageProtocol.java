package org.paradise.palmbeach.basic.messaging;

import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.agent.protocol.Protocol;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.paradise.palmbeach.utils.context.Context;
import lombok.NonNull;
import org.paradise.palmbeach.core.scheduler.exception.ForcedWakeUpException;
import org.paradise.palmbeach.core.scheduler.executor.Executor;

import java.io.Serializable;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import static org.paradise.palmbeach.core.simulation.PalmBeachSimulation.scheduler;

public abstract class MessageProtocol extends Protocol implements Messenger {

    // Variables.

    private final List<Executor.Condition> messageReceptionCondition;

    private final Deque<Message<? extends Serializable>> receivedMessages;

    private final Set<MessageReceiverObserver> observers;

    // Constructors.

    protected MessageProtocol(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
        this.messageReceptionCondition = Lists.newLinkedList();
        this.receivedMessages = Lists.newLinkedList();
        this.observers = Sets.newHashSet();
    }

    // Methods.

    @Override
    public void agentStarted() {
        // Nothing
    }

    @Override
    public void agentStopped() {
        // Nothing
    }

    @Override
    public void agentKilled() {
        // Nothing
    }

    /**
     * Receive the specified {@link Message}. This method is not obligated to call directly the method {@link #deliver(Message)}. The reception of a
     * {@code Message} is just here to get in memory that a {@code Message} has been received, but maybe it must wait other message reception before
     * trigger the message delivering.
     *
     * @param message the Message to receive
     */
    protected abstract void receive(@NonNull Message<? extends Serializable> message);

    /**
     * Deliver the specified {@link Message}. This method must normally call the method {@link #offerMessage(Message)} which will add the {@code
     * Message} in the received message deque and wake up all message reception listeners.
     * <p>
     * <strong>This method must notify {@link MessageReceiver.MessageReceiverObserver} by call the call back method
     * {@link #notifyMessageDelivery(Message)}</strong>
     *
     * @param message the Message to deliver
     */
    protected void deliver(@NonNull Message<? extends Serializable> message) {
        offerMessage(message);
        notifyMessageDelivery(message);
    }

    /**
     * Offer in the deque {@link #receivedMessages} the specified {@link Message}. The call of this method wakeup all message reception listeners.
     *
     * @param message the Message to offer in the deque {@link #receivedMessages}
     */
    protected void offerMessage(@NonNull Message<? extends Serializable> message) {
        receivedMessages.offer(message);
        wakeupOnDeliveryMessage();
    }

    /**
     * Wait until the {@link MessageProtocol} received a {@link Message}.
     *
     * @throws ForcedWakeUpException if the Thread is interrupted after or during the wait
     */
    protected void waitMessageReception() throws ForcedWakeUpException {
        while (!hasMessage()) {
            Executor.Condition condition = scheduler().generateCondition();
            messageReceptionCondition.add(condition);
            scheduler().await(condition);
        }
    }

    /**
     * Wakeup and clear all {@link Executor.Condition} which where waiting for the message reception.
     */
    private void wakeupOnDeliveryMessage() {
        List<Executor.Condition> conditions = Lists.newLinkedList(messageReceptionCondition);
        messageReceptionCondition.clear();
        for (Executor.Condition condition : conditions) {
            condition.wakeup();
        }
    }

    @Override
    public void addObserver(MessageReceiverObserver observer) {
        observers.add(observer);
    }

    protected void notifyMessageDelivery(Message<? extends Serializable> msg) {
        for (MessageReceiverObserver observer : observers) {
            if (observer.interestedBy(msg))
                observer.messageDelivery(this, msg);
        }
    }

    @Override
    public boolean hasMessage() {
        return !receivedMessages.isEmpty();
    }

    @Override
    public Message<? extends Serializable> nextMessage() throws ForcedWakeUpException {
        waitMessageReception();
        return receivedMessages.pollFirst();
    }
}
