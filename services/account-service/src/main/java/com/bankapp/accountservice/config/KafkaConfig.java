package com.bankapp.accountservice.config;

import com.bankapp.events.account.v1.CreditFailed;
import com.bankapp.events.account.v1.DebitFailed;
import com.bankapp.events.account.v1.FundsCredited;
import com.bankapp.events.account.v1.FundsDebited;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.application.name}")
    private String applicationName; // groupId

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    // --- Producer ---
    @Bean
    public Map<String, Object> kafkaProperties() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 15000);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 5);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        configProps.put("schema.registry.url", schemaRegistryUrl);
        return configProps;
    }

    @Bean
    public KafkaTemplate<String, FundsDebited> fundsDebitedKafkaTemplate() {
        ProducerFactory<String, FundsDebited> pf = new DefaultKafkaProducerFactory<>(kafkaProperties());
        return new KafkaTemplate<>(pf);
    }


    @Bean
    public KafkaTemplate<String, FundsCredited> fundsCreditedKafkaTemplate() {
        ProducerFactory<String, FundsCredited> pf = new DefaultKafkaProducerFactory<>(kafkaProperties());
        return new KafkaTemplate<>(pf);
    }


    @Bean
    public KafkaTemplate<String, DebitFailed> debitFailedKafkaTemplate() {
        ProducerFactory<String, DebitFailed> pf = new DefaultKafkaProducerFactory<>(kafkaProperties());
        return new KafkaTemplate<>(pf);
    }


    @Bean
    public KafkaTemplate<String, CreditFailed> creditFailedKafkaTemplate() {
        ProducerFactory<String, CreditFailed> pf = new DefaultKafkaProducerFactory<>(kafkaProperties());
        return new KafkaTemplate<>(pf);
    }

    // --- Consumer ---
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, applicationName);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // на случай, если топик уже содержит данные

        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("specific.avro.reader", true); // важно для работы SpecificRecord

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }
}
