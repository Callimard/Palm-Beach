package scheduler.exception;

public class ForcedWakeUpException extends Exception {
    public ForcedWakeUpException(Throwable throwable) {
        super(throwable);
    }
}
