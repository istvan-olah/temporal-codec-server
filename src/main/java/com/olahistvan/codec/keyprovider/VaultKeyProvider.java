package com.olahistvan.codec.keyprovider;

import com.olahistvan.codec.dto.CodecConfigDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Implementation of the KeyProvider interface that uses HashiCorp Vault for key management.
 */
@Component
public class VaultKeyProvider implements KeyProvider {

    private final VaultTemplate vaultTemplate;
    private final String codecConfigPath;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Constructs a new VaultKeyProvider.
     *
     * @param vaultTemplate the VaultTemplate to use for interacting with Vault
     * @param codecConfigPath the path in Vault where the codec configuration is stored
     */
    public VaultKeyProvider(VaultTemplate vaultTemplate, @Value("${codec.vault.config-path}") String codecConfigPath) {
        this.vaultTemplate = vaultTemplate;
        this.codecConfigPath = codecConfigPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(cacheNames = "current-key")
    public String getCurrentKeyId() {
        CodecConfigDto response = getCodecConfig();
        return response.getCurrentKeyId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(cacheNames = "get-key", key = "#keyId")
    public SecretKey getKey(String keyId) {
        CodecConfigDto response = getCodecConfig();
        String key = response.getKeys()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(keyId))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find key with id: " + keyId));
        return new SecretKeySpec(key.getBytes(UTF_8), "AES");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(cacheNames = {"current-key", "get-key"}, allEntries = true)
    @Scheduled(cron = "${codec.vault.schedule-cron}")
    public void rotateCurrentKey() {
        setNewKey(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(cacheNames = {"current-key", "get-key"}, allEntries = true)
    public void reset() {
        setNewKey(true);
    }

    /**
     * Retrieves the codec configuration from Vault.
     *
     * @return the codec configuration
     * @throws RuntimeException if the configuration is not retrieved from Vault
     */
    private CodecConfigDto getCodecConfig() {
        VaultKeyValueOperations kv = vaultTemplate.opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2);
        VaultResponseSupport<CodecConfigDto> response = kv.get(codecConfigPath, CodecConfigDto.class);
        if (response == null || response.getData() == null) {
            throw new RuntimeException("Config not retrieved from Vault");
        }
        return response.getData();
    }

    /**
     * Saves the codec configuration to Vault.
     *
     * @param config the codec configuration to save
     */
    private void saveCodecConfig(CodecConfigDto config) {
        VaultKeyValueOperations kv = vaultTemplate.opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2);
        kv.put(codecConfigPath, config);
    }

    /**
     * Sets a new key in the codec configuration.
     *
     * @param override if true, overrides all the existing keys; otherwise, adds a new key and saves it as currentKeyId
     */
    private void setNewKey(boolean override) {
        long currentTimeMillis = System.currentTimeMillis();
        String keyId = String.format("key-%d", currentTimeMillis);
        byte[] keyBytes = new byte[32];
        secureRandom.nextBytes(keyBytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String key = encoder.encodeToString(keyBytes).substring(0, 32);

        CodecConfigDto codecConfig;
        if (override) {
            codecConfig = new CodecConfigDto();
            codecConfig.setKeys(new HashMap<>());
        } else {
            codecConfig = getCodecConfig();
        }
        codecConfig.setCurrentKeyId(keyId);
        codecConfig.getKeys().put(keyId, key);
        saveCodecConfig(codecConfig);
    }
}