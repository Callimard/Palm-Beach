package simulation.configuration.exception;

public class GenerationFailedException extends Exception {
    public GenerationFailedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
