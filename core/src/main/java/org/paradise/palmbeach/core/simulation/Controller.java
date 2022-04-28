package org.paradise.palmbeach.core.simulation;

import lombok.*;
import org.paradise.palmbeach.core.scheduler.Scheduler;
import org.paradise.palmbeach.core.scheduler.executor.Executable;

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
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public abstract class Controller implements Executable {

    // Variables.

    @NonNull
    private final Scheduler.ScheduleMode scheduleMode;
    private final int scheduleTime;
    private final int executionsStep;
    private final int repetitions;

    // Methods.

    /**
     * Create an instance of the specified {@link Controller} class. The specified class must have a construct as described in the general doc of
     * {@code Controller}.
     *
     * @param controllerClass the Controller class
     * @param scheduleMode    the Controller schedule mode
     * @param scheduleTime    the Controller time when it must be executed
     * @param executionsStep  the Controller execution time step (if other than ONCE schedule mode Controller)
     * @param repetitions     the Controller number of repetitions (if REPEATEDLY schedule mode Controller)
     *
     * @return a new instance of the specified {@code Controller} class.
     *
     * @throws NoSuchMethodException     if the {@code Controller} class does not have the specific needed constructor
     * @throws InvocationTargetException if the constructor has thrown an exception
     * @throws InstantiationException    if the instantiation failed
     * @throws IllegalAccessException    if the construct is not accessible
     * @throws NullPointerException      if controllerClass or schedulerMode is null
     */
    public static Controller instantiateController(@NonNull Class<? extends Controller> controllerClass, @NonNull Scheduler.ScheduleMode scheduleMode,
                                                   int scheduleTime, int executionsStep, int repetitions)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Controller> constructor = controllerClass.getConstructor(Scheduler.ScheduleMode.class, int.class, int.class, int.class);
        return constructor.newInstance(scheduleMode, scheduleTime, executionsStep, repetitions);
    }

}
