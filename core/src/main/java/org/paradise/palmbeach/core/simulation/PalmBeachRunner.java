package org.paradise.palmbeach.core.simulation;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.paradise.palmbeach.core.simulation.configuration.SimulationConfiguration;
import org.paradise.palmbeach.core.simulation.exception.RunSimulationErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class used to run any {@link PalmBeachSimulation}
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PalmBeachRunner {

    public static final long SIMULATION_WAIT_END_TIMEOUT = 500L;

    public static void launchSimulation(Class<?> mainClass, String[] args) throws RunSimulationErrorException {
        try {
            loadLoggerConfig(mainClass);
            displayArgs(args);
            Config mainConfig = getMainConfig(mainClass, args);
            log.debug("MainConfig {}", mainConfig);
            createAndStartSimulation(mainConfig);
            waitSimulationEnd();
            log.info("END MAIN THREAD");
        } catch (RunSimulationErrorException e) {
            log.error("Cannot run Palm Beach Simulation cause to an Error", e);
            throw e;
        }
    }

    private static void loadLoggerConfig(Class<?> mainClass) {
        Properties prop = new Properties();
        String propFileName = "slf4j.properties";

        InputStream inputStream = mainClass.getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            try {
                prop.load(inputStream);
                PropertyConfigurator.configure(prop);
            } catch (IOException e) {
                BasicConfigurator.configure();
            }
        } else {
            BasicConfigurator.configure();
        }
    }

    private static void displayArgs(String[] args) {
        for (String arg : args) {
            log.debug("Arg = {}", arg);
        }
    }

    private static Config getMainConfig(Class<?> mainClass, String[] args) {
        Config mainConfig;
        if (args.length >= 1) {
            String configName = args[0];
            mainConfig = ConfigFactory.load(mainClass.getClassLoader(), configName);
        } else
            mainConfig = ConfigFactory.load(mainClass.getClassLoader(), SimulationConfiguration.DEFAULT_SIMULATION_CONFIG_NAME);
        return mainConfig;
    }

    private static void createAndStartSimulation(Config mainConfig) throws RunSimulationErrorException {
        try {
            SimulationConfiguration simulationConfiguration = new SimulationConfiguration(mainConfig);
            PalmBeachSimulation palmBeachSimulation = simulationConfiguration.generate();
            log.info("Generate PalmBeachSimulation {}", palmBeachSimulation);
            PalmBeachSimulation.setSingletonInstance(palmBeachSimulation);
            PalmBeachSimulation.start();
        } catch (Exception e) {
            throw new RunSimulationErrorException(e);
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
