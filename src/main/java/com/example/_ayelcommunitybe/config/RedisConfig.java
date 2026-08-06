package com.example._ayelcommunitybe.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Duration;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

    public static final String POST_DETAIL_CACHE = "postDetail";
    public static final String WEEKLY_POPULAR_CACHE = "weeklyPopular";

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory
    ) {

        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer()
                        .configure(objectMapper -> {
                            objectMapper.registerModule(
                                    new JavaTimeModule()
                            );
                            objectMapper.disable(
                                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                            );
                        });

        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.RedisCacheManagerBuilder
                        .fromConnectionFactory(connectionFactory);

        RedisCacheConfiguration defaultConfiguration =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new StringRedisSerializer()
                                        )
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                valueSerializer
                                        )
                        )
                        .entryTtl(
                                Duration.ofMinutes(5)
                        );

        RedisCacheConfiguration postDetailConfiguration =
                defaultConfiguration.entryTtl(
                        Duration.ofMinutes(1)
                );

        RedisCacheConfiguration weeklyPopularConfiguration =
                defaultConfiguration.entryTtl(
                        Duration.ofMinutes(5)
                );

        builder
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(
                        Map.of(
                                POST_DETAIL_CACHE,
                                postDetailConfiguration,
                                WEEKLY_POPULAR_CACHE,
                                weeklyPopularConfiguration
                        )
                )
                .transactionAware();

        return builder.build();
    }
}