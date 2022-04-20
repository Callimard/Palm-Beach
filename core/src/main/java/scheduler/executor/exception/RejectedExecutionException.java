package scheduler.executor.exception;

public class RejectedExecutionException extends ExecutorException {
    public RejectedExecutionException(String message) {
        super(message);
    }
}
