package org.palmbeach.core.simulation.configuration;

import com.typesafe.config.Config;
import org.palmbeach.core.environment.Environment;
import org.palmbeach.core.environment.network.Network;
import org.palmbeach.core.environment.network.NetworkTest;
import org.palmbeach.core.junit.PalmBeachTest;
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
@DisplayName("NetworkConfiguration tests")
@Tag("NetworkConfiguration")
@PalmBeachTest
public class NetworkConfigurationTest {

    @Nested
    @DisplayName("NetworkConfiguration constructor()")
    @Tag("constructor")
    class Constructor {

        @SuppressWarnings("ConstantConditions")
        @Test
        @DisplayName("constructor() throws NullPointerException with null parameters")
        void withNullParameters(@Mock Config config) {
            assertThrows(NullPointerException.class, () -> new NetworkConfiguration(null, config));
            assertThrows(NullPointerException.class, () -> new NetworkConfiguration("pnName", null));
            assertThrows(NullPointerException.class, () -> new NetworkConfiguration(null, null));
            assertDoesNotThrow(() -> new NetworkConfiguration("pNName", config));
        }
    }

    @Nested
    @DisplayName("NetworkConfiguration generate()")
    @Tag("generate")
    class Generate {

        @Test
        @DisplayName("generate() throws UnsupportedOperationException")
        void throwsUnsupportedOperation(@Mock Config config) {
            when(config.getString(NetworkConfiguration.CLASS_PROPERTY)).thenReturn(NetworkTest.BasicNetwork.class.getName());
            NetworkConfiguration pnConfiguration = new NetworkConfiguration("name", config);

            assertThrows(UnsupportedOperationException.class, pnConfiguration::generate);
        }
    }

    @Nested
    @DisplayName("NetworkConfiguration generateNetwork()")
    @Tag("generateNetwork")
    class GenerateNetwork {

        @Test
        @DisplayName("generateNetwork() generate correct Network")
        void generateCorrectNetwork(@Mock Environment environment, @Mock Config config) {
            String pnName = "pnName";
            when(config.getString(NetworkConfiguration.CLASS_PROPERTY)).thenReturn(NetworkTest.BasicNetwork.class.getName());
            NetworkConfiguration pnConfiguration = new NetworkConfiguration(pnName, config);

            AtomicReference<Network> pn = new AtomicReference<>();
            assertDoesNotThrow(() -> pn.set(pnConfiguration.generateNetwork(environment)));
            assertThat(pn.get()).isNotNull();
            assertThat(pn.get().getClass()).isEqualTo(NetworkTest.BasicNetwork.class);
            assertThat(pn.get().getName()).isEqualTo(pnName);
            assertThat(pn.get().getEnvironment()).isSameAs(environment);
        }
    }

    @Nested
    @DisplayName("NetworkConfiguration toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull(@Mock Config config) {
            String pnName = "pnName";
            when(config.getString(NetworkConfiguration.CLASS_PROPERTY)).thenReturn(NetworkTest.BasicNetwork.class.getName());
            NetworkConfiguration pnConfiguration = new NetworkConfiguration(pnName, config);

            assertThat(pnConfiguration.toString()).isNotNull();
        }
    }
}
