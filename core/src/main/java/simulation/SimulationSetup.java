package simulation;

import event.Event;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Each {@link PalmBeachSimulation} must have a {@code SimulationSetup} which will set up the simulation and "start" be for example create the first
 * {@link Event}.
 * <p>
 * All {@code SimulationSetup} subclasses must have a default constructor.
 * </pre>
 */
public interface SimulationSetup {

    /**
     * Method call when all elements of the Simulation has been prepared. This method is the last method called before the start of the Simulation. It
     * is this method which initiates the beginning of the Simulation.
     */
    void setupSimulation();

    static SimulationSetup initiateSimulationSetup(Class<? extends SimulationSetup> simulationSetupClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends SimulationSetup> constructor = simulationSetupClass.getConstructor();
        return constructor.newInstance();
    }
}
