package simulation;

import agent.SimpleAgent;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import environment.Environment;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import scheduler.Scheduler;
import simulation.exception.PalmBeachSimulationSingletonAlreadyCreateException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@ToString
@Slf4j
public class PalmBeachSimulation {

    // Constants.

    private static final long WAITING_END_TIMEOUT = 500L;

    // Static.

    private static PalmBeachSimulation palmBeachSimulation;

    // Variables.

    private final AtomicBoolean started = new AtomicBoolean(false);

    @NonNull
    private final SimulationSetup simulationSetup;

    @NonNull
    private final Scheduler scheduler;

    @ToString.Exclude
    private final Scheduler.WaitingSchedulerEndObserver schedulerObserver;

    private final Map<String, Environment> environments;
    private final Map<SimpleAgent.AgentIdentifier, SimpleAgent> agents;
    private final Set<Controller> controllers;

    // Constructors.

    private PalmBeachSimulation(@NonNull SimulationSetup simulationSetup, @NonNull Scheduler scheduler,
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
                    log.error("Cannot add the Environment {} because an Environment has already been added with the name {}", environment,
                              environment.getName());
                } else
                    log.info("Environment {} added", environment);
            }
        }
    }

    private void fillAgents(Set<SimpleAgent> agents) {
        if (agents != null) {
            for (SimpleAgent agent : agents) {
                SimpleAgent old = this.agents.putIfAbsent(agent.getIdentifier(), agent);
                if (old != null) {
                    log.error("cannot add the SimpleAgent {} because an SimpleAgent has already been added with the identifier {}", agent,
                              agent.getIdentifier());
                } else
                    log.info("Agent {} added", agent);
            }
        }
    }

    // Methods.

    public static PalmBeachSimulation generateSingletonPalmBeachSimulation(@NonNull SimulationSetup simulationSetup, @NonNull Scheduler scheduler,
                                                                           Set<Environment> environments, Set<SimpleAgent> agents,
                                                                           Set<Controller> controllers) {
        if (palmBeachSimulation == null) {
            palmBeachSimulation = new PalmBeachSimulation(simulationSetup, scheduler, environments, agents, controllers);
            return palmBeachSimulation;
        } else
            throw new PalmBeachSimulationSingletonAlreadyCreateException();
    }

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

    public static void waitSimulationEnd() throws InterruptedException {
        while (!palmBeachSimulation.scheduler.isKilled()) {
            palmBeachSimulation.schedulerObserver.waitSchedulerEnd(WAITING_END_TIMEOUT);
        }
    }

    // Simulation methods.

    public static Scheduler scheduler() {
        return palmBeachSimulation.scheduler;
    }

    public static boolean addEnvironment(Environment environment) {
        Environment old = palmBeachSimulation.environments.putIfAbsent(environment.getName(), environment);
        if (old != null) {
            log.error("Cannot add Environment {} because already added Environment with the name {}", environment, environment.getName());
        }

        log.info("Environment {} added in the Simulation", environment);
        return false;
    }

    public static Environment getEnvironment(String environmentName) {
        return palmBeachSimulation.environments.get(environmentName);
    }

    /**
     * Add the {@link SimpleAgent} in the simulation. Just add it, does not manage if is evolving or not in some {@link Environment} or not.
     *
     * @param agent to add
     *
     * @return if the {@code SimpleAgent} has been added, else false.
     */
    public static boolean addAgent(SimpleAgent agent) {
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
}
