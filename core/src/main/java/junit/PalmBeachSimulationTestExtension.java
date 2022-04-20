package junit;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import scheduler.Scheduler;
import scheduler.SimpleScheduler;
import scheduler.executor.multithread.MultiThreadExecutor;
import simulation.PalmBeachSimulation;
import simulation.SimulationSetup;

import static org.junit.jupiter.api.Assertions.fail;

public class PalmBeachSimulationTestExtension implements BeforeTestExecutionCallback {

    public static final long SIMULATION_MAX_DURATION = Long.MAX_VALUE;

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) {
        PalmBeachSimulation.clear();
        SimulationSetup simulationSetup = new BasicSimulationSetup();
        Scheduler scheduler = new SimpleScheduler(SIMULATION_MAX_DURATION, new MultiThreadExecutor(4));
        PalmBeachSimulation palmBeachSimulation = new PalmBeachSimulation(simulationSetup, scheduler, null, null, null);
        PalmBeachSimulation.setSingletonInstance(palmBeachSimulation);
    }

    public static void waitSimulationEnd() throws InterruptedException {
        int counter = 0;
        while (!PalmBeachSimulation.isEnded()) {
            PalmBeachSimulation.waitSimulationEnd(500L);
            counter++;
            if (counter > 5)
                fail("Too much wait End Simulation");
        }
    }

    // Inner classes.

    private static class BasicSimulationSetup implements SimulationSetup {

        @Override
        public void setupSimulation() {
            // Nothing.
        }
    }
}
