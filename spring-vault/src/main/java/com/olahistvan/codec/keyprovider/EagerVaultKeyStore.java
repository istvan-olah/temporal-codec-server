package com.olahistvan.codec.keyprovider;

import com.olahistvan.codec.clients.VaultClient;
import com.olahistvan.codec.dto.CodecConfigDto;
import com.olahistvan.codec.key.components.KeyGenerator;
import com.olahistvan.codec.key.components.KeyProvider;
import com.olahistvan.codec.key.components.KeyRotator;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Implementation of the KeyProvider interface that uses HashiCorp Vault for key management.
 */
public class EagerVaultKeyStore implements KeyProvider, KeyRotator {

    protected final VaultClient vaultClient;
    protected final KeyGenerator keyGenerator;

    public EagerVaultKeyStore(VaultClient vaultClient, KeyGenerator keyGenerator) {
        this.vaultClient = vaultClient;
        this.keyGenerator = keyGenerator;
    }

    @Override
    public String getCurrentKeyId() {
        CodecConfigDto response = vaultClient.getCodecConfig();
        return response.getCurrentKeyId();
    }

    @Override
    public SecretKey getKey(String keyId) {
        CodecConfigDto response = vaultClient.getCodecConfig();
        String key = filterKey(response, keyId)
                .orElseThrow(() -> new RuntimeException("Could not find key with id: " + keyId));
        return new SecretKeySpec(key.getBytes(UTF_8), "AES");
    }

    @Override
    public void rotateCurrentKey() {
        KeyGenerator.CodecKey codecKey = keyGenerator.generateNewKey();
        CodecConfigDto codecConfig = vaultClient.getCodecConfig();
        codecConfig.setCurrentKeyId(codecKey.getId());
        codecConfig.getKeys().put(codecKey.getId(), codecKey.getValue());
        vaultClient.saveCodecConfig(codecConfig);
    }

    protected Optional<String> filterKey(CodecConfigDto config, String keyId) {
        return config.getKeys()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(keyId))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}