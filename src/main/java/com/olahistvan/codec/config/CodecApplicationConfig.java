package com.olahistvan.codec.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.olahistvan.codec.config.properties.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for the codec application.
 * Enables caching and scheduling, and configures the cache manager.
 */
@Configuration
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties
public class CodecApplicationConfig {

    /**
     * Configures the Caffeine cache with the specified properties.
     *
     * @param cacheProperties the properties for configuring the cache
     * @return the configured Caffeine cache
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig(CacheProperties cacheProperties) {
        return Caffeine.newBuilder().expireAfterWrite(cacheProperties.getTtl(), TimeUnit.MINUTES);
    }

    /**
     * Configures the cache manager with the specified Caffeine cache.
     *
     * @param caffeine the Caffeine cache to use
     * @return the configured cache manager
     */
    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }

}