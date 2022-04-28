package org.palmbeach.blockchain.transaction;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.palmbeach.blockchain.Hashable;
import org.palmbeach.core.common.validation.Validate;

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
        Validate.min(timestamp, 0L, "Timestamp cannot be less than 0");
        this.timestamp = timestamp;
        this.sender = sender;
    }
}
