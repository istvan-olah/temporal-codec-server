package com.olahistvan.codec.key.components.impl;

import com.olahistvan.codec.key.components.KeyGenerator;
import com.olahistvan.codec.key.components.KeyProvider;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DefaultKeyProvider implements KeyProvider {

    private final KeyGenerator.CodecKey key;

    public DefaultKeyProvider(KeyGenerator.CodecKey key) {
        this.key = key;
    }

    @Override
    public String getCurrentKeyId() {
        return key.getId();
    }

    @Override
    public SecretKey getKey(String keyId) {
        if (key.getId().equals(keyId)) {
            return new SecretKeySpec(key.getValue().getBytes(UTF_8), "AES");
        }
        throw new RuntimeException("Could not find key with id: " + keyId);
    }
}
