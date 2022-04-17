package simulation.configuration;

import com.typesafe.config.Config;
import environment.physical.PhysicalNetwork;
import environment.physical.PhysicalNetworkTest;
import junit.PalmBeachTest;
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
@DisplayName("PhysicalNetworkConfiguration tests")
@Tag("PhysicalNetworkConfiguration")
@PalmBeachTest
public class PhysicalNetworkConfigurationTest {

    @Nested
    @DisplayName("PhysicalNetworkConfiguration constructor()")
    @Tag("constructor")
    class Constructor {

        @SuppressWarnings("ConstantConditions")
        @Test
        @DisplayName("constructor() throws NullPointerException with null parameters")
        void withNullParameters(@Mock Config config) {
            assertThrows(NullPointerException.class, () -> new PhysicalNetworkConfiguration(null, config));
            assertThrows(NullPointerException.class, () -> new PhysicalNetworkConfiguration("pnName", null));
            assertThrows(NullPointerException.class, () -> new PhysicalNetworkConfiguration(null, null));
            assertDoesNotThrow(() -> new PhysicalNetworkConfiguration("pNName", config));
        }
    }

    @Nested
    @DisplayName("PhysicalNetworkConfiguration generate()")
    @Tag("generate")
    class Generate {

        @Test
        @DisplayName("generate() generate correct PhysicalNetwork")
        void generateCorrectPhysicalNetwork(@Mock Config config) {
            String pnName = "pnName";
            when(config.getString(PhysicalNetworkConfiguration.CLASS_PROPERTY)).thenReturn(PhysicalNetworkTest.BasicPhysicalNetwork.class.getName());
            PhysicalNetworkConfiguration pnConfiguration = new PhysicalNetworkConfiguration(pnName, config);

            AtomicReference<PhysicalNetwork> pn = new AtomicReference<>();
            assertDoesNotThrow(() -> pn.set(pnConfiguration.generate()));
            assertThat(pn.get()).isNotNull();
            assertThat(pn.get().getClass()).isEqualTo(PhysicalNetworkTest.BasicPhysicalNetwork.class);
            assertThat(pn.get().getName()).isEqualTo(pnName);
        }
    }

    @Nested
    @DisplayName("PhysicalNetworkConfiguration toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull(@Mock Config config) {
            String pnName = "pnName";
            when(config.getString(PhysicalNetworkConfiguration.CLASS_PROPERTY)).thenReturn(PhysicalNetworkTest.BasicPhysicalNetwork.class.getName());
            PhysicalNetworkConfiguration pnConfiguration = new PhysicalNetworkConfiguration(pnName, config);

            assertThat(pnConfiguration.toString()).isNotNull();
        }
    }
}
