package physical;

import agent.SimpleAgent;
import common.Context;
import environment.Environment;
import environment.network.Network;
import event.Event;
import lombok.NonNull;

public class FullyConnectedNetwork extends Network {

    // Constants.

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
    public boolean hasConnection(SimpleAgent.AgentIdentifier source, SimpleAgent.AgentIdentifier target) {
        return getEnvironment().agentIsEvolving(source) && getEnvironment().agentIsEvolving(target);
    }

    @Override
    protected void simulateSending(SimpleAgent.AgentIdentifier source, SimpleAgent.AgentIdentifier target, Event<?> event) {
        // TODO
    }
}
