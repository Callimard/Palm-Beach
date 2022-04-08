package scheduler;

import lombok.NonNull;
import scheduler.exception.CannotKillSchedulerException;
import scheduler.exception.CannotStartSchedulerException;
import scheduler.exception.ForcedWakeUpException;
import scheduler.exception.ImpossibleSchedulingException;
import scheduler.executor.Executor;
import scheduler.executor.exception.NotInExecutorContextException;

/**
 * Schedules {@link Executable}s. It is the heart of the Simulation, it manages which {@code Executable} must be executed and when.
 */
public interface Scheduler {

    /**
     * The waiting time for schedule something now (The now is 1 and not 0 because we cannot schedule a task for the current step).
     */
    long NOW = 1;

    /**
     * Start the {@link Scheduler}. A {@code Scheduler} can be started only one times.
     *
     * @throws CannotStartSchedulerException if the {@code Scheduler} cannot be started
     */
    void start();

    /**
     * @return true if the {@link Scheduler} has been started, else false.
     */
    boolean isRunning();

    /**
     * Kill the {@link Scheduler}. When a {@code Scheduler} has been killed, it cannot be started again.
     *
     * @throws CannotKillSchedulerException if the {@code Scheduler} cannot be killed
     */
    void kill();

    /**
     * @return true if the {@link Scheduler} has been killed, else false.
     */
    boolean isKilled();

    /**
     * Add the specified {@link SchedulerObserver} if it has not already been added.
     *
     * @param observer the observer
     *
     * @return true if the observer has been added, else false.
     */
    boolean addSchedulerObserver(@NonNull SchedulerObserver observer);

    /**
     * Schedules the specified {@link Executable} to be executed at the specified time. The specified time must be greater or equal to {@link #NOW}.
     *
     * @param executable the executable to schedule
     * @param time       the time when the executable must be executed
     *
     * @throws NullPointerException          if specified executable is null
     * @throws IllegalArgumentException      if time is less than {@link #NOW}
     * @throws ImpossibleSchedulingException if the {@code Scheduler} is not in a correct state to schedule the {@code Executable}
     */
    void scheduleAtTime(@NonNull Executable executable, long time);

    /**
     * Schedules the specified {@link Executable} in function of the specified parameter. This method is the general method of a {@link Scheduler}.
     * However, it is recommended to use the following methods which are more specific:
     * <ul>
     *     <li>{@link #scheduleOnce(Executable, long)}</li>
     *     <li>{@link #scheduleRepeatedly(Executable, long, long, long)}</li>
     *     <li>{@link #scheduleInfinitely(Executable, long, long)}</li>
     * </ul>
     * <p>
     * By default, these methods use {@code scheduleExecutable(Executable, long, ScheduleMode, long, long)} with correct parameter to schedule an
     * {@code Executable}.
     *
     * @param executable        the executable to schedule
     * @param waitingTime       the time to wait from the current time of the scheduler before the executable begin to be executed
     * @param scheduleMode      the schedule mode
     * @param nbRepetitions     the number of repetition (only use in {@link ScheduleMode#REPEATEDLY})
     * @param executionTimeStep the time step between executions (only use in {@link ScheduleMode#REPEATEDLY} or {@link ScheduleMode#INFINITELY})
     *
     * @throws NullPointerException          if specified executable or scheduleMode is null
     * @throws IllegalArgumentException      if waitingTime is less than {@link #NOW} or if nbRepetitions or executionTimeStep is less than 1 in
     *                                       {@link ScheduleMode#REPEATEDLY} or {@link ScheduleMode#INFINITELY}
     * @throws ImpossibleSchedulingException if the {@code Scheduler} is not in a correct state to schedule the {@code Executable}
     * @see #scheduleOnce(Executable, long)
     * @see #scheduleRepeatedly(Executable, long, long, long)
     * @see #scheduleInfinitely(Executable, long, long)
     */
    void scheduleExecutable(@NonNull Executable executable, long waitingTime, @NonNull ScheduleMode scheduleMode, long nbRepetitions,
                            long executionTimeStep);

    /**
     * Schedules one time the specified {@link Executable}. The execution will be done after waiting the specified waiting time.
     *
     * @param executable  the executable to schedule
     * @param waitingTime the time to wait from the current time of the scheduler before the executable begin to be executed
     *
     * @throws NullPointerException     if specified executable is null
     * @throws IllegalArgumentException if time is less than {@link #NOW}
     */
    default void scheduleOnce(@NonNull Executable executable, long waitingTime) {
        scheduleExecutable(executable, waitingTime, ScheduleMode.ONCE, -1, -1);
    }

    /**
     * Schedules the specified {@link Executable} repeatedly. The first execution will be done after waiting the specified {@code waitingTime}. After
     * that, the {@code Executable} will be executed each {@code executionTimeStep} and for {@code nbRepetitions} times.
     *
     * @param executable        the executable to schedule
     * @param waitingTime       the time to wait from the current time of the scheduler before the executable begin to be executed
     * @param nbRepetitions     the number of repetition
     * @param executionTimeStep the time step between executions
     *
     * @throws NullPointerException          if executable is null
     * @throws IllegalArgumentException      if waitingTime is less than {@link #NOW} or if nbRepetitions or executionTimeStep is less than 1
     * @throws ImpossibleSchedulingException if the {@code Scheduler} is not in a correct state to schedule the {@code Executable}
     */
    default void scheduleRepeatedly(@NonNull Executable executable, long waitingTime, long nbRepetitions, long executionTimeStep) {
        scheduleExecutable(executable, waitingTime, ScheduleMode.REPEATEDLY, nbRepetitions, executionTimeStep);
    }

    /**
     * Schedules the specified {@link Executable} infinitely. The first execution will be done after waiting the specified {code waitingTime}. After
     * that, the {@code Executable} will be executed each {@code executionTimeStep} and for an infinite time.
     *
     * @param executable        the executable to schedule
     * @param waitingTime       the time to wait from the current time of the scheduler before the executable begin to be executed
     * @param executionTimeStep the time step between executions
     *
     * @throws NullPointerException          if executable is null
     * @throws IllegalArgumentException      if waitingTime is less than {@link #NOW} or if executionTimeStep is less than 1
     * @throws ImpossibleSchedulingException if the {@code Scheduler} is not in a correct state to schedule the {@code Executable}
     */
    default void scheduleInfinitely(@NonNull Executable executable, long waitingTime, long executionTimeStep) {
        scheduleExecutable(executable, waitingTime, ScheduleMode.INFINITELY, -1, executionTimeStep);
    }

    /**
     * Make wait the execution of the current {@link Executable} on the specified {@link Executor.Condition}. The execution will be resumed when the
     * method {@link Executor.Condition#wakeup()} is called.
     *
     * @param condition the wake-up condition
     *
     * @throws ForcedWakeUpException         if it is not the {@code Condition} which wakes up the {@code Executable}.
     * @throws NotInExecutorContextException if the method is called out of the {@code Executor} context.
     */
    void await(@NonNull Executor.Condition condition) throws ForcedWakeUpException;

    /**
     * Make wait the execution of the current {@link Executable} on the specified {@link Executor.Condition}. The execution will be resumed when the
     * method {@link Executor.Condition#wakeup()} is called or if the timeout has been reached.
     *
     * @param condition the condition of {@code Executable} wake-up
     * @param timeout   the timeout when the {@code Executable} must at most wake-up
     *
     * @throws ForcedWakeUpException         if it is not the {@code Condition} which wakes up the {@code Executable}
     * @throws IllegalArgumentException      if the timeout is less than 1
     * @throws NotInExecutorContextException if the method is called out of the {@code Executor} context.
     */
    void await(@NonNull Executor.Condition condition, long timeout) throws ForcedWakeUpException;

    /**
     * @return a {@link Executor.Condition} that which can be used by {@code Scheduler} await methods. Never returns null.
     *
     * @see #await(Executor.Condition)
     * @see #await(Executor.Condition, long)
     */
    Executor.Condition generateCondition();

    /**
     * @return the current time of the {@link Scheduler}.
     */
    long getCurrentTime();

    /**
     * @return the max time that the {@link Scheduler} can reach.
     */
    long getMaxDuration();

    /**
     * @return true if the {@link Scheduler} reach the time of the simulation duration or has been killed, else false.
     */
    default boolean isEnded() {
        return getCurrentTime() > getMaxDuration() || isKilled();
    }

    // Inner classes.

    /**
     * Enum to specify schedule mode.
     */
    enum ScheduleMode {
        ONCE, REPEATEDLY, INFINITELY
    }

    /**
     * Time mode of the simulation.
     */
    enum TimeMode {
        REAL_TIME, DISCRETE_TIME
    }

    /**
     * Observer to be notified of {@link Scheduler} event.
     */
    interface SchedulerObserver {

        /**
         * Called when the {@link Scheduler} is started with the method {@link #start()}.
         */
        void schedulerStarted();

        /**
         * Called whe the {@link Scheduler} is killed with the method {@link #kill()}.
         */
        void schedulerKilled();

        /**
         * Call when the {@link Scheduler} has reach the {@link Scheduler#getMaxDuration()} time.
         */
        void schedulerReachEnd();

        /**
         * Call when the {@link Scheduler} has not anymore {@link Executable} to execute.
         */
        void noExecutableToExecute();
    }

}
