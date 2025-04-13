package com.olahistvan.codec.keyprovider;

import com.olahistvan.codec.clients.VaultClient;
import com.olahistvan.codec.dto.CodecConfigDto;
import com.olahistvan.codec.key.components.KeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Implementation of the KeyProvider interface that uses HashiCorp Vault for key management.
 */
public class CachedVaultKeyStore extends EagerVaultKeyStore {

    private static final Logger logger = LoggerFactory.getLogger(CachedVaultKeyStore.class);


    private CodecConfigDto cachedConfig;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CachedVaultKeyStore(VaultClient vaultClient, KeyGenerator keyGenerator, long refreshIntervalSeconds) {
        super(vaultClient, keyGenerator);
        cachedConfig = vaultClient.getCodecConfig();

        scheduler.scheduleAtFixedRate(this::safeRefresh, refreshIntervalSeconds, refreshIntervalSeconds, TimeUnit.SECONDS);

    }

    @Override
    public String getCurrentKeyId() {
        return cachedConfig.getCurrentKeyId();
    }

    @Override
    public SecretKey getKey(String keyId) {
        String key = filterKey(cachedConfig, keyId)
                .orElseGet(() ->
                        filterKey(refreshConfig(), keyId)
                                .orElseThrow(() -> new RuntimeException("Could not find key with id: " + keyId)));

        return new SecretKeySpec(key.getBytes(UTF_8), "AES");
    }

    private synchronized CodecConfigDto refreshConfig() {
        cachedConfig = vaultClient.getCodecConfig();
        return cachedConfig;
    }

    private void safeRefresh() {
        try {
            refreshConfig();
        } catch (Exception e) {
            logger.error("Background refresh failed for vault", e);
        }
    }

}