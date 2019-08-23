package cn.itcast.core.test;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

//加载spring测试环境
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/applicationContext*.xml"})
public class TestBrandDao {

    @Autowired
    private BrandDao brandDao;

    @Test
    public void testFindById() {
        Brand brand = brandDao.selectByPrimaryKey(1L);
        System.out.println("======" + brand);
    }

    @Test
    public void testByQuery() {
        //1. 创建查询对象
        BrandQuery query = new BrandQuery();
        //设置排序
        query.setOrderByClause("id desc");
        //开启去重
        query.setDistinct(true);
        //设置需要查询出来的字段
        //query.setFields("id, name");
        //2. 创建where条件查询对象
        BrandQuery.Criteria criteria = query.createCriteria();
        //根据名称模糊查询
        criteria.andNameLike("%华%");

        //查询并返回结果
        List<Brand> brands = brandDao.selectByExample(query);
        System.out.println("=======" + brands);
    }
}
