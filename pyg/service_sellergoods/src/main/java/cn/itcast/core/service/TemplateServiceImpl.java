package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TypeTemplateDao templateDao;

    @Autowired
    private SpecificationOptionDao optionDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageResult findPage(TypeTemplate template, Integer page, Integer rows) {
        /**
         * 缓存模板中的数据到redis中, 供前台搜索使用
         */
        List<TypeTemplate> typeTemplates = templateDao.selectByExample(null);
        if (typeTemplates != null) {
            for (TypeTemplate typeTemplate : typeTemplates) {
                //1.获取品牌json字符串
                String brandJsonStr = typeTemplate.getBrandIds();
                //2. 将品牌json字符串转换成集合
                List<Map> brandList = JSON.parseArray(brandJsonStr, Map.class);
                //3. 将模板id作为小key, 品牌集合作为value缓存入redis中
                redisTemplate.boundHashOps(Constants.REDIS_BRANDLIST).put(typeTemplate.getId(), brandList);

                //4. 根据模板id, 获取规格以及规格选项集合数据
                List<Map> specList = findBySpecList(typeTemplate.getId());
                redisTemplate.boundHashOps(Constants.REDIS_SPECLIST).put(typeTemplate.getId(), specList);
            }
        }

        /**
         * 分页查询
         */
        PageHelper.startPage(page, rows );
        //创建查询对象
        TypeTemplateQuery query = new TypeTemplateQuery();
        //创建sql语句的where查询条件对象
        TypeTemplateQuery.Criteria criteria = query.createCriteria();
        if (template != null) {
            if (template.getName() != null && !"".equals(template.getName())) {
                criteria.andNameLike("%"+template.getName()+"%");
            }
        }

        Page<TypeTemplate> templateList = (Page<TypeTemplate>)templateDao.selectByExample(query);
        return new PageResult(templateList.getTotal(), templateList.getResult());
    }

    @Override
    public void add(TypeTemplate template) {
        templateDao.insertSelective(template);
    }

    @Override
    public void update(TypeTemplate template) {
        templateDao.updateByPrimaryKeySelective(template);
    }

    @Override
    public TypeTemplate findOne(Long id) {
        return templateDao.selectByPrimaryKey(id);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                templateDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 根据模板id, 查询对应的规格集合和规格选项集合数据
     * @param id    模板id
     * @return
     */
    @Override
    public List<Map> findBySpecList(Long id) {
        //1. 根据模板id, 查询对应模板实体对象
        TypeTemplate template = templateDao.selectByPrimaryKey(id);
        //2. 找到模板实体对象后获取规格json格式字符串
        String specJsonStr = template.getSpecIds();
        //3. 解析json格式字符串为java对象
        List<Map> specList = JSON.parseArray(specJsonStr, Map.class);
        //4. 遍历规格集合
        if (specList != null) {
            for (Map specMap : specList) {
                //5. 根据规格对象中的规格id, 查询对应的规格选项集合
                Long specId = Long.parseLong(String.valueOf(specMap.get("id")));
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = query.createCriteria();
                criteria.andSpecIdEqualTo(specId);
                List<SpecificationOption> optionList = optionDao.selectByExample(query);
                //6. 将规格选项集合封装到规格对象中返回
                specMap.put("options", optionList);
            }
        }

        return specList;
    }
}
