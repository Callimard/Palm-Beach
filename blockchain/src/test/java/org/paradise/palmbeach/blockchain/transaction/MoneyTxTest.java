package org.paradise.palmbeach.blockchain.transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.paradise.palmbeach.utils.junit.ParadiseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("MoneyTx tests")
@Tag("MoneyTx")
@ParadiseTest
public class MoneyTxTest {

    @Nested
    @DisplayName("MoneyTx constructor()")
    @Tag("constructor")
    class Constructor {

        @ParameterizedTest
        @ValueSource(longs = {-13213L, -2131L, -31212325L, -1L})
        @DisplayName("constructor throws IllegalArgumentException if amount or timestamp is less than 1")
        void amountLessThanOne(long negative) {
            assertThrows(IllegalArgumentException.class, () -> new MoneyTx(1, "Sender", "Receiver", negative));
            assertThrows(IllegalArgumentException.class, () -> new MoneyTx(negative, "Sender", "Receiver", 1));
            assertThrows(IllegalArgumentException.class, () -> new MoneyTx(negative, "Sender", "Receiver", negative));
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 2L, 316L, 234654L, 1654L})
        @DisplayName("constructor does not throw exception with amount greater or equal to 1")
        void amountGreaterOrEqualToOne(long positive) {
            assertDoesNotThrow(() -> new MoneyTx(1, "Sender", "Receiver", positive));
            assertDoesNotThrow(() -> new MoneyTx(positive, "Sender", "Receiver", 1));
            assertDoesNotThrow(() -> new MoneyTx(positive, "Sender", "Receiver", positive));
        }
    }

    @Nested
    @DisplayName("MoneyTx sha256Base64Hash()")
    @Tag("sha256Base64Hash")
    class Sha256Base64Hash {

        @ParameterizedTest
        @ValueSource(longs = {1L, 2L, 316L, 234654L, 1654L, 6565465456L, 54454556543189789L, 1564465L})
        @DisplayName("sha256Base64Hash() returns the same hash for MoneyTx construct with same parameters")
        void hashBetweenDifferentMoneyTX(long amount) {
            String sender = "Sender";
            String receiver = "Receiver";

            MoneyTx tx0 = new MoneyTx(System.currentTimeMillis(), sender, receiver, amount);
            MoneyTx tx1 = new MoneyTx(System.currentTimeMillis(), sender, receiver, amount);
            MoneyTx tx2 = new MoneyTx(System.currentTimeMillis(), sender, receiver, amount + 1);

            assertThat(tx0.sha256Base64Hash()).isEqualTo(tx1.sha256Base64Hash());
            assertThat(tx1.sha256Base64Hash()).isNotEqualTo(tx2.sha256Base64Hash());
        }
    }

    @Nested
    @DisplayName("MoneyTx equals()")
    @Tag("equals")
    class Equals {

        @Test
        @DisplayName("equals() returns false with different MoneyTx")
        void differentMoneyTx() {
            long timestamp = System.currentTimeMillis();
            String sender = "Sender";
            String receiver = "Receiver";
            long amount = 3204654056L;

            MoneyTx tx0 = new MoneyTx(timestamp, sender, receiver, amount);
            MoneyTx tx1 = new MoneyTx(timestamp + 1, sender, receiver, amount);

            assertThat(tx0).isNotEqualTo(tx1);
        }

        @Test
        @DisplayName("equals() returns true with equal MoneyTx")
        void equalMoneyTx() {
            long timestamp = System.currentTimeMillis();
            String sender = "Sender";
            String receiver = "Receiver";
            long amount = 3204654056L;

            MoneyTx tx0 = new MoneyTx(timestamp, sender, receiver, amount);
            MoneyTx tx1 = new MoneyTx(timestamp, sender, receiver, amount);

            assertThat(tx0).isEqualTo(tx1);
        }


    }

    @Nested
    @DisplayName("MoneyTx hashCode()")
    @Tag("hashCode")
    class HashCode {

        @Test
        @DisplayName("hashCode() returns same value for equal MoneyTx")
        void correctHashCode() {
            long timestamp = System.currentTimeMillis();
            String sender = "Sender";
            String receiver = "Receiver";
            long amount = 3204654056L;

            MoneyTx tx0 = new MoneyTx(timestamp, sender, receiver, amount);
            MoneyTx tx1 = new MoneyTx(timestamp, sender, receiver, amount);

            assertThat(tx0).hasSameHashCodeAs(tx1);
        }
    }

    @Nested
    @DisplayName("MoneyTx toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void neverReturnsNull() {
            MoneyTx moneyTx = new MoneyTx(System.currentTimeMillis(), "Sender", "Receiver", 5613L);
            assertThat(moneyTx.toString()).isNotNull();
        }
    }
}
