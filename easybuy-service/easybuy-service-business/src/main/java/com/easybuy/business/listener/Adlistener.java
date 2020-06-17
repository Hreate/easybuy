package com.easybuy.business.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class Adlistener {

    @RabbitListener(queues = "ad_update_queue")
    public void receiveMessage(String message) {
        Mono.just(message)
                .map(mes -> {
                    System.out.println("接收到的消息为：" + message);
                    return mes;
                })
                //retrieve()：执行HTTP请求并检索响应体:
                .flatMap(mes -> {
                    var webclient = WebClient.create("http://49.235.222.12");
                    return webclient.get().uri("/ad_update?position=" + message).retrieve().bodyToMono(String.class);
                })
                .doOnSuccess(System.out::println)
                .subscribe();
    }
}
