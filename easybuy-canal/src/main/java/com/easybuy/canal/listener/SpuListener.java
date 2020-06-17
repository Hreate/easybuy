package com.easybuy.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.easybuy.canal.config.RabbitMQConfigure;
import com.wwjd.starter.canal.annotation.CanalEventListener;
import com.wwjd.starter.canal.annotation.ListenPoint;
import com.wwjd.starter.canal.client.core.CanalMsg;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;

@CanalEventListener
public class SpuListener {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * spu 表更新
     *
     * @param
     * @param
     */
    @ListenPoint(schema = "changgou_goods", table = {"tb_spu"}, eventType = CanalEntry.EventType.UPDATE)
    public void spuUp(CanalMsg canalMsg, CanalEntry.RowChange rowChange) {
        List<CanalEntry.Column> beforeColumnsList = rowChange.getRowDatasList().get(0).getBeforeColumnsList();
        Mono<HashMap<String, String>> oldData = this.getDataMap(beforeColumnsList);
        List<CanalEntry.Column> afterColumnsList = rowChange.getRowDatasList().get(0).getAfterColumnsList();
        Mono<HashMap<String, String>> newData = this.getDataMap(afterColumnsList);
        Mono.zip(oldData, newData)
                .filter(tuple2 -> "0".equals(tuple2.getT1().get("is_marketable")) && "1".equals(tuple2.getT2().get("is_marketable")))
                .doOnNext(tuple2 -> rabbitTemplate.convertAndSend(RabbitMQConfigure.GOODS_UP_EXCHANGE, "", tuple2.getT2().get("id")))
                .subscribe();
    }

    private Mono<HashMap<String, String>> getDataMap(List<CanalEntry.Column> columnsList) {
        return Mono.just(columnsList)
                .flatMap(columnList -> {
                    var map = new HashMap<String, String>();
                    columnList.forEach(column -> map.put(column.getName(), column.getValue()));
                    return Mono.just(map);
                });
    }
}
