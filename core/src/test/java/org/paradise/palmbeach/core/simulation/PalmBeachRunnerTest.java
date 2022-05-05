package org.paradise.palmbeach.core.simulation;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.paradise.palmbeach.core.junit.PalmBeachTest;
import org.paradise.palmbeach.core.simulation.exception.RunSimulationErrorException;

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
    @DisplayName("PalmbeachRunner main()")
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

    @Nested
    @DisplayName("PalmBeachRunner launchSimulation()")
    @Tag("launchSimulation")
    class launchSimulation {

        @Nested
        @DisplayName("launchSimulation(ClassLoader, args)")
        class LaunchSimulationFirst {
            @Test
            @DisplayName("launchSimulation(ClassLoader, args) throws RunSimulationErrorException if PalmBeachSimulation singleton already set")
            void alreadySetSingleton(@Mock PalmBeachSimulation palmBeachSimulation) {
                PalmBeachSimulation.setSingletonInstance(palmBeachSimulation);
                assertThrows(RunSimulationErrorException.class, () -> PalmBeachRunner.launchSimulation(PalmBeachRunner.class, new String[0]));
            }

            @Test
            @DisplayName("launchSimulation(ClassLoader, args) does not throws exception with empty args and use default config file name")
            void withEmptyArgs() {
                assertDoesNotThrow(() -> PalmBeachRunner.launchSimulation(PalmBeachRunner.class, new String[0]));
            }

            @Test
            @DisplayName("launchSimulation(ClassLoader, args) does not throws exception with empty args and use specified arg file")
            void withArgs() {
                assertDoesNotThrow(() -> PalmBeachRunner.launchSimulation(PalmBeachRunner.class, new String[]{"configuration"}));
            }
        }

        @Nested
        @DisplayName("launchSimulation(args)")
        class LaunchSimulationSecond {
            @Test
            @DisplayName("launchSimulation(args) throws RunSimulationErrorException if PalmBeachSimulation singleton already set")
            void alreadySetSingleton(@Mock PalmBeachSimulation palmBeachSimulation) {
                PalmBeachSimulation.setSingletonInstance(palmBeachSimulation);
                assertThrows(RunSimulationErrorException.class, () -> PalmBeachRunner.launchSimulation(new String[0]));
            }

            @Test
            @DisplayName("launchSimulation(args) does not throws exception with empty args and use default config file name")
            void withEmptyArgs() {
                assertDoesNotThrow(() -> PalmBeachRunner.launchSimulation(new String[0]));
            }

            @Test
            @DisplayName("launchSimulation(args) does not throws exception with empty args and use specified arg file")
            void withArgs() {
                assertDoesNotThrow(() -> PalmBeachRunner.launchSimulation(new String[]{"configuration"}));
            }
        }
    }
}
