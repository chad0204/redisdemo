package com.pc.cacheloader.handler.es;

import com.alibaba.fastjson.JSONObject;
import com.pc.cacheloader.cache.Level2Cache;
import com.pc.cacheloader.model.BaseDO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 */
@Slf4j
public abstract class EsCacheHandler<T extends BaseDO> implements Level2Cache<T> {

    private TransportClient client;

    public EsCacheHandler(TransportClient transportClient) {
        this.client = transportClient;
    }

    //创建索引库
    public void createIndexResponse(String indexName, String type, String jsonData, String id) {
        IndexRequestBuilder requestBuilder = client.prepareIndex(indexName, type);
        requestBuilder
                .setId(id)
                .setSource(jsonData)
                .execute().actionGet();
    }

    public void clearIndex() {
        BulkByScrollResponse response =
                DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                        .filter(QueryBuilders.matchAllQuery())
                        .source("ids")
                        .get();
        response.getDeleted();
    }

    public boolean updateIndex(String indexName, String type, String json, String id) {
        System.out.println("es updateIndex");
        return true;
    }

    //判断符合条件的索引是否存在
    public Boolean isIndexDocumentExit(String indexName, String type, String key) {
        try {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.existsQuery(key));
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).setTypes(type);

            searchRequestBuilder.setQuery(boolQueryBuilder);
            SearchResponse searchResponse = searchRequestBuilder.setSize(1).execute().get();
            return searchResponse.getHits().totalHits > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //删除索引
    public Boolean deleteIndex(String indexName, String type, String key, String value) {

        try {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery(key, value));

            BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).setTypes(type);

            searchRequestBuilder.setQuery(boolQueryBuilder);
            SearchResponse searchResponse = searchRequestBuilder.execute().get();
            for (SearchHit hit : searchResponse.getHits()) {
                String id = hit.getId();
                bulkRequestBuilder.add(client.prepareDelete(indexName, type, id));
            }
            if (bulkRequestBuilder.numberOfActions() > 0) {
                BulkResponse responses = bulkRequestBuilder.get();
                if (responses.hasFailures()) {
                    for (BulkItemResponse response : responses.getItems()) {
                        log.error(response.getFailureMessage());
                    }
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param queryBuilder
     * @param indexName
     * @param type
     * @param sort
     * @return
     */
    public List<T> searchObj(QueryBuilder queryBuilder, String indexName, String type, String sort, SortOrder sortOrder) {
        List<T> list = new ArrayList<>();
        Type tp = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).setSize(10000).setTypes(type)
                .setQuery(queryBuilder);
        if (sortOrder != null) {
            searchRequestBuilder.addSort(SortBuilders.fieldSort(sort).order(sortOrder));
        }
        SearchResponse searchResponse =
                searchRequestBuilder
                        .execute()
                        .actionGet();
        if (null != searchResponse) {
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHists = hits.getHits();
            for (SearchHit hit : searchHists) {
                String obj = hit.getSourceAsString();
                T t = JSONObject.parseObject(obj, (Class<T>) tp);
                list.add(t);
            }
        }
        return list;
    }


    /**
     * @param t
     * @return
     */
    public String objToEsJson(T t) {
        return null;
    }

}
