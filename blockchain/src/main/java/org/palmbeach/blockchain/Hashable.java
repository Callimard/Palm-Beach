package org.palmbeach.blockchain;

public interface Hashable {

    /**
     * Returns the current hash of the object. The hash is computed with the hash algorithm SHA256 and the bytes results is encoded in Base64L
     *
     * @return the result string of the SHA256 hash encode in Base64.
     */
    String sha256Base64Hash();

}
