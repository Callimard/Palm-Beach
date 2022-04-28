package org.paradise.palmbeach.blockchain.transaction;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.paradise.palmbeach.blockchain.Hashable;

import static org.paradise.palmbeach.utils.validation.Validate.min;

@EqualsAndHashCode
@ToString
public abstract class Transaction implements Hashable {

    // Variables.

    @Getter
    private final long timestamp;

    @Getter
    @NonNull
    private final String sender;

    // Constructors.

    protected Transaction(long timestamp, @NonNull String sender) {
        min(timestamp, 0L, "Timestamp cannot be less than 0");
        this.timestamp = timestamp;
        this.sender = sender;
    }
}
