package scheduler.executor.multithread;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import scheduler.executor.Executable;
import scheduler.executor.Executor;
import scheduler.executor.exception.NotInExecutorContextException;
import scheduler.executor.exception.RejectedExecutionException;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static common.validation.Validate.min;

@ToString
@Slf4j
public class MultiThreadExecutor implements Executor {

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

    private final Queue<Executable> toExecute;

    @ToString.Exclude
    private final List<Executor.ExecutorThread> executorThreads;

    private int activeThreads = 0;

    private final AtomicBoolean shutdown = new AtomicBoolean(false);

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
        this.toExecute = Queues.newConcurrentLinkedQueue();
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

            if (!toExecute.offer(executable))
                throw new IllegalArgumentException("Executable " + executable + " has not been added");

            if (toExecute.size() == 1)
                waitExecutableCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Executable> shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            executorThreads.forEach(Executor.ExecutorThread::kill);
            try {
                lock.lock();
                List<Executable> remainingExecutables = Lists.newArrayList(toExecute);
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
                return executor.toExecute.poll();
            } finally {
                lock.unlock();
            }
        }

        private void enterExecutionZone() throws InterruptedException {
            executor.executionZone.acquire();
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
                executor.activeThreads++;
            } finally {
                lock.unlock();
            }
        }

        private void decreaseActiveThreads() {
            try {
                lock.lock();
                executor.activeThreads--;

                if (executor.isQuiescence()) {
                    executor.waitQuiescenceCondition.signalAll();
                }
            } finally {
                lock.unlock();
            }
        }

        private void leaveExecutionZone() {
            executor.executionZone.release();
        }

        @Override
        public void await() throws InterruptedException {
            leaveExecutionZone();
            prepareWaiting();

            try {
                synchronized (getLockMonitor()) {
                    log.debug("ExecutorThread wait");
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
            decreaseActiveThreads();
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
                increaseActiveThreads();
                synchronized (getLockMonitor()) {
                    log.debug("WakeUp ExecutorThread");
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
