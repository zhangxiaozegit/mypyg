package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandDao brandDao;

    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

    @Override
    public PageResult findPage(Brand brand, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);

        //创建查询对象
        BrandQuery query = new BrandQuery();
        //按照id降序排序
        query.setOrderByClause("id desc");
        //创建sql语句中的where查询条件对象
        BrandQuery.Criteria criteria = query.createCriteria();
        if (brand != null) {
            //按照名称模糊查询
            if (brand.getName() != null && !"".equals(brand.getName())) {
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if (brand.getFirstChar() != null && !"".equals(brand.getFirstChar())) {
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }

        //查询并返回结果
        Page<Brand> brandList = (Page<Brand>)brandDao.selectByExample(query);
        //从分页助手集合对象中提取我们需要的数据, 封装成PageResult对象返回
        return new PageResult(brandList.getTotal(), brandList.getResult());
    }

    @Override
    public void add(Brand brand) {
        //不判断传入对象的属性是否为null, 所有属性必须有值, 要么会执行失败报错
        //brandDao.insert(brand);
        //会判断传入的对象中的每一个属性是否为null, 如果不为null才会参与拼接sql语句
        brandDao.insertSelective(brand);
    }

    @Override
    public Brand findOne(Long id) {
        Brand brand = brandDao.selectByPrimaryKey(id);
        return brand;
    }

    @Override
    public void update(Brand brand) {
        //根据主键id作为修改条件, 并且会对传入的对象中的每个属性进行判断是否为null
        brandDao.updateByPrimaryKeySelective(brand);
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
                brandDao.deleteByPrimaryKey(id);
                //根据非主键作为删除条件, 进行查询
                //brandDao.deleteByExample();

            }
        }
    }

    @Override
    public List<Map> selectOptionList() {
        return brandDao.selectOptionList();
    }
}
