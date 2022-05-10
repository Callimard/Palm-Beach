package org.paradise.palmbeach.utils.validation;

import lombok.*;

/**
 * Tool class which has static method which simplify the common verifications that we can do on variable.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Validate {

    // Constructors.

    // Methods.

    /**
     * @param toVerify the value to verify
     * @param minValue the min value ({@code toVerify < minValue})
     * @param errorMsg the error message
     *
     * @throws IllegalArgumentException if {@code toVerify < minValue}
     */
    public static void min(long toVerify, long minValue, String errorMsg) {
        if (toVerify < minValue)
            throw new IllegalArgumentException(errorMsg);
    }

    /**
     * @param toVerify the value to verify
     * @param maxValue the min value ({@code toVerify < minValue})
     * @param errorMsg the error message
     *
     * @throws IllegalArgumentException if {@code toVerify > maxValue}
     */
    public static void max(long toVerify, long maxValue, String errorMsg) {
        if (toVerify > maxValue)
            throw new IllegalArgumentException(errorMsg);
    }

    /**
     * @param toVerify the value to verify
     * @param minValue the min value include
     * @param maxValue the max value include
     * @param errorMsg the error message
     *
     * @throws IllegalArgumentException if {@code toVerify NOT IN [minValue, maxValue]}.
     */
    public static void interval(long toVerify, long minValue, long maxValue, String errorMsg) {
        if (toVerify < minValue || toVerify > maxValue)
            throw new IllegalArgumentException(errorMsg);
    }

    // Inner classes.

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public abstract static class NumberValidator<T extends Number> implements Validator<T> {

        @NonNull
        @Getter
        private final T reference;

        @Getter
        private final String errorMsg;
    }

    public static class MinIntValidator extends NumberValidator<Integer> {

        // Constructors.

        public MinIntValidator(@NonNull Integer min) {
            this(min, null);
        }

        public MinIntValidator(@NonNull Integer min, String errorMsg) {
            super(min, errorMsg);
        }

        // Methods.

        @Override
        public void validate(Integer value) {
            min(value, getReference(), getErrorMsg());
        }
    }

    public static class MaxIntValidator extends NumberValidator<Integer> {

        // Constructors.

        public MaxIntValidator(@NonNull Integer min) {
            this(min, null);
        }

        public MaxIntValidator(@NonNull Integer max, String errorMsg) {
            super(max, errorMsg);
        }

        // Methods.

        @Override
        public void validate(Integer value) {
            max(value, getReference(), getErrorMsg());
        }
    }

    public static class IntervalIntValidator extends NumberValidator<Integer> {

        // Variables.

        private final Integer max;

        // Constructors.

        public IntervalIntValidator(@NonNull Integer min, @NonNull Integer max) {
            this(min, max, null);
        }

        public IntervalIntValidator(@NonNull Integer min, @NonNull Integer max, String errorMsg) {
            super(min, errorMsg);
            this.max = max;
        }

        // Methods.

        @Override
        public void validate(Integer value) {
            interval(value, getReference(), max, getErrorMsg());
        }
    }

    public static class MinLongValidator extends NumberValidator<Long> {

        // Constructors.

        public MinLongValidator(@NonNull Long min) {
            this(min, null);
        }

        public MinLongValidator(@NonNull Long min, String errorMsg) {
            super(min, errorMsg);
        }

        // Methods.

        @Override
        public void validate(Long value) {
            min(value, getReference(), getErrorMsg());
        }
    }

    public static class MaxLongValidator extends NumberValidator<Long> {

        // Constructors.

        public MaxLongValidator(@NonNull Long min) {
            this(min, null);
        }

        public MaxLongValidator(@NonNull Long max, String errorMsg) {
            super(max, errorMsg);
        }

        // Methods.

        @Override
        public void validate(Long value) {
            max(value, getReference(), getErrorMsg());
        }
    }

    public static class IntervalLongValidator extends NumberValidator<Long> {

        // Variables.

        private final Long max;

        // Constructors.

        public IntervalLongValidator(@NonNull Long min, @NonNull Long max) {
            this(min, max, null);
        }

        public IntervalLongValidator(@NonNull Long min, @NonNull Long max, String errorMsg) {
            super(min, errorMsg);
            this.max = max;
        }

        // Methods.

        @Override
        public void validate(Long value) {
            interval(value, getReference(), max, getErrorMsg());
        }
    }

    @FunctionalInterface
    public interface Validator<T> {

        /**
         * Verify the value.
         * <p>
         * Throws IllegalArgumentException if value is not valide
         *
         * @param value value to validate
         *
         * @throws IllegalArgumentException if the value is not valide
         */
        void validate(T value);

    }

}
