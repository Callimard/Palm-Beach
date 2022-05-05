package org.paradise.palmbeach.core.junit;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.paradise.palmbeach.core.scheduler.Scheduler;
import org.paradise.palmbeach.core.scheduler.SimpleScheduler;
import org.paradise.palmbeach.core.scheduler.executor.multithread.MultiThreadExecutor;
import org.paradise.palmbeach.core.simulation.PalmBeachSimulation;

import static org.junit.jupiter.api.Assertions.fail;

public class PalmBeachSimulationTestExtension implements BeforeTestExecutionCallback {

    public static final long SIMULATION_MAX_DURATION = Long.MAX_VALUE;

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) {
        PalmBeachSimulation.clear();
        Scheduler scheduler = new SimpleScheduler(SIMULATION_MAX_DURATION, new MultiThreadExecutor(4));
        PalmBeachSimulation palmBeachSimulation = new PalmBeachSimulation(scheduler, null, null, null, null, null);
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
}
