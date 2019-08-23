package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentDao contentDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Content> findAll() {
        return contentDao.selectByExample(null);
    }

    @Override
    public PageResult findPage(Content content, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);

        //创建查询对象
        ContentQuery query = new ContentQuery();
        //按照id降序排序
        query.setOrderByClause("id desc");
        //创建sql语句中的where查询条件对象
        ContentQuery.Criteria criteria = query.createCriteria();
        if (content != null) {
           if (content.getTitle() != null && !"".equals(content.getTitle())) {
               criteria.andTitleLike("%"+content.getTitle()+"%");
           }
        }

        //查询并返回结果
        Page<Content> categoryList = (Page<Content>)contentDao.selectByExample(query);
        //从分页助手集合对象中提取我们需要的数据, 封装成PageResult对象返回
        return new PageResult(categoryList.getTotal(), categoryList.getResult());
    }

    @Override
    public void add(Content content) {
        //1. 向数据库中添加广告数据
        contentDao.insertSelective(content);
        //2. 根据广告分类id, 删除redis中对应的广告集合数据
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content.getCategoryId());

    }

    @Override
    public Content findOne(Long id) {
        Content content = contentDao.selectByPrimaryKey(id);
        return content;
    }

    @Override
    public void update(Content content) {
        //1. 根据广告主键id, 查询mysql数据库中广告对象(没有更新前的老对象)
        Content oldContent = contentDao.selectByPrimaryKey(content.getId());
        //2. 根据老的广告对象中的分类id, 清除redis中对应的广告集合数据
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(oldContent.getCategoryId());
        //3. 根据页面传入进来的新广告对象中的分类id, 清除redis中对应的广告集合数据
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content.getCategoryId());
        //4. 将页面传入的新的广告对象更新到mysql数据库中
        contentDao.updateByPrimaryKeySelective(content);

    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                //1. 根据广告id, 到数据库中查询对应的广告对象
                Content content = contentDao.selectByPrimaryKey(id);
                //2. 根据广告对象中的分类id, 删除redis中对应的广告集合数据
                redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content.getCategoryId());
                //3. 根据主键id删除数据库中的广告数据
                contentDao.deleteByPrimaryKey(id);


            }
        }
    }

    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        ContentQuery query = new ContentQuery();
        query.setOrderByClause("sort_order desc");
        ContentQuery.Criteria criteria = query.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        List<Content> contentList = contentDao.selectByExample(query);
        return contentList;
    }

    /**
     * 一个广告分类id, 对应一堆的广告数据就是List<Content>
     *     使用Hash类型存入广告数据
     *     key              value(Hash)
     *     contentList      广告分类id(小key)    对应的广告集合数据List<Content>
     *
     */
    @Override
    public List<Content> findByCategoryIdFromRedis(Long categoryId) {
        //1. 根据分类id从redis中获取广告集合数据
        List<Content> contentList = (List<Content>)redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).get(categoryId);

        //2. 获取不到数据, 从mysql数据库中查询
        if (contentList == null || contentList.size() == 0) {
            //3. mysql中查询到数据后, 存入redis中一份
            contentList = findByCategoryId(categoryId);
            redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).put(categoryId, contentList);
        }

        //4. 返回数据
        return contentList;
    }


}
