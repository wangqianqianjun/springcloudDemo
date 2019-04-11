package com.cache.config;

/**
 * @author wangqianjun
 * Created by wangqianjun on 2019/3/26.
 */
public interface CacheConstant {
    /**
     * 后缀
     */
    String CACHE_SUFFIX = "~start~key";

    /**
     * redis lock 超时时间
     */
    long LOCK_TIME_EXPIRE = 3000;

    /**
     * redis lock 重试时间
     */
    long LOCK_TIME_OUT = 3000;

    /**
     * cas 超时时间
     */
    long CAS_TIME_OUT = 1000;
}
