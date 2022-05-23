package org.paradise.palmbeach.blockchain.block;

import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.paradise.palmbeach.blockchain.transaction.Transaction;

import java.util.Map;

/**
 * Blockchain which does not allow fork. All added blocks must be coherent. If the current height of the Blockchain is for n and a block for the
 * height n+2 is added, the block n+2 will be added but when the block n+1 will be received, the block n+2 coherence will be verified and if it not
 * the case, the block n+2 will be removed. Therefore, the user must ensure that each block added will finally be coherent in the blockchain. The
 * blockchain does not store several block for a same height.
 * <p>
 * There no also is a block validity or other similar things. This object is just a data structure. Only the coherence is guaranteed between 0 and
 * current height and the coherence is judge only on block height and previous of block.
 *
 * @param <T>
 */
@Slf4j
public class NonForkBlockchain<T extends Transaction> implements Blockchain<T> {

    // Variables.

    private long currentHeight = 0L;
    private final Map<Long, Block<T>> blockHeight = Maps.newHashMap();
    private final Map<String, Block<T>> blockHash = Maps.newHashMap();

    // Constructors.

    public NonForkBlockchain(@NonNull Block<T> genesis) {
        if (genesis.isGenesis()) {
            blockHash.put(genesis.sha256Base64Hash(), genesis);
            blockHeight.put(genesis.getHeight(), genesis);
        } else
            throw new IncoherentBlockchainException("Block " + genesis + " is not a genesis block");
    }

    // Methods.

    @Override
    public NonForkBlockchain<T> copy() {
        NonForkBlockchain<T> bc = new NonForkBlockchain<>(new Block<>(genesisBlock()));

        for (int i = 1; i <= currentHeight(); i++) {
            bc.addBlock(new Block<>(getBlock(i)));
        }

        return bc;
    }

    @Override
    public Block<T> genesisBlock() {
        return getBlock(0L);
    }

    @Override
    public void addBlock(@NonNull Block<T> block) {
        if (!hasBlock(block.getHeight())) {
            if (isCoherent(block)) {
                internAdd(block);
                if (block.getHeight() == currentHeight + 1) {
                    currentHeight++;
                    mergeBlockchain();
                }
            } else {
                throw new IncoherentBlockchainException(
                        "Cannot add block " + block + " because is previous is not coherent (h - 1 block is " + getBlock(currentHeight) + ")");
            }
        } else {
            throw new IncoherentBlockchainException("Cannot add block " + block + " already added block " + getBlock(block.getHeight()) + " for the" +
                                                            " height " + block.getHeight());
        }
    }

    private boolean isCoherent(Block<T> block) {
        long previousHeight = block.getHeight() - 1;
        Block<? extends T> previousBlock = getBlock(previousHeight);
        if (previousBlock != null) {
            return previousBlock.sha256Base64Hash().equals(block.getPrevious());
        } else
            return true;
    }

    private void mergeBlockchain() {
        long ite = currentHeight + 1;
        if (hasBlock(ite)) {
            Block<T> next = getBlock(ite);
            if (!isCoherent(next)) {
                while (hasBlock(ite)) {
                    Block<T> toRemove = getBlock(ite);
                    internRemove(toRemove);
                    log.error("Find incoherent block {} at the height {} -> incoherent block has been removed", toRemove, ite);
                    ite++;
                }
            } else {
                do {
                    currentHeight++;
                } while (hasBlock(++ite));
            }
        }
    }

    private void internAdd(Block<T> block) {
        blockHeight.put(block.getHeight(), block);
        blockHash.put(block.sha256Base64Hash(), block);
    }

    private void internRemove(Block<T> b) {
        blockHeight.remove(b.getHeight());
        blockHash.remove(b.sha256Base64Hash());
    }

    @Override
    public long currentHeight() {
        return currentHeight;
    }

    @Override
    public boolean hasBlock(long height) {
        return blockHeight.containsKey(height);
    }

    @Override
    public Block<T> getBlock(long height) {
        return blockHeight.get(height);
    }

    @Override
    public boolean hasBlock(String hash) {
        return blockHash.containsKey(hash);
    }

    @Override
    public Block<T> getBlock(String hash) {
        return this.blockHash.get(hash);
    }
}
