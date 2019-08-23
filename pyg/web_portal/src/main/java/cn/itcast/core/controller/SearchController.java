package cn.itcast.core.controller;

import cn.itcast.core.service.SearchService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 搜索业务(全文检索)
 */
@RestController
@RequestMapping("/itemsearch")
public class SearchController {

    @Reference
    private SearchService searchService;

    /**
     * 搜索
     * @param paramMap  页面传入进来的json格式参数
     * @return
     */
    @RequestMapping("/search")
    public Map<String, Object> search(@RequestBody Map paramMap) {
        Map<String, Object> resultMap = searchService.search(paramMap);
        return resultMap;
    }
}
