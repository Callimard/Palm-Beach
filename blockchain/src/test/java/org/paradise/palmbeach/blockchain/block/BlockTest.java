package org.paradise.palmbeach.blockchain.block;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.paradise.palmbeach.blockchain.transaction.MoneyTx;
import org.paradise.palmbeach.blockchain.transaction.Transaction;
import org.paradise.palmbeach.utils.junit.ParadiseTest;

import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("Block tests")
@Tag("Block")
@ParadiseTest
public class BlockTest {

    @Nested
    @DisplayName("Block constructor()")
    @Tag("constructor")
    class Constructor {

        @ParameterizedTest
        @ValueSource(longs = {-11656545616L, -32134561, -233245, -1})
        @DisplayName("constructor() throws IllegalArgumentException with negative height and or negative timestamp")
        void withNegativeValue(long negative) {
            Set<Transaction> tx = Sets.newHashSet();

            assertThrows(IllegalArgumentException.class, () -> new Block<>(negative, 1, "Previous", tx));
            assertThrows(IllegalArgumentException.class, () -> new Block<>(1, negative, "Previous", tx));
            assertThrows(IllegalArgumentException.class, () -> new Block<>(negative, negative, "Previous", tx));
        }

        @ParameterizedTest
        @ValueSource(longs = {0, 1, 121645, 1651, 654, 65465})
        @DisplayName("constructor() does not throw Exception with correct value")
        void withCorrectValue(long positive) {
            assertDoesNotThrow(() -> new Block<>(positive, 1, "Previous", Sets.newHashSet()));
            assertDoesNotThrow(() -> new Block<>(1, positive, "Previous", Sets.newHashSet()));
            assertDoesNotThrow(() -> new Block<>(positive, positive, "Previous", Sets.newHashSet()));
        }
    }

    @Nested
    @DisplayName("Block isGenesis()")
    @Tag("isGenesis")
    class IsGenesis {

        @Test
        @DisplayName("isGenesis() returns false if the block is not genesis")
        void notGenesis() {
            Block<MoneyTx> b0 = new Block<>(1, 1, "Previous", Sets.newHashSet());
            Block<MoneyTx> b1 = new Block<>(Block.GENESIS_BLOCK_HEIGHT, 1, "Previous", Sets.newHashSet());
            Block<MoneyTx> b2 = new Block<>(1, Block.GENESIS_BLOCK_TIMESTAMP, "Previous", Sets.newHashSet());
            Block<MoneyTx> b3 = new Block<>(1, 1, Block.GENESIS_BLOCK_PREVIOUS, Sets.newHashSet());
            Block<MoneyTx> b4 = new Block<>(1, Block.GENESIS_BLOCK_TIMESTAMP, Block.GENESIS_BLOCK_PREVIOUS, Sets.newHashSet());
            Block<MoneyTx> b5 = new Block<>(Block.GENESIS_BLOCK_HEIGHT, Block.GENESIS_BLOCK_TIMESTAMP, "Previous", Sets.newHashSet());


            assertThat(b0.isGenesis()).isFalse();
            assertThat(b1.isGenesis()).isFalse();
            assertThat(b2.isGenesis()).isFalse();
            assertThat(b3.isGenesis()).isFalse();
            assertThat(b4.isGenesis()).isFalse();
            assertThat(b5.isGenesis()).isFalse();
        }

        @Test
        @DisplayName("isGenesis() returns true if the block is genesis")
        void isGenesis() {
            Block<MoneyTx> b0 = new Block<>(Block.GENESIS_BLOCK_HEIGHT, Block.GENESIS_BLOCK_TIMESTAMP, Block.GENESIS_BLOCK_PREVIOUS,
                                            Sets.newHashSet());
            assertThat(b0.isGenesis()).isTrue();
        }
    }

    @Nested
    @DisplayName("Block sha256Base64Hash()")
    @Tag("sha256Base64Hash")
    class Sha256Base64Hash {

        @Test
        @DisplayName("sha256Base64Hash() returns same hash for block with create with same parameters")
        void hashBetweenDifferentBlock() {
            Random r = new Random();

            long height = r.nextLong(Long.MAX_VALUE);
            long timestamp = System.currentTimeMillis();
            String previous = String.valueOf(r.nextLong());
            Set<MoneyTx> txs =
                    Sets.newHashSet(new MoneyTx(System.currentTimeMillis(), "S0", "R0", r.nextLong(Long.MAX_VALUE)),
                                    new MoneyTx(System.currentTimeMillis(), "S1", "R1", r.nextLong(Long.MAX_VALUE)));

            Block<MoneyTx> b0 = new Block<>(height, timestamp, previous, txs);
            Block<MoneyTx> b1 = new Block<>(height, timestamp, previous, txs);
            Block<MoneyTx> b2 = new Block<>(height, timestamp - 1, previous, txs);

            assertThat(b0.sha256Base64Hash()).isEqualTo(b1.sha256Base64Hash());
            assertThat(b0.sha256Base64Hash()).isNotEqualTo(b2.sha256Base64Hash());
        }
    }

    @Nested
    @DisplayName("Block toString()")
    @Tag("toString")
    class ToString {

        @Test
        @DisplayName("toString() never returns null")
        void nerverReturnsNull() {
            Block<MoneyTx> b = new Block<>(1, 1, "Previous", Sets.newHashSet());
            assertThat(b.toString()).isNotNull();
        }
    }
}
