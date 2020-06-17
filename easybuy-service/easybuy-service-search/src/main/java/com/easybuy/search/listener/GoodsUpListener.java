package com.easybuy.search.listener;

import com.easybuy.search.config.RabbitMQConfigure;
import com.easybuy.search.service.ESManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsUpListener {
    @Autowired
    private ESManagerService esManagerService;

    @RabbitListener(queues = RabbitMQConfigure.SEARCH_ADD_QUEUE)
    public void receiveMessage(String spuId){
        System.out.println("接收到的消息为：" + spuId);
        //查询skuList，并导入到索引库
        esManagerService.importDataBySpuId(spuId);
    }
}
