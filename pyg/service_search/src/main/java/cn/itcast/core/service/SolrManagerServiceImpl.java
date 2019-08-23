package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;

import java.util.List;
import java.util.Map;

@Service
public class SolrManagerServiceImpl implements  SolrManagerService{

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private ItemDao itemDao;

    @Override
    public void addItemToSolr(Long goodsId) {
        /**
         * 1. 根据商品id查询对应的库存集合数据
         */
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(goodsId);
        List<Item> itemList = itemDao.selectByExample(query);

        /**
         * 2. 将库存集合数据放入solr索引库中
         */
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

    @Override
    public void deleteItemByGoodsId(Long goodsId) {
        //创建查询对象
        Query query = new SimpleQuery();
        //创建条件对象
        Criteria criteria = new Criteria("item_goodsid").is(goodsId);
        //将条件对象放入查询对象中
        query.addCriteria(criteria);
        //根据查询对象删除
        solrTemplate.delete(query);
        //提交
        solrTemplate.commit();
    }
}
