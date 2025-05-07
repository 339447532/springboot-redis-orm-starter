package com.asd.redis.orm.config;

import com.asd.redis.orm.core.RedisOrmTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis ORM自动配置类
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
@EnableConfigurationProperties(RedisOrmProperties.class)
public class RedisOrmAutoConfiguration {

    private final RedisOrmProperties properties;

    public RedisOrmAutoConfiguration(RedisOrmProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置key的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置value的序列化方式
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisOrmTemplate redisOrmTemplate(RedisTemplate<String, Object> redisTemplate) {
        return new RedisOrmTemplate(redisTemplate, properties);
    }
}