package org.paradise.palmbeach.basic.network;

import org.paradise.palmbeach.core.agent.SimpleAgent;
import com.google.common.collect.Sets;
import org.paradise.palmbeach.utils.context.Context;
import org.paradise.palmbeach.core.environment.Environment;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class FullyConnectedNetwork extends NetworkWithDelay {

    // Constructors.

    public FullyConnectedNetwork(@NonNull String name, @NonNull Environment environment, Context context) {
        super(name, environment, context);
    }

    // Methods.

    @Override
    public void environmentAddAgent(SimpleAgent.AgentIdentifier addedAgent) {
        // Nothing
    }

    @Override
    public void environmentRemoveAgent(SimpleAgent.AgentIdentifier removedAgent) {
        // Nothing
    }

    /**
     * Only verify if both {@link SimpleAgent} are evolving in the {@link Environment} of the {@link FullyConnectedNetwork}.
     *
     * @param source the source agent
     * @param target the target agent
     *
     * @return true if both agent are in the environment, else false.
     */
    @Override
    public boolean hasConnection(SimpleAgent.@NonNull AgentIdentifier source, SimpleAgent.@NonNull AgentIdentifier target) {
        return getEnvironment().agentIsEvolving(source) && getEnvironment().agentIsEvolving(target);
    }

    @Override
    public Set<SimpleAgent.AgentIdentifier> directNeighbors(SimpleAgent.@NonNull AgentIdentifier agent) {
        if (getEnvironment().agentIsEvolving(agent))
            return getEnvironment().evolvingAgents();
        else
            throw new NotInNetworkException("Agent " + agent + " is not in the Network " + this);
    }

    @Override
    public Set<Connection> allConnections() {
        Set<SimpleAgent.AgentIdentifier> alreadySeen = Sets.newHashSet();
        Set<Connection> allConnections = Sets.newHashSet();
        for (SimpleAgent.AgentIdentifier a0 : getEnvironment().evolvingAgents()) {
            for (SimpleAgent.AgentIdentifier a1 : getEnvironment().evolvingAgents()) {
                if (!alreadySeen.contains(a1)) {
                    Connection connection = new NonOrientedConnection(a0, a1);
                    allConnections.add(connection);
                }
            }
            alreadySeen.add(a0);
        }
        return allConnections;
    }
}
