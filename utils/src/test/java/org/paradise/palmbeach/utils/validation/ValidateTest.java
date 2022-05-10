package org.paradise.palmbeach.utils.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("Validate tests")
@Tag("Validate")
public class ValidateTest {

    @Nested
    @DisplayName("Validate min()")
    @Tag("min")
    class Min {

        @ParameterizedTest
        @ValueSource(ints = {-15464, -4654, 0})
        @DisplayName("min() throw IllegalArgumentException if toVerify is less than minValue")
        void lessValue(int value) {
            assertThrows(IllegalArgumentException.class, () -> Validate.min(value, 1, ""));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 165456, 4, 65465, 56})
        @DisplayName("min() does not throw exception with greater or equal values")
        void greaterOrEqualValue(int value) {
            assertDoesNotThrow(() -> Validate.min(value, 1, ""));
        }


        @Nested
        @DisplayName("Validate MinIntValidator")
        @Tag("MinIntValidator")
        class MinIntValidator {

            @Test
            @DisplayName("validate() throws IllegalArgumentException if value is not valid")
            void notValid() {
                Validate.MinIntValidator validator = new Validate.MinIntValidator(1);
                assertThrows(IllegalArgumentException.class, () -> validator.validate(0));
            }

            @Test
            @DisplayName("validate() does not throw Exception if value is valid")
            void withValid() {
                Validate.MinIntValidator validator = new Validate.MinIntValidator(1);
                assertDoesNotThrow(() -> validator.validate(5));
            }
        }

        @Nested
        @DisplayName("Validate MinIntValidator")
        @Tag("MinIntValidator")
        class MinLongValidator {

            @Test
            @DisplayName("validate() throws IllegalArgumentException if value is not valid")
            void notValid() {
                Validate.MinLongValidator validator = new Validate.MinLongValidator(1L);
                assertThrows(IllegalArgumentException.class, () -> validator.validate(0L));
            }

            @Test
            @DisplayName("validate() does not throw Exception if value is valid")
            void withValid() {
                Validate.MinLongValidator validator = new Validate.MinLongValidator(1L);
                assertDoesNotThrow(() -> validator.validate(5L));
            }
        }
    }

    @Nested
    @DisplayName("Validate max()")
    @Tag("min")
    class Max {

        @ParameterizedTest
        @ValueSource(ints = {2, 165456, 4, 65465, 56})
        @DisplayName("max() throw IllegalArgumentException if toVerify is greater than minValue")
        void greaterValue(int value) {
            assertThrows(IllegalArgumentException.class, () -> Validate.max(value, 1, ""));
        }

        @ParameterizedTest
        @ValueSource(ints = {-15464, -4654, 0, 1})
        @DisplayName("max() does not throw exception with less or equal values")
        void lessOrEqualValue(int value) {
            assertDoesNotThrow(() -> Validate.max(value, 1, ""));
        }

        @Nested
        @DisplayName("Validate MaxIntValidator")
        @Tag("MaxIntValidator")
        class MaxIntValidator {

            @Test
            @DisplayName("validate() throws IllegalArgumentException if value is not valid")
            void notValid() {
                Validate.MaxIntValidator validator = new Validate.MaxIntValidator(1);
                assertThrows(IllegalArgumentException.class, () -> validator.validate(2));
            }

            @Test
            @DisplayName("validate() does not throw Exception if value is valid")
            void withValid() {
                Validate.MaxIntValidator validator = new Validate.MaxIntValidator(0);
                assertDoesNotThrow(() -> validator.validate(0));
            }
        }

        @Nested
        @DisplayName("Validate MinIntValidator")
        @Tag("MinIntValidator")
        class MaxLongValidator {

            @Test
            @DisplayName("validate() throws IllegalArgumentException if value is not valid")
            void notValid() {
                Validate.MaxLongValidator validator = new Validate.MaxLongValidator(1L);
                assertThrows(IllegalArgumentException.class, () -> validator.validate(2L));
            }

            @Test
            @DisplayName("validate() does not throw Exception if value is valid")
            void withValid() {
                Validate.MaxLongValidator validator = new Validate.MaxLongValidator(1L);
                assertDoesNotThrow(() -> validator.validate(0L));
            }
        }
    }

    @Nested
    @DisplayName("Validate interval()")
    @Tag("interval")
    class Interval {

        @ParameterizedTest
        @ValueSource(ints = {-1546, -215156, -21, -1, 0, 11, 12, 1564, 156132, 6546, 65465})
        @DisplayName("max() throw IllegalArgumentException if toVerify is not in the interval")
        void notInIntervalValue(int value) {
            assertThrows(IllegalArgumentException.class, () -> Validate.interval(value, 1, 10, ""));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
        @DisplayName("max() does not throw exception if toVerify is in the interval")
        void inIntervalValue(int value) {
            assertDoesNotThrow(() -> Validate.interval(value, 1, 10, ""));
        }

        @Nested
        @DisplayName("Validate IntervalIntValidator")
        @Tag("IntervalIntValidator")
        class IntervalIntValidator {

            @Test
            @DisplayName("validate() throws IllegalArgumentException if value is not valid")
            void notValid() {
                Validate.IntervalIntValidator validator = new Validate.IntervalIntValidator(1, 4);
                assertThrows(IllegalArgumentException.class, () -> validator.validate(0));
            }

            @Test
            @DisplayName("validate() does not throw Exception if value is valid")
            void withValid() {
                Validate.IntervalIntValidator validator = new Validate.IntervalIntValidator(1, 4);
                assertDoesNotThrow(() -> validator.validate(2));
            }
        }

        @Nested
        @DisplayName("Validate IntervalLongValidator")
        @Tag("IntervalLongValidator")
        class IntervalLongValidator {

            @Test
            @DisplayName("validate() throws IllegalArgumentException if value is not valid")
            void notValid() {
                Validate.IntervalLongValidator validator = new Validate.IntervalLongValidator(1L, 4L);
                assertThrows(IllegalArgumentException.class, () -> validator.validate(0L));
            }

            @Test
            @DisplayName("validate() does not throw Exception if value is valid")
            void withValid() {
                Validate.IntervalLongValidator validator = new Validate.IntervalLongValidator(1L, 4L);
                assertDoesNotThrow(() -> validator.validate(2L));
            }
        }
    }

}
