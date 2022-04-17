package common.validation;

import common.Context;
import common.SimpleContext;
import junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("Context tests")
@Tag("Context")
@PalmBeachTest
public class ContextTest {

    @Nested
    @DisplayName("Context instantiateContext()")
    @Tag("instantiateContext")
    class InstantiateContext {

        @Test
        @DisplayName("instantiateContext() throws NullPointerException with null contextClass")
        void withNullContextClass() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> Context.instantiateContext(null));
        }

        @Test
        @DisplayName("instantiateContext() create a new instance of the specified class")
        void withCorrectContextClass() {
            AtomicReference<Context> context = new AtomicReference<>();
            assertDoesNotThrow(() -> context.set(Context.instantiateContext(SimpleContext.class)));
            assertThat(context.get()).isNotNull();
        }
    }


}
