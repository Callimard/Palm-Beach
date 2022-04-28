package org.paradise.palmbeach.basic.messaging;

import org.paradise.palmbeach.core.agent.SimpleAgent;
import org.paradise.palmbeach.core.junit.PalmBeachTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Nested
@DisplayName("Message tests")
@Tag("Message")
@PalmBeachTest
public class MessageTest {

    @Nested
    @DisplayName("Message toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull(@Mock SimpleAgent.AgentIdentifier i0) {
            Message<String> msg = new Message<>(i0, "msg");
            assertThat(msg.toString()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Message equals() and hashCode()")
    @Tag("equalsAndHashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("equals() returns true with equal contents")
        void withEqualsContent(@Mock SimpleAgent.AgentIdentifier i0) {
            Message<String> msg0 = new Message<>(i0, "msg");
            Message<String> msg1 = new Message<>(i0, "msg");
            assertThat(msg0).isEqualTo(msg1);
        }

        @Test
        @DisplayName("hashCode() does not throw Exception")
        void doesNotThrowException(@Mock SimpleAgent.AgentIdentifier i0) {
            Message<String> msg0 = new Message<>(i0, "msg");
            assertDoesNotThrow(msg0::hashCode);
        }
    }

}
