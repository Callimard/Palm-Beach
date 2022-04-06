package environment;

import agent.SimpleAgent;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import common.Context;
import common.SimpleContext;
import environment.physical.PhysicalNetwork;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

/**
 * Represents an {@code Environment} where {@link SimpleAgent} are evolving in.
 * <p>
 * In {@code Environment}, it is not directly {@link SimpleAgent} which are manipulated but {@link agent.SimpleAgent.AgentIdentifier}. The instance of
 * {@code SimpleAgent} is manage by the Simulation. In that way it is more simple to manipulate agent because {@link
 * agent.SimpleAgent.AgentIdentifier} is immutable. To simplify, in the documentation of {@code Environment}, agent means {@code AgentIdentifier}.
 */
@ToString
@Slf4j
public abstract class Environment {

    // Variables.

    @Getter
    @NonNull
    private final String name;

    @Getter
    private final Context context;

    @ToString.Exclude
    private final Set<SimpleAgent.AgentIdentifier> agents;

    @ToString.Exclude
    private final Map<String, PhysicalNetwork> physicalNetworks;

    // Constructors.

    protected Environment(@NonNull String name) {
        this(name, null);
    }

    protected Environment(@NonNull String name, Context context) {
        this.name = name;
        this.context = context != null ? context : new SimpleContext();
        this.agents = Sets.newConcurrentHashSet();
        this.physicalNetworks = Maps.newHashMap();
    }

    // Methods.

    /**
     * Add the agent in the {@link Environment}. An agent cannot be added several times in a {@code Environment}. If the agent is already added in the
     * {@code Environment}, returns false.
     *
     * @param agent the agent identifier
     *
     * @return true if the agent has been added, else false.
     */
    public boolean addAgent(@NonNull SimpleAgent.AgentIdentifier agent) {
        return agents.add(agent);
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
     * Add the specified {@link PhysicalNetwork} to the {@link Environment}. In {@code Environment}, {@code PhysicalNetwork} are mapped by their name
     * . Therefore, if several {@code PhysicalNetwork} has the same name, the last {@code PhysicalNetwork} added will be taken in account and erase
     * previous added {@code PhysicalNetwork}. You may watch that each {@code PhysicalNetwork} has a unique name.
     *
     * @param physicalNetwork the physical network to add
     */
    public void addPhysicalNetwork(@NonNull PhysicalNetwork physicalNetwork) {
        PhysicalNetwork old = physicalNetworks.put(physicalNetwork.getName(), physicalNetwork);
        log.info("PhysicalNetwork {} has been added to the Environment {}", physicalNetwork, getName());
        if (old != null)
            log.warn("PhysicalNetwork previously added in the Environment {} with the name {} has been erase and replace", getName(),
                     physicalNetwork.getName());
    }

    /**
     * @param physicalNetworkName the name of the physical network
     *
     * @return the {@link PhysicalNetwork} with the specified name if it has been added to the {@link Environment}, else null.
     */
    public PhysicalNetwork getPhysicalNetwork(String physicalNetworkName) {
        return physicalNetworks.get(physicalNetworkName);
    }
}
