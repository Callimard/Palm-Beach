package scheduler.exception;

public class ForcedWakeUpException extends ConditionException {
    public ForcedWakeUpException(String message) {
        super(message);
    }
}
