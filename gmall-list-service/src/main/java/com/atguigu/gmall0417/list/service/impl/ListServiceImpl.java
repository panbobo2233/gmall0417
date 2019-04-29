package com.atguigu.gmall0417.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0417.bean.SkuInfo;
import com.atguigu.gmall0417.bean.SkuLsInfo;
import com.atguigu.gmall0417.bean.SkuLsParams;
import com.atguigu.gmall0417.bean.SkuLsResult;
import com.atguigu.gmall0417.config.RedisUtil;
import com.atguigu.gmall0417.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.ibatis.annotations.Param;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    //以前这里需要写mapper,是因为需要从数据库取数据

    @Autowired
    private JestClient jestClient;

    @Autowired
    private RedisUtil redisUtil;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";

    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        //保存到es,必须知道index,type
        //如何向es中添加数据，查询Search.Build(quary)
        //PUT /gmall/SkuInfo/1
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        //执行操作
        try{
            jestClient.execute(index);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
        //先写dsl语句，使用java代码写
        String query=makeQueryStringForSearch(skuLsParams);
        //执行

        //准备
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult =null;
        //执行
        try {
            //执行的结果（也就是kibana右边的结果）
            //searchResult --变成我们封装好的结果集
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkuLsResult skuLsResult =  makeResultForSearch(skuLsParams, searchResult);
        //这才是我们想要的数据，controller中将该数据结果集渲染到页面
        return  skuLsResult ;
    }

    //searchResult --变成我们封装好的结果集
    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {
        SkuLsResult skuLsResult = new SkuLsResult();
        //将skulsresult对象的属性分别赋值 赋值，值从哪里来？searchResult
        /**
         * List<SkuLsInfo> skuLsInfoList
         * long total;
         * long totalPages;
         * List<String> attrValueIdList;
         */
        ArrayList<SkuLsInfo> skuLsInfoArrayList = new ArrayList<>();
        //获取，循环查出结果集
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo,Void>hit:hits){
            //获取对象
            SkuLsInfo skuLsInfo = hit.source;//取完数据时，数据是高亮的么
            //取出高亮字段skuName将原来的对象值进行覆盖
            //hit.highlight是map
            if(hit.highlight!=null && hit.highlight.size()>0){
                List<String> list = hit.highlight.get("skuName");
                String skuNameHl = list.get(0);
                skuLsInfo.setSkuName(skuNameHl);
            }
            //将skulsinfo 添加到skulsinfoarraylist集合中
            skuLsInfoArrayList.add(skuLsInfo);
        }

        //skulsinfo对象--
        skuLsResult.setSkuLsInfoList(skuLsInfoArrayList);
        //总数
        skuLsResult.setTotal(searchResult.getTotal());//这里不太理解
        //总页数
        //long totalPage=(searchResult.getTotal()%skuLsParams.getPageSize()==0?searchResult.getTotal()/skuLsParams.getPageSize():(searchResult.getTotal()/skuLsParams.getPageSize()+1));
        //这条公式好好理解一下
        long totalPage = (searchResult.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPage);

        //平台属性 实际从聚合中取得
        ArrayList<String> arrayList = new ArrayList<>();
        //取得数据
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        for (TermsAggregation.Entry bucket:buckets){
            String valueId = bucket.getKey();
            arrayList.add(valueId);
        }
        //平台属性值添加
        skuLsResult.setAttrValueIdList(arrayList);
        return skuLsResult;
    }

    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        //query bool filter
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //skuName 作为关键字
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);

//            设置高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();
            highlighter.field("skuName");
            highlighter.preTags("<span style='color:red'>");
            highlighter.postTags("</span>");

            //添加高亮
            searchSourceBuilder.highlight(highlighter);
        }
        //三级分类id
        if(skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0 ){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id());
            //bool:filter:term
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //平台属性值
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            for (int i=0;i<skuLsParams.getValueId().length;i++){
                //获取平台属性值
                String valueId = skuLsParams.getValueId()[i];
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        //设置分页
        //从哪里开始？pageNo=（pageNo -1 ）*pageSize
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());
        //设置排序 mysql默认是升序， oracle默认是降序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        //设置聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);

        //执行query方法
        searchSourceBuilder.query(boolQueryBuilder);

        String query = searchSourceBuilder.toString();
        System.out.println("query="+query);
        return query;
    }

    @Override
    public void incrHotScore(String skuId) {
        //获取redis
        Jedis jedis = redisUtil.getJedis();

        //更新规则
        int timesToEs=10;
        //对redis中商品进行次数累加
        Double hotScore =  jedis.zincrby("hotScore",1,"skuId"+skuId);
        if (hotScore%timesToEs==0){
            //更新es
            updateHotScore(skuId,Math.round(hotScore));

        }
    }

    private void updateHotScore(String skuId, long hotScore) {
        String updateJson="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";

        Update update = new Update.Builder(updateJson).index("gmall").type("SkuInfo").id(skuId).build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
