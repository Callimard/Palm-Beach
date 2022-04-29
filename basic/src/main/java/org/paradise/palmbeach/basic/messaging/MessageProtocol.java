package org.paradise.palmbeach.basic.messaging;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.NonNull;
import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.agent.protocol.Protocol;
import org.paradise.palmbeach.core.scheduler.exception.ForcedWakeUpException;
import org.paradise.palmbeach.core.scheduler.executor.Executor;
import org.paradise.palmbeach.utils.context.Context;

import java.util.Deque;
import java.util.List;
import java.util.Set;

import static org.paradise.palmbeach.core.simulation.PalmBeachSimulation.scheduler;

public abstract class MessageProtocol<T extends MessageEncapsuler> extends Protocol implements Messenger {

    // Variables.

    private final List<Executor.Condition> messageReceptionCondition;

    private final Deque<Object> contentReceived;

    private final Set<MessageReceiverObserver> observers;

    // Constructors.

    protected MessageProtocol(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
        this.messageReceptionCondition = Lists.newLinkedList();
        this.contentReceived = Lists.newLinkedList();
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
     * Receive the specified {@link Message}. This method is not obligated to call directly the method {@link #deliver(MessageEncapsuler)}. The
     * reception of a {@code Message} is just here to get in memory that a {@code Message} has been received, but maybe it must wait other message
     * reception before trigger the message delivering.
     *
     * @param message the Message to receive
     */
    protected abstract void receive(@NonNull T message);

    /**
     * Deliver the specified {@link Message}. This method must normally call the method {@link #offerMessage(Message)} which will add the {@code
     * Message} in the received message deque and wake up all message reception listeners.
     * <p>
     * <strong>This method must notify {@link MessageReceiver.MessageReceiverObserver} by call the call back method
     * {@link #notifyMessageDelivery(Message)}</strong>
     *
     * @param message the Message to deliver
     */
    protected void deliver(@NonNull T message) {
        offerMessage(message.getContent());
        notifyMessageDelivery(message.getContent());
    }

    /**
     * Offer in the deque {@link #contentReceived} the specified {@link Message}. The call of this method wakeup all message reception listeners.
     *
     * @param message the Message to offer in the deque {@link #contentReceived}
     */
    protected void offerMessage(@NonNull Message<?> message) {
        contentReceived.offer(message);
        wakeupOnDeliveryMessage();
    }

    /**
     * Wait until the {@link MessageProtocol} received a {@link Message}.
     *
     * @throws ForcedWakeUpException if the Thread is interrupted after or during the wait
     */
    protected void waitMessageReception() throws ForcedWakeUpException {
        while (!hasContent()) {
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

    protected void notifyMessageDelivery(Message<?> msg) {
        for (MessageReceiverObserver observer : observers) {
            if (observer.interestedBy(msg))
                observer.messageDelivery(this, msg);
        }
    }

    @Override
    public boolean hasContent() {
        return !contentReceived.isEmpty();
    }

    @Override
    public Object nextContent() throws ForcedWakeUpException {
        waitMessageReception();
        return contentReceived.pollFirst();
    }
}
