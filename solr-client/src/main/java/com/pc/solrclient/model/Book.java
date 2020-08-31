package com.pc.solrclient.model;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * 实体类上的注解@field和@SolrDocument 是为了QueryResponse.getBeans()能转化成相关实体类，不然无法识别
 *
 * @author pengchao
 * @date 20:06 2020-08-10
 */
@SolrDocument(solrCoreName = "new_core")
public class Book {

    @Id
    @Field
    private String id;

    @Field("description")
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
