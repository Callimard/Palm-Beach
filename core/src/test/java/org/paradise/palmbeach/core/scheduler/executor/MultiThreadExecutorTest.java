package org.paradise.palmbeach.core.scheduler.executor;

import org.paradise.palmbeach.core.scheduler.executor.exception.NotInExecutorContextException;
import org.paradise.palmbeach.core.scheduler.executor.exception.RejectedExecutionException;
import org.paradise.palmbeach.core.scheduler.executor.multithread.MultiThreadExecutor;
import org.paradise.palmbeach.core.junit.PalmBeachTest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Nested
@DisplayName("MultiThreadExecutor tests")
@Tag("MultiThreadExecutor")
@Slf4j
@PalmBeachTest
public class MultiThreadExecutorTest {

    private static final int RUNNING_THREAD = 4;

    private static final int NUMBER_CORRECT_EXECUTABLE = 50;
    private static final int NUMBER_FAILED_EXECUTABLE = 50;
    private static final int NUMBER_WAITING_EXECUTABLE = 10;
    private static final int NUMBER_NOTIFIER_EXECUTABLE = 5;

    private static final long DEFAULT_TERMINATION_WAITING_TIMEOUT = 50L;

    @Nested
    @DisplayName("MultiThreadExecutor constructor")
    @Tag("constructor")
    class Constructor {

        @ParameterizedTest
        @ValueSource(ints = {-1654, -546565, -1, 0})
        @DisplayName("constructor throws IllegalArgumentException with less than 1 maxRunningThreads")
        void withUnCorrectMaxRunningThreads(int maxRunningThreads) {
            assertThrows(IllegalArgumentException.class, () -> new MultiThreadExecutor(maxRunningThreads));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 4, 8, 10})
        @DisplayName("constructor does not throw exception with correct parameters and is in correct state")
        void withCorrectParameter(int maxRunningThreads) {
            AtomicReference<Executor> executor = new AtomicReference<>();

            assertDoesNotThrow(() -> executor.set(new MultiThreadExecutor(maxRunningThreads)));

            assertThat(executor.get().isQuiescence()).isTrue();
            assertThat(executor.get().isShutdown()).isFalse();
            assertThat(executor.get().isTerminated()).isFalse();
        }
    }

    @Nested
    @DisplayName("MultiThreadExecutor execute()")
    @Tag("execute")
    class Execute {

        @Test
        @DisplayName("execute() throws NullPointerException with null Executable")
        void withNullExecutable() {
            Executor executor = new MultiThreadExecutor(RUNNING_THREAD);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> executor.execute(null));
        }

        @Test
        @DisplayName("execute() throws RejectedExecutionException if Executor is shutdown")
        void withShutdownExecutor(@Mock Executable executable) throws Exception {
            Executor executor = new MultiThreadExecutor(RUNNING_THREAD);
            executor.shutdown();

            assertThat(executor.isShutdown()).isTrue();
            assertThrows(RejectedExecutionException.class, () -> executor.execute(executable));
            verify(executable, times(0)).execute();
        }

        @Test
        @DisplayName("execute() execute all Executables")
        void executeAllExecutables() throws InterruptedException {
            Executor executor = new MultiThreadExecutor(RUNNING_THREAD);
            List<BasicExecutable> executables = generateBasicExecutables();

            executables.forEach(executor::execute);

            awaitExecutorQuiescence(executor);
            checkAllExecutableHasBeenExecuted(executables);
        }

        @Test
        @DisplayName("execute() execute all Executables even some of them fail")
        void executeAllExecutablesEvenFailedExecutables() throws InterruptedException {
            Executor executor = new MultiThreadExecutor(RUNNING_THREAD);
            List<BasicExecutable> executables = generateBasicAndFailExecutables();

            executables.forEach(executor::execute);

            awaitExecutorQuiescence(executor);
            checkAllExecutableHasBeenExecuted(executables);
        }

        @Test
        @DisplayName("execute() execute all Executables even with waiting Executables")
        void executeAllExecutableEvenWithWaitingExecutables() throws InterruptedException {
            Executor executor = new MultiThreadExecutor(RUNNING_THREAD);
            List<BasicExecutable> correctExecutables = generateBasicExecutables();
            List<WaitingExecutable> waitingExecutables = generateWaitingExecutables(executor);
            List<BasicExecutable> executables = generateBasicExecutables(correctExecutables, waitingExecutables);

            executables.forEach(executor::execute);

            awaitExecutorQuiescence(executor);
            checkAllExecutableHasBeenExecuted(correctExecutables);
            checkAllExecutableHasNotBeenExecuted(waitingExecutables);
        }

        @Test
        @DisplayName("execute() execute all Executables event with failed and waiting Executables")
        void executeAllTypeOfExecutables() throws InterruptedException {
            Executor executor = new MultiThreadExecutor(RUNNING_THREAD);
            List<BasicExecutable> correctExecutables = generateBasicExecutables();
            List<FailedExecutable> failedExecutables = generateFailedExecutables();
            List<WaitingExecutable> waitingExecutables = generateWaitingExecutables(executor);
            List<BasicExecutable> executables = generateBasicExecutables(correctExecutables, failedExecutables, waitingExecutables);

            executables.forEach(executor::execute);

            awaitExecutorQuiescence(executor);
            checkAllExecutableHasBeenExecuted(correctExecutables);
            checkAllExecutableHasBeenExecuted(failedExecutables);
            checkAllExecutableHasNotBeenExecuted(waitingExecutables);
        }

        @Test
        @DisplayName("execute() execute all waiting executables which are wakeup")
        void executeWakeUpWaitingExecutable() throws InterruptedException {
            Executor executor = new MultiThreadExecutor(RUNNING_THREAD);
            List<NotifierExecutable> notifierExecutables = generateNotifierExecutables(executor);
            List<WaitingExecutable> waitingExecutables = generateWaitingExecutables(executor, notifierExecutables);

            waitingExecutables.forEach(executor::execute);
            awaitExecutorQuiescence(executor);

            checkAllExecutableHasNotBeenExecuted(waitingExecutables);

            notifierExecutables.forEach(executor::execute);
            awaitExecutorQuiescence(executor);

            checkAllExecutableHasBeenExecuted(notifierExecutables);
            checkAllExecutableHasBeenExecuted(waitingExecutables);
        }
    }

    @Nested
    @DisplayName("MultiThreadExecutor shutdown()")
    @Tag("shutdown")
    class Shutdown {

        @Test
        @DisplayName("shutdown() wake up waiting thread")
        void shutdownWakeUpWaitingThread() throws InterruptedException {
            Executor executor = new MultiThreadExecutor(RUNNING_THREAD);
            List<WaitingExecutable> waitingExecutables = generateWaitingExecutables(executor);

            waitingExecutables.forEach(executor::execute);
            awaitExecutorQuiescence(executor);

            checkAllExecutableHasNotBeenExecuted(waitingExecutables);

            executor.shutdown();
            awaitExecutorTermination(executor);

            checkAllExecutableHasNotBeenExecuted(waitingExecutables);
        }
    }

    @Nested
    @DisplayName("MultiThreadExecutor getCurrentExecutorThread()")
    @Tag("getCurrentExecutorThread")
    class GetCurrentExecutorThread {

        @Test
        @DisplayName("getCurrentExecutorThread() throws NotInExecutorContextException if not in Executor context")
        void notInExecutorContext() {
            Executor executor = new MultiThreadExecutor(RUNNING_THREAD);

            assertThrows(NotInExecutorContextException.class, executor::getCurrentExecutorThread);
        }
    }

    @Nested
    @DisplayName("MultiThreadExecutor awaitQuiescence(long)")
    @Tag("awaitQuiescence")
    class AwaitQuiescence {

        @Test
        @DisplayName("awaitQuiescence(long) returns true if executor is quiescence")
        void returnsTrueWithQuiescenceExecutor() throws InterruptedException {
            Executor executor = new MultiThreadExecutor(RUNNING_THREAD);

            assertThat(executor.awaitQuiescence(100L)).isTrue();
        }
    }

    @Nested
    @DisplayName("MultiThreadExecutor awaitTermination()")
    @Tag("awaitTermination")
    class AwaitTermination {

        @Test
        @DisplayName("awaitTermination() returns if the Executor is terminated")
        void returnsTrueWithTerminatedExecutor() throws InterruptedException {
            Executor executor = new MultiThreadExecutor(RUNNING_THREAD);
            executor.shutdown();

            awaitExecutorTermination(executor);

            assertThat(executor.awaitTermination(100L)).isTrue();
        }
    }

    private List<BasicExecutable> generateBasicExecutables() {
        List<BasicExecutable> executables = new ArrayList<>();
        for (int i = 0; i < NUMBER_CORRECT_EXECUTABLE; i++) {
            executables.add(new BasicExecutable());
        }
        return executables;
    }

    private List<FailedExecutable> generateFailedExecutables() {
        List<FailedExecutable> failedExecutables = new ArrayList<>();
        for (int i = 0; i < NUMBER_FAILED_EXECUTABLE; i++) {
            failedExecutables.add(new FailedExecutable());
        }
        return failedExecutables;
    }

    private List<WaitingExecutable> generateWaitingExecutables(Executor executor) {
        List<WaitingExecutable> waitingExecutables = new ArrayList<>();
        for (int i = 0; i < NUMBER_WAITING_EXECUTABLE; i++) {
            waitingExecutables.add(new WaitingExecutable(executor, null));
        }
        return waitingExecutables;
    }

    private List<WaitingExecutable> generateWaitingExecutables(Executor executor, List<NotifierExecutable> notifierExecutables) {
        List<WaitingExecutable> waitingExecutables = new ArrayList<>();
        for (NotifierExecutable notifierExecutable : notifierExecutables) {
            waitingExecutables.add(new WaitingExecutable(executor, notifierExecutable));
        }
        return waitingExecutables;
    }

    private List<NotifierExecutable> generateNotifierExecutables(Executor executor) {
        List<NotifierExecutable> notifierExecutables = new ArrayList<>();
        for (int i = 0; i < NUMBER_NOTIFIER_EXECUTABLE; i++) {
            notifierExecutables.add(new NotifierExecutable(executor.generateCondition(), new AtomicBoolean(false)));
        }
        return notifierExecutables;
    }

    private List<BasicExecutable> generateBasicAndFailExecutables() {
        List<BasicExecutable> executables = new ArrayList<>();
        int addedCorrect = 0;
        int addedFail = 0;
        for (int i = 0; i < NUMBER_CORRECT_EXECUTABLE + NUMBER_FAILED_EXECUTABLE; i++) {
            if (i % 2 == 0) {
                if (addedCorrect < NUMBER_CORRECT_EXECUTABLE) {
                    executables.add(new BasicExecutable());
                    addedCorrect++;
                } else {
                    executables.add(new FailedExecutable());
                    addedFail++;
                }
            } else {
                if (addedFail < NUMBER_FAILED_EXECUTABLE) {
                    executables.add(new FailedExecutable());
                    addedFail++;
                } else {
                    executables.add(new BasicExecutable());
                    addedCorrect++;
                }
            }
        }
        return executables;
    }

    private List<BasicExecutable> generateBasicExecutables(List<BasicExecutable> basicExecutables, List<WaitingExecutable> waitingExecutables) {
        List<BasicExecutable> executables = new ArrayList<>();
        int addedCorrect = 0;
        int addedWaiting = 0;
        for (int i = 0; i < basicExecutables.size() + waitingExecutables.size(); i++) {
            if (i % 2 == 0) {
                if (addedCorrect < basicExecutables.size()) {
                    executables.add(basicExecutables.get(addedCorrect++));
                } else {
                    executables.add(waitingExecutables.get(addedWaiting++));
                }
            } else {
                if (addedWaiting < waitingExecutables.size()) {
                    executables.add(waitingExecutables.get(addedWaiting++));
                } else {
                    executables.add(basicExecutables.get(addedCorrect++));
                }
            }
        }
        return executables;
    }

    private List<BasicExecutable> generateBasicExecutables(List<BasicExecutable> basicExecutables,
                                                           List<FailedExecutable> failedExecutables, List<WaitingExecutable> waitingExecutables) {
        List<BasicExecutable> executables = new ArrayList<>();
        int addedCorrect = 0;
        int addedFailed = 0;
        int addedWaiting = 0;

        for (int i = 0; i < basicExecutables.size() + failedExecutables.size() + waitingExecutables.size(); i++) {
            if (i % 3 == 0) {
                if (addedCorrect < basicExecutables.size()) {
                    executables.add(basicExecutables.get(addedCorrect++));
                } else if (addedFailed < failedExecutables.size()) {
                    executables.add(failedExecutables.get(addedFailed++));
                } else {
                    executables.add(waitingExecutables.get(addedWaiting++));
                }
            } else if (i % 3 == 1) {
                if (addedFailed < failedExecutables.size()) {
                    executables.add(failedExecutables.get(addedFailed++));
                } else if (addedCorrect < basicExecutables.size()) {
                    executables.add(basicExecutables.get(addedCorrect++));
                } else {
                    executables.add(waitingExecutables.get(addedWaiting++));
                }
            } else {
                if (addedWaiting < waitingExecutables.size()) {
                    executables.add(waitingExecutables.get(addedWaiting++));
                } else if (addedFailed < failedExecutables.size()) {
                    executables.add(failedExecutables.get(addedFailed++));
                } else {
                    executables.add(basicExecutables.get(addedCorrect++));
                }
            }
        }
        return executables;
    }

    private void awaitExecutorQuiescence(Executor executor) throws InterruptedException {
        int counter = 0;
        while (!executor.isQuiescence()) {
            executor.awaitQuiescence(1000);
            log.debug("WakeUp wait quiescence, Executor = {}", executor);
            counter++;
            if (counter >= 5)
                fail("To mush time to wait quiescence");
        }
    }

    private void awaitExecutorTermination(Executor executor) throws InterruptedException {
        int counter = 0;
        while (!executor.isTerminated()) {
            executor.awaitTermination(DEFAULT_TERMINATION_WAITING_TIMEOUT);
            log.debug("WakeUp wait termination, Executor = {}", executor);
            counter++;
            if (counter >= 5)
                fail("To mush time to wait quiescence");
        }
    }

    private void checkAllExecutableHasBeenExecuted(List<? extends BasicExecutable> executables) {
        executables.forEach(executable -> assertThat(executable.isExecuted()).isTrue());
    }

    private void checkAllExecutableHasNotBeenExecuted(List<? extends BasicExecutable> executables) {
        executables.forEach(executable -> assertThat(executable.isExecuted()).isFalse());
    }

    // Inner class

    @EqualsAndHashCode
    @ToString
    public static class BasicExecutable implements Executable {

        private static int counter = 0;

        private final int id;

        public BasicExecutable() {
            id = counter++;
        }

        @Getter
        private boolean executed = false;

        @Override
        public void execute() throws Exception {
            executed = true;
        }
    }

    public static class FailedExecutable extends BasicExecutable {
        @Override
        public void execute() throws Exception {
            super.execute();
            throw new RuntimeException("Fail Executable");
        }
    }

    @AllArgsConstructor
    public static class WaitingExecutable extends BasicExecutable {

        private final Executor executor;

        private final NotifierExecutable notifierExecutable;

        @Override
        public void execute() throws Exception {
            if (notifierExecutable == null) {
                Executor.ExecutorThread executorThread = executor.getCurrentExecutorThread();
                prepareCondition(executorThread);
                executorThread.await();
                super.execute();
            } else {
                synchronized (notifierExecutable) {
                    Executor.ExecutorThread executorThread = executor.getCurrentExecutorThread();
                    prepareCondition(executorThread);
                    if (!notifierExecutable.getWaitingExecutableFree().get())
                        executorThread.await();

                    super.execute();
                }
            }
        }

        private void prepareCondition(Executor.ExecutorThread executorThread) {
            if (notifierExecutable != null)
                notifierExecutable.condition.prepare(executorThread);
        }

        @Override
        public Object getLockMonitor() {
            return notifierExecutable;
        }
    }

    @AllArgsConstructor
    public static class NotifierExecutable extends BasicExecutable {

        @Getter
        private final Executor.Condition condition;

        @Getter
        @NonNull
        private AtomicBoolean waitingExecutableFree;

        @Override
        public synchronized void execute() throws Exception {
            waitingExecutableFree.set(true);
            condition.wakeup();
            super.execute();
        }

        @Override
        public Object getLockMonitor() {
            return this;
        }
    }

}
