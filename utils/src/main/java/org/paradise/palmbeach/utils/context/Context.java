package org.paradise.palmbeach.utils.context;

import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Allow objects of the simulation to have context add shares value with others.
 * <p>
 * This is interesting to collect datas dynamically during the simulation and make graphic display more easily.
 * <p>
 * {@code Context} sub classes must have a default constructor.
 */
public interface Context {

    /**
     * Map the specified value with the specified key. If there already is a value mapped with the specified kay, the old value is erased and replaced
     * by the new specified value.
     *
     * @param key   the key associated to the value
     * @param value the value
     */
    void map(String key, Object value);

    /**
     * @param key the key associated to a value
     *
     * @return the mapped value to the specified key. If there is no value, returns null.
     */
    Object getValue(String key);

    default boolean hasValue(String key) {
        return getValue(key) != null;
    }

    default void setInt(String key, int value) {
        map(key, value);
    }

    default Integer getInt(String key) {
        if (hasValue(key)) {
            Object value = getValue(key);
            if (value instanceof String sInteger) {
                return Integer.valueOf(sInteger);
            } else {
                return (Integer) getValue(key);
            }
        }
        return null;
    }

    default void setLong(String key, long value) {
        map(key, value);
    }

    default Long getLong(String key) {
        if (hasValue(key)) {
            Object value = getValue(key);
            if (value instanceof String sLong) {
                return Long.valueOf(sLong);
            } else {
                return (Long) getValue(key);
            }
        }
        return null;
    }

    default void setString(String key, String value) {
        map(key, value);
    }

    default String getString(String key) {
        return (String) getValue(key);
    }

    /**
     * @return true if there is no mapping value on the context.
     */
    boolean isEmpty();

    /**
     * @param contextClass the context class
     *
     * @return a new instance of a {@link Context} which has the same type specified.
     *
     * @throws NoSuchMethodException     if the {@code Context} class does not have the specific needed constructor
     * @throws InvocationTargetException if the constructor has thrown an exception
     * @throws InstantiationException    if the instantiation failed
     * @throws IllegalAccessException    if the construct is not accessible
     * @throws NullPointerException      if contextClass is null
     */
    static Context instantiateContext(@NonNull Class<? extends Context> contextClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Context> constructor = contextClass.getConstructor();
        return constructor.newInstance();
    }
}
