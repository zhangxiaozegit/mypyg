package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationDao specDao;

    @Autowired
    private SpecificationOptionDao optionDao;

    @Override
    public PageResult findPage(Specification spec, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        //创建查询对象
        SpecificationQuery query = new SpecificationQuery();
        //创建sql语句中的where查询条件对象
        SpecificationQuery.Criteria criteria = query.createCriteria();
        if (spec != null) {
            if (spec.getSpecName() != null && !"".equals(spec.getSpecName())) {
                criteria.andSpecNameLike("%"+spec.getSpecName()+"%");
            }
        }

        Page<Specification> specList = (Page<Specification>)specDao.selectByExample(query);
        return new PageResult(specList.getTotal(), specList.getResult());
    }

    @Override
    public void add(SpecEntity specEntity) {
        //1. 添加规格对象
        specDao.insertSelective(specEntity.getSpecification());
        //2. 遍历规格选项集合
        if (specEntity.getSpecificationOptionList() != null) {
            for (SpecificationOption option : specEntity.getSpecificationOptionList()) {
                //使用规格表的主键, 作为规格选项表的外键
                option.setSpecId(specEntity.getSpecification().getId());
                //3. 添加规格选项对象
                optionDao.insertSelective(option);
            }
        }

    }

    @Override
    public SpecEntity findOne(Long id) {
        //1. 根据规格id, 查询规格对象
        Specification specification = specDao.selectByPrimaryKey(id);

        //2. 根据规格id, 查询规格选项集合对象
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = query.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<SpecificationOption> optionList = optionDao.selectByExample(query);

        //3. 封装实体对象返回
        SpecEntity entity = new SpecEntity();
        entity.setSpecification(specification);
        entity.setSpecificationOptionList(optionList);
        return entity;
    }

    @Override
    public void update(SpecEntity specEntity) {
        //1. 修改保存规格实体对象
        specDao.updateByPrimaryKeySelective(specEntity.getSpecification());

        //2. 根据规格主键id, 删除对应的规格选项集合
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = query.createCriteria();
        criteria.andSpecIdEqualTo(specEntity.getSpecification().getId());
        optionDao.deleteByExample(query);

        //3. 遍历页面传入的规格选项集合对象
        if (specEntity.getSpecificationOptionList() != null) {
            for (SpecificationOption option : specEntity.getSpecificationOptionList()) {

                //将规格id作为规格选项表的外键, 添加到对应的属性中
                option.setSpecId(specEntity.getSpecification().getId());
                //4. 将页面传入的新的规格选项对象添加到规格选项表中
                optionDao.insertSelective(option);
            }
        }

    }

    @Override
    public void delete(Long[] ids) {
        //1. 遍历id集合
        if (ids != null) {
            for (Long id : ids) {
                //2. 根据规格id删除规格数据
                specDao.deleteByPrimaryKey(id);

                //3. 根据规格id删除规格选项数据
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = query.createCriteria();
                criteria.andSpecIdEqualTo(id);
                optionDao.deleteByExample(query);
            }
        }

    }

    @Override
    public List<Map> selectOptionList() {
        return specDao.selectOptionList();
    }
}
