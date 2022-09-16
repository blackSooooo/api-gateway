package worksmobile.intern.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.*;
import reactor.core.publisher.Mono;
import worksmobile.intern.apigateway.subscriber.ReactiveMessageRoutingSubscriber;

@Configuration
public class RedisConfig {
    private static final String TOPIC_1 = "routing-register";
    private static final String TOPIC_2 = "routing-update";
    private static final String TOPIC_3 = "routing-delete";
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory, ReactiveMessageRoutingSubscriber reactiveMessageRoutingSubscriber) {
        ReactiveRedisMessageListenerContainer container = new ReactiveRedisMessageListenerContainer(reactiveRedisConnectionFactory);

        container
                .receive(registerTopic())
                .map(ReactiveSubscription.Message::getMessage)
                .onErrorResume(throwable -> Mono.just("Disconnected by shutdown"))
                .subscribe(msg -> {
                    reactiveMessageRoutingSubscriber.registerEvent(msg);
                });
        container
                .receive(updateTopic())
                .map(ReactiveSubscription.Message::getMessage)
                .onErrorResume(throwable -> Mono.just("Disconnected by shutdown"))
                .subscribe(msg -> {
                    reactiveMessageRoutingSubscriber.updateEvent(msg);
                });
        container
                .receive(deleteTopic())
                .map(ReactiveSubscription.Message::getMessage)
                .onErrorResume(throwable -> Mono.just("Disconnected by shutdown"))
                .subscribe(msg -> {
                    reactiveMessageRoutingSubscriber.deleteEvent(msg);
                });
        return container;
    }

    @Bean
    ChannelTopic registerTopic() {
        return new ChannelTopic(TOPIC_1);
    }

    @Bean
    ChannelTopic updateTopic() { return new ChannelTopic(TOPIC_2); }

    @Bean
    ChannelTopic deleteTopic() { return new ChannelTopic(TOPIC_3); }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        RedisSerializer<String> serializer = new StringRedisSerializer();
        RedisSerializationContext serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext()
                .key(serializer)
                .value(new GenericJackson2JsonRedisSerializer(objectMapper))
                .hashKey(serializer)
                .hashValue(new GenericJackson2JsonRedisSerializer(objectMapper))
                .build();
        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
    }
}
