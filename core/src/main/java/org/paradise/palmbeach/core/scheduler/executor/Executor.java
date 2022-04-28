package org.paradise.palmbeach.core.scheduler.executor;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.paradise.palmbeach.core.scheduler.exception.NotPreparedConditionException;
import org.paradise.palmbeach.core.scheduler.executor.exception.AlreadyPreparedConditionException;
import org.paradise.palmbeach.core.scheduler.executor.exception.NotInExecutorContextException;
import org.paradise.palmbeach.core.scheduler.executor.exception.RejectedExecutionException;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Interface for pool of thread which can execute {@link Executable}. The inspiration is the interface {@link ExecutorService}, however this last one
 * is too general and {@code Executor} is only here to execute {@code Executables}.
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
    void execute(@NonNull Executable executable);

    /**
     * Shutdown the {@link Executor} and returns all {@link Executable} which were waiting to be executing but was not in execution. Try to interrupt
     * {@link ExecutorThread} in execution but not sure that it stop them. In worst case, {@code ExecutorThreads} which are executing will finish
     * their execution.
     * <p>
     * When all {@code ExecutorThread} has been finish their execution. The methods {@link #isTerminated()} returns true and all threads waiting on
     * the method {@link #awaitTermination(long)} are notified.
     *
     * @return the list of non executed {@code Executable}, never returns null.
     */
    List<Executable> shutdown();

    /**
     * @return true if methods {@link #shutdown()} has been called at least one times.
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
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    boolean awaitTermination(long timeout) throws InterruptedException;

    /**
     * @return true if the {@link Executor} is not shutdown and have no {@link Executable}s to execute.
     */
    boolean isQuiescence();

    /**
     * Wait until either the {@link Executor} becomes quiescence. Unlock if the {@code Executor} is shutdown while waiting and throws an exception.
     *
     * @return true if the {@code Executor} is quiescence after the wait, else false.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    boolean awaitQuiescence() throws InterruptedException;

    /**
     * Wait until either the {@link Executor} becomes quiescence, either the timeout has been reached. Unlock if the {@code Executor} is shutdown
     * while waiting and throws an exception.
     *
     * @return true if the {@code Executor} is quiescence after the wait, else false.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    boolean awaitQuiescence(long timeout) throws InterruptedException;

    /**
     * Returns the current {@link ExecutorThread} which is executing the current {@link Executable}. Only {@code Executable} which are executed in an
     * {@link Executor} can called this method. If this method is called out of the execution context of an {@code Executor} (not in a thread created
     * by an {@code Executor}), this method will throw an {@link NotInExecutorContextException}.
     *
     * @return the instance of the current {@code ExecutorThread}, never returns null.
     *
     * @throws NotInExecutorContextException if the method is called out of the {@code Executor} context.
     */
    ExecutorThread getCurrentExecutorThread();

    /**
     * @return a new instance of {@link Condition} that {@link Executor} can use. Never returns null.
     */
    Condition generateCondition();

    // Inner class.

    /**
     * An {@link Executor} guaranties that threads which execute {@link Executable} inherit this {@code ExecutorThread} class.
     */
    abstract class ExecutorThread extends Thread {

        protected ExecutorThread() {
            super();
        }

        @Override
        public abstract void run();

        /**
         * Stop the thread execution and wait the call of {@link #wakeUp()} to resume its execution. It is to the user to ensure that the condition to
         * wait has been synchronised and avoid the infinite wait.
         * <p>
         * If an {@link ExecutorThread} is stopped, then there is a free place for another {@code ExecutorThread} and the {@link Executor} will manage
         * the execution of a new {@link ExecutorThread}.
         *
         * @throws InterruptedException if the waiting thread is interrupted while waiting
         */
        public abstract void await() throws InterruptedException;

        /**
         * Wake up the thread and notify the {@link Executor} that this current thread can resume its execution. The {@code Executor} then will manage
         * when the execution of the {@code ExecutorThread} will be resumed to keep the {@code Executor} properties and consistency correct. For
         * example, if the {@code Executor} is multi thread and the max thread is 4. If already 4 {@code ExecutorThread} are executing while another
         * {@code ExecutorThread} wake up, then the {@code Executor} will not directly resume the execution of the wake-up {@code ExecutorThread} but
         * until the end of the execution of 1 {@code ExecutorThread}.
         */
        public abstract void wakeUp();

        public abstract void kill();
    }

    /**
     * {@code Condition} which allow {@link Executable} to wait on it and be wake up after.
     * <p>
     * It is advices {@code Condition} to use only one times a {@code Condition} and generate a {@code Condition} at each time you need to wait. This
     * will avoid concurrency problem.
     */
    @NoArgsConstructor
    class Condition {

        // Variables.

        private Executor.ExecutorThread executorThread;

        // Methods.

        /**
         * Prepare the {@link Condition} by giving to him the {@link Executor.ExecutorThread} in which the {@link Executable} will wait and will be
         * make up.
         *
         * @param executorThread the executor thread of the {@code Condition}
         *
         * @throws AlreadyPreparedConditionException if the {@code Condition} has already been prepared
         */
        public void prepare(Executor.ExecutorThread executorThread) {
            if (this.executorThread == null)
                this.executorThread = executorThread;
            else
                throw new AlreadyPreparedConditionException();
        }

        /**
         * Wakes up and resumes the execution of the {@link Executable} which are waiting on the current {@link Condition}. When a {@link Condition}
         * has been wake up, it must be again prepared before recall at new times this method.
         */
        public void wakeup() {
            if (executorThread != null) {
                executorThread.wakeUp();
                executorThread = null;
            } else
                throw new NotPreparedConditionException("Condition call wake up whereas its ExecutorThread is null.");
        }
    }
}
