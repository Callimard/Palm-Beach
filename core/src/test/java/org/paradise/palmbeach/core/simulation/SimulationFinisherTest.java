package org.paradise.palmbeach.core.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.paradise.palmbeach.core.junit.PalmBeachTest;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("SimulationFinisher tests")
@Tag("SimulationFinisher")
@PalmBeachTest
public class SimulationFinisherTest {

    @Nested
    @DisplayName("SimulationFinisher initiateSimulationFinisher")
    @Tag("initiateSimulationFinisher")
    class InitiateSimulationFinisher {

        @Test
        @DisplayName("initiateSimulationFinisher() throws NullPointerException with null simulationFinisherClass")
        void withNullParameter() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> SimulationFinisher.initiateSimulationFinisher(null));
        }

        @Test
        @DisplayName("initiateSimulationFinisher() does not throw exception and create a correct SimulationFinisher")
        void withCorrectParameter() {
            AtomicReference<SimulationFinisher> simulationFinisher = new AtomicReference<>();

            assertDoesNotThrow(() -> simulationFinisher.set(SimulationFinisher.initiateSimulationFinisher(BasicSimulationFinisher.class)));
            assertThat(simulationFinisher.get()).isNotNull();
            assertThat(simulationFinisher.get().getClass()).isEqualTo(SimulationFinisherTest.BasicSimulationFinisher.class);
        }
    }

    // Inner classes.

    public static class BasicSimulationFinisher implements SimulationFinisher {

        @Override
        public void finishSimulation() {
            // Nothing
        }
    }
}
