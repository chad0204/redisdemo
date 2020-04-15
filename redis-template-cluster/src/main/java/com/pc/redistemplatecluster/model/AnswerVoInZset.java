package com.pc.redistemplatecluster.model;

import org.springframework.data.redis.core.ZSetOperations;

/**
 * 分值对象
 *
 * @author dongxie
 * @date 14:16 2020-04-12
 */
public class AnswerVoInZset implements ZSetOperations.TypedTuple<String> {
    private String name;
    private double score;

    public AnswerVoInZset(String name, double score) {
        this.name = name;
        this.score = score;
    }


    @Override
    public String getValue() {
        return name;
    }

    @Override
    public Double getScore() {
        return score;
    }

    @Override
    public int compareTo(ZSetOperations.TypedTuple<String> o) {
        return 0;
    }
}
