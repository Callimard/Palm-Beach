package org.palmbeach.core.scheduler.executor;

import org.palmbeach.core.junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.palmbeach.core.scheduler.exception.NotPreparedConditionException;
import org.palmbeach.core.scheduler.executor.Executor;
import org.palmbeach.core.scheduler.executor.exception.AlreadyPreparedConditionException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("Executor tests")
@Tag("Executor")
@PalmBeachTest
public class ExecutorTest {

    @Nested
    @DisplayName("Executor.Condition tests")
    @Tag("Executor.Condition")
    class ConditionTest {

        @Nested
        @DisplayName("Condition prepare()")
        @Tag("prepare")
        class Prepare {

            @Test
            @DisplayName("prepare() throws AlreadyPreparedConditionException if it has been already prepared")
            void alreadyPrepared(@Mock Executor.ExecutorThread executorThread) {
                Executor.Condition condition = new Executor.Condition();
                condition.prepare(executorThread);

                assertThrows(AlreadyPreparedConditionException.class, () -> condition.prepare(executorThread));
            }

            @Test
            @DisplayName("prepare() does not throws exception if it has not been already prepared")
            void notAlreadyPrepared(@Mock Executor.ExecutorThread executorThread) {
                Executor.Condition condition = new Executor.Condition();
                assertDoesNotThrow(() -> condition.prepare(executorThread));
            }
        }

        @Nested
        @DisplayName("Condition wakeup()")
        @Tag("wakeup")
        class Wakeup {

            @Test
            @DisplayName("wakeup() throws NotPreparedConditionException if not prepared")
            void notPrepared() {
                Executor.Condition condition = new Executor.Condition();

                assertThrows(NotPreparedConditionException.class, condition::wakeup);
            }

            @Test
            @DisplayName("wakeup() does not throw exception if prepared")
            void prepared(@Mock Executor.ExecutorThread executorThread) {
                Executor.Condition condition = new Executor.Condition();
                condition.prepare(executorThread);

                assertDoesNotThrow(condition::wakeup);
            }
        }
    }
}
