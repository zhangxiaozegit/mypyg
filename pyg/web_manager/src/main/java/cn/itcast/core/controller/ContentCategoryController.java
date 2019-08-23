package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.ContentCategoryService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 广告分类管理
 */
@RestController
@RequestMapping("/contentCategory")
public class ContentCategoryController {

    @Reference
    private ContentCategoryService categoryService;

    @RequestMapping("/findAll")
    public List<ContentCategory> findAll() {
        List<ContentCategory> list = categoryService.findAll();
        return list;
    }

    /**
     * 添加
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody ContentCategory category) {
        try {
            categoryService.add(category);
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
    public ContentCategory findOne(Long id) {
        ContentCategory one = categoryService.findOne(id);
        return one;
    }

    /**
     * 保存修改
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody ContentCategory category) {
        try {
            categoryService.update(category);
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
            categoryService.delete(ids);
            return new Result(true, "删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败!");
        }
    }

    /**
     * 高级分页查询
     * @param page      当前页
     * @param rows      每页展示数据条数
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody(required = false) ContentCategory category, Integer page, Integer rows) {
        PageResult pageResult = categoryService.findPage(category, page, rows);
        return pageResult;
    }

}
