package simulation.configuration;

import agent.SimpleAgent;
import agent.protocol.Protocol;
import com.google.common.collect.Maps;
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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static common.Tools.extractClass;
import static java.util.Locale.ENGLISH;

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
 * network.fullyConnected.class=environment.physical.Network
 * network.fullyConnected.context.class=context.CustomContext
 * network.fullyConnected.context.key1="value1"
 * network.fullyConnected.context.key2="value2"
 *
 * # Environments
 *
 * environment.simpleEnvironment.class=environment.SimpleEnvironment
 * environment.simpleEnvironment.context.class=context.CustomContext
 * environment.simpleEnvironment.Networks=[fullyConnected]
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
    public static final String PHYSICAL_NETWORK_PROPERTY = "network";
    public static final String ENVIRONMENT_PROPERTY = "environment";
    public static final String PROTOCOL_PROPERTY = "protocol";
    public static final String BEHAVIOR_PROPERTY = "behavior";
    public static final String AGENT_PROPERTY = "agent";

    // Variables.

    private final int threads;
    private final int maxDuration;
    private final String setupClass;

    private final Set<ControllerConfiguration> controllers;
    private final Map<String, NetworkConfiguration> networks;
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

            this.networks = new HashMap<>();
            parseNetworksConfiguration();

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

    private void parseNetworksConfiguration() {
        if (getBaseConfig().hasPath(PHYSICAL_NETWORK_PROPERTY)) {
            ConfigObject pNConfigObject = getBaseConfig().getObject(PHYSICAL_NETWORK_PROPERTY);
            for (String pNIdentifier : pNConfigObject.keySet()) {
                this.networks.put(pNIdentifier,
                                  new NetworkConfiguration(pNIdentifier, pNConfigObject.toConfig().getConfig(pNIdentifier)));
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
            addEnvironmentNetwork(environmentConfiguration, environment);
        }
        return allEnvironments;
    }

    private void addEnvironmentNetwork(EnvironmentConfiguration environmentConfiguration, Environment environment)
            throws GenerationFailedException {
        for (String pNIdentifier : environmentConfiguration.getNetworks()) {
            NetworkConfiguration pNConfiguration = networks.get(pNIdentifier);
            if (pNConfiguration != null) {
                environment.addNetwork(pNConfiguration.generateNetwork(environment));
            } else
                log.error("No Network identified by {} find in the configuration", pNIdentifier);
        }
    }

    private Set<SimpleAgent> generateAllAgents(Map<String, Environment> allEnvironments) throws GenerationFailedException {
        Set<SimpleAgent> allAgents = new HashSet<>();
        for (AgentConfiguration agentConfiguration : agents) {
            Set<SimpleAgent> generatedAgents = agentConfiguration.generate();
            for (SimpleAgent agent : generatedAgents) {
                addProtocols(agentConfiguration, agent);
                addBehaviors(agentConfiguration, agent);
                addInEnvironments(allEnvironments, agentConfiguration, agent);
            }
            allAgents.addAll(generatedAgents);
        }
        return allAgents;
    }

    private void addProtocols(AgentConfiguration agentConfiguration, SimpleAgent agent) throws GenerationFailedException {
        Map<String, Protocol> agentProtocolContext = createAndAddProtocols(agentConfiguration, agent);
        associateProtocolDependencies(agentConfiguration, agentProtocolContext);
    }

    private Map<String, Protocol> createAndAddProtocols(AgentConfiguration agentConfiguration, SimpleAgent agent)
            throws GenerationFailedException {
        Map<String, Protocol> agentProtocolContext = Maps.newHashMap();

        for (String protocolIdentifier : agentConfiguration.getProtocols()) {
            ProtocolConfiguration protocolConfiguration = protocols.get(protocolIdentifier);
            Protocol protocol = protocolConfiguration.generateProtocol(agent);
            agentProtocolContext.put(protocolIdentifier, protocol);
            agent.addProtocol(protocol);
        }

        return agentProtocolContext;
    }

    private void associateProtocolDependencies(AgentConfiguration agentConfiguration, Map<String, Protocol> agentProtocolContext)
            throws GenerationFailedException {
        for (String protocolIdentifier : agentConfiguration.getProtocols()) {
            ProtocolConfiguration protocolConfiguration = protocols.get(protocolIdentifier);
            Protocol protocol = agentProtocolContext.get(protocolIdentifier);
            setAllProtocolDependencies(protocol, protocolConfiguration, agentProtocolContext);
        }
    }

    private void setAllProtocolDependencies(Protocol protocol, ProtocolConfiguration protocolConfiguration,
                                            Map<String, Protocol> agentProtocolContext)
            throws GenerationFailedException {
        for (Map.Entry<String, String> dependency : protocolConfiguration.getProtocolDependencies().entrySet()) {
            String protocolDependencyName = dependency.getKey();
            String dependencyIdentifier = dependency.getValue();

            Protocol protocolDependency = agentProtocolContext.get(dependencyIdentifier);
            if (protocolDependency != null) {
                setProtocolDependency(protocol, protocolDependencyName, protocolDependency);
            } else
                log.error("Cannot find protocol with the identifier {}", dependencyIdentifier);
        }
    }

    private void setProtocolDependency(Protocol protocol, String protocolDependencyName, Protocol protocolDependency)
            throws GenerationFailedException {
        try {
            String setterName = "set" + protocolDependencyName.substring(0, 1).toUpperCase(ENGLISH) + protocolDependencyName.substring(1);
            PropertyDescriptor dependencyProperty = new PropertyDescriptor(protocolDependencyName, protocol.getClass(), null, setterName);
            dependencyProperty.getWriteMethod().invoke(protocol, protocolDependency);
            log.info("Set dependency {} to the protocol {}", protocolDependencyName, protocol);
        } catch (IntrospectionException e) {
            throw new GenerationFailedException(
                    "No field " + protocolDependencyName + " in the protocol " + protocol.getClass().getName(), e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new GenerationFailedException(
                    "Fail to set the protocol dependency " + protocolDependencyName + " of the protocol " + protocol.getClass().getName(),
                    e);
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

    private void addInEnvironments(Map<String, Environment> allEnvironments, AgentConfiguration agentConfiguration, SimpleAgent agent) {
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
