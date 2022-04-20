package simulation;

import junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("SimulationSetup tests")
@Tag("SimulationSetup")
@PalmBeachTest
public class SimulationSetupTest {

    @Nested
    @DisplayName("SimulationSetup initiateSimulationSetup")
    @Tag("initiateSimulationSetup")
    class InitiateSimulationSetup {

        @Test
        @DisplayName("initiateSimulationSetup() throws NullPointerException with null simulationSetupClass")
        void withNullParameter() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> SimulationSetup.initiateSimulationSetup(null));
        }

        @Test
        @DisplayName("initiateSimulationSetup() does not throw exception and create a correct SimulationSetup")
        void withCorrectParameter() {
            AtomicReference<SimulationSetup> simulationSetup = new AtomicReference<>();

            assertDoesNotThrow(() -> simulationSetup.set(SimulationSetup.initiateSimulationSetup(BasicSimulationSetup.class)));
            assertThat(simulationSetup.get()).isNotNull();
            assertThat(simulationSetup.get().getClass()).isEqualTo(BasicSimulationSetup.class);
        }
    }

    // Inner classes.

    public static class BasicSimulationSetup implements SimulationSetup {

        @Override
        public void setupSimulation() {
            // Nothing
        }
    }

}
