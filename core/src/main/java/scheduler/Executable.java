package scheduler;

/**
 * Executable which can be scheduled and executed by a {@link Scheduler}.
 */
@FunctionalInterface
public interface Executable {

    /**
     * The execution method called when the {@link Executable} has been scheduled and must be executed.
     */
    void execute();

}
