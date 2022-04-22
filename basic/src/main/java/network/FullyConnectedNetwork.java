package network;

import agent.SimpleAgent;
import common.Context;
import environment.Environment;
import lombok.NonNull;

import java.util.Set;

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
    public Set<SimpleAgent.AgentIdentifier> agentDirectConnections(SimpleAgent.@NonNull AgentIdentifier agent) {
        if (getEnvironment().agentIsEvolving(agent))
            return getEnvironment().evolvingAgents();
        else
            throw new NotInNetworkException("Agent " + agent + " is not in the Network " + this);
    }
}
