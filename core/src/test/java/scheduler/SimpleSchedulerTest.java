package scheduler;

import junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import scheduler.exception.CannotKillSchedulerException;
import scheduler.exception.CannotStartSchedulerException;
import scheduler.exception.ForcedWakeUpException;
import scheduler.exception.ImpossibleSchedulingException;
import scheduler.executor.Executable;
import scheduler.executor.Executor;
import scheduler.executor.exception.NotInExecutorContextException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Nested
@DisplayName("SimpleScheduler tests")
@Tag("SimpleScheduler")
@PalmBeachTest
public class SimpleSchedulerTest {

    private static final long DEFAULT_MAX_DURATION = 1500L;

    @Nested
    @DisplayName("SimpleScheduler constructor")
    @Tag("Constructor")
    class Constructor {

        @ParameterizedTest
        @ValueSource(ints = {-9645, -64, -546413, -15, -1, 0})
        @DisplayName("constructor throws IllegalException with less than 1 max duration")
        void withLessThanOneMaxDuration(int maxDuration, @Mock Executor executor) {
            assertThrows(IllegalArgumentException.class, () -> new SimpleScheduler(maxDuration, executor));
        }

        @Test
        @DisplayName("constructor throws NUllPointerException with null executor")
        void withNullExecutor() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> new SimpleScheduler(1, null));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 1545, 645654, 6, 466})
        @DisplayName("constructors does not throw exception with correct parameter")
        void withCorrectParameter(int maxDuration, @Mock Executor executor) {
            assertDoesNotThrow(() -> new SimpleScheduler(maxDuration, executor));
        }
    }

    @Nested
    @DisplayName("SimpleScheduler toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void nerverReturnsNull(@Mock Executor executor) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThat(scheduler.toString()).isNotNull();
        }
    }

    @Nested
    @DisplayName("SimpleScheduler addSchedulerObserve()")
    @Tag("addSchedulerObserver")
    class AddSchedulerObserver {

        @Test
        @DisplayName("addSchedulerObserver() throws NullPointerException with null Observer")
        void withNullObserver(@Mock Executor executor) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> scheduler.addSchedulerObserver(null));
        }

        @Test
        @DisplayName("addSchedulerObserver() returns true if it is the first time that the observer is added")
        void firstTimeAdded(@Mock Executor executor, @Mock Scheduler.SchedulerObserver observer) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThat(scheduler.addSchedulerObserver(observer)).isTrue();
        }

        @Test
        @DisplayName("addSchedulerObserver() returns false if the observer has already been added")
        void alreadyAddedObserver(@Mock Executor executor, @Mock Scheduler.SchedulerObserver observer) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);
            scheduler.addSchedulerObserver(observer);

            assertThat(scheduler.addSchedulerObserver(observer)).isFalse();
        }
    }


    @Nested
    @DisplayName("SimpleScheduler start()")
    @Tag("start")
    class Start {

        @Test
        @DisplayName("start() does not throws exception after Scheduler creation with no executable, directly kill by no executable to execute and " +
                "call SchedulerObserver methods")
        void afterCreation(@Mock Executor executor, @Mock Scheduler.SchedulerObserver observer) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);
            scheduler.addSchedulerObserver(observer);

            assertThat(scheduler.isEnded()).isFalse();
            assertDoesNotThrow(scheduler::start);
            verify(observer, times(1)).schedulerStarted();
            verify(observer, times(1)).noExecutableToExecute();
            verify(observer, times(1)).schedulerKilled();
            assertThat(scheduler.isEnded()).isTrue();
        }

        @Test
        @DisplayName("start() throws CannotStartSchedulerException if at been already started")
        void alreadyStarted(@Mock Executor executor) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);
            scheduler.start();

            assertThrows(CannotStartSchedulerException.class, scheduler::start);
        }

        @Test
        @DisplayName("start() executes schedules Executable which are scheduled before max duration")
        void executeExecutable(@Mock Executor executor, @Mock Scheduler.SchedulerObserver observer, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);
            scheduler.addSchedulerObserver(observer);
            scheduler.scheduleOnce(executable, DEFAULT_MAX_DURATION - 1);
            scheduler.start();
            scheduler.kill();

            verify(executor, times(1)).execute(executable);
        }

        @Test
        @DisplayName("start() direct finish by end reach if Executable are scheduled after the max duration")
        void reachEnd(@Mock Executor executor, @Mock Scheduler.SchedulerObserver observer, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);
            scheduler.addSchedulerObserver(observer);
            scheduler.scheduleOnce(executable, DEFAULT_MAX_DURATION + 1);
            scheduler.start();

            verify(observer, times(1)).schedulerReachEnd();
        }
    }

    @Nested
    @DisplayName("SimpleScheduler kill()")
    @Tag("kill")
    class Kill {

        @Test
        @DisplayName("kill() throws CannotKillSchedulerException is Scheduler is just created")
        void withCreatedScheduler(@Mock Executor executor) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThrows(CannotKillSchedulerException.class, scheduler::kill);
        }
    }

    @Nested
    @DisplayName("SimpleScheduler scheduleAtTime()")
    @Tag("scheduleAtTime")
    class ScheduleAtTime {

        @Test
        @DisplayName("scheduleAtTime() throws NullPointerException with null Executable")
        void withNullExecutable(@Mock Executor executor) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> scheduler.scheduleAtTime(null, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1516, -665, -1, 0})
        @DisplayName("scheduleAtTime() throws IllegalArgumentException with less than 1 time")
        void withNotCorrectTime(int time, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleAtTime(executable, time));
        }

        @Test
        @DisplayName("scheduleAtTime() throws ImpossibleSchedulingException if is killed")
        void killedScheduler(@Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);
            scheduler.start(); // Directly killed

            assertThrows(ImpossibleSchedulingException.class, () -> scheduler.scheduleAtTime(executable, Scheduler.NOW));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 1456, 4, 6546})
        @DisplayName("scheduleAtTime() does not throws exception with correct parameters")
        void withCorrectParameters(int time, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertDoesNotThrow(() -> scheduler.scheduleAtTime(executable, time));
        }
    }

    @Nested
    @DisplayName("SimpleScheduler scheduleExecutable()")
    @Tag("scheduleExecutable")
    class ScheduleExecutable {

        @Test
        @DisplayName("scheduleExecutable() throws NullPointerException with null Executable")
        void withNullExecutable(@Mock Executor executor) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> scheduler.scheduleExecutable(null, 1, Scheduler.ScheduleMode.ONCE, 1, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {-5445645, -6654, -1, 0})
        @DisplayName("scheduleExecutable() throws IllegalArgumentException with less than 1 waiting time")
        void withUnCorrectWaitingTime(int waitingTime, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThrows(IllegalArgumentException.class,
                         () -> scheduler.scheduleExecutable(executable, waitingTime, Scheduler.ScheduleMode.ONCE, 1, 1));
            assertThrows(IllegalArgumentException.class,
                         () -> scheduler.scheduleExecutable(executable, waitingTime, Scheduler.ScheduleMode.REPEATEDLY, 1,
                                                            1));
            assertThrows(IllegalArgumentException.class,
                         () -> scheduler.scheduleExecutable(executable, waitingTime, Scheduler.ScheduleMode.INFINITELY, 1, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {-5445645, -6654, -1, 0})
        @DisplayName("scheduleExecutable() throws IllegalArgumentException with less than 1 executionTimeStep for REPEATEDLY and INFINITELY mode")
        void withUnCorrectExecutionTimeStep(int executionTimeStep, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThrows(IllegalArgumentException.class,
                         () -> scheduler.scheduleExecutable(executable, 1, Scheduler.ScheduleMode.REPEATEDLY, 1, executionTimeStep));
            assertThrows(IllegalArgumentException.class,
                         () -> scheduler.scheduleExecutable(executable, 1, Scheduler.ScheduleMode.INFINITELY, 1, executionTimeStep));
        }

        @ParameterizedTest
        @ValueSource(ints = {-5445645, -6654, -1, 0})
        @DisplayName("scheduleExecutable() throws IllegalArgumentException with less than 1 nbRepetitions for REPEATEDLY mode")
        void withUnCorrectNbRepetitions(int nbRepetitions, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThrows(IllegalArgumentException.class,
                         () -> scheduler.scheduleExecutable(executable, 1, Scheduler.ScheduleMode.REPEATEDLY, nbRepetitions, 1));
        }

        @Test
        @DisplayName("scheduleExecutable() does not throw exception with correct parameters")
        void withCorrectParameters(@Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertDoesNotThrow(() -> scheduler.scheduleExecutable(executable, 1, Scheduler.ScheduleMode.ONCE, 1, 1));
            assertDoesNotThrow(() -> scheduler.scheduleExecutable(executable, 1, Scheduler.ScheduleMode.REPEATEDLY, 1, 1));
            assertDoesNotThrow(() -> scheduler.scheduleExecutable(executable, 1, Scheduler.ScheduleMode.INFINITELY, 1, 1));
        }

        @Test
        @DisplayName("scheduleExecutable() throws ImpossibleSchedulingException if is killed")
        void killedScheduler(@Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);
            scheduler.start(); // Directly killed

            assertThrows(ImpossibleSchedulingException.class, () -> scheduler.scheduleExecutable(executable, Scheduler.NOW,
                                                                                                 Scheduler.ScheduleMode.ONCE, 1, 1));
            assertThrows(ImpossibleSchedulingException.class, () -> scheduler.scheduleExecutable(executable, Scheduler.NOW,
                                                                                                 Scheduler.ScheduleMode.REPEATEDLY, 1, 1));
            assertThrows(ImpossibleSchedulingException.class, () -> scheduler.scheduleExecutable(executable, Scheduler.NOW,
                                                                                                 Scheduler.ScheduleMode.INFINITELY, 1, 1));
        }
    }

    @Nested
    @DisplayName("SimpleScheduler scheduleOnce()")
    @Tag("scheduleOnce")
    class ScheduleOnce {

        @Test
        @DisplayName("scheduleOnce() throws NullPointerException with null executable")
        void withNullExecutable(@Mock Executor executor) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> scheduler.scheduleOnce(null, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {-446, -1456465, -454, -1, 0})
        @DisplayName("scheduleOnce() throws IllegalArgumentException with less than 1 waiting time")
        void withUnCorrectWaitingTime(int waitingTime, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleOnce(executable, waitingTime));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 156, 564657613, 56797, 654, 655})
        @DisplayName("scheduleOnce() does not throw exception with correct parameters")
        void withCorrectParameters(int waitingTime, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertDoesNotThrow(() -> scheduler.scheduleOnce(executable, waitingTime));
        }

        @Test
        @DisplayName("scheduleOnce() throws ImpossibleSchedulingException if is killed")
        void killedScheduler(@Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);
            scheduler.start(); // Directly killed

            assertThrows(ImpossibleSchedulingException.class, () -> scheduler.scheduleOnce(executable, Scheduler.NOW));
        }
    }

    @Nested
    @DisplayName("SimpleScheduler scheduleRepeatedly()")
    @Tag("scheduleRepeatedly")
    class ScheduleRepeatedly {

        @Test
        @DisplayName("scheduleRepeatedly() throws NullPointerException with null executable")
        void withNullExecutable(@Mock Executor executor) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> scheduler.scheduleRepeatedly(null, 1, 1, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {-446, -1456465, -454, -1, 0})
        @DisplayName("scheduleRepeatedly() throws IllegalArgumentException with less than 1 waiting time")
        void withUnCorrectWaitingTime(int waitingTime, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleRepeatedly(executable, waitingTime, 1, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {-446, -1456465, -454, -1, 0})
        @DisplayName("scheduleRepeatedly() throws IllegalArgumentException with less than 1 nb repetitions")
        void withUnCorrectNbRepetitions(int nbRepetitions, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleRepeatedly(executable, 1, nbRepetitions, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {-446, -1456465, -454, -1, 0})
        @DisplayName("scheduleRepeatedly() throws IllegalArgumentException with less than 1 execution time step")
        void withUnCorrectExecutionTimeStep(int executionTimeStep, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleRepeatedly(executable, 1, 1, executionTimeStep));
        }

        @Test
        @DisplayName("scheduleRepeatedly() does not throw exception with correct parameters")
        void withCorrectParameters(@Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertDoesNotThrow(() -> scheduler.scheduleRepeatedly(executable, 1, 1, 1));
        }

        @Test
        @DisplayName("scheduleRepeatedly() throws ImpossibleSchedulingException if is killed")
        void killedScheduler(@Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);
            scheduler.start(); // Directly killed

            assertThrows(ImpossibleSchedulingException.class, () -> scheduler.scheduleRepeatedly(executable, Scheduler.NOW, 1, 1));
        }
    }

    @Nested
    @DisplayName("SimpleScheduler scheduleInfinitely()")
    @Tag("scheduleInfinitely")
    class ScheduleInfinitely {

        @Test
        @DisplayName("scheduleInfinitely() throws NullPointerException with null executable")
        void withNullExecutable(@Mock Executor executor) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> scheduler.scheduleInfinitely(null, 1, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {-446, -1456465, -454, -1, 0})
        @DisplayName("scheduleInfinitely() throws IllegalArgumentException with less than 1 waiting time")
        void withUnCorrectWaitingTime(int waitingTime, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleInfinitely(executable, waitingTime, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {-446, -1456465, -454, -1, 0})
        @DisplayName("scheduleInfinitely() throws IllegalArgumentException with less than 1 execution time step")
        void withUnCorrectExecutionTimeStep(int executionTimeStep, @Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleInfinitely(executable, 1, executionTimeStep));
        }

        @Test
        @DisplayName("scheduleInfinitely() does not throw exception with correct parameters")
        void withCorrectParameters(@Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            assertDoesNotThrow(() -> scheduler.scheduleInfinitely(executable, 1, 1));
        }

        @Test
        @DisplayName("scheduleInfinitely() throws ImpossibleSchedulingException if is killed")
        void killedScheduler(@Mock Executor executor, @Mock Executable executable) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);
            scheduler.start(); // Directly killed

            assertThrows(ImpossibleSchedulingException.class, () -> scheduler.scheduleInfinitely(executable, Scheduler.NOW, 1));
        }
    }

    @Nested
    @DisplayName("SimpleScheduler await()")
    @Tag("await")
    class Await {

        @Nested
        @DisplayName("await(Condition)")
        class MainAwait {

            @Test
            @DisplayName("await(Condition) throws NullPointerException with null Condition")
            void withNullCondition(@Mock Executor executor) {
                Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

                //noinspection ConstantConditions
                assertThrows(NullPointerException.class, () -> scheduler.await(null));
            }

            @Test
            @DisplayName("await(Condition) does not throw exception with non null Condition")
            void withNonNullCondition(@Mock Executor executor, @Mock Executor.ExecutorThread executorThread, @Mock Executor.Condition condition) {
                Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

                when(executor.getCurrentExecutorThread()).thenReturn(executorThread);

                assertDoesNotThrow(() -> scheduler.await(condition));

            }

            @Test
            @DisplayName("await(Condition) throws NotInExecutorContextException if the method is called out of Executor context")
            void outOfExecutorContext(@Mock Executor executor, @Mock Executor.Condition condition) {
                Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

                when(executor.getCurrentExecutorThread()).thenThrow(new NotInExecutorContextException());

                assertThrows(NotInExecutorContextException.class, () -> scheduler.await(condition));
            }

            @Test
            @DisplayName("await(Condition) throws ForcedWakeUpException if the wake up is not done by the Condition")
            void forcedWakeUp(@Mock Executor executor, @Mock Executor.ExecutorThread executorThread, @Mock Executor.Condition condition)
                    throws InterruptedException {
                Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

                when(executor.getCurrentExecutorThread()).thenReturn(executorThread);
                doThrow(new InterruptedException()).when(executorThread).await();

                assertThrows(ForcedWakeUpException.class, () -> scheduler.await(condition));
            }
        }

        @Nested
        @DisplayName("await(Condition, Long)")
        class SecondaryAwait {

            @Test
            @DisplayName("await(Condition, Long) throws NullPointerException with null Condition")
            void withNullCondition(@Mock Executor executor) {
                Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

                //noinspection ConstantConditions
                assertThrows(NullPointerException.class, () -> scheduler.await(null, 1L));
            }

            @ParameterizedTest
            @ValueSource(ints = {-165465, -1646, -1, 0})
            @DisplayName("await(Condition, Long) throws NullPointerException with null Condition")
            void withUnCorrectTimeout(int timeout, @Mock Executor executor) {
                Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

                //noinspection ConstantConditions
                assertThrows(NullPointerException.class, () -> scheduler.await(null, timeout));
            }

            @Test
            @DisplayName("await(Condition, Long) does not throw exception with correct parameters")
            void witCorrectParameters(@Mock Executor executor, @Mock Executor.ExecutorThread executorThread, @Mock Executor.Condition condition) {
                Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

                when(executor.getCurrentExecutorThread()).thenReturn(executorThread);

                assertDoesNotThrow(() -> scheduler.await(condition, 1L));

            }

            @Test
            @DisplayName("await(Condition, Long) throws NotInExecutorContextException if the method is called out of Executor context")
            void outOfExecutorContext(@Mock Executor executor, @Mock Executor.Condition condition) {
                Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

                when(executor.getCurrentExecutorThread()).thenThrow(new NotInExecutorContextException());

                assertThrows(NotInExecutorContextException.class, () -> scheduler.await(condition, 1L));
            }

            @Test
            @DisplayName("await(Condition, Long) throws ForcedWakeUpException if the wake up is not done by the Condition")
            void forcedWakeUp(@Mock Executor executor, @Mock Executor.ExecutorThread executorThread, @Mock Executor.Condition condition)
                    throws InterruptedException {
                Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

                when(executor.getCurrentExecutorThread()).thenReturn(executorThread);
                doThrow(new InterruptedException()).when(executorThread).await();

                assertThrows(ForcedWakeUpException.class, () -> scheduler.await(condition, 1L));
            }
        }
    }

    @Nested
    @DisplayName("SimpleScheduler generateCondition()")
    @Tag("generateCondition")
    class GenerateCondition {

        @Test
        @DisplayName("generateCondition() never returns null")
        void neverReturnsNull(@Mock Executor executor, @Mock Executor.Condition condition) {
            Scheduler scheduler = new SimpleScheduler(DEFAULT_MAX_DURATION, executor);

            when(executor.generateCondition()).thenReturn(condition);

            assertThat(scheduler.generateCondition()).isNotNull();
        }
    }
}
