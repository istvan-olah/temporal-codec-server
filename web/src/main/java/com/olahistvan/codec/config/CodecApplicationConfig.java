package com.olahistvan.codec.config;

import com.olahistvan.codec.clients.VaultClient;
import com.olahistvan.codec.key.components.KeyGenerator;
import com.olahistvan.codec.key.components.KeyProvider;
import com.olahistvan.codec.key.components.impl.DefaultKeyGenerator;
import com.olahistvan.codec.keyprovider.CachedVaultKeyStore;
import com.olahistvan.codec.keyprovider.EagerVaultKeyStore;
import com.olahistvan.codec.payloadcodec.SimplePayloadCodec;
import io.temporal.payload.codec.PayloadCodec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.vault.core.VaultTemplate;

/**
 * Configuration class for the codec application.
 * Enables caching and scheduling, and configures the cache manager.
 */
@Configuration
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties
public class CodecApplicationConfig {

    @Bean
    public KeyGenerator keyGenerator() {
        return new DefaultKeyGenerator();
    }

    @Bean("sharedKeyStore")
    public KeyProvider sharedKeyStore(VaultTemplate vaultTemplate, @Value("${codec.shared-vault.config-path}") String codecConfigPath,
                                      KeyGenerator keyGenerator) {
        VaultClient vaultClient = new VaultClient(vaultTemplate, codecConfigPath);
        return new EagerVaultKeyStore(vaultClient, keyGenerator);
    }

    @Bean("sharedPayloadCodec")
    public PayloadCodec sharedPayloadCodec(@Qualifier("sharedKeyStore") KeyProvider vaultKeyStore,
                                     @Value("${codec.shared-vault.tag}") String tag) {
        return new SimplePayloadCodec(vaultKeyStore, tag);
    }
    @Bean("greetingKeyStore")
    public KeyProvider greetingKeyStore(VaultTemplate vaultTemplate, @Value("${codec.greeting-vault.config-path}") String codecConfigPath,
                                      KeyGenerator keyGenerator) {
        VaultClient vaultClient = new VaultClient(vaultTemplate, codecConfigPath);
        return new EagerVaultKeyStore(vaultClient, keyGenerator);
    }

    @Bean("greetingPayloadCodec")
    public PayloadCodec greetingPayloadCodec(@Qualifier("greetingKeyStore") KeyProvider vaultKeyStore,
                                     @Value("${codec.greeting-vault.tag}") String tag) {
        return new SimplePayloadCodec(vaultKeyStore, tag);
    }
    @Bean("farewellKeyStore")
    public KeyProvider farewellKeyStore(VaultTemplate vaultTemplate, @Value("${codec.farewell-vault.config-path}") String codecConfigPath,
                                      KeyGenerator keyGenerator,
                                        @Value("${codec.farewell-vault.cache-refresh-interval}") Integer refreshInterval) {
        VaultClient vaultClient = new VaultClient(vaultTemplate, codecConfigPath);
        return new CachedVaultKeyStore(vaultClient, keyGenerator, refreshInterval);
    }

    @Bean("farewellPayloadCodec")
    public PayloadCodec farewellPayloadCodec(@Qualifier("farewellKeyStore") KeyProvider vaultKeyStore,
                                     @Value("${codec.farewell-vault.tag}") String tag) {
        return new SimplePayloadCodec(vaultKeyStore, tag);
    }


}