package org.paradise.palmbeach.core.simulation.configuration;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import org.paradise.palmbeach.core.environment.Environment;
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
@DisplayName("EnvironmentConfiguration tests")
@Tag("EnvironmentConfiguration")
@PalmBeachTest
public class EnvironmentConfigurationTest {

    @Nested
    @DisplayName("EnvironmentConfiguration constructor()")
    @Tag("constructor")
    class Constructor {

        @SuppressWarnings("ConstantConditions")
        @Test
        @DisplayName("constructor() throws NullPointerException with null basic config")
        void withNullParameter(@Mock Config config) {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> new EnvironmentConfiguration(null, config));
            assertThrows(NullPointerException.class, () -> new EnvironmentConfiguration(null, null));
            assertThrows(NullPointerException.class, () -> new EnvironmentConfiguration("environmentName", null));
        }

        @Test
        @DisplayName("constructor() with empty configuration does not throw exception")
        void withEmptyName(@Mock Config config) {
            assertDoesNotThrow(() -> new EnvironmentConfiguration("environmentName", config));
        }
    }

    @Nested
    @DisplayName("EnvironmentConfiguration generate()")
    @Tag("generate")
    class Generate {

        @Test
        @DisplayName("generate() returns correct Environment")
        void generateCorrectEnvironment(@Mock Config config) {
            String envName = "envName";
            when(config.getString(EnvironmentConfiguration.CLASS_PROPERTY)).thenReturn(Environment.class.getName());
            when(config.hasPath(EnvironmentConfiguration.CONTEXT_PROPERTY)).thenReturn(false);
            when(config.hasPath(EnvironmentConfiguration.PHYSICAL_NETWORKS_PROPERTY)).thenReturn(true);
            when(config.getStringList(EnvironmentConfiguration.PHYSICAL_NETWORKS_PROPERTY)).thenReturn(Lists.newArrayList("pN1", "pN2"));
            EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration(envName, config);

            AtomicReference<Environment> environment = new AtomicReference<>();
            assertDoesNotThrow(() -> environment.set(environmentConfiguration.generate()));
            assertThat(environment.get()).isNotNull();
            assertThat(environment.get().getName()).isEqualTo(envName);
        }
    }

    @Nested
    @DisplayName("EnvironmentConfiguration toString()")
    @Tag("toString")
    class toString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull(@Mock Config config) {
            String envName = "envName";
            when(config.getString(EnvironmentConfiguration.CLASS_PROPERTY)).thenReturn(Environment.class.getName());
            when(config.hasPath(EnvironmentConfiguration.CONTEXT_PROPERTY)).thenReturn(false);
            when(config.hasPath(EnvironmentConfiguration.PHYSICAL_NETWORKS_PROPERTY)).thenReturn(true);
            when(config.getStringList(EnvironmentConfiguration.PHYSICAL_NETWORKS_PROPERTY)).thenReturn(Lists.newArrayList("pN1", "pN2"));
            EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration(envName, config);

            assertThat(environmentConfiguration.toString()).isNotNull();
        }
    }

}
