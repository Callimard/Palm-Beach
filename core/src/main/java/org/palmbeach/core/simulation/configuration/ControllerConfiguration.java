package org.palmbeach.core.simulation.configuration;

import com.typesafe.config.Config;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.palmbeach.core.scheduler.Scheduler;
import org.palmbeach.core.simulation.configuration.exception.GenerationFailedException;
import org.palmbeach.core.simulation.configuration.exception.WrongControllerConfigurationException;
import org.palmbeach.core.simulation.Controller;

import static org.palmbeach.core.common.Tools.extractClass;

/**
 * Configuration for {@link Controller}.
 * <p>
 * Example of complete {@code Controller} configuration:
 * <pre>
 * controller.myController.class=simulation.MyController
 * controller.myController.schedule-mode=REPEATEDLY
 * controller.myController.schedule-time=1
 * controller.myController.executions-step=50
 * controller.myController.repetitions=5
 * controller.myController.custom-property=custom-value
 * </pre>
 */
@Getter
@ToString
public class ControllerConfiguration extends PalmBeachConfiguration<Controller> {

    // Constants.

    public static final String CLASS_PROPERTY = "class";
    public static final String SCHEDULE_MODE_PROPERTY = "schedule-mode";
    public static final String SCHEDULE_TIME_PROPERTY = "schedule-time";
    public static final String EXECUTIONS_STEP_PROPERTY = "executions-step";
    public static final String REPETITIONS_PROPERTY = "repetitions";

    // Variables.

    private final String controllerClass;

    private final Scheduler.ScheduleMode scheduleMode;

    private final int scheduleTime;

    private final int executionsStep;

    private final int repetitions;

    // Constructors

    public ControllerConfiguration(@NonNull Config baseConfig) throws WrongControllerConfigurationException {
        super(baseConfig);
        this.controllerClass = getBaseConfig().getString(CLASS_PROPERTY);
        this.scheduleMode = getBaseConfig().getEnum(Scheduler.ScheduleMode.class, SCHEDULE_MODE_PROPERTY);
        this.scheduleTime = getBaseConfig().getInt(SCHEDULE_TIME_PROPERTY);
        verifyTimeNumber(this.scheduleTime, "Schedule time cannot be less than 1");

        if (!this.scheduleMode.equals(Scheduler.ScheduleMode.ONCE)) {
            this.executionsStep = getBaseConfig().getInt(EXECUTIONS_STEP_PROPERTY);
            verifyTimeNumber(this.executionsStep, "ExecutionStep cannot be less than 1 in REPEATEDLY or INFINITELY schedule mode");

            if (this.scheduleMode.equals(Scheduler.ScheduleMode.REPEATEDLY)) {
                this.repetitions = getBaseConfig().getInt(REPETITIONS_PROPERTY);
                verifyTimeNumber(this.repetitions, "Repetitions cannot be less than 1 in REPEATEDLY schedule mode");
            } else
                this.repetitions = -1;
        } else {
            this.executionsStep = -1;
            this.repetitions = -1;
        }
    }

    private void verifyTimeNumber(int repetitions, String s) throws WrongControllerConfigurationException {
        if (repetitions < 1)
            throw new WrongControllerConfigurationException(s);
    }

    // Methods

    @Override
    public Controller generate() throws GenerationFailedException {
        try {
            return Controller.instantiateController(extractClass(controllerClass), scheduleMode, scheduleTime, executionsStep, repetitions);
        } catch (Exception e) {
            throw new GenerationFailedException("Cannot generate Controller from the configuration " + this, e);
        }
    }
}
