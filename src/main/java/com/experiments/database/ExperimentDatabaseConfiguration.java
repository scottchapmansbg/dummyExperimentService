package com.experiments.database;

import java.io.IOException;

import com.experiments.domain.Experiment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class ExperimentDatabaseConfiguration {
    @Bean
    public ReactiveRedisOperations<String, Experiment> redisOperations(ReactiveRedisConnectionFactory factory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<Experiment> serializer = new Jackson2JsonRedisSerializer<>(Experiment.class) {
            @Override
            public byte[] serialize(Experiment experiment) throws SerializationException {
                try {
                    return objectMapper.writeValueAsBytes(experiment);
                } catch (JsonProcessingException e) {
                    throw new SerializationException("Could not write JSON: " + e.getMessage(), e);
                }
            }

            @Override
            public Experiment deserialize(byte[] bytes) throws SerializationException {
                try {
                    return objectMapper.readValue(bytes, Experiment.class);
                } catch (IOException e) {
                    throw new SerializationException("Could not read JSON: " + e.getMessage(), e);
                }
            }
        };

        RedisSerializationContext.RedisSerializationContextBuilder<String, Experiment> builder = RedisSerializationContext
                .newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, Experiment> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
