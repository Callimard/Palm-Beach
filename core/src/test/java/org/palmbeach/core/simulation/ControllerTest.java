package org.palmbeach.core.simulation;

import org.palmbeach.core.junit.PalmBeachTest;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.palmbeach.core.scheduler.Scheduler;
import org.palmbeach.core.simulation.Controller;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("Controller tests")
@Tag("Controller")
@PalmBeachTest
public class ControllerTest {

    @Nested
    @DisplayName("Controller instantiateController()")
    @Tag("instantiateController")
    class InstantiateController {

        @SuppressWarnings("ConstantConditions")
        @Test
        @DisplayName("instantiateController() throws NullPointerException with null controllerClass or null schedulerMode")
        void withNullParameter() {
            assertThrows(NullPointerException.class, () -> Controller.instantiateController(null, Scheduler.ScheduleMode.ONCE, 1, 1, 1));
            assertThrows(NullPointerException.class, () -> Controller.instantiateController(BasicController.class, null, 1, 1, 1));
            assertThrows(NullPointerException.class, () -> Controller.instantiateController(null, null, 1, 1, 1));
            assertDoesNotThrow(() -> Controller.instantiateController(BasicController.class, Scheduler.ScheduleMode.ONCE, 1, 1, 1));
        }

        @Test
        @DisplayName("instantiateController() does not throw exception and create a correct Controller")
        void withCorrectParameter() {
            AtomicReference<Controller> controller = new AtomicReference<>();

            assertDoesNotThrow(() -> controller.set(Controller.instantiateController(BasicController.class, Scheduler.ScheduleMode.ONCE, 1, 1, 1)));
            assertThat(controller.get()).isNotNull();
            assertThat(controller.get().getClass()).isEqualTo(BasicController.class);
            assertThat(controller.get().getScheduleMode()).isEqualTo(Scheduler.ScheduleMode.ONCE);
            assertThat(controller.get().getScheduleTime()).isEqualByComparingTo(1);
            assertThat(controller.get().getExecutionsStep()).isEqualByComparingTo(1);
            assertThat(controller.get().getRepetitions()).isEqualByComparingTo(1);
        }
    }

    @Nested
    @DisplayName("Controller toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull() {
            BasicController controller = new BasicController(Scheduler.ScheduleMode.ONCE, 1, 1, 1);

            assertThat(controller.toString()).isNotNull();
        }
    }

    // Inner classes.

    public static class BasicController extends Controller {

        public BasicController(Scheduler.@NonNull ScheduleMode scheduleMode, int scheduleTime, int executionsStep, int repetitions) {
            super(scheduleMode, scheduleTime, executionsStep, repetitions);
        }

        @Override
        public void execute() {
            // Nothing
        }
    }
}
