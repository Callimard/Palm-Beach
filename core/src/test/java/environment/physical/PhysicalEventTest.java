package environment.physical;

import event.Event;
import junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("PhysicalEvent tests")
@Tag("PhysicalEvent")
@PalmBeachTest
public class PhysicalEventTest {

    @Nested
    @DisplayName("PhysicalEvent constructor")
    @Tag("constructor")
    class Constructor {

        @Test
        @DisplayName("constructor throws NullPointerException if the Event is null")
        void withNullEvent() {
            assertThrows(NullPointerException.class, () -> new BasicPhysicalEvent(null));
        }

        @Test
        @DisplayName("constructor does not throw exception with non null Event")
        void withNonNullEvent(@Mock Event<?> event) {
            assertDoesNotThrow(() -> new BasicPhysicalEvent(event));
        }
    }

    // Inner classes.

    public static class BasicPhysicalEvent extends PhysicalEvent {

        protected BasicPhysicalEvent(Event<?> content) {
            super(content);
        }
    }
}
