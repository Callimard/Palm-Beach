package org.palmbeach.core.agent.behavior;

import org.palmbeach.core.agent.SimpleAgent;
import org.palmbeach.core.agent.behavior.Behavior;
import org.palmbeach.core.common.Context;
import org.palmbeach.core.junit.PalmBeachTest;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

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
    }

    @Nested
    @DisplayName("Static instantiateBehavior()")
    @Tag("instantiateBehavior")
    class InstantiateBehavior {

        @Test
        @DisplayName("instantiateBehavior() throws Exception with non correct Behavior classes")
        void nonCorrectBehaviorClasses(@Mock SimpleAgent agent) {
            assertThrows(Exception.class, () -> Behavior.instantiateBehavior(NoCorrectConstructorBehavior.class, agent, null));
            assertThrows(Exception.class, () -> Behavior.instantiateBehavior(WrongConstructorVisibilityBehavior.class, agent, null));
            assertThrows(Exception.class, () -> Behavior.instantiateBehavior(ThrowExceptionConstructorBehavior.class, agent, null));
        }

        @Test
        @DisplayName("instantiateBehavior() create an instance with a correct Behavior class")
        void withCorrectBehaviorClass(@Mock SimpleAgent agent) {
            AtomicReference<Behavior> behavior = new AtomicReference<>();

            assertDoesNotThrow(() -> behavior.set(Behavior.instantiateBehavior(BasicBehavior.class, agent, null)));
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
            Behavior b = new BasicBehavior(agent, null);

            assertDoesNotThrow(b::play);
        }

        @Test
        @DisplayName("play() does not throws exception even if the Behavior is already played")
        void alreadyPlayed(@Mock SimpleAgent agent) {
            Behavior b = new BasicBehavior(agent, null);
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
            Behavior b = new BasicBehavior(agent, null);
            b.play();

            assertDoesNotThrow(b::stopPlay);
        }

        @Test
        @DisplayName("stopPlay() does not throws exception even if the Behavior is already stopped")
        void alreadyStopped(@Mock SimpleAgent agent) {
            Behavior b = new BasicBehavior(agent, null);

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
            Behavior b = new BasicBehavior(agent, null);

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
            Behavior b = new BasicBehavior(agent, null);

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
            Behavior b = new BasicBehavior(agent, null);

            assertThat(b.toString()).isNotNull();
        }
    }

    // Inner classes.

    public static class BasicBehavior extends Behavior {

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

        public CorrectConstructorBehavior(@NonNull SimpleAgent agent, Context context) {
            super(agent, context);
        }
    }

    public static class NoCorrectConstructorBehavior extends BasicBehavior {

        public NoCorrectConstructorBehavior(@NonNull SimpleAgent agent, @SuppressWarnings("unused") String toMuchArgs) {
            super(agent, null);
        }
    }

    public static class WrongConstructorVisibilityBehavior extends BasicBehavior {

        protected WrongConstructorVisibilityBehavior(@NonNull SimpleAgent agent, Context context) {
            super(agent, context);
        }
    }

    public static class ThrowExceptionConstructorBehavior extends BasicBehavior {

        public ThrowExceptionConstructorBehavior(@NonNull SimpleAgent agent, Context context) {
            super(agent, context);
            throw new RuntimeException();
        }
    }

}
