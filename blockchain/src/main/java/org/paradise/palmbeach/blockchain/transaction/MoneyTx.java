package org.paradise.palmbeach.blockchain.transaction;

import com.google.common.primitives.Longs;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.commons.codec.digest.DigestUtils.sha256;
import static org.paradise.palmbeach.utils.validation.Validate.min;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MoneyTx extends Transaction {

    // Variables.

    @NonNull
    @Getter
    private final String receiver;

    @Getter
    private final long amount;

    // Constructors.

    public MoneyTx(long timestamp, @NonNull String sender, @NonNull String receiver, long amount) {
        super(timestamp, sender);
        this.receiver = receiver;

        min(amount, 1, "Amount cannot be less or equal to 0");
        this.amount = amount;
    }

    // Methods.

    @Override
    public String sha256Base64Hash() {
        String senderHash = encodeBase64String(sha256(getSender()));
        String receiverHash = encodeBase64String(sha256(getReceiver()));
        String amountHash = encodeBase64String(sha256(Longs.toByteArray(getAmount())));

        String txConcat = senderHash + receiverHash + amountHash;

        return encodeBase64String(sha256(txConcat));
    }
}
