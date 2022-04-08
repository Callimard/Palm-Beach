package scheduler.executor.exception;

public class NotInExecutorContextException extends ExecutorException {
    public NotInExecutorContextException(String message) {
        super(message);
    }
}
