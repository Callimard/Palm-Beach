package org.palmbeach.core.common.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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

}
