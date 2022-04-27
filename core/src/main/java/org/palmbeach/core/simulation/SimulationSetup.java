package org.palmbeach.core.simulation;

import org.palmbeach.core.event.Event;
import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Each {@link PalmBeachSimulation} must have a {@code SimulationSetup} which will set up the simulation and "start" be for example create the first
 * {@link Event}.
 * <p>
 * All {@code SimulationSetup} subclasses must have a default constructor.
 */
public interface SimulationSetup {

    /**
     * Method call when all elements of the Simulation has been prepared. This method is the last method called before the start of the Simulation. It
     * is this method which initiates the beginning of the Simulation.
     */
    void setupSimulation();

    /**
     * Create an instance of the specified {@link SimulationSetup} class. The specified class must have a construct as described in the general doc of
     * * {@code SimulationSetup}.
     *
     * @param simulationSetupClass the SimulationSetup class
     *
     * @return a new instance of the specified {@code SimulationSetup} class.
     *
     * @throws NoSuchMethodException     if the {@code SimulationSetup} class does not have the specific needed constructor
     * @throws InvocationTargetException if the constructor has thrown an exception
     * @throws InstantiationException    if the instantiation failed
     * @throws IllegalAccessException    if the construct is not accessible
     * @throws NullPointerException      if the simulationSetupClass is null
     */
    static SimulationSetup initiateSimulationSetup(@NonNull Class<? extends SimulationSetup> simulationSetupClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends SimulationSetup> constructor = simulationSetupClass.getConstructor();
        return constructor.newInstance();
    }
}
