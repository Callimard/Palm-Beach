package org.paradise.palmbeach.blockchain.block;

import lombok.NonNull;
import org.paradise.palmbeach.blockchain.transaction.Transaction;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An interface which represent a Blockchain data structure. This class is only a data structure, it does not manage the distributed system part of a
 * blockchain.
 * <p>
 * A blockchain guarantees that blocks between height 0 and {@link #currentHeight()} are coherent.
 *
 * @param <T> transaction type contains in block
 */
public interface Blockchain<T extends Transaction> extends Iterable<Block<T>> {

    Blockchain<T> copy();

    /**
     * @return the genesis block.
     */
    Block<T> genesisBlock();

    /**
     * @param block the block to add in the BC
     *
     * @throws IncoherentBlockchainException if the block cannot be added in the blockchain
     */
    void addBlock(@NonNull Block<T> block);

    /**
     * @return the current height of the blockchain
     */
    long currentHeight();

    /**
     * @param height the height of the block
     *
     * @return true if there is a block for the specified height, else false.
     */
    boolean hasBlock(long height);

    /**
     * @param height the height of the block
     *
     * @return the block at the specific height, null if no block at this height in the {@link Blockchain}.
     */
    Block<T> getBlock(long height);

    /**
     * @param hash hash of the researched block
     *
     * @return true if there is a block for the specified hash, else false.
     */
    boolean hasBlock(String hash);

    /**
     * @param hash hash of the researched block
     *
     * @return the block associated to the specified block hash. If not present is the {@link Blockchain}, returns null.
     */
    Block<T> getBlock(String hash);

    @Override
    default Iterator<Block<T>> iterator() {
        return new BlockchainIterator<>(this);
    }

    // Inner classes.

    /**
     * Browse the {@link Blockchain} from the {@link Blockchain#currentHeight()} to height 0.
     *
     * @param <T> transaction type contains in block
     */
    class BlockchainIterator<T extends Transaction> implements Iterator<Block<T>> {

        // Variables.

        @NonNull
        private final Blockchain<T> blockchain;
        private Block<T> ite;

        // Constructors.

        public BlockchainIterator(@NonNull Blockchain<T> blockchain) {
            this.blockchain = blockchain;
            this.ite = this.blockchain.getBlock(this.blockchain.currentHeight());
        }

        // Methods.

        @Override
        public boolean hasNext() {
            return this.ite != null;
        }

        @Override
        public Block<T> next() {
            if (ite != null) {
                Block<T> tmp = ite;
                ite = blockchain.getBlock(ite.getPrevious());
                return tmp;
            } else
                throw new NoSuchElementException();
        }
    }

    class IncoherentBlockchainException extends RuntimeException {
        public IncoherentBlockchainException(String message) {
            super(message);
        }
    }
}
