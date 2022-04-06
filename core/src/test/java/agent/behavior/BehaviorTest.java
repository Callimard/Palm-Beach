package agent.behavior;

import agent.SimpleAgent;
import agent.behavior.Behavior;
import common.Context;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import junit.PalmBeachTest;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("Behavior tests")
@Tag("Behavior")
@PalmBeachTest
public class BehaviorTest {

    @Nested
    @DisplayName("Behavior constructor")
    class Constructor {

        @Nested
        @DisplayName("Behavior(SimpleAgent, Context)")
        class PrimaryConstructor {

            @Test
            @DisplayName("Throws NullPointerException if agent is null")
            void withNullAgent(@Mock Context context) {
                //noinspection ConstantConditions
                assertThrows(NullPointerException.class, () -> new BasicBehavior(null, context));
            }

            @Test
            @DisplayName("Does not throw exception if the context is null and create an empty not null context")
            void withNullContext(@Mock SimpleAgent agent) {
                AtomicReference<Behavior> behavior = new AtomicReference<>();
                assertDoesNotThrow(() -> behavior.set(new BasicBehavior(agent, null)));

                assertThat(behavior.get().getContext()).isNotNull();
                assertThat(behavior.get().getContext().isEmpty()).isTrue();
            }
        }

        @Nested
        @DisplayName("Behavior(SimpleAgent)")
        class SecondaryConstructor {

            @Test
            @DisplayName("Throws NullPointerException if agent is null")
            void withNullAgent() {
                //noinspection ConstantConditions
                assertThrows(NullPointerException.class, () -> new BasicBehavior(null));
            }

            @Test
            @DisplayName("Does not throw exception if agent is not null")
            void withNotNullAgent(@Mock SimpleAgent agent) {
                AtomicReference<Behavior> behavior = new AtomicReference<>();
                assertDoesNotThrow(() -> behavior.set(new BasicBehavior(agent)));

                assertThat(behavior.get().getContext()).isNotNull();
                assertThat(behavior.get().getContext().isEmpty()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Static instantiateBehavior()")
    @Tag("instantiateBehavior")
    class InstantiateBehavior {

        @Test
        @DisplayName("instantiateBehavior() throws Exception with non correct Behavior classes")
        void nonCorrectBehaviorClasses(@Mock SimpleAgent agent) {
            assertThrows(Exception.class, () -> Behavior.instantiateBehavior(NoCorrectConstructorBehavior.class, agent));
            assertThrows(Exception.class, () -> Behavior.instantiateBehavior(WrongConstructorVisibilityBehavior.class, agent));
            assertThrows(Exception.class, () -> Behavior.instantiateBehavior(ThrowExceptionConstructorBehavior.class, agent));
        }

        @Test
        @DisplayName("instantiateBehavior() create an instance with a correct Behavior class")
        void withCorrectBehaviorClass(@Mock SimpleAgent agent) {
            AtomicReference<Behavior> behavior = new AtomicReference<>();

            assertDoesNotThrow(() -> behavior.set(Behavior.instantiateBehavior(BasicBehavior.class, agent)));
            assertThat(behavior.get()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Behavior play()")
    @Tag("play")
    class Play {

        @Test
        @DisplayName("play() does not throws exception if the Behavior is not already played")
        void notAlreadyPlayed(@Mock SimpleAgent agent) {
            Behavior b = new BasicBehavior(agent);

            assertDoesNotThrow(b::play);
        }

        @Test
        @DisplayName("play() does not throws exception even if the Behavior is already played")
        void alreadyPlayed(@Mock SimpleAgent agent) {
            Behavior b = new BasicBehavior(agent);
            b.play();

            assertDoesNotThrow(b::play);
        }
    }

    @Nested
    @DisplayName("Behavior stopPlay()")
    @Tag("stopPlay")
    class StopPlay {

        @Test
        @DisplayName("stopPlay() does not throws exception if the Behavior is played")
        void notAlreadyStopped(@Mock SimpleAgent agent) {
            Behavior b = new BasicBehavior(agent);
            b.play();

            assertDoesNotThrow(b::stopPlay);
        }

        @Test
        @DisplayName("stopPlay() does not throws exception even if the Behavior is already stopped")
        void alreadyStopped(@Mock SimpleAgent agent) {
            Behavior b = new BasicBehavior(agent);

            assertDoesNotThrow(b::stopPlay);
        }
    }

    @Nested
    @DisplayName("Behavior getAgent()")
    @Tag("getAgent")
    class GetAgent {

        @Test
        @DisplayName("getAgent() never returns null")
        void neverReturnsNull(@Mock SimpleAgent agent) {
            Behavior b = new BasicBehavior(agent);

            assertThat(b.getAgent()).isNotNull().isSameAs(agent);
        }
    }

    @Nested
    @DisplayName("Behavior getContext()")
    @Tag("getContext")
    class GetContext {

        @Test
        @DisplayName("getContext() never returns null")
        void neverReturnsNull(@Mock SimpleAgent agent) {
            Behavior b = new BasicBehavior(agent);

            assertThat(b.getContext()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Behavior toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull(@Mock SimpleAgent agent) {
            Behavior b = new BasicBehavior(agent);

            assertThat(b.toString()).isNotNull();
        }
    }

    // Inner classes.

    public static class BasicBehavior extends Behavior {

        public BasicBehavior(@NonNull SimpleAgent agent) {
            super(agent);
        }

        public BasicBehavior(@NonNull SimpleAgent agent, Context context) {
            super(agent, context);
        }

        @Override
        protected void beginToBePlayed() {
            // Nothing
        }

        @Override
        protected void stopToBePlayed() {
            // Nothing
        }
    }

    public static class CorrectConstructorBehavior extends BasicBehavior {

        public CorrectConstructorBehavior(@NonNull SimpleAgent agent) {
            super(agent);
        }
    }

    public static class NoCorrectConstructorBehavior extends BasicBehavior {

        public NoCorrectConstructorBehavior(@NonNull SimpleAgent agent, @SuppressWarnings("unused") String toMuchArgs) {
            super(agent);
        }
    }

    public static class WrongConstructorVisibilityBehavior extends BasicBehavior {

        protected WrongConstructorVisibilityBehavior(@NonNull SimpleAgent agent) {
            super(agent);
        }
    }

    public static class ThrowExceptionConstructorBehavior extends BasicBehavior {

        public ThrowExceptionConstructorBehavior(@NonNull SimpleAgent agent) {
            super(agent);
            throw new RuntimeException();
        }
    }

}
