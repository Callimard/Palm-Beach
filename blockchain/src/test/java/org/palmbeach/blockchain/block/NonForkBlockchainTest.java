package org.palmbeach.blockchain.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.palmbeach.blockchain.transaction.Transaction;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DisplayName("NonForkBlockchain tests")
@Tag("NonForkBlockchain")
public class NonForkBlockchainTest {

    @Nested
    @DisplayName("NonForkBlockchain constructor()")
    @Tag("constructor")
    class Constructor {

        @Test
        @DisplayName("constructor() throws an IncoherentBlockchainException if the genesis block is not a genesis block")
        void notGenesisBlock() {
            Block<Transaction> notGenesis = new Block<>(1, 1, "Prev", Sets.newHashSet());
            assertThrows(Blockchain.IncoherentBlockchainException.class, () -> new NonForkBlockchain<>(notGenesis));
        }

        @Test
        @DisplayName("constructor() does not throw exception with genesis block")
        void withGenesisBlock() {
            Block<Transaction> genesis =
                    new Block<>(Block.GENESIS_BLOCK_HEIGHT, Block.GENESIS_BLOCK_TIMESTAMP, Block.GENESIS_BLOCK_PREVIOUS, Sets.newHashSet());

            AtomicReference<NonForkBlockchain<Transaction>> bc = new AtomicReference<>();
            assertDoesNotThrow(() -> bc.set(new NonForkBlockchain<>(genesis)));
            assertThat(bc.get().genesisBlock()).isSameAs(genesis);
            assertThat(bc.get().currentHeight()).isEqualByComparingTo(0L);
        }
    }

    @Nested
    @DisplayName("NonForkBlockchain addBlock()")
    @Tag("addBlock")
    class AddBlock {

        @Test
        @DisplayName("addBlock() add the block for the next height if it is coherent")
        void addCoherentBlock() {
            NonForkBlockchain<Transaction> bc = generateBC();
            Block<Transaction> b1 = new Block<>(bc.currentHeight() + 1,
                                                System.currentTimeMillis(),
                                                bc.getBlock(bc.currentHeight()).sha256Base64Hash(),
                                                Sets.newHashSet());
            bc.addBlock(b1);
            assertThat(bc.currentHeight()).isEqualByComparingTo(1L);
            assertThat(bc.hasBlock(1L)).isTrue();
            assertThat(bc.getBlock(1L)).isSameAs(b1);
            assertThat(bc.hasBlock(b1.sha256Base64Hash())).isTrue();
            assertThat(bc.getBlock(b1.sha256Base64Hash())).isSameAs(b1);
        }

        @Test
        @DisplayName("addBlock() throws IncoherentBlockchainException if there already is a block at the height of the to add block")
        void alreadyAddedBlockForSpecificHeight() {
            NonForkBlockchain<Transaction> bc = generateBC();
            Block<Transaction> b1 = new Block<>(bc.currentHeight() + 1,
                                                System.currentTimeMillis(),
                                                bc.getBlock(bc.currentHeight()).sha256Base64Hash(),
                                                Sets.newHashSet());
            Block<Transaction> b2 = new Block<>(bc.currentHeight() + 1, // same height of b1
                                                System.currentTimeMillis(),
                                                b1.sha256Base64Hash(),
                                                Sets.newHashSet());
            bc.addBlock(b1);

            assertThrows(Blockchain.IncoherentBlockchainException.class, () -> bc.addBlock(b2));
            assertThat(bc.currentHeight()).isEqualByComparingTo(1L);
        }

        @Test
        @DisplayName("addBlock() can add several block")
        void addSeveralBlocks() {
            NonForkBlockchain<Transaction> bc = generateBC();
            long number = 50L;
            List<Block<Transaction>> blocks = generateBlock(bc.genesisBlock(), number);

            for (Block<Transaction> b : blocks) {
                bc.addBlock(b);
            }

            assertThat(bc.currentHeight()).isEqualTo(number);
            assertThat(bc).containsAll(blocks);

            verifyBlockHeight(bc);
            verifyIteratorBrowsing(bc);
        }

        @Test
        @DisplayName("addBlock() does not matter of adding order while all added block becomes coherent")
        void randomAdding() {
            NonForkBlockchain<Transaction> bc = generateBC();
            long number = 50L;
            List<Block<Transaction>> blocks = generateBlock(bc.genesisBlock(), number);
            Collections.shuffle(blocks);
            System.out.println(blocks);

            for (Block<Transaction> b : blocks) {
                bc.addBlock(b);
            }
            assertThat(bc.currentHeight()).isEqualTo(number);
            assertThat(bc).containsAll(blocks);

            verifyBlockHeight(bc);
            verifyIteratorBrowsing(bc);
        }

        @Test
        @DisplayName("addBlock() throws IncoherentBlockchainException if the next block added is not coherent")
        void notCoherentNextBlock() {
            NonForkBlockchain<Transaction> bc = generateBC();
            Block<Transaction> b1 = new Block<>(bc.currentHeight() + 1,
                                                System.currentTimeMillis(),
                                                "WRONG_HASH",
                                                Sets.newHashSet());

            assertThrows(Blockchain.IncoherentBlockchainException.class, () -> bc.addBlock(b1));
        }

        @Test
        @DisplayName("addBlock() stay merge when a block is add between to \"sub chain\" ")
        void addBetweenSubChain() {
            NonForkBlockchain<Transaction> bc = generateBC();
            long number = 579L;
            List<Block<Transaction>> blocks = generateBlock(bc.genesisBlock(), number);

            for (int i = 0; i < (number / 2L); i++) {
                bc.addBlock(blocks.get(i));
            }
            assertThat(bc.currentHeight()).isEqualByComparingTo((number / 2L));

            for (int i = (int) (number / 2) + 1; i < number; i++) {
                bc.addBlock(blocks.get(i));
            }
            assertThat(bc.currentHeight()).isEqualByComparingTo((number / 2L));

            bc.addBlock(blocks.get((int) (number / 2)));
            assertThat(bc.currentHeight()).isEqualByComparingTo(number);
            assertThat(bc).containsAll(blocks);

            verifyBlockHeight(bc);
            verifyIteratorBrowsing(bc);
        }

        @Test
        @DisplayName("addBlock() remove incoherent block added")
        void removeIncoherentBlock() {
            NonForkBlockchain<Transaction> bc = generateBC();
            Block<Transaction> b1 = new Block<>(bc.currentHeight() + 1,
                                                System.currentTimeMillis(),
                                                bc.getBlock(bc.currentHeight()).sha256Base64Hash(),
                                                Sets.newHashSet());
            Block<Transaction> b2 = new Block<>(bc.currentHeight() + 2, // same height of b1
                                                System.currentTimeMillis(),
                                                b1.sha256Base64Hash(),
                                                Sets.newHashSet());
            Block<Transaction> b3 = new Block<>(bc.currentHeight() + 3, // same height of b1
                                                System.currentTimeMillis(),
                                                b2.sha256Base64Hash(),
                                                Sets.newHashSet());
            Block<Transaction> b4 = new Block<>(bc.currentHeight() + 4, // same height of b1
                                                System.currentTimeMillis(),
                                                b3.sha256Base64Hash(),
                                                Sets.newHashSet());

            Block<Transaction> incoherent = new Block<>(bc.currentHeight() + 3, // same height of b1
                                                        System.currentTimeMillis(),
                                                        "PREVIOUS_HASH_WRONG",
                                                        Sets.newHashSet());
            bc.addBlock(incoherent);
            List<Block<Transaction>> incoherentBC = generateBlock(incoherent, 75);
            for (Block<Transaction> incoherentBlock : incoherentBC) {
                bc.addBlock(incoherentBlock);
            }
            assertThat(bc.currentHeight()).isEqualByComparingTo(0L);

            bc.addBlock(b1);
            assertThat(bc.currentHeight()).isEqualByComparingTo(1L);

            bc.addBlock(b2);
            assertThat(bc.currentHeight()).isEqualByComparingTo(2L);
            assertThat(bc.getBlock(2L)).isSameAs(b2);
            verifyBlockHeight(bc);
            verifyIteratorBrowsing(bc);

            assertThat(bc).doesNotContainAnyElementsOf(incoherentBC);

            assertDoesNotThrow(() -> bc.addBlock(b4)); // Order important
            assertDoesNotThrow(() -> bc.addBlock(b3));
            assertThat(bc.currentHeight()).isEqualByComparingTo(4L);
        }
    }

    private void verifyBlockHeight(NonForkBlockchain<Transaction> bc) {
        for (long i = 0; i <= bc.currentHeight(); i++) {
            Block<Transaction> b = bc.getBlock(i);
            assertThat(b).isNotNull();
            assertThat(b.getHeight()).isEqualByComparingTo(i);
        }
    }

    private void verifyIteratorBrowsing(NonForkBlockchain<Transaction> bc) {
        long h = bc.currentHeight();
        for (Block<Transaction> b : bc) {
            assertThat(b.getHeight()).isEqualByComparingTo(h--);
        }
    }

    private NonForkBlockchain<Transaction> generateBC() {
        Block<Transaction> genesis =
                new Block<>(Block.GENESIS_BLOCK_HEIGHT, Block.GENESIS_BLOCK_TIMESTAMP, Block.GENESIS_BLOCK_PREVIOUS, Sets.newHashSet());
        return new NonForkBlockchain<>(genesis);
    }

    private List<Block<Transaction>> generateBlock(Block<Transaction> genesis, long number) {
        List<Block<Transaction>> blocks = Lists.newArrayList();
        Block<Transaction> previous = genesis;
        for (int i = 0; i < number; i++) {
            Block<Transaction> b =
                    new Block<>((genesis.getHeight() + 1) + i, System.currentTimeMillis(), previous.sha256Base64Hash(), Sets.newHashSet());
            previous = b;
            blocks.add(b);
        }

        return blocks;
    }
}
