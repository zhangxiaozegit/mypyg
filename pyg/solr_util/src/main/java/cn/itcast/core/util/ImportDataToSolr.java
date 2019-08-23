package cn.itcast.core.util;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ImportDataToSolr {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 将库存表中审核通过的数据导入到solr索引库中
     */
    public void importItemToSolr () {
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        //审核状态为1的
        criteria.andStatusEqualTo("1");
        List<Item> itemList = itemDao.selectByExample(query);
        //遍历数据库获取到的库存集合数据
        if (itemList != null) {
            for (Item item : itemList) {
                //获取每一个库存对象的规格json字符串
                String specJsonStr = item.getSpec();
                //将规格json格式字符串解析成Map
                Map<String, String> jsonMap = JSON.parseObject(specJsonStr, Map.class);
                //将map数据放入Item的规格中
                item.setSpecMap(jsonMap);
            }
        }

        //保存库存集合数据到solr索引库中
        solrTemplate.saveBeans(itemList);
        //提交
        solrTemplate.commit();
    }

    /**
     * 这个工具项目的入口
     * 这个项目是jar包的项目, 写完后会运行mvn package命令打成jar包
     * 在服务器上可以使用java -jar xxxxxx.jar来运行jar包, 原理就是这个命令会调用这个jar包中的main方法
     * @param args
     */
    public static void main(String[] args) {
        //创建spring运行环境
        ApplicationContext applicaiton = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        //获取当前类的实例化对象
        ImportDataToSolr importDataToSolr = (ImportDataToSolr)applicaiton.getBean("importDataToSolr");
        //运行方法
        importDataToSolr.importItemToSolr();
    }
}
