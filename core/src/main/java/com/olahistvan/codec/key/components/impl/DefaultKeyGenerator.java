package com.olahistvan.codec.key.components.impl;

import com.olahistvan.codec.key.components.KeyGenerator;

import java.security.SecureRandom;
import java.util.Base64;

public class DefaultKeyGenerator implements KeyGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public CodecKey generateNewKey() {
        long currentTimeMillis = System.currentTimeMillis();
        String keyId = String.format("key-%d", currentTimeMillis);
        byte[] keyBytes = new byte[32];
        secureRandom.nextBytes(keyBytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String key = encoder.encodeToString(keyBytes).substring(0, 32);

        return new CodecKey(keyId, key);
    }
}
