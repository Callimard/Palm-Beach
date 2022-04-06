package event;

/**
 * Interface use by class which want treat {@link Event}
 */
public interface EventCatcher {

    /**
     * Treats the specified {@link Event}.
     *
     * @param event the event
     */
    void processEvent(Event<?> event);

    /**
     * @param event the event
     *
     * @return true if the event can be processed, else false.
     */
    boolean canProcessEvent(Event<?> event);

}
