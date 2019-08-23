package cn.itcast.core.listener;

import cn.itcast.core.service.SolrManagerService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 自定义监听器
 * 监听消息服务器, 发送来的消息, 也就是商品id
 * 根据商品id, 获取数据库中商品的详细数据, 然后放入solr索引库中, 供前台系统搜索使用
 */
public class ItemSearchListener implements MessageListener{

    @Autowired
    private SolrManagerService solrManagerService;

    @Override
    public void onMessage(Message message) {
        //为了方便获取文本消息, 将jdk底层的消息对象强转成activeMq的文本消息对象
        ActiveMQTextMessage atm = (ActiveMQTextMessage)message;
        try {
            //获取文本消息, 商品id
            String goodsId = atm.getText();
            //根据商品id获取商品详细数据, 放入solr索引库供前台系统搜索使用
            solrManagerService.addItemToSolr(Long.parseLong(goodsId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
