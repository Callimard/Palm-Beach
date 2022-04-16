package common;

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
