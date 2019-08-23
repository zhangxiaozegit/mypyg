package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GoodsServiceImpl implements  GoodsService {

    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private GoodsDescDao descDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private SellerDao sellerDao;

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private ItemCatDao catDao;

    @Autowired
    private JmsTemplate jmsTemplate;

    //注入商品上架存放消息的队列对象
    @Autowired
    private ActiveMQTopic topicPageAndSolrDestination;

    //注入商品下架存放消息的队列对象
    @Autowired
    private ActiveMQQueue queueSolrDeleteDestination;

    @Override
    public PageResult search(Goods goods, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        //创建查询条件对象
        GoodsQuery query = new GoodsQuery();
        //创建where条件对象
        GoodsQuery.Criteria criteria = query.createCriteria();
        if (goods != null) {
            //根据商品名称模糊查询
            if (goods.getGoodsName() != null && !"".equals(goods.getGoodsName())) {
                criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
            }
            //根据状态精确查询
            if (goods.getAuditStatus() != null && !"".equals(goods.getAuditStatus())) {
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            //根据账户名称精确查询自己添加的商品, 如果是管理员账户则查询所有商品
            if (goods.getSellerId() != null && !"".equals(goods.getSellerId())
                    && !"admin".equals(goods.getSellerId()) && !"wc".equals(goods.getSellerId())) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            //查询商品的删除状态为未删除的数据
            criteria.andIsDeleteIsNull();
        }

        Page<Goods> goodsList = (Page<Goods>)goodsDao.selectByExample(query);
        return new PageResult(goodsList.getTotal(), goodsList.getResult());
    }

    @Override
    public void add(GoodsEntity goodsEntity) {
        /**
         * 1. 保存商品对象
         */
        //新添加的商品审核状态默认为0, 未审核
        goodsEntity.getGoods().setAuditStatus("0");
        goodsDao.insertSelective(goodsEntity.getGoods());

        /**
         * 2. 保存商品详情对象
         */
        //获取商品自增的主键id, 放入商品详情表, 作为商品详情的主键id
        goodsEntity.getGoodsDesc().setGoodsId(goodsEntity.getGoods().getId());
        descDao.insertSelective(goodsEntity.getGoodsDesc());

        /**
         * 3. 遍历库存集合, 保存库存对象
         */
        insertItemList(goodsEntity);

    }

    @Override
    public GoodsEntity findOne(Long id) {
        //1. 根据商品id查询商品对象
        Goods goods = goodsDao.selectByPrimaryKey(id);

        //2. 根据商品id查询商品详情对象
        GoodsDesc goodsDesc = descDao.selectByPrimaryKey(id);

        //3. 根据商品id查询库存集合对象
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(query);

        //4. 将查询到的对象封装到商品实体中
        GoodsEntity entity = new GoodsEntity();
        entity.setGoods(goods);
        entity.setGoodsDesc(goodsDesc);
        entity.setItemList(itemList);
        return entity;
    }

    @Override
    public void update(GoodsEntity goodsEntity) {
        //1. 修改保存商品对象
        goodsDao.updateByPrimaryKeySelective(goodsEntity.getGoods());

        //2. 修改保存商品详情对象
        descDao.updateByPrimaryKeySelective(goodsEntity.getGoodsDesc());

        //3. 根据商品id删除对应的库存集合数据
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(goodsEntity.getGoods().getId());
        itemDao.deleteByExample(query);

        //4. 添加页面存入的库存对象
        insertItemList(goodsEntity);
    }

    @Override
    public void delete(final Long id) {
        /**
         * 1. 根据商品id到数据库中逻辑删除
         */
        Goods goods = new Goods();
        //商品id, 作为修改条件
        goods.setId(id);
        //设置删除状态为1, 已删除
        goods.setIsDelete("1");
        goodsDao.updateByPrimaryKeySelective(goods);

        /**
         * 2. 将商品id作为消息发送给消息服务器的下架队列中
         */
        jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                return textMessage;
            }
        });

    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        //1. 遍历商品id数组
        if (ids != null) {
            for (final Long goodsId : ids) {
                //2. 修改商品状态
                Goods goods = new Goods();
                goods.setId(goodsId);
                goods.setAuditStatus(status);
                goodsDao.updateByPrimaryKeySelective(goods);

                //3. 修改库存状态
                //创建修改内容对象
                Item item = new Item();
                item.setStatus(status);

                //创建查询对象, 设置修改条件
                ItemQuery query = new ItemQuery();
                ItemQuery.Criteria criteria = query.createCriteria();
                criteria.andGoodsIdEqualTo(goodsId);
                itemDao.updateByExampleSelective(item, query);

                //4. 判断审核是否通过
                if ("1".equals(status)) {
                    //将审核通过的商品id作为消息发送给消息服务器
                    jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            TextMessage textMessage = session.createTextMessage(String.valueOf(goodsId));
                            return textMessage;
                        }
                    });
                }
            }
        }

    }

    /**
     * 设置库存对象的属性
     * @param item          需要设置的库存对象
     * @param goodsEntity   页面传入的商品实体对象
     * @return
     */
    private Item setItemValues(Item item, GoodsEntity goodsEntity) {
        //商品id
        item.setGoodsId(goodsEntity.getGoods().getId());
        //新添加的库存数据审核状态默认为0, 未审核
        item.setStatus("0");
        //卖家id
        item.setSellerId(goodsEntity.getGoods().getSellerId());
        //卖家名称
        Seller seller = sellerDao.selectByPrimaryKey(goodsEntity.getGoods().getSellerId());
        item.setSeller(seller.getName());
        //品牌名称
        Brand brand = brandDao.selectByPrimaryKey(goodsEntity.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //分类id
        item.setCategoryid(goodsEntity.getGoods().getCategory3Id());
        //分类名称
        ItemCat itemCat = catDao.selectByPrimaryKey(goodsEntity.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());
        //创建时间
        item.setCreateTime(new Date());
        //更新时间
        item.setUpdateTime(new Date());
        //商品示例图片
        String imgJsonStr = goodsEntity.getGoodsDesc().getItemImages();
        List<Map> imgList = JSON.parseArray(imgJsonStr, Map.class);
        if (imgList != null && imgList.size() > 0) {
            item.setImage(String.valueOf(imgList.get(0).get("url")));
        }
        return item;
    }

    /**
     * 添加库存集合到数据库
     * @param goodsEntity   页面传入的实体对象
     */
    private void insertItemList(GoodsEntity goodsEntity) {
        //判断是否启用规格, 也就是是否有库存
        if ("1".equals(goodsEntity.getGoods().getIsEnableSpec())) {
            //启动规格
            if (goodsEntity.getItemList() != null) {
                for (Item item : goodsEntity.getItemList()) {
                    //标题: 商品名称 + 规格组成标题
                    String title = goodsEntity.getGoods().getGoodsName();
                    String specJsonStr = item.getSpec();
                    Map<String, String> specMap = JSON.parseObject(specJsonStr, Map.class);
                    if (specMap != null) {
                        Collection<String> values = specMap.values();
                        if (values != null) {
                            for (String specValue : values) {
                                title += " " + specValue;
                            }
                        }
                    }
                    item.setTitle(title);

                    //设置item库存对象的属性
                    item = setItemValues(item, goodsEntity);

                    //4. 保存库存集合对象
                    itemDao.insertSelective(item);
                }
            }
        } else{
            //未启用规格
            //创建初始化规格对象
            Item item = new Item();
            //使用商品名称作为初始化库存标题
            item.setTitle(goodsEntity.getGoods().getGoodsName());
            //设置item库存对象的属性
            item = setItemValues(item, goodsEntity);

            //初始化规格
            item.setSpec("{}");
            //初始化价格
            item.setPrice(new BigDecimal("99999999999"));
            //初始化库存量
            item.setNum(0);
            //初始化是否默认
            item.setIsDefault("1");

            //保存初始化规格对象
            itemDao.insertSelective(item);

        }
    }
}
