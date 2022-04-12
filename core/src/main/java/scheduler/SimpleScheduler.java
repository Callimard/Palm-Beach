package scheduler;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import scheduler.exception.CannotKillSchedulerException;
import scheduler.exception.CannotStartSchedulerException;
import scheduler.exception.ForcedWakeUpException;
import scheduler.exception.ImpossibleSchedulingException;
import scheduler.executor.Executable;
import scheduler.executor.Executor;

import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;

import static common.validation.Validate.min;

@ToString
@Slf4j
public class SimpleScheduler implements Scheduler {

    // Variables.

    private long currentTime = 0L;
    private final long maxDuration;

    private SchedulerState state;

    @NonNull
    private final Executor executor;

    @ToString.Exclude
    private final Map<Long, Deque<Executable>> executables;

    @ToString.Exclude
    private final Set<SchedulerObserver> observers;

    @ToString.Exclude
    private final StepWatcher stepWatcher;

    // Constructors.

    public SimpleScheduler(long maxDuration, @NonNull Executor executor) {
        min(maxDuration, 1, "MaxDuration Scheduler must be greater than 0, current = " + maxDuration);
        this.maxDuration = maxDuration;
        this.executor = executor;
        this.state = SchedulerState.CREATED;
        this.executables = Maps.newConcurrentMap();
        this.observers = Sets.newConcurrentHashSet();
        this.stepWatcher = new StepWatcher();
        stepWatcher.setDaemon(true);
        log.info("Scheduler CREATED");
    }

    // Methods.

    @Override
    public synchronized void start() {
        if (state.equals(SchedulerState.CREATED)) {
            // Instructions order important
            state = SchedulerState.STARTED;
            log.info("Scheduler STARTED");
            executeNextStep();
            stepWatcher.start();
            notifySchedulerStarted();
        } else
            throw new CannotStartSchedulerException("Scheduler is not in the correct state to be started. Current state " + state);
    }

    private synchronized void executeNextStep() {
        TreeSet<Long> sortedScheduledTimes = Sets.newTreeSet(executables.keySet());
        if (sortedScheduledTimes.isEmpty()) {
            endByNoExecutable();
        } else {
            currentTime = sortedScheduledTimes.first();
            if (!isEnded()) {
                Deque<Executable> execDeque = executables.get(currentTime);
                executables.remove(currentTime);
                execDeque.forEach(executor::execute);
            } else
                notifySchedulerReachEnd();
        }
    }

    private void endByNoExecutable() {
        if (!isKilled()) {
            notifySchedulerHasNoExecutable();
            kill();
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return state.equals(SchedulerState.STARTED);
    }

    @Override
    public synchronized void kill() {
        if (isRunning()) {
            // Instructions order important
            state = SchedulerState.KILLED;
            log.info("Scheduler KILLED");
            stepWatcher.kill();
            executor.shutdown();
            executables.clear();
            notifySchedulerKilled();
        } else
            throw new CannotKillSchedulerException("Scheduler is not in the correct state to be killed. Current state " + state);
    }

    @Override
    public synchronized boolean isKilled() {
        return state.equals(SchedulerState.KILLED);
    }

    @Override
    public boolean addSchedulerObserver(@NonNull SchedulerObserver observer) {
        return observers.add(observer);
    }

    @Override
    public void scheduleAtTime(@NonNull Executable executable, long time) {
        min(time, getCurrentTime() + 1, "ScheduleTime " + time + " already passed, currentTime = " + getCurrentTime());

        if (!isKilled())
            executables.computeIfAbsent(time, k -> new ConcurrentLinkedDeque<>()).offer(executable);
        else
            throw new ImpossibleSchedulingException("Scheduler not in correct state to schedule Executable, state = " + state);
    }

    @Override
    public void scheduleExecutable(@NonNull Executable executable, long waitingTime, @NonNull ScheduleMode scheduleMode, long nbRepetitions,
                                   long executionTimeStep) {
        switch (scheduleMode) {
            case ONCE -> addOnceExecutable(executable, waitingTime);
            case REPEATEDLY -> addRepeatedlyExecutable(executable, waitingTime, nbRepetitions, executionTimeStep);
            case INFINITELY -> addInfinitelyExecutable(executable, waitingTime, executionTimeStep);
        }
    }

    private void addOnceExecutable(@NonNull Executable executable, long waitingTime) {
        min(waitingTime, 1, "WaitingTime must be greater than 0");

        scheduleAtTime(executable, getCurrentTime() + waitingTime);
    }

    private void addRepeatedlyExecutable(Executable executable, long waitingTime, long nbRepetitions, long executionTimeStep) {
        min(nbRepetitions, 1, "NbRepetitions must be greater than 0");
        min(executionTimeStep, 1, "ExecutionTimeStep must be greater than 0");

        scheduleAtTime(new RepeatedExecutable(this, executable, nbRepetitions, executionTimeStep), getCurrentTime() + waitingTime);
    }

    private void addInfinitelyExecutable(Executable executable, long waitingTime, long executionTimeStep) {
        min(executionTimeStep, 1, "ExecutionTimeStep must be greater than 0");

        scheduleAtTime(new InfiniteExecutable(this, executable, executionTimeStep), getCurrentTime() + waitingTime);
    }

    @Override
    public void await(@NonNull Executor.Condition condition) throws ForcedWakeUpException {
        Executor.ExecutorThread executorThread = executor.getCurrentExecutorThread();
        condition.prepare(executorThread);
        executorThreadWait(executorThread);
    }

    private void executorThreadWait(Executor.ExecutorThread executorThread) throws ForcedWakeUpException {
        try {
            executorThread.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ForcedWakeUpException(e);
        }
    }

    @Override
    public void await(@NonNull Executor.Condition condition, long timeout) throws ForcedWakeUpException {
        min(timeout, 1, "Timeout must be greater or equal to 1");

        scheduleOnce(new WakeupExecutable(condition), timeout);
        await(condition);
    }

    @Override
    public Executor.Condition generateCondition() {
        return executor.generateCondition();
    }

    @Override
    public long getCurrentTime() {
        return currentTime;
    }

    @Override
    public long getMaxDuration() {
        return maxDuration;
    }

    private void notifySchedulerStarted() {
        observers.forEach(SchedulerObserver::schedulerStarted);
    }

    private void notifySchedulerKilled() {
        observers.forEach(SchedulerObserver::schedulerKilled);
    }

    private void notifySchedulerReachEnd() {
        observers.forEach(SchedulerObserver::schedulerReachEnd);
    }

    private void notifySchedulerHasNoExecutable() {
        observers.forEach(SchedulerObserver::noExecutableToExecute);
    }

    // Inner classes.

    private enum SchedulerState {
        CREATED, STARTED, KILLED
    }

    /**
     * Thread launch to manage {@link Scheduler} step. This thread wait until the {@link Executor} finish to execute all {@link Executable} of the
     * current step to pass to the new step.
     */
    private class StepWatcher extends Thread {

        // Variables.

        private boolean killed = false;
        private final SimpleScheduler scheduler;

        // Constructors.

        public StepWatcher() {
            super();
            this.scheduler = SimpleScheduler.this;
            log.info("StepWatcher CREATED");
        }

        // Methods.

        @Override
        public void run() {
            log.info("Start of StepWatcher");
            while (!killed) {
                try {
                    if (!executor.awaitQuiescence())
                        continue;
                } catch (InterruptedException e) {
                    interrupt();
                    killed = true;
                }

                if (!killed)
                    scheduler.executeNextStep();
            }
            log.info("End of StepWatcher");
        }

        public void kill() {
            killed = true;
            log.info("StepWatcher KILLED");
            interrupt();
        }
    }

    /**
     * Used to wake up {@link Executable} which is waiting with the method {@link #await(Executor.Condition, long)}. This {@code Executable} is
     * schedule to be executed at the specified timeout specified during the call of the wait and call {@link Executor.Condition#wakeup()}.
     */
    protected static record WakeupExecutable(@NonNull Executor.Condition condition) implements Executable {

        @Override
        public void execute() {
            condition.wakeup();
        }
    }

    /**
     * {@link Executable} which after its execution schedules a new execution of him to repeat its execution.
     */
    @Getter
    @AllArgsConstructor
    protected abstract static class LoopExecutable implements Executable {

        @NonNull
        private final Scheduler scheduler;

        @NonNull
        private final Executable executable;

        @Override
        public void execute() throws Exception {
            executable.execute();
            scheduleNextExecution();
        }

        protected abstract void scheduleNextExecution();

        @Override
        public Object getLockMonitor() {
            return executable.getLockMonitor();
        }
    }

    /**
     * {@link Executable} use to repeat <strong>finitely</strong> an execution.
     */
    protected static class RepeatedExecutable extends LoopExecutable {

        private long nbNextExecutions;
        private final long executionTimeStep;

        public RepeatedExecutable(@NonNull Scheduler scheduler, @NonNull Executable executable, long nbNextExecutions, long executionTimeStep) {
            super(scheduler, executable);
            this.nbNextExecutions = nbNextExecutions;
            this.executionTimeStep = executionTimeStep;
        }

        @Override
        protected void scheduleNextExecution() {
            if (nbNextExecutions > 1) {
                nbNextExecutions -= 1;
                getScheduler().scheduleOnce(this, executionTimeStep);
            }
        }
    }

    /**
     * {@link Executable} use to repeat <strong>infinitely</strong> an execution.
     */
    protected static class InfiniteExecutable extends LoopExecutable {

        private final long executionTimeStep;

        public InfiniteExecutable(@NonNull Scheduler scheduler, @NonNull Executable executable, long executionTimeStep) {
            super(scheduler, executable);
            this.executionTimeStep = executionTimeStep;
        }

        @Override
        protected void scheduleNextExecution() {
            getScheduler().scheduleOnce(this, executionTimeStep);
        }
    }

}
