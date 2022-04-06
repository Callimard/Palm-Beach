package event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import junit.PalmBeachTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Nested
@DisplayName("Event tests")
@Tag("Event")
@PalmBeachTest
public class EventTest {

    @Nested
    @DisplayName("Event getContent()")
    @Tag("getContent")
    class GetContent {

        @ParameterizedTest
        @ValueSource(strings = {" #% ", "[)*<-&# ", "", "\n", "\0", "%[#@@>"})
        @DisplayName("getContent() returns the correct content of the Event")
        void getContentTest(String content) {
            Event<String> event = new BasicEvent(content);

            assertThat(event.getContent()).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("Event equals()")
    @Tag("equals")
    class Equals {

        @Test
        @DisplayName("equals() returns false with other type")
        void withOtherType() {
            Event<String> e0 = new BasicEvent("content");
            Object other = new Object();

            assertThat(e0).isNotEqualTo(other);
        }

        @Test
        @DisplayName("equals() returns true with equals event")
        void withEqualEvent() {
            Event<String> e0 = new BasicEvent("content");
            Event<String> e1 = new BasicEvent("content");

            assertThat(e0).isEqualTo(e1);
        }
    }

    @Nested
    @DisplayName("Event hashCode()")
    @Tag("hashCode")
    class HashCode {

        @Test
        @DisplayName("hashCode() does not throw exception")
        void neverReturnsNull() {
            Event<String> e0 = new BasicEvent("content");

            assertDoesNotThrow(e0::hashCode);
        }
    }

    @Nested
    @DisplayName("Event toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull() {
            Event<String> e0 = new BasicEvent("content");

            assertThat(e0.toString()).isNotNull();
        }
    }

    // Inner classes.

    public static class BasicEvent extends Event<String> {

        public BasicEvent(String content) {
            super(content);
        }
    }

}
