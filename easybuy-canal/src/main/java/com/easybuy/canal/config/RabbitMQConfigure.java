package com.easybuy.canal.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfigure {
    public final static String AD_UPDATE_QUEUE = "ad_update_queue";
    public final static String SEARCH_ADD_QUEUE = "search_add_queue";

    public final static String GOODS_UP_EXCHANGE = "goods_up_exchange";

    @Bean
    public Queue adUpdateQueue() {
        return new Queue(RabbitMQConfigure.AD_UPDATE_QUEUE);
    }

    @Bean
    public Queue searchAddQueue() {
        return new Queue(RabbitMQConfigure.SEARCH_ADD_QUEUE);
    }

    @Bean
    public Exchange goodsUpExchange() {
        return ExchangeBuilder.fanoutExchange(RabbitMQConfigure.GOODS_UP_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding goodsUpExchangeBinding(@Qualifier("searchAddQueue") Queue queue, @Qualifier("goodsUpExchange") Exchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with("")
                .noargs();
    }
}
