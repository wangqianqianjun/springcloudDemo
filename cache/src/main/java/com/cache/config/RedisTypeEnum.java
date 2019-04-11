package com.cache.config;

/**
 * Created by wangqianjun on 2019/3/26.
 */
public enum RedisTypeEnum {

    String,
    List,
    Hash,
    Set,
    Zset,
    Geo,
    HyperLogLog;

    private RedisTypeEnum() {

    }
}
