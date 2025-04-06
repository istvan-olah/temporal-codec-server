package com.olahistvan.codec.keyprovider;

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

    /**
     * Rotates the current key, generating a new key and updating the current key ID.
     */
    void rotateCurrentKey();

    /**
     * Resets the key provider, generating a new key and clearing all existing keys.
     */
    void reset();
}