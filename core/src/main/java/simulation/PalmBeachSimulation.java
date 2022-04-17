package simulation;

import agent.SimpleAgent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import environment.Environment;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import scheduler.Scheduler;
import simulation.exception.PalmBeachSimulationSingletonAlreadyCreateException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents the Simulation.
 * <p>
 * Use the Singleton pattern, a unique instance of {@code PalmBeachSimulation} is created with the method {@link
 * #setSingletonInstance(PalmBeachSimulation)}, after that, static methods can be used to manipulated and interact with the
 * {@code PalmBeachSimulation}
 */
@Slf4j
public class PalmBeachSimulation {

    // Static.

    private static PalmBeachSimulation palmBeachSimulation;

    // Variables.

    private final AtomicBoolean started = new AtomicBoolean(false);

    @NonNull
    private final SimulationSetup simulationSetup;

    @NonNull
    private final Scheduler scheduler;

    private final Scheduler.WaitingSchedulerEndObserver schedulerObserver;

    private final Map<String, Environment> environments;
    private final Map<SimpleAgent.AgentIdentifier, SimpleAgent> agents;
    private final Set<Controller> controllers;

    // Constructors.

    public PalmBeachSimulation(@NonNull SimulationSetup simulationSetup, @NonNull Scheduler scheduler,
                               Set<Environment> environments, Set<SimpleAgent> agents,
                               Set<Controller> controllers) {
        this.simulationSetup = simulationSetup;

        this.scheduler = scheduler;
        this.schedulerObserver = new Scheduler.WaitingSchedulerEndObserver();
        this.scheduler.addSchedulerObserver(this.schedulerObserver);

        this.environments = Maps.newConcurrentMap();
        fillEnvironments(environments);

        this.agents = Maps.newConcurrentMap();
        fillAgents(agents);

        this.controllers = controllers != null && !controllers.isEmpty() ? Sets.newHashSet(controllers) : new HashSet<>();
    }

    private void fillEnvironments(Set<Environment> environments) {
        if (environments != null) {
            for (Environment environment : environments) {
                Environment old = this.environments.putIfAbsent(environment.getName(), environment);
                if (old != null) {
                    log.error("Cannot add the Environment {} in Simulation because an Environment has already been added with the name {}",
                              environment,
                              environment.getName());
                } else
                    log.info("Environment {} added in Simulation", environment);
            }
        }
    }

    private void fillAgents(Set<SimpleAgent> agents) {
        if (agents != null) {
            for (SimpleAgent agent : agents) {
                SimpleAgent old = this.agents.putIfAbsent(agent.getIdentifier(), agent);
                if (old != null) {
                    log.error("cannot add the SimpleAgent {} in Simulation because an SimpleAgent has already been added with the identifier {}",
                              agent,
                              agent.getIdentifier());
                } else
                    log.info("Agent {} added in Simulation", agent);
            }
        }
    }

    // Methods.

    /**
     * Set the {@link PalmBeachSimulation} singleton instance to the specified instance, only if the singleton has not already been set.
     *
     * @param simulation instance of the simulation
     */
    public static void setSingletonInstance(@NonNull PalmBeachSimulation simulation) {
        if (palmBeachSimulation == null) {
            palmBeachSimulation = simulation;
        } else
            throw new PalmBeachSimulationSingletonAlreadyCreateException();
    }

    /**
     * Start the {@link PalmBeachSimulation}, if the simulation has already been started, do nothing.
     */
    public static void start() {
        if (palmBeachSimulation.started.compareAndSet(false, true)) {
            scheduleControllers();
            setupSimulation();
            startScheduler();
        } else
            log.error("Already started PalmBeachSimulation");
    }

    private static void scheduleControllers() {
        for (Controller controller : palmBeachSimulation.controllers) {
            palmBeachSimulation.scheduler.scheduleExecutable(controller, controller.getScheduleTime(), controller.getScheduleMode(),
                                                             controller.getRepetitions(),
                                                             controller.getExecutionsStep());
            log.info("Controller {} has been scheduled", controller);
        }
    }

    private static void setupSimulation() {
        log.info("Setup simulation with {}", palmBeachSimulation.simulationSetup);
        palmBeachSimulation.simulationSetup.setupSimulation();
    }

    private static void startScheduler() {
        log.info("Start scheduler");
        palmBeachSimulation.scheduler.start();
    }

    public static void waitSimulationEnd(long timeout) throws InterruptedException {
        while (!palmBeachSimulation.scheduler.isKilled()) {
            palmBeachSimulation.schedulerObserver.waitSchedulerEnd(timeout);
        }
    }

    // Simulation methods.

    /**
     * <strong>WARNING!</strong> Very dangerous method, only use for UT and clear the singleton, however, there is no protection of the current
     * Simulation. Therefore, if user calls clear, it must be aware that it can break the execution.
     */
    public static void clear() {
        palmBeachSimulation = null;
    }

    public static boolean isEnded() {
        return palmBeachSimulation.scheduler.isKilled();
    }

    public static Scheduler scheduler() {
        return palmBeachSimulation.scheduler;
    }

    /**
     * Add the {@link Environment} in the simulation. {@code Environment} are mapped with their name, therefore, if there already is a {@code
     * Environment} added with the same name, the specified {@code Environment} will not be added and the method will return false.
     *
     * @param environment the Environment to add
     *
     * @return true if the {@code Environment} has been added, else false.
     *
     * @throws NullPointerException if the environment is null
     */
    public static boolean addEnvironment(@NonNull Environment environment) {
        Environment old = palmBeachSimulation.environments.putIfAbsent(environment.getName(), environment);
        if (old != null) {
            log.error("Cannot add Environment {} because already added Environment with the name {}", environment, environment.getName());
            return false;
        }

        log.info("Environment {} added in the Simulation", environment);
        return true;
    }

    public static Environment getEnvironment(String environmentName) {
        return palmBeachSimulation.environments.get(environmentName);
    }

    public static List<Environment> allEnvironments() {
        return Lists.newArrayList(palmBeachSimulation.environments.values());
    }

    /**
     * Add the {@link SimpleAgent} in the simulation. Just add it, does not manage if it is evolving or not in some {@link Environment}.
     * <p>
     * {@code SimpleAgent} are mapped with their {@link agent.SimpleAgent.AgentIdentifier}. Therefore, if there already is a {@code SimpleAgent} with
     * the same {@code AgentIdentifier} in the Simulation, the specified {@code SimpleAgent} will not be added in the Simulation and the method will
     * return false.
     *
     * @param agent to add
     *
     * @return if the {@code SimpleAgent} has been added, else false.
     *
     * @throws NullPointerException if agent is null
     */
    public static boolean addAgent(@NonNull SimpleAgent agent) {
        SimpleAgent old = palmBeachSimulation.agents.putIfAbsent(agent.getIdentifier(), agent);
        if (old != null) {
            log.error("Cannot add agent {} because already added agent with the identifier {}", agent, agent.getIdentifier());
            return false;
        }

        log.info("Agent {} added in the simulation", agent);
        return true;
    }

    public static SimpleAgent getAgent(SimpleAgent.AgentIdentifier agentIdentifier) {
        return palmBeachSimulation.agents.get(agentIdentifier);
    }

    public static List<SimpleAgent> allAgents() {
        return Lists.newArrayList(palmBeachSimulation.agents.values());
    }
}
