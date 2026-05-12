package com.example.ctu.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.example.ctu.otp.dto.OtpEmailMessage;

@Configuration
@SuppressWarnings("null")
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, OtpEmailMessage> otpProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, OtpEmailMessage> otpKafkaTemplate(ProducerFactory<String, OtpEmailMessage> otpProducerFactory) {
        return new KafkaTemplate<>(otpProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, OtpEmailMessage> otpConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.ctu.otp.dto");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OtpEmailMessage.class.getName());
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(OtpEmailMessage.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OtpEmailMessage> otpKafkaListenerContainerFactory(
            ConsumerFactory<String, OtpEmailMessage> otpConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, OtpEmailMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(otpConsumerFactory);
        return factory;
    }
}
