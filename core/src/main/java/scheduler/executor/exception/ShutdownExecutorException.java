package scheduler.executor.exception;

public class ShutdownExecutorException extends ExecutorException {
    public ShutdownExecutorException(String message) {
        super(message);
    }
}
