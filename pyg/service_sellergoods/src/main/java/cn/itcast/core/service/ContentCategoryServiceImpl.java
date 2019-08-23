package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentCategoryDao;
import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.ad.ContentCategoryQuery;
import cn.itcast.core.pojo.entity.PageResult;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private ContentCategoryDao categoryDao;

    @Override
    public List<ContentCategory> findAll() {
        return categoryDao.selectByExample(null);
    }

    @Override
    public PageResult findPage(ContentCategory category, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);

        //创建查询对象
        ContentCategoryQuery query = new ContentCategoryQuery();
        //按照id降序排序
        query.setOrderByClause("id desc");
        //创建sql语句中的where查询条件对象
        ContentCategoryQuery.Criteria criteria = query.createCriteria();
        if (category != null) {
           if (category.getName() != null && !"".equals(category.getName())) {
               criteria.andNameLike("%"+category.getName()+"%");
           }
        }

        //查询并返回结果
        Page<ContentCategory> categoryList = (Page<ContentCategory>)categoryDao.selectByExample(query);
        //从分页助手集合对象中提取我们需要的数据, 封装成PageResult对象返回
        return new PageResult(categoryList.getTotal(), categoryList.getResult());
    }

    @Override
    public void add(ContentCategory category) {
        //不判断传入对象的属性是否为null, 所有属性必须有值, 要么会执行失败报错
        //brandDao.insert(brand);
        //会判断传入的对象中的每一个属性是否为null, 如果不为null才会参与拼接sql语句
        categoryDao.insertSelective(category);
    }

    @Override
    public ContentCategory findOne(Long id) {
        ContentCategory category = categoryDao.selectByPrimaryKey(id);
        return category;
    }

    @Override
    public void update(ContentCategory category) {
        //根据主键id作为修改条件, 并且会对传入的对象中的每个属性进行判断是否为null
        categoryDao.updateByPrimaryKeySelective(category);
        //根据非主键作为修改条件, 并且会对传入的对象中的每个属性进行判断是否为null
        //brandDao.updateByExampleSelective(, );
        //根据主键id作为修改条件,不会判断传入对象的属性是否为null
        //brandDao.updateByPrimaryKey();
        //根据非主键作为修改条件, 不会判断传入对象的属性是否为null
        //brandDao.updateByExample(, );
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                //根据主键id删除
                categoryDao.deleteByPrimaryKey(id);
                //根据非主键作为删除条件, 进行查询
                //brandDao.deleteByExample();

            }
        }
    }


}
