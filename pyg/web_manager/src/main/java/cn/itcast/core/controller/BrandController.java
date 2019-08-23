package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 品牌管理
 * 品牌增删改查
 * @RestController注解作用: 相当于在类上加上@Controller注解, 然后这个类的所有方法的返回数据都会被自动转换成json格式字符串返回.
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<Brand> findAll() {
        List<Brand> list = brandService.findAll();
        return list;
    }

    /**
     * 分页查询
     * @param page  当前页数
     * @param rows  每页展示多少条数据
     * @return
     */
//    @RequestMapping("/findPage")
//    public PageResult findPage(Integer page, Integer rows) {
//        PageResult pageResult = brandService.findPage(page, rows);
//        return pageResult;
//    }

    /**
     * 添加
     * @param brand 添加的品牌对象
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Brand brand) {
        try {
            brandService.add(brand);
            return new Result(true, "保存成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "保存失败!");
        }
    }

    /**
     * 根据主键id, 查询实体类
     * @param id    主键id
     * @return
     */
    @RequestMapping("/findOne")
    public Brand findOne(Long id) {
        Brand one = brandService.findOne(id);
        return one;
    }

    /**
     * 保存修改
     * @param brand 品牌对象
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand) {
        try {
            brandService.update(brand);
            return new Result(true, "修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败!");
        }
    }

    /**
     * 删除
     * @param ids   需要删除的id数组
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            brandService.delete(ids);
            return new Result(true, "删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败!");
        }
    }

    /**
     * 高级分页查询
     * @param brand     查询对象
     * @param page      当前页
     * @param rows      每页展示数据条数
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody(required = false) Brand brand, Integer page, Integer rows) {
        PageResult pageResult = brandService.findPage(brand, page, rows);
        return pageResult;
    }

    /**
     * 模板中的select2下拉框使用
     * 返回的数据例如:
     *  List<Map>
     *      id:1
     *      text:联想
     *
     *      id:2
     *      text:华为
     *
     *      id:3
     *      text: 三星
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        List<Map> maps = brandService.selectOptionList();
        return maps;
    }
}
