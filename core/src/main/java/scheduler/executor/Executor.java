package scheduler.executor;

import scheduler.Executable;
import scheduler.executor.exception.RejectedExecutionException;
import scheduler.executor.exception.ShutdownExecutorException;

import java.util.List;

/**
 * Interface for pool of thread which can execute {@link Executable}. The inspiration is the interface {@link java.util.concurrent.ExecutorService,
 * however this last one is to general and {@code Executor} is only here to execute {@code Executables}.
 */
public interface Executor {

    /**
     * Give the specified {@link Executable} to be executed. The {@link Executor} will finally execute it by trying to respect the order of method
     * call. The execution of the {@code Executable} depend on number of threads which the executor has and the time of the execution of previous
     * {@code Executables}.
     * <p>
     * This method can be called only if the {@code Executor} is not shutdown. Else an exception is thrown
     *
     * @param executable the executable to execute
     *
     * @throws RejectedExecutionException if the {@code Executor} is shutdown
     */
    void execute(Executable executable);

    /**
     * Shutdown the {@link Executor}. Finish executing all given {@link Executable} but does not accept any more other {@code Executable} to execute.
     * <p>
     * When all remaining {@code Executables} has been executable. The methods {@link #isTerminated()} returns true and all threads waiting on the
     * method {@link #awaitTermination(long)} are notified.
     */
    void shutdown();

    /**
     * Shutdown the {@link Executor} and returns all {@link Executable} which were waiting to be executing but was not in execution. Finish executing
     * all {@code Executables} in execution.
     * <p>
     * When all remaining {@code Executables} has been executable. The methods {@link #isTerminated()} returns true and all threads waiting on the
     * method {@link #awaitTermination(long)} are notified.
     *
     * @return the list of non executed {@code Executable}, never returns null.
     */
    List<Executable> shutdownNow();

    /**
     * @return true if methods {@link #shutdown()} or {@link #shutdownNow()} has been called at least one times.
     */
    boolean isShutdown();

    /**
     * @return true after a shutdown and if the {@link Executor} has finish to execute all {@link Executable} which must finish executing, else false.
     */
    boolean isTerminated();

    /**
     * Wait until either the {@link Executor} becomes terminated, either the timeout has been reached.
     *
     * @param timeout the timeout before wakeup
     *
     * @return true if the {@code Executor} is terminated after the wait, else false.
     */
    boolean awaitTermination(long timeout);

    /**
     * @return true if the {@link Executor} is not shutdown and have no {@link Executable}s to execute.
     */
    boolean isQuiescence();

    /**
     * Wait until either the {@link Executor} becomes quiescence. Unlock if the {@code Executor} is shutdown while waiting and throws an exception.
     *
     * @return true if the {@code Executor} is quiescence after the wait, else false.
     *
     * @throws ShutdownExecutorException if the {@code Executor} is shutdown while waiting.
     */
    boolean awaitQuiescence();

    /**
     * Wait until either the {@link Executor} becomes quiescence, either the timeout has been reached. Unlock if the {@code Executor} is shutdown
     * while waiting and throws an exception.
     *
     * @return true if the {@code Executor} is quiescence after the wait, else false.
     *
     * @throws ShutdownExecutorException if the {@code Executor} is shutdown while waiting.
     */
    boolean awaitQuiescence(long timeout);
}
