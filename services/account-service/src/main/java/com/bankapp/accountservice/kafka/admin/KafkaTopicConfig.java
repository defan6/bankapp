package com.bankapp.accountservice.kafka.admin;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {




    @Bean
    public NewTopic accountDebitTopic(){
        return TopicBuilder.name("account-debit-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic accountDebitedTopic(){
        return TopicBuilder.name("account-debited-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic accountDebitFailedTopic(){
        return TopicBuilder.name("account-debit-failed-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic accountCreditTopic(){
        return TopicBuilder.name("account-credit-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic accountCreditedTopic(){
        return TopicBuilder.name("account-credited-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic accountCreditFailedTopic(){
        return TopicBuilder.name("account-credit-failed-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
