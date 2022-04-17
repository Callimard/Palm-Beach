package simulation;

import junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Nested
@DisplayName("PalmBeachRunner tests")
@Tag("PalmBeachRunner")
@PalmBeachTest
public class PalmBeachRunnerTest {

    @Nested
    @DisplayName("PalmBeachRunner main()")
    @Tag("main")
    class Main {

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
