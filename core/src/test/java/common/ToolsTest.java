package common;

import junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("Tools tests")
@Tag("Tools")
@PalmBeachTest
public class ToolsTest {

    @Nested
    @DisplayName("Tools extractClass()")
    @Tag("extractClass")
    class ExtractClass {

        @Test
        @DisplayName("extractClass() throws NullPointerException with null className")
        void withNullClassName() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> Tools.extractClass(null));
        }

        @Test
        @DisplayName("extractClass() extract the correct class")
        void produceTheCorrectClass() throws ClassNotFoundException {
            Class<? extends InternalUTToolClass> internalUTToolClassClass = Tools.extractClass(InternalUTToolClass.class.getName());
            assertThat(internalUTToolClassClass).isEqualTo(InternalUTToolClass.class);
        }
    }

    public static class InternalUTToolClass {
        // Nothing
    }

}
