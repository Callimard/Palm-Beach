package simulation.configuration;

import com.typesafe.config.Config;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import scheduler.Scheduler;
import simulation.Controller;
import simulation.configuration.exception.GenerationFailedException;

import static common.Tools.extractClass;

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

    public ControllerConfiguration(@NonNull Config baseConfig) {
        super(baseConfig);
        this.controllerClass = getBaseConfig().getString(CLASS_PROPERTY);
        this.scheduleMode = getBaseConfig().getEnum(Scheduler.ScheduleMode.class, SCHEDULE_MODE_PROPERTY);
        this.scheduleTime = getBaseConfig().getInt(SCHEDULE_TIME_PROPERTY);
        if (!this.scheduleMode.equals(Scheduler.ScheduleMode.ONCE)) {
            this.executionsStep = getBaseConfig().getInt(EXECUTIONS_STEP_PROPERTY);
            if (this.scheduleMode.equals(Scheduler.ScheduleMode.REPEATEDLY))
                this.repetitions = getBaseConfig().getInt(REPETITIONS_PROPERTY);
            else
                this.repetitions = -1;

        } else {
            this.executionsStep = -1;
            this.repetitions = -1;
        }
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
