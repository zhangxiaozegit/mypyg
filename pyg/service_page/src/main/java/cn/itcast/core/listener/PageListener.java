package cn.itcast.core.listener;

import cn.itcast.core.service.PageService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;

/**
 * 自定义监听器,
 * 监听消息服务器发送来的消息, 商品id, 根据商品id, 获取商品详细数据对象再根据模板生成静态化页面
 */
public class PageListener implements MessageListener {

    @Autowired
    private PageService pageService;

    @Override
    public void onMessage(Message message) {
        //1. 将消息对象强转成activeMq的文本消息对象方便获取文本类型的消息数据
        ActiveMQTextMessage atm =(ActiveMQTextMessage)message;
        try {
            //2. 获取文本消息, 商品id
            String goodsId = atm.getText();
            //3. 根据商品id获取生成静态页面所需要的商品详细数据
            Map<String, Object> rootMap = pageService.findGoodsData(Long.parseLong(goodsId));
            //4. 根据数据和模板生成静态化页面
            pageService.createStaticPage(Long.parseLong(goodsId), rootMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
