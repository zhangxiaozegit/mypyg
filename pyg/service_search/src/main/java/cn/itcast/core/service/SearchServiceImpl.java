package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

//    @Override
//    public Map<String, Object> search(Map paramMap) {
//        /**
//         * 获取查询条件参数
//         */
//        //查询关键字
//        String keywords = String.valueOf(paramMap.get("keywords"));
//        //当前页
//        Integer pageNo = Integer.parseInt(String.valueOf(paramMap.get("pageNo")));
//        //每页展示条数
//        Integer pageSize = Integer.parseInt(String.valueOf(paramMap.get("pageSize")));
//
//        /**
//         * 创建查询对象
//         */
//        Query query = new SimpleQuery();
//
//        /**
//         * 设置查询条件
//         * contains方法:
//         *      相当于数据库中的like模糊查询, 对需要查询的关键字不进行切分词, 整体模糊查询类似于 %关键字%
//         * is方法:
//         *      首先会对查询的关键字进行切分词, 将切分出来的词再进行一个一个查询, 然后将多个查询结果组合在一起返回
//         */
//        Criteria criteria = new Criteria("item_keywords").is(keywords);
//        //将条件放入查询对象中
//        query.addCriteria(criteria);
//
//        /**
//         * 设置分页条件
//         */
//        if (pageNo == null || "".equals(pageNo) || pageNo < 1) {
//            pageNo = 1;
//        }
//
//        //计算从第几条开始查询
//        Integer start = (pageNo - 1) * pageSize;
//
//        //从第几条开始查询
//        query.setOffset(start);
//        //每页查询多少条数据
//        query.setRows(pageSize);
//
//        /**
//         * 查询并获取结果
//         */
//        ScoredPage<Item> items = solrTemplate.queryForPage(query, Item.class);
//
//        /**
//         * 获取到的查询结果中的数据封装后返回
//         */
//        Map<String, Object> resultMap = new HashMap<>();
//        //查询到的结果集
//        resultMap.put("rows", items.getContent());
//        //查询到的总条数
//        resultMap.put("total", items.getTotalElements());
//        //查询到的总页数
//        resultMap.put("totalPages", items.getTotalPages());
//        return resultMap;
//    }

    @Override
    public Map<String, Object> search(Map paramMap) {
        /**
         * 获取查询参数
         */
        //获取用户选中的分类条件
        String categoryName = String.valueOf(paramMap.get("category"));

        /**
         * 1. 根据关键字  高亮,分页,排序,过滤查询
         */
        Map<String, Object> resultMap = hightPageQuery(paramMap);

        /**
         * 2. 根据关键字查询对分类进行分组, 目的是对分类去除重复
         */
        List<String> categoryNameList = findGroupByCategory(paramMap);
        resultMap.put("categoryList", categoryNameList);

        /**
         * 3. 根据关键字查询对应的品牌和规格
         */
        if (categoryName != null && !"".equals(categoryName)) {
            //根据用户选中的分类, 找对应的品牌集合和规格集合作为过滤条件
            Map<String, List> brandAndSpecList = findBrandListAndSpecListByCategoryName(categoryName);
            resultMap.putAll(brandAndSpecList);
        } else {
            if (categoryNameList != null && categoryNameList.size() > 0) {
                //如果用户没有选中具体分类, 则默认根据查询得到的分类集合中的第一个分类名称,
                // 找到对应的品牌集合和规格集合作为过滤条件
                categoryName = categoryNameList.get(0);
                Map<String, List> brandAndSpecList = findBrandListAndSpecListByCategoryName(categoryName);
                resultMap.putAll(brandAndSpecList);
            }
        }



        return resultMap;
    }

    /**
     * 高亮, 分页, 过滤, 排序查询
     * @param paramMap
     * @return
     */
    private Map<String, Object> hightPageQuery (Map paramMap) {
        /**
         * 获取查询条件参数
         */
        //查询关键字
        String keywords = String.valueOf(paramMap.get("keywords"));
        //当前页
        Integer pageNo = Integer.parseInt(String.valueOf(paramMap.get("pageNo")));
        //每页展示条数
        Integer pageSize = Integer.parseInt(String.valueOf(paramMap.get("pageSize")));
        //获取排序的域名
        String sortField = String.valueOf(paramMap.get("sortField"));
        //获取排序方式
        String sortType = String.valueOf(paramMap.get("sort"));
        //获取选中的分类名称
        String category = String.valueOf(paramMap.get("category"));
        //获取选中的品牌
        String brand = String.valueOf(paramMap.get("brand"));
        //获取选中的规格
        String spec = String.valueOf(paramMap.get("spec"));
        //获取选中的价格
        String price = String.valueOf(paramMap.get("price"));

        //多关键搜索, 将关键字中的空格全部去掉, 让solr使用默认的分词效果进行切分词
        if (keywords != null) {
            keywords = keywords.replaceAll(" ", "");
        }

        /**
         * 创建查询对象
         */
        HighlightQuery query = new SimpleHighlightQuery();

        /**
         * 设置查询条件
         */
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //将条件放入查询对象中
        query.addCriteria(criteria);

        /**
         * 设置分页条件
         */
        if (pageNo == null || "".equals(pageNo) || pageNo < 1) {
            pageNo = 1;
        }

        //计算从第几条开始查询
        Integer start = (pageNo - 1) * pageSize;

        //从第几条开始查询
        query.setOffset(start);
        //每页查询多少条数据
        query.setRows(pageSize);

        /**
         * 设置高亮
         */
        //创建高亮选项
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置需要高亮显示的域名
        highlightOptions.addField("item_title");
        //设置高亮前缀
        highlightOptions.setSimplePrefix("<em style=\"color:red\">");
        //设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //将高亮选项放入查询对象中
        query.setHighlightOptions(highlightOptions);

        /**
         * 排序
         */
        if (sortField != null && sortType != null && !"".equals(sortField) && !"".equals(sortType)) {
            //升序
            if ("ASC".equals(sortType)) {
                //创建排序对象
                Sort sort = new Sort(Sort.Direction.ASC, "item_"+ sortField);
                //将排序对象放入查询对象中
                query.addSort(sort);
            }
            //降序
            if ("DESC".equals(sortType)) {
                //创建排序对象
                Sort sort = new Sort(Sort.Direction.DESC, "item_"+ sortField);
                //将排序对象放入查询对象中
                query.addSort(sort);
            }
        }

        /**
         * 过滤查询
         */
        //根据选中的分类过滤
        if (category != null && !"".equals(category)) {
            //创建过滤查询对象
            FilterQuery filterQuery = new SimpleFilterQuery();
            //创建过滤条件对象
            Criteria filterCriteria = new Criteria("item_category").is(category);
            //将过滤条件放入过滤查询对象中
            filterQuery.addCriteria(filterCriteria);
            //将过滤查询对象放入查询对象中
            query.addFilterQuery(filterQuery);
        }

        //根据选中的品牌过滤
        if (brand != null && !"".equals(brand)) {
            //创建过滤查询对象
            FilterQuery filterQuery = new SimpleFilterQuery();
            //创建过滤条件对象
            Criteria filterCriteria = new Criteria("item_brand").is(brand);
            //将过滤条件放入过滤查询对象中
            filterQuery.addCriteria(filterCriteria);
            //将过滤查询对象放入查询对象中
            query.addFilterQuery(filterQuery);
        }

        //根据选中的规格过滤
        //获取到的规格数据: {"网络":"联通4G","机身内存":"128G"}
        if (spec != null && !"".equals(spec)) {
            //将选中的多个规格json字符串转换成Map
            Map<String, String> specMap = JSON.parseObject(spec, Map.class);
            if (specMap != null && specMap.size() > 0) {
                Set<Map.Entry<String, String>> entries = specMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    //创建过滤查询对象
                    FilterQuery filterQuery = new SimpleFilterQuery();
                    //创建过滤条件对象
                    Criteria filterCriteria = new Criteria("item_spec_" + entry.getKey()).is(entry.getValue());
                    //将过滤条件放入过滤查询对象中
                    filterQuery.addCriteria(filterCriteria);
                    //将过滤查询对象放入查询对象中
                    query.addFilterQuery(filterQuery);
                }
            }
        }

        //根据选中的价格过滤
        if (price != null && !"".equals(price)) {
            //进行切割获取价格最小值和最大值, priceArray[0]是最小值, priceArray[1]是最大值
            String[] priceArray = price.split("-");
            //第一个元素, 最小值不等于0, 则大于等于最小值
            if (!"0".equals(priceArray[0])) {
                //创建过滤查询对象
                FilterQuery filterQuery = new SimpleFilterQuery();
                //创建过滤条件对象
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(priceArray[0]);
                //将过滤条件放入过滤查询对象中
                filterQuery.addCriteria(filterCriteria);
                //将过滤查询对象放入查询对象中
                query.addFilterQuery(filterQuery);
            }

            //第二个元素, 最大值不等于*, 则小于等于最大值
            if (!"*".equals(priceArray[1])) {
                //创建过滤查询对象
                FilterQuery filterQuery = new SimpleFilterQuery();
                //创建过滤条件对象
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(priceArray[1]);
                //将过滤条件放入过滤查询对象中
                filterQuery.addCriteria(filterCriteria);
                //将过滤查询对象放入查询对象中
                query.addFilterQuery(filterQuery);
            }

        }

        /**
         * 查询并获取结果
         */
        HighlightPage<Item> items = solrTemplate.queryForHighlightPage(query, Item.class);

        //获取高亮结果集
        List<HighlightEntry<Item>> highlighted = items.getHighlighted();

        List<Item> resultList = new ArrayList<>();
        if (highlighted != null) {
            for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
                //不带高亮标题的实体对象
                Item item = itemHighlightEntry.getEntity();

                List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
                if (highlights != null && highlights.size() > 0) {
                    List<String> snipplets = highlights.get(0).getSnipplets();
                    if (snipplets != null && snipplets.size() > 0) {
                        //高亮标题
                        String hightTitle = snipplets.get(0);
                        //判断高亮标题不为null和空字符串
                        if (hightTitle != null && !"".equals(hightTitle)) {
                            //将高亮标题覆盖实体对象中不带高亮的标题
                            item.setTitle(hightTitle);
                        }
                    }
                }
                resultList.add(item);
            }
        }

        /**
         * 获取到的查询结果中的数据封装后返回
         */
        Map<String, Object> resultMap = new HashMap<>();
        //查询到的结果集
        resultMap.put("rows", resultList);
        //查询到的总条数
        resultMap.put("total", items.getTotalElements());
        //查询到的总页数
        resultMap.put("totalPages", items.getTotalPages());
        return resultMap;
    }

    /**
     * 根据关键字查询, 对分类进行分组去重, 结果只需要获取到的去重后的分类名称集合
     * @param paramMap
     * @return
     */
    private List<String> findGroupByCategory(Map paramMap) {
        /**
         * 1. 获取查询条件
         */
        //查询关键字
        String keywords = String.valueOf(paramMap.get("keywords"));

        //多关键搜索, 将关键字中的空格全部去掉, 让solr使用默认的分词效果进行切分词
        if (keywords != null) {
            keywords = keywords.replaceAll(" ", "");
        }

        /**
         * 2. 创建查询对象
         */
        Query query = new SimpleQuery();

        /**
         * 3. 设置查询条件
         */
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //将条件放入查询对象中
        query.addCriteria(criteria);

        /**
         * 4. 设置分组内容
         */
        //创建分组选项对象
        GroupOptions groupOptions = new GroupOptions();
        //设置根据分类域进行分组
        groupOptions.addGroupByField("item_category");
        //将分组选项放入查询对象中
        query.setGroupOptions(groupOptions);

        /**
         * 5. 查询并返回结果
         */
        GroupPage<Item> items = solrTemplate.queryForGroupPage(query, Item.class);

        /**
         * 6. 封装查询到的结果集返回
         */
        List<String> resultList = new ArrayList<>();
        //获取根据分类域进行分组的结果
        GroupResult<Item> item_category = items.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();
        List<GroupEntry<Item>> content = groupEntries.getContent();
        if (content != null) {
            for (GroupEntry<Item> itemGroupEntry : content) {
                resultList.add(itemGroupEntry.getGroupValue());
            }
        }
        return resultList;
    }

    /**
     * 根据分类名称查询对应的品牌集合和规格集合
     * @param categoryName  分类名称
     * @return
     */
    private Map<String, List> findBrandListAndSpecListByCategoryName(String categoryName) {
        //1. 根据分类名称到redis中获取对应的模板id
        Long templateId= (Long)redisTemplate.boundHashOps(Constants.REDIS_CATEGORYLIST).get(categoryName);

        //2. 根据模板id到redis中获取对应的品牌集合
        List<Map> brandList = (List<Map>)redisTemplate.boundHashOps(Constants.REDIS_BRANDLIST).get(templateId);

        //3. 根据模板id到redis中获取对应的规格集合
        List<Map> specList = (List<Map>)redisTemplate.boundHashOps(Constants.REDIS_SPECLIST).get(templateId);

        //4. 封装查询到的数据后返回
        Map<String, List> resultMap = new HashMap<>();
        resultMap.put("brandList", brandList);
        resultMap.put("specList",specList );
        return resultMap;
    }
}
