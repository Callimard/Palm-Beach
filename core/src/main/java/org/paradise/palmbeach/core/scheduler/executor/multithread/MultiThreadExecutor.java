package org.paradise.palmbeach.core.scheduler.executor.multithread;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.paradise.palmbeach.core.scheduler.executor.Executable;
import org.paradise.palmbeach.core.scheduler.executor.Executor;
import org.paradise.palmbeach.core.scheduler.executor.exception.FailToPollExecutable;
import org.paradise.palmbeach.core.scheduler.executor.exception.NotInExecutorContextException;
import org.paradise.palmbeach.core.scheduler.executor.exception.RejectedExecutionException;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.paradise.palmbeach.utils.validation.Validate.min;

@ToString
@Slf4j
public class MultiThreadExecutor implements Executor {

    // Constants.

    private static final Object NULL_LOCK_MONITOR_KEY = new Object();

    // Variables.

    @ToString.Exclude
    private final Lock lock = new ReentrantLock();

    @ToString.Exclude
    private final java.util.concurrent.locks.Condition waitExecutableCondition = lock.newCondition();

    @ToString.Exclude
    private final java.util.concurrent.locks.Condition waitQuiescenceCondition = lock.newCondition();

    @ToString.Exclude
    private final Semaphore executionZone;

    @Getter
    private final int maxRunningThreads;

    private final Map<Object, List<Executable>> toExecute;

    @ToString.Exclude
    private final List<Executor.ExecutorThread> executorThreads;

    private int activeThreads = 0;

    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private final Map<Object, Long> lockCounter = Maps.newConcurrentMap();

    // Constructors.

    /**
     * Constructs an {@link MultiThreadExecutor} which use several concurrent {@link Executor.ExecutorThread} to execute given {@link Executable}. It
     * is possible to set maxRunningThreads to 1, in that case it is a sigle thread {@link Executor}.
     * <p>
     * However, this implementation guarantees that never more than maxRunningThreads will be concurrently in execution, but it is possible in certain
     * case that more than maxRunningThreads threads are created.
     *
     * @param maxRunningThreads the max number of concurrent executing thread
     *
     * @throws IllegalArgumentException if maxRunningThreads is less than 1
     */
    public MultiThreadExecutor(int maxRunningThreads) {
        min(maxRunningThreads, 1, "MaxRunningThreads must be greater or equal to 1");

        this.maxRunningThreads = maxRunningThreads;
        this.toExecute = Maps.newHashMap();
        this.executionZone = new Semaphore(this.maxRunningThreads, true);
        this.executorThreads = new Vector<>();
        createExecutorThreads();
    }

    // Methods.

    private void createExecutorThreads() {
        for (int i = 0; i < maxRunningThreads; i++) {
            Executor.ExecutorThread executorThread = new MultiThreadExecutor.InternalThread();
            executorThreads.add(executorThread);
            executorThread.start();
        }
    }

    @Override
    public void execute(@NonNull Executable executable) {
        try {
            lock.lock();
            if (isShutdown())
                throw new RejectedExecutionException("Executor " + this + " shutdown, cannot execute Executable anymore");

            addExecutable(executable);

            if (toExecute.size() == 1)
                waitExecutableCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void addExecutable(Executable executable) {
        if (executable.getLockMonitor() == null) {
            toExecute.merge(NULL_LOCK_MONITOR_KEY, Lists.newArrayList(executable), (old, v) -> {
                old.addAll(v);
                return old;
            });
        } else {
            toExecute.merge(executable.getLockMonitor(), Lists.newArrayList(executable), (old, v) -> {
                old.addAll(v);
                return old;
            });
        }
    }

    @Override
    public List<Executable> shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            executorThreads.forEach(Executor.ExecutorThread::kill);
            try {
                lock.lock();

                List<Executable> remainingExecutables = Lists.newArrayList();
                for (List<Executable> executables : toExecute.values()) {
                    remainingExecutables.addAll(executables);
                }

                toExecute.clear();
                return remainingExecutables;
            } finally {
                lock.unlock();
            }
        } else
            return Collections.emptyList();

    }

    @Override
    public boolean isShutdown() {
        return shutdown.get();
    }

    @Override
    public boolean isTerminated() {
        return isQuiescence() && isShutdown();
    }

    @Override
    public boolean awaitTermination(long timeout) throws InterruptedException {
        return awaitQuiescence(timeout) && isTerminated();
    }

    @Override
    public boolean isQuiescence() {
        return toExecute.isEmpty() && noActiveThread();
    }

    private boolean noActiveThread() {
        try {
            lock.lock();
            return activeThreads == 0;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean awaitQuiescence() throws InterruptedException {
        try {
            lock.lock();
            while (!isQuiescence()) {
                waitQuiescenceCondition.await();
            }

            return isQuiescence();
        } finally {
            lock.unlock();
        }

    }

    @Override
    public boolean awaitQuiescence(long timeout) throws InterruptedException {
        try {
            lock.lock();
            boolean hasBeenWakeUp = waitQuiescenceCondition.await(timeout, TimeUnit.MILLISECONDS);
            log.debug("AwaitQuiescence(timeout) -> hasBeenWakeUp ? {}", hasBeenWakeUp);
            return isQuiescence();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Executor.ExecutorThread getCurrentExecutorThread() {
        if (Thread.currentThread() instanceof Executor.ExecutorThread executorThread)
            return executorThread;
        else
            throw new NotInExecutorContextException();
    }

    @Override
    public Executor.Condition generateCondition() {
        return new Executor.Condition();
    }

    // Inner class.

    /**
     * Concrete implementation of {@link Executor.ExecutorThread} which can only be used in {@link MultiThreadExecutor}.
     */
    private class InternalThread extends Executor.ExecutorThread {

        // Variables.

        private final MultiThreadExecutor executor;

        private Executable currentExecutable;

        private final AtomicBoolean waiting = new AtomicBoolean(false);

        private final AtomicBoolean awake = new AtomicBoolean(false);

        private final AtomicBoolean killed = new AtomicBoolean(false);

        private boolean lastRun = false;

        private int increaseCounter = 0;

        // Constructors.

        public InternalThread() {
            super();
            this.executor = MultiThreadExecutor.this;
            log.info("ExecutorThread {} created", this);
        }

        // Methods.

        @Override
        public void run() {
            try {
                log.info("ExecutorThread {} begin to run", this);
                while (!lastRun) {
                    currentExecutable = nextExecutable();
                    enterExecutionZone();
                    execute();
                    leaveExecutionZone();
                    currentExecutable = null;
                }
            } catch (InterruptedException e) {
                log.debug("ExecutorThread {} INTERRUPTED -> kill it", this);
                interrupt();
            } finally {
                log.info("End of ExecutorThread {}, killed {}, lastRun {}", this, killed.get(), lastRun);
                kill();
                removeFromExecutorThreads();
            }
        }

        private Executable nextExecutable() throws InterruptedException {
            try {
                lock.lock();

                while (executor.toExecute.isEmpty()) {
                    // Wait until executable is offered
                    executor.waitExecutableCondition.await();
                }

                increaseActiveThreads();
                return pollExecutor();
            } finally {
                lock.unlock();
            }
        }

        private Executable pollExecutor() {
            final Set<Object> allLockUsed = lockCounter.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 1L)
                    .sorted(Comparator.comparingLong(Map.Entry::getValue))
                    .map(Map.Entry::getKey).collect(Collectors.toSet());

            List<Object> possibleExecutables =
                    executor.toExecute.keySet().stream()
                            .filter(lockMonitorKey -> !allLockUsed.contains(lockMonitorKey) && lockMonitorKey != NULL_LOCK_MONITOR_KEY).toList();

            if (!possibleExecutables.isEmpty()) {
                Object lockMonitorKey = possibleExecutables.get(0);
                return getExecutable(lockMonitorKey);
            } else {
                if (executor.toExecute.containsKey(NULL_LOCK_MONITOR_KEY)) {
                    return getExecutable(NULL_LOCK_MONITOR_KEY);
                } else {
                    for (Object lockUsed : allLockUsed) {
                        if (executor.toExecute.containsKey(lockUsed)) {
                            return getExecutable(lockUsed);
                        }
                    }

                    throw new FailToPollExecutable();
                }
            }
        }

        /**
         * @param lockMonitorKey lockMonitor for which we want executables
         *
         * @return a {@link Executable} of the list mapped to the specified lockMonitorKey. <strong>DOES NOT VERIFY IF KEY IS PRESENT.</strong>
         */
        private Executable getExecutable(Object lockMonitorKey) {
            List<Executable> executables = executor.toExecute.get(lockMonitorKey);
            Executable chosen = executables.remove(0);
            if (executables.isEmpty()) {
                executor.toExecute.remove(lockMonitorKey);
            }
            return chosen;
        }

        private void enterExecutionZone() throws InterruptedException {
            executor.executionZone.acquire();
            if (currentExecutable.getLockMonitor() != null) {
                lockCounter.merge(currentExecutable.getLockMonitor(), 1L, Long::sum);
            }
        }

        private void execute() {
            try {
                currentExecutable.execute();
            } catch (Exception e) {
                log.error("Executable execution throws Exception", e);
            } finally {
                decreaseActiveThreads();
            }
        }

        private void increaseActiveThreads() {
            try {
                lock.lock();
                increaseCounter++;
                executor.activeThreads++;
            } finally {
                lock.unlock();
            }
        }

        private void decreaseActiveThreads() {
            try {
                lock.lock();
                executor.activeThreads -= increaseCounter;
                increaseCounter = 0;

                if (executor.isQuiescence()) {
                    executor.waitQuiescenceCondition.signalAll();
                }
            } finally {
                lock.unlock();
            }
        }

        private void leaveExecutionZone() {
            if (currentExecutable.getLockMonitor() != null) {
                lockCounter.computeIfPresent(currentExecutable.getLockMonitor(), (k, v) -> v - 1);
            }
            executor.executionZone.release();
        }

        @Override
        public void await() throws InterruptedException {
            leaveExecutionZone();
            prepareWaiting();

            try {
                synchronized (getLockMonitor()) {
                    log.debug("ExecutorThread wait");
                    decreaseActiveThreads();
                    getLockMonitor().wait();
                    log.debug("ExecutorThread wake up");
                }
            } catch (InterruptedException e) {
                log.info("Executable INTERRUPTED while waiting");
                increaseActiveThreads();
                interrupt();
                throw e;
            } finally {
                afterWaiting();
            }

            enterExecutionZone();
        }

        private void prepareWaiting() {
            lastRun = true;
            waiting.set(true);
            createNewExecutorThread();
        }

        private void afterWaiting() {
            waiting.set(false);
            awake.set(false);
        }

        private void createNewExecutorThread() {
            Executor.ExecutorThread executorThread = new MultiThreadExecutor.InternalThread();
            executor.executorThreads.add(executorThread);
            executorThread.start();
        }

        private void removeFromExecutorThreads() {
            executor.executorThreads.remove(this);
        }

        @Override
        public void wakeUp() {
            if (waiting.get() && awake.compareAndSet(false, true)) {
                synchronized (getLockMonitor()) {
                    log.debug("WakeUp ExecutorThread");
                    increaseActiveThreads();
                    getLockMonitor().notifyAll();
                }
            }
        }

        @Override
        public void kill() {
            if (killed.compareAndSet(false, true))
                interrupt();
        }

        private Object getLockMonitor() {
            return currentExecutable.getLockMonitor() != null ? currentExecutable.getLockMonitor() : this;
        }
    }

}
