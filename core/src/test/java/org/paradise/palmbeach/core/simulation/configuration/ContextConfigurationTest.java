package org.paradise.palmbeach.core.simulation.configuration;

import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import org.paradise.palmbeach.utils.context.Context;
import org.paradise.palmbeach.utils.context.SimpleContext;
import org.paradise.palmbeach.core.junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Nested
@DisplayName("ContextConfiguration tests")
@Tag("ContextConfiguration")
@PalmBeachTest
public class ContextConfigurationTest {

    @Nested
    @DisplayName("ContextConfiguration constructor()")
    @Tag("constructor")
    class Constructor {

        @Test
        @DisplayName("constructor() throws NullPointerException with null basic config")
        void withNullParameter() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> new ContextConfiguration(null));
        }

        @Test
        @DisplayName("constructor() with empty configuration does not throw exception")
        void withEmptyName(@Mock Config config) {
            assertDoesNotThrow(() -> new ContextConfiguration(config));
        }
    }

    @Nested
    @DisplayName("ContextConfiguration generate")
    @Tag("generate")
    class Generate {

        @Test
        @DisplayName("generate() generate correct Context")
        void withCorrectConfig(@Mock Config config, @Mock ConfigValue value1) {
            String key1 = "key1";
            String v1 = "value1";

            Map<String, ConfigValue> keyValue = Maps.asMap(Sets.newSet(key1), (k) -> value1);

            when(config.entrySet()).thenReturn(keyValue.entrySet());
            when(value1.unwrapped()).thenReturn(v1);

            ContextConfiguration contextConfiguration = new ContextConfiguration(config);

            AtomicReference<Context> context = new AtomicReference<>();
            assertDoesNotThrow(() -> context.set(contextConfiguration.generate()));

            assertThat(context.get()).isNotNull();
            assertThat(context.get().getClass()).isEqualTo(SimpleContext.class);
            assertThat(context.get().getValue(key1)).isEqualTo(v1);
        }
    }

    @Nested
    @DisplayName("ContextConfiguration toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull(@Mock Config config, @Mock ConfigValue value1) {
            String key1 = "key1";
            String v1 = "value1";

            Map<String, ConfigValue> keyValue = Maps.asMap(Sets.newSet(key1), (k) -> value1);

            when(config.entrySet()).thenReturn(keyValue.entrySet());
            when(value1.unwrapped()).thenReturn(v1);

            ContextConfiguration contextConfiguration = new ContextConfiguration(config);

            assertThat(contextConfiguration.toString()).isNotNull();
        }
    }

}
