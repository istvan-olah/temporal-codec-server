package com.olahistvan.codec.key.components;

import javax.crypto.SecretKey;

/**
 * Interface for managing cryptographic keys.
 */
public interface KeyProvider {

    /**
     * Retrieves the current key ID.
     *
     * @return the current key ID
     */
    String getCurrentKeyId();

    /**
     * Retrieves the secret key associated with the given key ID.
     *
     * @param keyId the ID of the key to retrieve
     * @return the secret key
     */
    SecretKey getKey(String keyId);

}