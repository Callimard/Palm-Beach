package junit;

import org.apache.log4j.PropertyConfigurator;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import simulation.PalmBeachSimulation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PalmBeachTestExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        configureLogger();
    }

    private void configureLogger() throws IOException {
        Properties prop = new Properties();
        String propFileName = "slf4j.properties";

        InputStream inputStream = PalmBeachSimulation.class.getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            prop.load(inputStream);
            PropertyConfigurator.configure(prop);
        }
    }
}
