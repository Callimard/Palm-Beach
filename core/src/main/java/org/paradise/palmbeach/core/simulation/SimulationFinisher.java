package org.paradise.palmbeach.core.simulation;

import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A {@link PalmBeachSimulation} can have a {@code SimulationFinisher} which is called when the simulation has finish. This interface allow the user
 * to do some treatment before the end of the execution.
 * <p>
 * All {@code SimulationFinisher} subclasses must have a default constructor.
 */
public interface SimulationFinisher {

    /**
     * Call directly after the end of the {@link PalmBeachSimulation}
     */
    void finishSimulation();

    /**
     * Create an instance of the specified {@link SimulationFinisher} class. The specified class must have a construct as described in the general doc
     * of {@code SimulationFinisher}.
     *
     * @param simulationFinisherClass the SimulationSetup class
     *
     * @return a new instance of the specified {@code SimulationFinisher} class.
     *
     * @throws NoSuchMethodException     if the {@code SimulationFinisher} class does not have the specific needed constructor
     * @throws InvocationTargetException if the constructor has thrown an exception
     * @throws InstantiationException    if the instantiation failed
     * @throws IllegalAccessException    if the construct is not accessible
     * @throws NullPointerException      if the simulationFinisherClass is null
     */
    static SimulationFinisher initiateSimulationFinisher(@NonNull Class<? extends SimulationFinisher> simulationFinisherClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends SimulationFinisher> constructor = simulationFinisherClass.getConstructor();
        return constructor.newInstance();
    }
}
