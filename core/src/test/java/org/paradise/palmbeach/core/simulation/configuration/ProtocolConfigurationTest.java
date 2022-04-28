package org.paradise.palmbeach.core.simulation.configuration;

import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.agent.protocol.Protocol;
import org.paradise.palmbeach.core.agent.protocol.ProtocolTest;
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
@DisplayName("ControllerConfiguration tests")
@Tag("ControllerConfiguration")
@PalmBeachTest
public class ProtocolConfigurationTest {

    @Nested
    @DisplayName("ProtocolConfiguration constructor()")
    @Tag("constructor")
    class Constructor {

        @Test
        @DisplayName("constructor() throws NullPointerException with null basic config")
        void withNullParameter() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> new ProtocolConfiguration(null));
        }

        @Test
        @DisplayName("constructor() with empty configuration does not throw exception")
        void withEmptyName(@Mock Config config) {
            assertDoesNotThrow(() -> new ProtocolConfiguration(config));
        }
    }

    @Nested
    @DisplayName("ProtocolConfiguration generate()")
    @Tag("generate")
    class Generate {

        @Test
        @DisplayName("generate() throws UnsupportedOperationException")
        void unsupportedOperationException(@Mock Config config) {
            when(config.getString(ProtocolConfiguration.CLASS_PROPERTY)).thenReturn(ProtocolTest.BasicProtocol.class.getName());
            ProtocolConfiguration pConfiguration = new ProtocolConfiguration(config);

            assertThrows(UnsupportedOperationException.class, pConfiguration::generate);
        }
    }

    @Nested
    @DisplayName("ProtocolConfiguration generateProtocol()")
    @Tag("generateProtocol")
    class GenerateProtocol {

        @Test
        @DisplayName("generateProtocol() generate correct Protocol")
        void generateCorrectProtocol(@Mock Config config, @Mock SimpleAgent agent) {
            when(config.getString(ProtocolConfiguration.CLASS_PROPERTY)).thenReturn(ProtocolTest.BasicProtocol.class.getName());
            ProtocolConfiguration pConfiguration = new ProtocolConfiguration(config);

            AtomicReference<Protocol> protocol = new AtomicReference<>();
            assertDoesNotThrow(() -> protocol.set(pConfiguration.generateProtocol(agent)));
            assertThat(protocol.get()).isNotNull();
            assertThat(protocol.get().getAgent()).isEqualTo(agent);
        }
    }

    @Nested
    @DisplayName("ProtocolConfiguration toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull(@Mock Config config) {
            when(config.getString(ProtocolConfiguration.CLASS_PROPERTY)).thenReturn(ProtocolTest.BasicProtocol.class.getName());
            ProtocolConfiguration pConfiguration = new ProtocolConfiguration(config);

            assertThat(pConfiguration.toString()).isNotNull();
        }
    }
}
