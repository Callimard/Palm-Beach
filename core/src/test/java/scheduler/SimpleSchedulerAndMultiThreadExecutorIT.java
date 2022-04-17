package scheduler;

import junit.PalmBeachTest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import scheduler.executor.Executable;
import scheduler.executor.multithread.MultiThreadExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Nested
@DisplayName("SimpleScheduler and MultiThreadExecutor integration tests")
@Tag("SimpleScheduler_MultiThreadExecutor")
@Slf4j
@PalmBeachTest
class SimpleSchedulerAndMultiThreadExecutorIT {

    private static final Random r = new Random();

    private static final int NB_EXECUTOR_THREADS = 4;

    private static final long MAX_DURATION = 1000;

    private static final int NB_GENERATED_EXECUTABLES = 500;

    private static Scheduler scheduler;
    private static Scheduler.WaitingSchedulerEndObserver observer;

    @BeforeEach
    void setUp() {
        scheduler = new SimpleScheduler(MAX_DURATION, new MultiThreadExecutor(NB_EXECUTOR_THREADS));
        observer = new Scheduler.WaitingSchedulerEndObserver();
        scheduler.addSchedulerObserver(observer);
    }

    @Nested
    @DisplayName("SimpleScheduler schedule()")
    @Tag("scheduler")
    class Schedule {

        @Test
        @DisplayName("scheduleAtTime() execute Executable at expectedTime")
        void executeAtTime() {
            List<BasicExecutable> executables = generateExecutable(scheduler);
            executables.forEach(exec -> scheduler.scheduleAtTime(exec, exec.getExpectedExecutedTime()));
            scheduler.start();

            waitSchedulerEnd(scheduler, observer);

            checkAllExecutedAtExpectedTime(executables);
        }

        @Test
        @DisplayName("scheduleAtTime() finish even if there is Executable infinitely waiting")
        void withInfinitelyWaitingExecutable() {
            List<WaitingExecutable> executables = generateWaitingExecutable(scheduler);
            executables.forEach(exec -> scheduler.scheduleAtTime(exec, exec.getExpectedExecutedTime()));
            scheduler.start();

            waitSchedulerEnd(scheduler, observer);

            checkAllNotExecuted(executables);
        }

        @Test
        @DisplayName("scheduleAtTime() with await(Condition, long) wake up the specified executable")
        void wakeUpExecutableWithAwaitTimeout() {
            int waitingTime = 50;
            WaitingExecutable waitingExecutable = new WaitingExecutable(scheduler, Scheduler.NOW, waitingTime);
            scheduler.scheduleAtTime(waitingExecutable, waitingExecutable.getExpectedExecutedTime());
            scheduler.start();

            waitSchedulerEnd(scheduler, observer);

            assertThat(waitingExecutable.getExecutionCounter()).isEqualByComparingTo(1);
            assertThat(waitingExecutable.getExecutedTime()).isEqualByComparingTo(Scheduler.NOW + waitingTime);
        }

        @Test
        @DisplayName("scheduleExecutable() in Repeatedly mode execute the Executable correctly")
        void inRepeatedlyMode() {
            BasicExecutable basicExecutable = new BasicExecutable(scheduler, Scheduler.NOW);
            int nbRepetitions = 5;
            int executionTimeStep = 50;
            scheduler.scheduleExecutable(basicExecutable, basicExecutable.getExpectedExecutedTime(), Scheduler.ScheduleMode.REPEATEDLY,
                                         nbRepetitions, executionTimeStep);
            scheduler.start();

            waitSchedulerEnd(scheduler, observer);

            assertThat(basicExecutable.getExecutionCounter()).isEqualByComparingTo(nbRepetitions);
            assertThat(basicExecutable.getExecutedTime()).isEqualByComparingTo(Scheduler.NOW + (executionTimeStep * (nbRepetitions - 1)));
        }

        @Test
        @DisplayName("ScheduleExecutable() in Infinitely mode finish")
        void inInfinitelyMode() {
            BasicExecutable basicExecutable = new BasicExecutable(scheduler, Scheduler.NOW);
            int executionTimeStep = 50;
            scheduler.scheduleExecutable(basicExecutable, basicExecutable.getExpectedExecutedTime(), Scheduler.ScheduleMode.INFINITELY,
                                         -1, executionTimeStep);
            scheduler.start();

            waitSchedulerEnd(scheduler, observer);
        }

    }

    private List<BasicExecutable> generateExecutable(Scheduler scheduler) {
        List<BasicExecutable> executables = new ArrayList<>();
        for (int i = 0; i < NB_GENERATED_EXECUTABLES; i++) {
            executables.add(new BasicExecutable(scheduler, r.nextLong(Scheduler.NOW, MAX_DURATION + 1)));
        }
        return executables;
    }

    private List<WaitingExecutable> generateWaitingExecutable(Scheduler scheduler) {
        List<WaitingExecutable> executables = new ArrayList<>();
        for (int i = 0; i < NB_GENERATED_EXECUTABLES; i++) {
            executables.add(new WaitingExecutable(scheduler, r.nextLong(Scheduler.NOW, MAX_DURATION + 1)));
        }
        return executables;
    }

    private void checkAllExecutedAtExpectedTime(List<? extends BasicExecutable> executables) {
        for (BasicExecutable executable : executables) {
            assertThat(executable.getExecutionCounter()).isEqualByComparingTo(1);
            assertThat(executable.getExecutedTime()).isEqualByComparingTo(executable.getExpectedExecutedTime());
        }
    }

    private void checkAllNotExecuted(List<? extends BasicExecutable> executables) {
        for (BasicExecutable executable : executables) {
            assertThat(executable.getExecutionCounter()).isEqualByComparingTo(0);
        }
    }

    private void waitSchedulerEnd(Scheduler scheduler, Scheduler.WaitingSchedulerEndObserver observer) {
        try {
            int counter = 0;
            while (!scheduler.isKilled()) {
                observer.waitSchedulerEnd(1000L);
                counter++;
                log.debug("Wake up after waiting Scheduler end");
                if (counter >= 10) {
                    log.error("To much wait Scheduler end");
                    fail();
                }
            }
        } catch (InterruptedException e) {
            fail(e);
        }
    }

    // Inner classes.

    @RequiredArgsConstructor
    public static class BasicExecutable implements Executable {

        private final Scheduler scheduler;

        @Getter
        private final long expectedExecutedTime;

        @Getter
        private int executionCounter = 0;

        @Getter
        private long executedTime;

        @Override
        public void execute() throws Exception {
            executedTime = scheduler.getCurrentTime();
            executionCounter++;
        }
    }

    public static class WaitingExecutable extends BasicExecutable {

        private long waitingTime = 0;

        public WaitingExecutable(Scheduler scheduler, long expectedExecutedTime) {
            super(scheduler, expectedExecutedTime);
        }

        public WaitingExecutable(Scheduler scheduler, long expectedExecutedTime, long waitingTime) {
            super(scheduler, expectedExecutedTime);
            this.waitingTime = waitingTime;
        }

        @Override
        public void execute() throws Exception {
            if (waitingTime <= 0)
                scheduler.await(scheduler.generateCondition());
            else
                scheduler.await(scheduler.generateCondition(), waitingTime);

            super.execute();
        }
    }

}
