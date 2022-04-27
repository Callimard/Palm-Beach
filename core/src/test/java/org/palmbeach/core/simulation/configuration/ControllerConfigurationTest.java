package org.palmbeach.core.simulation.configuration;

import com.typesafe.config.Config;
import org.palmbeach.core.junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.palmbeach.core.scheduler.Scheduler;
import org.palmbeach.core.simulation.Controller;
import org.palmbeach.core.simulation.ControllerTest;
import org.palmbeach.core.simulation.configuration.exception.WrongControllerConfigurationException;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Nested
@DisplayName("ControllerConfiguration tests")
@Tag("ControllerConfiguration")
@PalmBeachTest
public class ControllerConfigurationTest {

    @Nested
    @DisplayName("ControllerConfiguration constructor()")
    @Tag("constructor")
    class Constructor {

        @Test
        @DisplayName("constructor() throws NullPointerException with null basic config")
        void withNullParameter() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> new ControllerConfiguration(null));
        }

        @Test
        @DisplayName("constructor() with correct configuration does not throw exception")
        void withEmptyName(@Mock Config config) {
            when(config.getEnum(Scheduler.ScheduleMode.class, ControllerConfiguration.SCHEDULE_MODE_PROPERTY)).thenReturn(
                    Scheduler.ScheduleMode.ONCE);
            when(config.getInt(ControllerConfiguration.SCHEDULE_TIME_PROPERTY)).thenReturn(1);

            assertDoesNotThrow(() -> new ControllerConfiguration(config));
        }

        @ParameterizedTest
        @ValueSource(ints = {-165465, -65465, -44, -1, 0})
        @DisplayName("constructor() with config in ONCE schedule mode with un correct value throws WrongControllerConfigurationException")
        void onceScheduleModeUnCorrectValue(int scheduleTime, @Mock Config config) {
            when(config.getEnum(Scheduler.ScheduleMode.class, ControllerConfiguration.SCHEDULE_MODE_PROPERTY)).thenReturn(
                    Scheduler.ScheduleMode.ONCE);
            when(config.getInt(ControllerConfiguration.SCHEDULE_TIME_PROPERTY)).thenReturn(scheduleTime);

            assertThrows(WrongControllerConfigurationException.class, () -> new ControllerConfiguration(config));
        }

        @ParameterizedTest
        @ValueSource(ints = {-165465, -65465, -44, -1, 0})
        @DisplayName("constructor() with config in INFINITELY schedule mode with wrong value throws WrongControllerConfigurationException")
        void infinitelyScheduleModeWrongExecutionStep(int executionsStep, @Mock Config config) {
            when(config.getEnum(Scheduler.ScheduleMode.class, ControllerConfiguration.SCHEDULE_MODE_PROPERTY)).thenReturn(
                    Scheduler.ScheduleMode.INFINITELY);
            when(config.getInt(ControllerConfiguration.SCHEDULE_TIME_PROPERTY)).thenReturn(1);
            when(config.getInt(ControllerConfiguration.EXECUTIONS_STEP_PROPERTY)).thenReturn(executionsStep);

            assertThrows(WrongControllerConfigurationException.class, () -> new ControllerConfiguration(config));
        }

        @ParameterizedTest
        @ValueSource(ints = {-165465, -65465, -44, -1, 0})
        @DisplayName("constructor() with config in REPEATEDLY schedule mode with wrong executionsStep throws WrongControllerConfigurationException")
        void repeatedlyScheduleModeWithWrongExecutionsStep(int executionsStep, @Mock Config config) {
            when(config.getEnum(Scheduler.ScheduleMode.class, ControllerConfiguration.SCHEDULE_MODE_PROPERTY)).thenReturn(
                    Scheduler.ScheduleMode.REPEATEDLY);
            when(config.getInt(ControllerConfiguration.SCHEDULE_TIME_PROPERTY)).thenReturn(1);
            when(config.getInt(ControllerConfiguration.EXECUTIONS_STEP_PROPERTY)).thenReturn(executionsStep);

            assertThrows(WrongControllerConfigurationException.class, () -> new ControllerConfiguration(config));
        }

        @ParameterizedTest
        @ValueSource(ints = {-165465, -65465, -44, -1, 0})
        @DisplayName("constructor() with config in REPEATEDLY schedule mode with wrong repetitions throws WrongControllerConfigurationException")
        void repeatedlyScheduleModeWithWrongRepetitions(int repetitions, @Mock Config config) {
            when(config.getEnum(Scheduler.ScheduleMode.class, ControllerConfiguration.SCHEDULE_MODE_PROPERTY)).thenReturn(
                    Scheduler.ScheduleMode.REPEATEDLY);
            when(config.getInt(ControllerConfiguration.SCHEDULE_TIME_PROPERTY)).thenReturn(1);
            when(config.getInt(ControllerConfiguration.EXECUTIONS_STEP_PROPERTY)).thenReturn(1);
            when(config.getInt(ControllerConfiguration.REPETITIONS_PROPERTY)).thenReturn(repetitions);

            assertThrows(WrongControllerConfigurationException.class, () -> new ControllerConfiguration(config));
        }
    }

    @Nested
    @DisplayName("ControllerConfiguration generate()")
    @Tag("generate")
    class Generate {

        @Test
        @DisplayName("generate() generate correct Controller for ONCE schedule mode")
        void onceScheduleMode(@Mock Config config) throws WrongControllerConfigurationException {
            int scheduleTime = 10;
            when(config.getString(ControllerConfiguration.CLASS_PROPERTY)).thenReturn(ControllerTest.BasicController.class.getName());
            when(config.getInt(ControllerConfiguration.SCHEDULE_TIME_PROPERTY)).thenReturn(scheduleTime);
            when(config.getEnum(Scheduler.ScheduleMode.class, ControllerConfiguration.SCHEDULE_MODE_PROPERTY)).thenReturn(
                    Scheduler.ScheduleMode.ONCE);
            ControllerConfiguration controllerConfiguration = new ControllerConfiguration(config);

            AtomicReference<Controller> controller = new AtomicReference<>();
            assertDoesNotThrow(() -> controller.set(controllerConfiguration.generate()));
            assertThat(controller.get()).isNotNull();
            assertThat(controller.get().getClass()).isEqualTo(ControllerTest.BasicController.class);
            assertThat(controller.get().getScheduleMode()).isEqualTo(Scheduler.ScheduleMode.ONCE);
            assertThat(controller.get().getScheduleTime()).isEqualTo(scheduleTime);
        }

        @Test
        @DisplayName("generate() generate correct Controller for REPEATEDLY schedule mode")
        void repeatedlyScheduleMode(@Mock Config config) throws WrongControllerConfigurationException {
            int scheduleTime = 10;
            int executionStep = 50;
            int repetitions = 75;
            when(config.getString(ControllerConfiguration.CLASS_PROPERTY)).thenReturn(ControllerTest.BasicController.class.getName());
            when(config.getInt(ControllerConfiguration.SCHEDULE_TIME_PROPERTY)).thenReturn(scheduleTime);
            when(config.getInt(ControllerConfiguration.EXECUTIONS_STEP_PROPERTY)).thenReturn(executionStep);
            when(config.getInt(ControllerConfiguration.REPETITIONS_PROPERTY)).thenReturn(repetitions);
            when(config.getEnum(Scheduler.ScheduleMode.class, ControllerConfiguration.SCHEDULE_MODE_PROPERTY)).thenReturn(
                    Scheduler.ScheduleMode.REPEATEDLY);
            ControllerConfiguration controllerConfiguration = new ControllerConfiguration(config);

            AtomicReference<Controller> controller = new AtomicReference<>();
            assertDoesNotThrow(() -> controller.set(controllerConfiguration.generate()));
            assertThat(controller.get()).isNotNull();
            assertThat(controller.get().getClass()).isEqualTo(ControllerTest.BasicController.class);
            assertThat(controller.get().getScheduleMode()).isEqualTo(Scheduler.ScheduleMode.REPEATEDLY);
            assertThat(controller.get().getScheduleTime()).isEqualTo(scheduleTime);
            assertThat(controller.get().getExecutionsStep()).isEqualTo(executionStep);
            assertThat(controller.get().getRepetitions()).isEqualTo(repetitions);
        }

        @Test
        @DisplayName("generate() generate correct Controller for INFINITELY schedule mode")
        void infinitelyScheduleMode(@Mock Config config) throws WrongControllerConfigurationException {
            int scheduleTime = 10;
            int executionStep = 50;
            when(config.getString(ControllerConfiguration.CLASS_PROPERTY)).thenReturn(ControllerTest.BasicController.class.getName());
            when(config.getInt(ControllerConfiguration.SCHEDULE_TIME_PROPERTY)).thenReturn(scheduleTime);
            when(config.getInt(ControllerConfiguration.EXECUTIONS_STEP_PROPERTY)).thenReturn(executionStep);
            when(config.getEnum(Scheduler.ScheduleMode.class, ControllerConfiguration.SCHEDULE_MODE_PROPERTY)).thenReturn(
                    Scheduler.ScheduleMode.INFINITELY);
            ControllerConfiguration controllerConfiguration = new ControllerConfiguration(config);

            AtomicReference<Controller> controller = new AtomicReference<>();
            assertDoesNotThrow(() -> controller.set(controllerConfiguration.generate()));
            assertThat(controller.get()).isNotNull();
            assertThat(controller.get().getClass()).isEqualTo(ControllerTest.BasicController.class);
            assertThat(controller.get().getScheduleMode()).isEqualTo(Scheduler.ScheduleMode.INFINITELY);
            assertThat(controller.get().getScheduleTime()).isEqualTo(scheduleTime);
            assertThat(controller.get().getExecutionsStep()).isEqualTo(executionStep);
        }
    }

    @Nested
    @DisplayName("ControllerConfiguration toString")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull(@Mock Config config) throws WrongControllerConfigurationException {
            int scheduleTime = 10;
            int executionStep = 50;
            int repetitions = 75;
            when(config.getString(ControllerConfiguration.CLASS_PROPERTY)).thenReturn(ControllerTest.BasicController.class.getName());
            when(config.getInt(ControllerConfiguration.SCHEDULE_TIME_PROPERTY)).thenReturn(scheduleTime);
            when(config.getInt(ControllerConfiguration.EXECUTIONS_STEP_PROPERTY)).thenReturn(executionStep);
            when(config.getInt(ControllerConfiguration.REPETITIONS_PROPERTY)).thenReturn(repetitions);
            when(config.getEnum(Scheduler.ScheduleMode.class, ControllerConfiguration.SCHEDULE_MODE_PROPERTY)).thenReturn(
                    Scheduler.ScheduleMode.REPEATEDLY);
            ControllerConfiguration controllerConfiguration = new ControllerConfiguration(config);

            assertThat(controllerConfiguration.toString()).isNotNull();
        }
    }
}
