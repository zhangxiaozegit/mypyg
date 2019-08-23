package cn.itcast.core.listener;

import cn.itcast.core.service.SolrManagerService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 自定义监听器
 * 监听来自于消息服务器发送过来的消息也就是商品id, 根据商品id,到solr索引库删除对应的数据
 */
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private SolrManagerService solrManagerService;

    @Override
    public void onMessage(Message message) {
        //将原生的消息对象强转成activeMq的文本消息对象, 为了方便获取文本消息
        ActiveMQTextMessage atm = (ActiveMQTextMessage)message;
        try {
            //1. 获取文本消息, 商品id
            String goodsId = atm.getText();
            //2. 到solr索引库中删除
            solrManagerService.deleteItemByGoodsId(Long.parseLong(goodsId));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
