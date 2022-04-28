package org.paradise.palmbeach.utils.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.paradise.palmbeach.utils.junit.ParadiseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("Tools tests")
@Tag("Tools")
@ParadiseTest
public class ReflectionToolsTest {

    @Nested
    @DisplayName("Tools extractClass()")
    @Tag("extractClass")
    class ExtractClass {

        @Test
        @DisplayName("extractClass() throws NullPointerException with null className")
        void withNullClassName() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> ReflectionTools.extractClass(null));
        }

        @Test
        @DisplayName("extractClass() extract the correct class")
        void produceTheCorrectClass() throws ClassNotFoundException {
            Class<? extends InternalUTToolClass> internalUTToolClassClass = ReflectionTools.extractClass(InternalUTToolClass.class.getName());
            assertThat(internalUTToolClassClass).isEqualTo(InternalUTToolClass.class);
        }
    }

    public static class InternalUTToolClass {
        // Nothing
    }

}
