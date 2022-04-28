package org.paradise.palmbeach.core.simulation.configuration;

import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.agent.behavior.Behavior;
import org.paradise.palmbeach.core.agent.behavior.BehaviorTest;
import com.typesafe.config.Config;
import org.paradise.palmbeach.core.junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Nested
@DisplayName("BehaviorConfiguration tests")
@Tag("BehaviorConfiguration")
@PalmBeachTest
public class BehaviorConfigurationTest {

    @Nested
    @DisplayName("BehaviorConfiguration constructor()")
    @Tag("constructor")
    class Constructor {

        @Test
        @DisplayName("constructor() throws NullPointerException with null basic config")
        void withNullParameter() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> new BehaviorConfiguration(null));
        }

        @Test
        @DisplayName("constructor() with empty configuration does not throw exception")
        void withEmptyName(@Mock Config config) {
            assertDoesNotThrow(() -> new BehaviorConfiguration(config));
        }
    }

    @Nested
    @DisplayName("BehaviorConfiguration generate()")
    @Tag("generate")
    class Generate {

        @Test
        @DisplayName("generate() throws UnsupportedOperationException")
        void unsupported(@Mock Config config) {
            BehaviorConfiguration behaviorConfiguration = new BehaviorConfiguration(config);
            assertThrows(UnsupportedOperationException.class, behaviorConfiguration::generate);
        }
    }

    @Nested
    @DisplayName("BehaviorConfiguration generateBehavior()")
    @Tag("generateBehavior")
    class GenerateBehavior {

        @Test
        @DisplayName("GenerateBehavior() does not throws exception with correct config")
        void unsupported(@Mock Config config, @Mock SimpleAgent agent) {
            when(config.getString(BehaviorConfiguration.CLASS_PROPERTY)).thenReturn(BehaviorTest.BasicBehavior.class.getName());

            BehaviorConfiguration behaviorConfiguration = new BehaviorConfiguration(config);

            AtomicReference<Behavior> behavior = new AtomicReference<>();
            assertDoesNotThrow(() -> behavior.set(behaviorConfiguration.generateBehavior(agent)));
            assertThat(behavior.get()).isNotNull();
            assertThat(behavior.get().getClass()).isEqualTo(BehaviorTest.BasicBehavior.class);
        }
    }

    @Nested
    @DisplayName("BehaviorConfiguration toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull(@Mock Config config) {
            when(config.getString(BehaviorConfiguration.CLASS_PROPERTY)).thenReturn(BehaviorTest.BasicBehavior.class.getName());
            BehaviorConfiguration behaviorConfiguration = new BehaviorConfiguration(config);

            assertThat(behaviorConfiguration.toString()).isNotNull();
        }
    }
}
