package com.easybuy.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.easybuy.canal.config.RabbitMQConfigure;
import com.wwjd.starter.canal.annotation.CanalEventListener;
import com.wwjd.starter.canal.annotation.ListenPoint;
import com.wwjd.starter.canal.client.core.CanalMsg;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;


@CanalEventListener
public class BusinessListener {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ListenPoint(schema = "changgou_business", table = "tb_ad")
    public void adUpdate(CanalMsg canalMsg, CanalEntry.RowChange rowChange) {

        System.out.println("广告表数据发生改变");

        Flux.fromIterable(rowChange.getRowDatasList())
                .flatMap(dataList -> Flux.fromIterable(dataList.getAfterColumnsList()))
                .filter(column -> "position".equals(column.getName()))
                .doOnNext(column -> {
                    System.out.printf("发送最新的数据到MQ：%s\n", column.getValue());
                    rabbitTemplate.convertAndSend("", RabbitMQConfigure.AD_UPDATE_QUEUE, column.getValue());
                })
                .subscribe();

    }
}
