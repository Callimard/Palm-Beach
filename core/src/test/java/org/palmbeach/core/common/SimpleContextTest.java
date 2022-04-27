package org.palmbeach.core.common;

import org.palmbeach.core.common.SimpleContext;
import org.palmbeach.core.junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("SimpleContext tests")
@Tag("SimpleContext")
@PalmBeachTest
public class SimpleContextTest {

    @Nested
    @DisplayName("SimpleContext map()")
    @Tag("map")
    class Map {

        @Test
        @DisplayName("map() associates key and value")
        void mapAssociateKeyAndValue() {
            String key = "0";
            String value = "value";

            SimpleContext context = new SimpleContext();
            context.map(key, value);

            assertThat(context.getValue(key)).isNotNull().isSameAs(value);
        }

        @Test
        @DisplayName("map() does not throw exception for any type of value")
        void mapWithSeveralType() {
            SimpleContext context = new SimpleContext();

            assertDoesNotThrow(() -> context.map("0", "value"));
            assertDoesNotThrow(() -> context.map("1", new Object()));
        }

        @Test
        @DisplayName("map() does not accept null value and throws NullPointerException")
        void mapWithNullValue() {
            SimpleContext context = new SimpleContext();

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> context.map("0", null));
        }

        @Test
        @DisplayName("map() erase the previous value of a key")
        void mapErasePreviousValue() {
            String key = "0";
            String value = "value";
            String otherValue = "otherValue";

            SimpleContext context = new SimpleContext();
            context.map(key, value);
            context.map(key, otherValue);

            assertThat(context.getValue(key)).isNotNull().isSameAs(otherValue);
        }

        @Test
        @DisplayName("map() throws NullPointerException with null key")
        void withNullKey() {
            SimpleContext context = new SimpleContext();

            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () -> context.map(null, "value"));
        }
    }

    @Nested
    @DisplayName("SimpleContext getValue()")
    @Tag("getValue")
    class GetValue {

        @Test
        @DisplayName("getValue() returns null if the key is not mapped")
        void notMappedKey() {
            SimpleContext context = new SimpleContext();

            assertThat(context.getValue("key")).isNull();
        }

        @Test
        @DisplayName("getValue() returns the value mapped with the key")
        void mappedKey() {
            SimpleContext context = new SimpleContext();
            context.map("key", "value");

            assertThat(context.getValue("key")).isNotNull();
        }
    }

    @Nested
    @DisplayName("SimpleContext get() set()")
    @Tag("getAndSet")
    class GetAndSet {

        @Test
        @DisplayName("setInt()")
        void setInt() {
            SimpleContext context = new SimpleContext();

            context.setInt("key", 1);
            assertThat(context.getInt("key")).isEqualTo(1);
        }

        @Test
        @DisplayName("getInt() throws ClassCastException if key is not a int")
        void getInt() {
            SimpleContext context = new SimpleContext();

            context.setString("key", "test");
            assertThrows(ClassCastException.class, () -> context.getInt("key"));
        }

        @Test
        @DisplayName("setLong()")
        void setLong() {
            SimpleContext context = new SimpleContext();

            context.setLong("key", 1L);
            assertThat(context.getLong("key")).isEqualTo(1L);
        }

        @Test
        @DisplayName("getLong() throws ClassCastException if key is not a long")
        void getLong() {
            SimpleContext context = new SimpleContext();

            context.setString("key", "test");
            assertThrows(ClassCastException.class, () -> context.getLong("key"));
        }

        @Test
        @DisplayName("setString()")
        void setString() {
            SimpleContext context = new SimpleContext();

            context.setString("key", "test");
            assertThat(context.getString("key")).isEqualTo("test");
        }

        @Test
        @DisplayName("getString() throws ClassCastException if key is not a String")
        void getString() {
            SimpleContext context = new SimpleContext();

            context.setLong("key", 1L);
            assertThrows(ClassCastException.class, () -> context.getString("key"));
        }
    }

    @Nested
    @DisplayName("SimpleContext isEmpty()")
    @Tag("isEmpty")
    class IsEmpty {

        @Test
        @DisplayName("isEmpty() returns true after the initialisation")
        void afterInitialisation() {
            SimpleContext context = new SimpleContext();

            assertThat(context.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty() returns false if at least map() has been call one times")
        void afterMapCall() {
            SimpleContext context = new SimpleContext();
            context.map("key", "value");

            assertThat(context.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("SimpleContext toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() returns non null value")
        void neverReturnsNull() {
            SimpleContext context = new SimpleContext();

            assertThat(context.toString()).isNotNull();
        }
    }

}
