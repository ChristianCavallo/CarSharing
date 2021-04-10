package com.carsharing.rentals_microservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka_rental_topic}")
    private String rental_topic_name;

    @Value("${kafka_car_requests_topic}")
    private String car_requests_topic;

    @Value("${kafka_car_responses_topic}")
    private String car_responses_topic;

    @Value("${kafka_replies_group}")
    private String car_replies_group;

    @Value("${kafka_logging_topic}")
    private String logging_topic_name;

    @Value("${kafka_group_id}")
    private String groupId;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // so the new group doesn't get old replies
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaTemplate<String, String> replyTemplate(ProducerFactory<String, String> pf,
                                                       ConcurrentKafkaListenerContainerFactory<String, String> factory) {
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(pf);
        factory.getContainerProperties().setMissingTopicsFatal(false);
        factory.setReplyTemplate(kafkaTemplate);
        return kafkaTemplate;
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(ProducerFactory<String, String> pf,
                                                                               ConcurrentKafkaListenerContainerFactory<String, String> factory) {
        ConcurrentMessageListenerContainer<String, String> replyContainer = factory.createContainer(car_responses_topic);
        replyContainer.getContainerProperties().setMissingTopicsFatal(false);
        replyContainer.getContainerProperties().setGroupId(car_replies_group);

        return new ReplyingKafkaTemplate<>(pf, replyContainer);
    }

    @Bean
    public NewTopic rentalTopic()
    {
        return TopicBuilder.name(rental_topic_name)
                .partitions(10)
                .build();
    }

    @Bean
    public NewTopic loggingTopic() {
        return TopicBuilder.name(logging_topic_name)
                .partitions(10)
                .build();
    }

    @Bean
    public NewTopic car_requests_topic() {
        return TopicBuilder.name(car_requests_topic)
                .partitions(10)
                .build();
    }

    @Bean
    public NewTopic kReplies() {
        return TopicBuilder.name(car_responses_topic)
                .partitions(10)
                .build();
    }
}
