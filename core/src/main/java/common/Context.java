package common;

/**
 * Allow objects of the simulation to have context add shares value with others.
 * <p>
 * This is interesting to collect datas dynamically during the simulation and make graphic display more easily.
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
}
