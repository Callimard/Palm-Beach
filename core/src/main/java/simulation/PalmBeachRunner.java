package simulation;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import simulation.configuration.SimulationConfiguration;

import static simulation.configuration.SimulationConfiguration.DEFAULT_SIMULATION_CONFIG_NAME;

/**
 * Class used to run any {@link PalmBeachSimulation}
 */
@Slf4j
public class PalmBeachRunner {

    public static final long SIMULATION_WAIT_END_TIMEOUT = 500L;

    public static void main(String[] args) {
        Config mainConfig = getMainConfig(args);
        createAndStartSimulation(mainConfig);
        waitSimulationEnd();
    }

    private static Config getMainConfig(String[] args) {
        Config mainConfig;
        if (args.length >= 1) {
            String configName = args[0];
            mainConfig = ConfigFactory.load(configName);
        } else
            mainConfig = ConfigFactory.load(DEFAULT_SIMULATION_CONFIG_NAME);
        return mainConfig;
    }

    private static void createAndStartSimulation(Config mainConfig) {
        try {
            SimulationConfiguration simulationConfiguration = new SimulationConfiguration(mainConfig);
            PalmBeachSimulation palmBeachSimulation = simulationConfiguration.generate();
            log.info("Generate PalmBeachSimulation {}", palmBeachSimulation);
            PalmBeachSimulation.setSingletonInstance(palmBeachSimulation);
            PalmBeachSimulation.start();
        } catch (Exception e) {
            log.error("Cannot run Palm Beach Simulation cause to an Error", e);
        }
    }

    private static void waitSimulationEnd() {
        try {
            while (!PalmBeachSimulation.isEnded())
                PalmBeachSimulation.waitSimulationEnd(SIMULATION_WAIT_END_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted during waiting the simulation end", e);
        }
    }

}