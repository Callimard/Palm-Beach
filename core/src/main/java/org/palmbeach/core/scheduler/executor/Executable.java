package org.palmbeach.core.scheduler.executor;

import org.palmbeach.core.scheduler.Scheduler;

/**
 * Executable which can be scheduled and executed by a {@link Scheduler}.
 */
@FunctionalInterface
public interface Executable {

    /**
     * The execution method called when the {@link Executable} has been scheduled and must be executed.
     */
    void execute() throws Exception;

    /**
     * Returns a non-null value if the current {@link Executable} is using the lock of the monitor.
     * <p>
     * For example in that case:
     * <pre>
     *  public void func() {
     *      final Object lockMonitor = new Object();
     *      synchronized(lockMonitor) {
     *          ...
     *          executable.execute();
     *          ...
     *      }
     *  }
     * </pre>
     * The method {@code getLockMonitor()} of the {@code Executable} must returns the reference of {@code lockMonitor} because the lockMonitor is used
     * during the call of the methods execute.
     *
     * @return an object which is the current lock monitor that the {@code Executable} is using.
     */
    default Object getLockMonitor() {
        return null;
    }
}
