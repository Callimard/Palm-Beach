package simulation.configuration;

import agent.SimpleAgent;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import environment.Environment;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import scheduler.Scheduler;
import scheduler.SimpleScheduler;
import scheduler.executor.Executor;
import scheduler.executor.multithread.MultiThreadExecutor;
import simulation.Controller;
import simulation.PalmBeachSimulation;
import simulation.SimulationSetup;
import simulation.configuration.exception.GenerationFailedException;
import simulation.configuration.exception.WrongAgentConfigurationException;
import simulation.configuration.exception.WrongControllerConfigurationException;
import simulation.configuration.exception.WrongSimulationConfigurationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static common.Tools.extractClass;

/**
 * Configuration for {@link PalmBeachSimulation}.
 * <p>
 * Example of complete {@code PalmBeachSimulation} configuration:
 * <pre>
 * # Simulation
 *
 * simulation.threads=4
 * simulation.max-duration=1500
 * simulation.setup-class=simulation.SimulationSetup
 *
 * # Controllers
 *
 * controller.myController.class=simulation.MyController
 * controller.myController.schedule-mode=REPEATEDLY
 * controller.myController.schedule-time=1
 * controller.myController.executions-step=50
 * controller.myController.repetitions=5
 * controller.myController.custom-property=custom-value
 *
 * # Physical Networks
 *
 * physical-network.fullyConnected.class=environment.physical.PhysicalNetwork
 * physical-network.fullyConnected.context.class=context.CustomContext
 * physical-network.fullyConnected.context.key1="value1"
 * physical-network.fullyConnected.context.key2="value2"
 *
 * # Environments
 *
 * environment.simpleEnvironment.class=environment.SimpleEnvironment
 * environment.simpleEnvironment.context.class=context.CustomContext
 * environment.simpleEnvironment.physicalNetworks=[fullyConnected]
 * environment.simpleEnvironment.context.key1="value1"
 * environment.simpleEnvironment.context.key2="value2"
 *
 * # Protocols
 *
 * protocol.transport.class=protocol.TransportProtocol
 * protocol.transport.context.class=context.CustomContext
 * protocol.transport.context.key1="value1"
 * protocol.transport.context.key2="value2"
 *
 * protocol.tendermint.class=protocol.TendermintProtocol
 * protocol.tendermint.context.class=context.CustomContext
 *
 * protocol.hackProtocol.class=protocol.HackProtocol
 *
 * # Behaviors
 *
 * behavior.correctBehavior.class=behavior.CorrectBehavior
 * behavior.correctBehavior.context.class=context.CustomContext
 * behavior.correctBehavior.context.key1="value1"
 * behavior.correctBehavior.context.key2="value2"
 *
 * behavior.byzantineBehavior.class=behavior.ByzantineBehavior
 *
 * # Agents
 *
 * agent.correctAgent.name-pattern="CorrectAgent %d"
 * agent.correctAgent.number=15
 * agent.correctAgent.context.class=context.CustomContext
 * agent.correctAgent.context.key1="value1"
 * agent.correctAgent.context.key2="value2"
 * agent.correctAgent.environments=[simpleEnvironment]
 * agent.correctAgent.protocols=[transport, tendermint]
 * agent.correctAgent.behaviors=[correctBehavior]
 *
 * agent.byzantineAgent.class=agent.ByzantineAgent
 * agent.byzantineAgent.name-pattern="ByzantineAgent %d"
 * agent.byzantineAgent.number=3
 * agent.byzantineAgent.environments=[simpleEnvironment]
 * agent.byzantineAgent.protocols=[transport, tendermint, hackProtocol]
 * agent.byzantineAgent.behaviors=[byzantineBehavior]
 * </pre>
 */
@Getter
@ToString
@Slf4j
public class SimulationConfiguration extends PalmBeachConfiguration<PalmBeachSimulation> {

    // Constants.

    public static final String DEFAULT_SIMULATION_CONFIG_NAME = "simulation";

    public static final String THREADS_PROPERTY = "threads";
    public static final String MAX_DURATION_PROPERTY = "max-duration";
    public static final String SETUP_CLASS_PROPERTY = "setup-class";

    public static final String SIMULATION_PROPERTY = "simulation";
    public static final String CONTROLLER_PROPERTY = "controller";
    public static final String PHYSICAL_NETWORK_PROPERTY = "physical-network";
    public static final String ENVIRONMENT_PROPERTY = "environment";
    public static final String PROTOCOL_PROPERTY = "protocol";
    public static final String BEHAVIOR_PROPERTY = "behavior";
    public static final String AGENT_PROPERTY = "agent";

    // Variables.

    private final int threads;
    private final int maxDuration;
    private final String setupClass;

    private final Set<ControllerConfiguration> controllers;
    private final Map<String, PhysicalNetworkConfiguration> physicalNetworks;
    private final Set<EnvironmentConfiguration> environments;
    private final Map<String, ProtocolConfiguration> protocols;
    private final Map<String, BehaviorConfiguration> behaviors;
    private final Set<AgentConfiguration> agents;

    // Constructors.

    public SimulationConfiguration(@NonNull Config baseConfig) throws WrongSimulationConfigurationException {
        super(baseConfig);

        Config simulationConfig = getBaseConfig().getConfig(SIMULATION_PROPERTY);
        this.threads = simulationConfig.getInt(THREADS_PROPERTY);
        if (this.threads < 1)
            throw new WrongSimulationConfigurationException("Threads cannot be less than 1");

        this.maxDuration = simulationConfig.getInt(MAX_DURATION_PROPERTY);
        if (this.maxDuration < 1)
            throw new WrongSimulationConfigurationException("Max duration cannot be less than 1");

        this.setupClass = simulationConfig.getString(SETUP_CLASS_PROPERTY);

        try {
            this.controllers = new HashSet<>();
            parseControllersConfiguration();

            this.physicalNetworks = new HashMap<>();
            parsePhysicalNetworksConfiguration();

            this.environments = new HashSet<>();
            parseEnvironmentsConfiguration();

            this.protocols = new HashMap<>();
            parseProtocolsConfiguration();

            this.behaviors = new HashMap<>();
            parseBehaviorsConfiguration();

            this.agents = new HashSet<>();
            parseAgentsConfiguration();
        } catch (Exception e) {
            throw new WrongSimulationConfigurationException("Fail to create Simulation configuration", e);
        }
    }

    private void parseControllersConfiguration() throws WrongControllerConfigurationException {
        if (getBaseConfig().hasPath(CONTROLLER_PROPERTY)) {
            ConfigObject controllerConfigObject = getBaseConfig().getObject(CONTROLLER_PROPERTY);
            for (String controllerIdentifier : controllerConfigObject.keySet()) {
                this.controllers.add(new ControllerConfiguration(controllerConfigObject.toConfig().getConfig(controllerIdentifier)));
            }
        }
    }

    private void parsePhysicalNetworksConfiguration() {
        if (getBaseConfig().hasPath(PHYSICAL_NETWORK_PROPERTY)) {
            ConfigObject pNConfigObject = getBaseConfig().getObject(PHYSICAL_NETWORK_PROPERTY);
            for (String pNIdentifier : pNConfigObject.keySet()) {
                this.physicalNetworks.put(pNIdentifier,
                                          new PhysicalNetworkConfiguration(pNIdentifier, pNConfigObject.toConfig().getConfig(pNIdentifier)));
            }
        }
    }

    private void parseEnvironmentsConfiguration() {
        if (getBaseConfig().hasPath(ENVIRONMENT_PROPERTY)) {
            ConfigObject environmentConfigObject = getBaseConfig().getObject(ENVIRONMENT_PROPERTY);
            for (String environmentIdentifier : environmentConfigObject.keySet()) {
                this.environments.add(new EnvironmentConfiguration(environmentIdentifier,
                                                                   environmentConfigObject.toConfig().getConfig(environmentIdentifier)));
            }
        }
    }

    private void parseProtocolsConfiguration() {
        if (getBaseConfig().hasPath(PROTOCOL_PROPERTY)) {
            ConfigObject protocolConfigObject = getBaseConfig().getObject(PROTOCOL_PROPERTY);
            for (String protocolIdentifier : protocolConfigObject.keySet()) {
                this.protocols.put(protocolIdentifier, new ProtocolConfiguration(protocolConfigObject.toConfig().getConfig(protocolIdentifier)));
            }
        }
    }

    private void parseBehaviorsConfiguration() {
        if (getBaseConfig().hasPath(BEHAVIOR_PROPERTY)) {
            ConfigObject behaviorConfigObject = getBaseConfig().getObject(BEHAVIOR_PROPERTY);
            for (String behaviorIdentifier : behaviorConfigObject.keySet()) {
                this.behaviors.put(behaviorIdentifier, new BehaviorConfiguration(behaviorConfigObject.toConfig().getConfig(behaviorIdentifier)));
            }
        }
    }

    private void parseAgentsConfiguration() throws WrongAgentConfigurationException {
        if (getBaseConfig().hasPath(AGENT_PROPERTY)) {
            ConfigObject agentConfigObject = getBaseConfig().getObject(AGENT_PROPERTY);
            for (String agentIdentifier : agentConfigObject.keySet()) {
                this.agents.add(new AgentConfiguration(agentConfigObject.toConfig().getConfig(agentIdentifier)));
            }
        }
    }

    // Methods.

    @Override
    public PalmBeachSimulation generate() throws GenerationFailedException {
        try {
            Executor executor = new MultiThreadExecutor(threads);
            Scheduler scheduler = new SimpleScheduler(maxDuration, executor);
            SimulationSetup simulationSetup = SimulationSetup.initiateSimulationSetup(extractClass(setupClass));
            Set<Controller> allControllers = generateAllControllers();
            Map<String, Environment> allEnvironments = generateAllEnvironments();
            Set<SimpleAgent> allAgents = generateAllAgents(allEnvironments);
            return new PalmBeachSimulation(simulationSetup, scheduler, new HashSet<>(allEnvironments.values()), allAgents, allControllers);
        } catch (Exception e) {
            throw new GenerationFailedException("Cannot generate PalmBeachSimulation from configuration " + this, e);
        }
    }

    private Set<Controller> generateAllControllers() throws GenerationFailedException {
        Set<Controller> allControllers = new HashSet<>();
        for (ControllerConfiguration controllerConfiguration : controllers) {
            allControllers.add(controllerConfiguration.generate());
        }
        return allControllers;
    }

    private Map<String, Environment> generateAllEnvironments() throws GenerationFailedException {
        Map<String, Environment> allEnvironments = new HashMap<>();
        for (EnvironmentConfiguration environmentConfiguration : environments) {
            Environment environment = environmentConfiguration.generate();
            allEnvironments.put(environmentConfiguration.getEnvironmentName(), environment);
            addEnvironmentPhysicalNetwork(environmentConfiguration, environment);
        }
        return allEnvironments;
    }

    private void addEnvironmentPhysicalNetwork(EnvironmentConfiguration environmentConfiguration, Environment environment)
            throws GenerationFailedException {
        for (String pNIdentifier : environmentConfiguration.getPhysicalNetworks()) {
            PhysicalNetworkConfiguration pNConfiguration = physicalNetworks.get(pNIdentifier);
            if (pNConfiguration != null) {
                environment.addPhysicalNetwork(pNConfiguration.generate());
            } else
                log.error("No PhysicalNetwork identified by {} find in the configuration", pNIdentifier);
        }
    }

    private Set<SimpleAgent> generateAllAgents(Map<String, Environment> allEnvironments) throws GenerationFailedException {
        Set<SimpleAgent> allAgents = new HashSet<>();
        for (AgentConfiguration agentConfiguration : agents) {
            Set<SimpleAgent> generatedAgents = agentConfiguration.generate();
            addProtocolsAndBehaviors(agentConfiguration, generatedAgents);
            addAgentsInEnvironments(allEnvironments, agentConfiguration, generatedAgents);
            allAgents.addAll(generatedAgents);
        }
        return allAgents;
    }

    private void addProtocolsAndBehaviors(AgentConfiguration agentConfiguration, Set<SimpleAgent> generatedAgents) {
        for (SimpleAgent agent : generatedAgents) {
            addProtocols(agentConfiguration, agent);
            addBehaviors(agentConfiguration, agent);
        }
    }

    private void addProtocols(AgentConfiguration agentConfiguration, SimpleAgent agent) {
        for (String protocolIdentifier : agentConfiguration.getProtocols()) {
            ProtocolConfiguration protocolConfiguration = protocols.get(protocolIdentifier);
            if (protocolConfiguration != null) {
                agent.addProtocol(protocolConfiguration);
            } else {
                log.error("No Protocol identified by {} find in the configuration", protocolIdentifier);
            }
        }
    }

    private void addBehaviors(AgentConfiguration agentConfiguration, SimpleAgent agent) {
        for (String behaviorIdentifier : agentConfiguration.getBehaviors()) {
            BehaviorConfiguration behaviorConfiguration = behaviors.get(behaviorIdentifier);
            if (behaviorConfiguration != null) {
                agent.addBehavior(behaviorConfiguration);
            } else {
                log.error("No Behavior identified by {} find in the configuration", behaviorIdentifier);
            }
        }
    }

    private void addAgentsInEnvironments(Map<String, Environment> allEnvironments, AgentConfiguration agentConfiguration,
                                         Set<SimpleAgent> generatedAgents) {
        for (SimpleAgent agent : generatedAgents) {
            for (String environmentIdentifier : agentConfiguration.getEnvironments()) {
                Environment environment = allEnvironments.get(environmentIdentifier);
                if (environment != null) {
                    environment.addAgent(agent.getIdentifier());
                } else {
                    log.error("Cannot find Environment identified by {} in the Simulation configuration", environmentIdentifier);
                }
            }
        }
    }
}
