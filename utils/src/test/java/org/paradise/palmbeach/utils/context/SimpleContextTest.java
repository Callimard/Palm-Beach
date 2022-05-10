package org.paradise.palmbeach.utils.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.paradise.palmbeach.utils.junit.ParadiseTest;
import org.paradise.palmbeach.utils.validation.Validate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("SimpleContext tests")
@Tag("SimpleContext")
@ParadiseTest
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
        @DisplayName("setInt(validator) throws IllegalArgumentException if value is not valid")
        void setIntNonValidValid() {
            SimpleContext context = new SimpleContext();

            Validate.Validator<Integer> validator = new Validate.MinIntValidator(1);
            assertThrows(IllegalArgumentException.class, () -> context.setInt("key", 0, validator));
            assertDoesNotThrow(() -> context.setInt("key", 0, null));
        }

        @Test
        @DisplayName("getInt() throws NumberFormatException if value is a string which does not represent a number")
        void getInt() {
            SimpleContext context = new SimpleContext();

            context.setString("key", "test");
            assertThrows(NumberFormatException.class, () -> context.getInt("key"));
        }

        @Test
        @DisplayName("getInt() does not throw exception with a string which represent a number")
        void getIntString() {
            SimpleContext context = new SimpleContext();
            int n = 464546;

            context.setString("key", String.valueOf(n));
            assertDoesNotThrow(() -> context.getInt("key"));
            assertThat(context.getInt("key")).isEqualTo(n);
        }

        @Test
        @DisplayName("getInt() returns null if the value is not an int or a string")
        void getIntWrongClass() {
            SimpleContext context = new SimpleContext();

            context.map("key", new Object());
            assertThat(context.getInt("key")).isNull();
        }

        @Test
        @DisplayName("getInt(default) returns default value if value is not present")
        void getIntDefaultValue() {
            SimpleContext context = new SimpleContext();

            String key = "key";
            int defaultValue = 5;
            int other = 19;

            assertThat(context.getInt(key, defaultValue)).isEqualByComparingTo(defaultValue);
            context.setInt(key, other);
            assertThat(context.getInt(key, defaultValue)).isEqualByComparingTo(other);
        }

        @Test
        @DisplayName("getInt(validator) throws IllegalArgumentException if the value in the context is not valid")
        void getIntWithNonValidValue() {
            SimpleContext context = new SimpleContext();

            String key = "key";
            int nonValid = 0;
            int valid = 1;
            int defaultValue = 5;
            Validate.Validator<Integer> validator = new Validate.MinIntValidator(1);

            context.setInt(key, nonValid);
            assertThrows(IllegalArgumentException.class, () -> context.getInt(key, defaultValue, validator));
            context.setInt(key, valid);
            assertThat(context.getInt(key, defaultValue, validator)).isEqualByComparingTo(valid);
        }

        @Test
        @DisplayName("setLong()")
        void setLong() {
            SimpleContext context = new SimpleContext();

            context.setLong("key", 1L);
            assertThat(context.getLong("key")).isEqualTo(1L);
        }

        @Test
        @DisplayName("setLong(validator) throws IllegalArgumentException if value is not valid")
        void setLongNonValidValid() {
            SimpleContext context = new SimpleContext();

            Validate.Validator<Long> validator = new Validate.MinLongValidator(1L);
            assertThrows(IllegalArgumentException.class, () -> context.setLong("key", 0L, validator));
            assertDoesNotThrow(() -> context.setLong("key", 0L, null));
        }

        @Test
        @DisplayName("getLong() throws NumberFormatException if value is a string which does not represent a number")
        void getLong() {
            SimpleContext context = new SimpleContext();

            context.setString("key", "test");
            assertThrows(NumberFormatException.class, () -> context.getLong("key"));
        }

        @Test
        @DisplayName("getLong() does not throw exception with a string which represent a number")
        void getLongString() {
            SimpleContext context = new SimpleContext();
            long n = 464546L;

            context.setString("key", String.valueOf(n));
            assertDoesNotThrow(() -> context.getLong("key"));
            assertThat(context.getLong("key")).isEqualTo(n);
        }

        @Test
        @DisplayName("getLong() returns null if the value is not an int or a string")
        void getLongWrongClass() {
            SimpleContext context = new SimpleContext();

            context.map("key", new Object());
            assertThat(context.getLong("key")).isNull();
        }

        @Test
        @DisplayName("getLong(default) returns default value if value is not present")
        void getLongDefaultValue() {
            SimpleContext context = new SimpleContext();

            String key = "key";
            long defaultValue = 5;
            long other = 19;

            assertThat(context.getLong(key, defaultValue)).isEqualByComparingTo(defaultValue);
            context.setLong(key, other);
            assertThat(context.getLong(key, defaultValue)).isEqualByComparingTo(other);
        }

        @Test
        @DisplayName("getLong(validator) throws IllegalArgumentException if the value in the context is not valid")
        void getLongWithNonValidValue() {
            SimpleContext context = new SimpleContext();

            String key = "key";
            long nonValid = 0;
            long valid = 1;
            long defaultValue = 5;
            Validate.Validator<Long> validator = new Validate.MinLongValidator(1L);

            context.setLong(key, nonValid);
            assertThrows(IllegalArgumentException.class, () -> context.getLong(key, defaultValue, validator));
            context.setLong(key, valid);
            assertThat(context.getLong(key, defaultValue, validator)).isEqualByComparingTo(valid);
        }

        @Test
        @DisplayName("setString()")
        void setString() {
            SimpleContext context = new SimpleContext();

            context.setString("key", "test");
            assertThat(context.getString("key")).isEqualTo("test");
        }

        @Test
        @DisplayName("setString(validator) throws IllegalArgumentException if value is not valid")
        void setStringNonValidValid() {
            SimpleContext context = new SimpleContext();

            Validate.Validator<String> validator = (String s) -> {
                if (s.isBlank())
                    throw new IllegalArgumentException();
            };

            assertThrows(IllegalArgumentException.class, () -> context.setString("key", "", validator));
            assertDoesNotThrow(() -> context.setString("key", "Valid", null));
        }

        @Test
        @DisplayName("getString() throws ClassCastException if key is not a String")
        void getString() {
            SimpleContext context = new SimpleContext();

            context.setLong("key", 1L);
            assertThrows(ClassCastException.class, () -> context.getString("key"));
        }

        @Test
        @DisplayName("getString(default) returns default value if value is not present")
        void getStringDefaultValue() {
            SimpleContext context = new SimpleContext();

            String key = "key";
            String defaultValue = "DEFAULT";
            String other = "Other";

            assertThat(context.getString(key, defaultValue)).isEqualTo(defaultValue);
            context.setString(key, other);
            assertThat(context.getString(key, defaultValue)).isEqualTo(other);
        }

        @Test
        @DisplayName("getString(validator) throws IllegalArgumentException if the value in the context is not valid")
        void getStringWithNonValidValue() {
            SimpleContext context = new SimpleContext();

            String key = "key";
            String nonValid = "";
            String valid = "Valid";
            String defaultValue = "DEFAULT";
            Validate.Validator<String> validator = (String s) -> {
                if (s.isBlank())
                    throw new IllegalArgumentException();
            };

            context.setString(key, nonValid);
            assertThrows(IllegalArgumentException.class, () -> context.getString(key, defaultValue, validator));
            context.setString(key, valid);
            assertThat(context.getString(key, defaultValue, validator)).isEqualTo(valid);
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
