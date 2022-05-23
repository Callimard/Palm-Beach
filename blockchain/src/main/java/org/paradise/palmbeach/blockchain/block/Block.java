package org.paradise.palmbeach.blockchain.block;

import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.paradise.palmbeach.blockchain.Hashable;
import org.paradise.palmbeach.blockchain.transaction.Transaction;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.commons.codec.digest.DigestUtils.sha256;
import static org.paradise.palmbeach.utils.validation.Validate.min;

/**
 * Immutable class which represent a {@code Block} which can be stored in a {@link Blockchain}.
 *
 * @param <T> the type of transaction
 */
@ToString
public class Block<T extends Transaction> implements Hashable {

    // Constants.

    public static final long GENESIS_BLOCK_HEIGHT = 0L;
    public static final long GENESIS_BLOCK_TIMESTAMP = 0L;
    public static final String GENESIS_BLOCK_PREVIOUS = "GENESIS";

    // Variables.

    @Getter
    private final long height;

    @Getter
    private final long timestamp;

    @Getter
    @NonNull
    private final String previous;

    @NonNull
    private final Set<T> transactions;

    // Constructors.

    public Block(long height, long timestamp, @NonNull String previous, @NonNull Set<T> transactions) {
        min(height, 0L, "Height cannot be less than 0");
        this.height = height;

        min(timestamp, 0L, "Timestamp cannot be less than 0");
        this.timestamp = timestamp;

        this.previous = previous;
        this.transactions = Sets.newHashSet(transactions);
    }

    @SuppressWarnings("unchecked")
    public Block(Block<T> base) {
        this.height = base.getHeight();
        this.timestamp = base.getTimestamp();
        this.previous = base.getPrevious();
        this.transactions = base.getTransactions().stream().map(tx -> (T) tx.copy()).collect(Collectors.toSet());
    }

    // Methods.

    public boolean isGenesis() {
        return height == GENESIS_BLOCK_HEIGHT && timestamp == GENESIS_BLOCK_TIMESTAMP && previous.equals(GENESIS_BLOCK_PREVIOUS);
    }

    @Override
    public String sha256Base64Hash() {
        String headerHash = headerHash();
        String txHash = transactionsHash();

        String blockConcat = headerHash + txHash;

        return encodeBase64String(sha256(blockConcat));
    }

    private String headerHash() {
        String heightHash = encodeBase64String(sha256(Longs.toByteArray(height)));
        String timestampHash = encodeBase64String(sha256(Longs.toByteArray(timestamp)));

        String headerConcat = heightHash + timestampHash + previous; // Previous already a hash

        return encodeBase64String(sha256(headerConcat));
    }

    private String transactionsHash() {
        StringBuilder builder = new StringBuilder();
        for (Transaction tx : transactions) {
            builder.append(tx.sha256Base64Hash());
        }

        String txConcat = builder.toString();

        return encodeBase64String(sha256(txConcat));
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    // Getters.

    public Set<T> getTransactions() {
        return Collections.unmodifiableSet(transactions);
    }
}
