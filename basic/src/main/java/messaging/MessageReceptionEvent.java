package messaging;

import event.Event;

import java.io.Serializable;

public class MessageReceptionEvent extends Event<Message<Serializable>> {

    public MessageReceptionEvent(Message<Serializable> message) {
        super(message);
    }
}
