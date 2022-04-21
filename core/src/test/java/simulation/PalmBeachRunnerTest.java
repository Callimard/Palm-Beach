package simulation;

import junit.PalmBeachTest;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import simulation.exception.RunSimulationErrorException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("PalmBeachRunner tests")
@Tag("PalmBeachRunner")
@PalmBeachTest
public class PalmBeachRunnerTest {

    @BeforeEach
    void setup() {
        PalmBeachSimulation.clear();
    }

    @Nested
    @DisplayName("PalmBeachRunner main()")
    @Tag("main")
    class Main {

        @Test
        @DisplayName("main() throws RunSimulationErrorException if PalmBeachSimulation singleton already set")
        void alreadySetSingleton(@Mock PalmBeachSimulation palmBeachSimulation) {
            PalmBeachSimulation.setSingletonInstance(palmBeachSimulation);
            assertThrows(RunSimulationErrorException.class, () -> PalmBeachRunner.main(new String[0]));
        }

        @Test
        @DisplayName("main() does not throws exception with empty args and use default config file name")
        void withEmptyArgs() {
            assertDoesNotThrow(() -> PalmBeachRunner.main(new String[0]));
        }

        @Test
        @DisplayName("main() does not throws exception with empty args and use specified arg file")
        void withArgs() {
            assertDoesNotThrow(() -> PalmBeachRunner.main(new String[]{"configuration"}));
        }
    }
}
