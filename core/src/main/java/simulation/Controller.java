package simulation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import scheduler.Scheduler;
import scheduler.executor.Executable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A {@code Controller} is a specific {@link Executable} which is the most time define in configuration file to be executed one or several time in the
 * Simulation. It is a good way to simulate event apparition during the Simulation.
 * <p>
 * Implementation specificity: {@code Controller} subclasses must have a constructor like this:
 * <pre>
 *     Controller(Scheduler.ScheduleMode, int scheduleTime, int executionStep, int repetitions) {
 *         ...
 *     }
 * </pre>
 */
@Getter
@AllArgsConstructor
@ToString
public abstract class Controller implements Executable {

    // Variables.

    @NonNull
    private final Scheduler.ScheduleMode scheduleMode;
    private final int scheduleTime;
    private final int executionsStep;
    private final int repetitions;

    // Methods.

    public static Controller instantiateController(Class<? extends Controller> controllerClass, Scheduler.ScheduleMode scheduleMode, int scheduleTime,
                                                   int executionsStep, int repetitions)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Controller> constructor = controllerClass.getConstructor(Scheduler.ScheduleMode.class, Integer.class, Integer.class,
                                                                                       Integer.class);
        return constructor.newInstance(scheduleMode, scheduleTime, executionsStep, repetitions);
    }

}
