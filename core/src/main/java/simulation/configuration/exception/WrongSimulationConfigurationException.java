package simulation.configuration.exception;

public class WrongSimulationConfigurationException extends Exception {
    public WrongSimulationConfigurationException(String message) {
        super(message);
    }

    public WrongSimulationConfigurationException(String s, Exception e) {
        super(s, e);
    }
}
