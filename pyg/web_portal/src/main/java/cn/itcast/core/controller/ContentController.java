package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {

    @Reference
    private ContentService contentService;

    /**
     * 根据广告分类id, 查询广告集合数据返回
     * @param categoryId
     * @return
     */
    @RequestMapping("/findByCategoryId")
    public List<Content> findByCategoryId(Long categoryId) {
        //从数据库中获取广告数据
        //List<Content> contentList = contentService.findByCategoryId(categoryId);
        //根据分类id, 到redis中获取广告集合数据
        List<Content> contentList = contentService.findByCategoryIdFromRedis(categoryId);
        return contentList;
    }
}
