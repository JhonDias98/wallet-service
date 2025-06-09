package com.wallet.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "wallet.notification.exchange";
    public static final String QUEUE = "wallet.notification.queue";
    public static final String ROUTING_KEY = "wallet.notification.key";

    @Bean
    public Queue walletQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public TopicExchange walletExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding walletBinding(Queue walletQueue, TopicExchange walletExchange) {
        return BindingBuilder.bind(walletQueue).to(walletExchange).with(ROUTING_KEY);
    }
}
