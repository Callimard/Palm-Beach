package messaging;

import agent.SimpleAgent;
import agent.protocol.Protocol;
import com.google.common.collect.Lists;
import common.Context;
import lombok.NonNull;
import scheduler.exception.ForcedWakeUpException;
import scheduler.executor.Executor;

import java.io.Serializable;
import java.util.Deque;
import java.util.List;

import static simulation.PalmBeachSimulation.scheduler;

public abstract class MessageProtocol extends Protocol implements Messenger {

    // Variables.

    private final List<Executor.Condition> messageReceptionListener;

    private final Deque<Message<? extends Serializable>> receivedMessages;

    // Constructors.

    protected MessageProtocol(@NonNull SimpleAgent agent, Context context) {
        super(agent, context);
        this.messageReceptionListener = Lists.newLinkedList();
        this.receivedMessages = Lists.newLinkedList();
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
     *
     * @param message the Message to deliver
     */
    protected abstract void deliver(@NonNull Message<? extends Serializable> message);

    /**
     * Offer in the deque {@link #receivedMessages} the specified {@link Message}. The call of this method wakeup all message reception listeners.
     *
     * @param message the Message to offer in the deque {@link #receivedMessages}
     */
    protected void offerMessage(@NonNull Message<? extends Serializable> message) {
        receivedMessages.offer(message);
        notifyMessageDeliver();
    }

    /**
     * Wait until the {@link MessageProtocol} received a {@link Message}.
     *
     * @throws ForcedWakeUpException if the Thread is interrupted after or during the wait
     */
    protected void waitMessageReception() throws ForcedWakeUpException {
        while (!hasMessage()) {
            Executor.Condition condition = scheduler().generateCondition();
            messageReceptionListener.add(condition);
            scheduler().await(condition);
        }
    }

    /**
     * Wakeup and clear all {@link Executor.Condition} which where waiting for the message reception.
     */
    private void notifyMessageDeliver() {
        List<Executor.Condition> conditions = Lists.newLinkedList(messageReceptionListener);
        messageReceptionListener.clear();
        for (Executor.Condition condition : conditions) {
            condition.wakeup();
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
