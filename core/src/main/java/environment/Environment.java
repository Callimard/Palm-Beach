package environment;

import agent.SimpleAgent;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import common.Context;
import common.SimpleContext;
import environment.network.Network;
import event.Event;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Represents an {@code Environment} where {@link SimpleAgent} are evolving in.
 * <p>
 * In {@code Environment}, it is not directly {@link SimpleAgent} which are manipulated but {@link SimpleAgent.AgentIdentifier}. The instance of
 * {@code SimpleAgent} is manage by the Simulation. In that way it is more simple to manipulate agent because {@link SimpleAgent.AgentIdentifier} is
 * immutable. To simplify, in the documentation of {@code Environment}, agent means {@code AgentIdentifier}.
 * <p>
 * An {@code Environment} can also have {@link Network} which simulate connections between agent in the {@code Environment}. A {@code Network}
 * also simulate {@link Event} sending between agents.
 * <p>
 * {@code Environment} sub classes must have at least this constructor:
 * <pre>
 *     Environment(String name, Context context) {
 *         ...
 *     }
 * </pre>
 */
@ToString
@Slf4j
public class Environment {

    // Variables.

    @Getter
    @NonNull
    private final String name;

    @Getter
    private final Context context;

    @ToString.Exclude
    private final Set<SimpleAgent.AgentIdentifier> agents;

    private final Map<String, Network> networks;

    @ToString.Exclude
    private final List<EnvironmentObserver> observers;

    // Constructors.

    /**
     * Constructs an {@link Environment} with the specified name (must be not null) and the specified context. If the specified context is null, a
     * default context class is used to create an empty {@link Context}. In that case, the context class implementation used is {@link
     * SimpleContext}.
     *
     * @param name    the environment name
     * @param context the context
     *
     * @throws NullPointerException if name is null
     */
    public Environment(@NonNull String name, Context context) {
        this.name = name;
        this.context = context != null ? context : new SimpleContext();
        this.agents = Sets.newConcurrentHashSet();
        this.networks = Maps.newHashMap();
        this.observers = new Vector<>();
    }

    // Methods.

    /**
     * @param observer the observer to add
     */
    public void addObserver(EnvironmentObserver observer) {
        if (!observers.contains(observer))
            observers.add(observer);
    }

    protected void notifyAgentAdded(SimpleAgent.AgentIdentifier addedAgent) {
        observers.forEach(o -> o.environmentAddAgent(addedAgent));
    }

    protected void notifyAgentRemoved(SimpleAgent.AgentIdentifier removedAgent) {
        observers.forEach(o -> o.environmentRemoveAgent(removedAgent));
    }

    /**
     * Create an instance of the specified {@link Environment} class. The specified class must have a construct as described in the general doc of
     * {@code Environment}.
     *
     * @param environmentClass the Environment class name
     * @param environmentName  the Environment name
     * @param context          the Environment context
     *
     * @return a new instance of the specified {@code Environment} class.
     *
     * @throws NoSuchMethodException     if the {@code Environment} class does not have the specific needed constructor
     * @throws InvocationTargetException if the constructor has thrown an exception
     * @throws InstantiationException    if the instantiation failed
     * @throws IllegalAccessException    if the construct is not accessible
     * @throws NullPointerException      if environmentClass or environmentName is null
     */
    public static Environment instantiateEnvironment(@NonNull Class<? extends Environment> environmentClass, @NonNull String environmentName,
                                                     Context context)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Environment> constructor = environmentClass.getConstructor(String.class, Context.class);
        return constructor.newInstance(environmentName, context);
    }

    /**
     * Add the agent in the {@link Environment}. An agent cannot be added several times in a {@code Environment}. If the agent is already added in the
     * {@code Environment}, returns false.
     *
     * @param agent the agent identifier
     *
     * @return true if the agent has been added, else false.
     */
    public boolean addAgent(@NonNull SimpleAgent.AgentIdentifier agent) {
        if (agents.add(agent)) {
            notifyAgentAdded(agent);
            return true;
        }
        return false;
    }

    /**
     * Remove the {@link SimpleAgent} of the {@link Environment} only if the {@code SimpleAgent} is evolving in.
     *
     * @param agent the agent identifier of the agent to remove
     */
    public void removeAgent(@NonNull SimpleAgent.AgentIdentifier agent) {
        if (agents.remove(agent)) {
            notifyAgentRemoved(agent);
        }
    }

    /**
     * @param agent the agent identifier
     *
     * @return true if the agens is evolving in the {@link Environment}, else false.
     */
    public boolean agentIsEvolving(@NonNull SimpleAgent.AgentIdentifier agent) {
        return agents.contains(agent);
    }

    /**
     * @return the current set of evolving agent, never returns null.
     */
    public Set<SimpleAgent.AgentIdentifier> evolvingAgents() {
        return Sets.newHashSet(agents);
    }

    /**
     * Add the specified {@link Network} to the {@link Environment}. In {@code Environment}, {@code Network} are mapped by their name . Therefore, if
     * several {@code Network} has the same name, the last {@code Network} added will be taken in account and erase previous added {@code Network}.
     * You may watch that each {@code Network} has a unique name.
     * <p>
     * <strong>This method is not thread safe.</strong>
     *
     * @param network the network to add
     */
    public void addNetwork(@NonNull Network network) {
        Network old = networks.put(network.getName(), network);
        log.info("Network {} has been added to the Environment {}", network, getName());
        if (old != null)
            log.warn("Network previously added in the Environment {} with the name {} has been erase and replace", getName(),
                     network.getName());
    }

    /**
     * @param networkName the name of the network
     *
     * @return the {@link Network} with the specified name if it has been added to the {@link Environment}, else null.
     */
    public Network getNetwork(String networkName) {
        return networks.get(networkName);
    }

    // Inner classes.

    public interface EnvironmentObserver {

        void environmentAddAgent(SimpleAgent.AgentIdentifier addedAgent);

        void environmentRemoveAgent(SimpleAgent.AgentIdentifier removedAgent);
    }
}
