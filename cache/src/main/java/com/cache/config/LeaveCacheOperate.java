package com.cache.config;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Method;
import java.util.*;

/**
 * cache的核心类,涉及cache的操作基本都在此类实现
 * 在子类中可以自由组合操作来实现具体业务功能
 * Created by wangqianjun on 2019/3/29.
 */
class LeaveCacheOperate {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    BeanFactory beanFactory;

    @Autowired
    RedisUtils redisUtils;

    KeyGenerator defaultKeyGenerator = new SimpleKeyGenerator();

    /**
     * 获取key值
     *
     * @param key
     * @param keyGeneratorName
     * @return
     */
    protected String getKey(String key, String keyGeneratorName, Object target, Method method, Object[] args) throws Exception {
        if (StringUtils.isNoneBlank(key) && StringUtils.isNoneBlank(keyGeneratorName)) {
            throw new Exception("key and keyGeneratorName cannot exist at the same time! method :" + method.getName());
        }
        if (StringUtils.isNoneBlank(key)) {
            return target.getClass().getName() + ":" + method.getName() + ":" + SpelParserValue.expressionParser(key, method, args).toString();
        }
        //首先在beanFactory中找到KeyGenerator
        KeyGenerator keyGenerator;
        keyGenerator = getBean(keyGeneratorName, KeyGenerator.class);

        //如果不存在,使用默认的defaultKeyGenerator
        if (keyGenerator == null) {
            keyGenerator = defaultKeyGenerator;
        }
        return keyGenerator.generate(target, method, args).toString();
    }

    /**
     * 根据condition条件判断是否满足缓存条件
     *
     * @param condition
     * @param method
     * @param args
     * @return
     */
    protected boolean checkCondition(String condition, Method method, Object[] args) throws Exception {
        if (StringUtils.isNoneBlank(condition)) {
            try {
                return (boolean) SpelParserValue.expressionParser(condition, method, args);
            } catch (Exception e) {
                throw new Exception("Illegal condition !");
            }
        }
        return true;
    }

    /**
     * 获取超时时间,如果设定随机数,则会在原有超时时间上增加随机数。防止缓存同时超时的一种方法。
     *
     * @param expireTime
     * @param expireRandom
     * @return
     */
    protected long expireTime(long expireTime, boolean expireRandom) {
        //是否随机
        if (expireRandom) {
            expireTime = expireTime + new Random().nextInt(100);
        }
        return expireTime;
    }


    /**
     * 从redis中获取值
     *
     * @param typeEnum 对象类型
     * @param key      key
     * @return
     */
    protected Object getValueFromCache(RedisTypeEnum typeEnum, String key) {
        if (typeEnum.equals(RedisTypeEnum.String)) {
            return redisTemplate.opsForValue().get(key);
        }

        if (typeEnum.equals(RedisTypeEnum.Hash)) {
            Map value = redisTemplate.opsForHash().entries(key);
            if (value == null || value.size() == 0) {
                return null;
            }
            return value;
        }

        if (typeEnum.equals(RedisTypeEnum.List)) {
            List value = redisTemplate.opsForList().range(key, 0, -1);
            if (value == null || value.size() == 0) {
                return null;
            }
            return value;
        }

        if (typeEnum.equals(RedisTypeEnum.Set)) {
            Set value = redisTemplate.opsForSet().members(key);
            if (value == null || value.size() == 0) {
                return null;
            }
            return value;
        }

        if (typeEnum.equals(RedisTypeEnum.Zset)) {
            Set value = redisTemplate.opsForZSet().range(key, 0, -1);
            if (value == null || value.size() == 0) {
                return null;
            }
            return value;
        }
        return null;
    }


    /**
     * 操作数据库，并且将数据缓存至缓存中
     *
     * @param joinPoint  切入点
     * @param typeEnum   类型
     * @param cacheName  命名空间
     * @param key        key
     * @param expireTime 超时时间
     * @return
     * @throws Throwable
     */
    protected Object doValue(ProceedingJoinPoint joinPoint, RedisTypeEnum typeEnum, String[] cacheName, String key, long expireTime) throws Throwable {
        Object value = joinPoint.proceed();
        //同步value到缓存
        syncToCache(value, typeEnum, cacheName, key, expireTime);
        return value;
    }

    /**
     * 进行缓存操作
     *
     * @param value      值
     * @param typeEnum   类型
     * @param cacheName  命名空间
     * @param key        key
     * @param expireTime 超时时间
     * @throws Exception
     */
    protected void syncToCache(Object value, RedisTypeEnum typeEnum, String[] cacheName, String key, long expireTime) throws Exception {
        /**
         * 进行值缓存
         */
        cacheValue(value, typeEnum, key, expireTime);
        /**
         * 将key放置到cacheName中进行统一管理
         */
        setKeySpace(cacheName, key, expireTime);
    }


    /**
     * 缓存处理
     */
    protected void cacheValue(Object value, RedisTypeEnum typeEnum, String key, long expireTime) {
        if (typeEnum.equals(RedisTypeEnum.String)) {
            redisUtils.set(key, value, expireTime);
            return;
        }
        if (typeEnum.equals(RedisTypeEnum.Hash)) {
            Map map = (Map) value;
            redisUtils.hMSet(key, map, expireTime);
            return;
        }

        if (typeEnum.equals(RedisTypeEnum.List)) {
            List list = (List) value;
            redisUtils.rightPushAll(key, list, expireTime);
            return;
        }

        if (typeEnum.equals(RedisTypeEnum.Set)) {
            Set set = (Set) value;
            redisUtils.sadd(key, set, expireTime);
            return;
        }

        if (typeEnum.equals(RedisTypeEnum.Zset)) {
            Set set = (Set) value;
            redisUtils.sadd(key, set, expireTime);
            return;
        }
    }

    /**
     * TODO 需要考虑命名空间为空的问题
     * 将key放置入统一的命名空间,进行集中式管理
     *
     * @param cacheName
     * @param key
     */
    protected void setKeySpace(String[] cacheName, String key, long expireTime) throws Exception {
        if (cacheName == null || cacheName.length == 0) {
            return;
        }
        redisTemplate.executePipelined((RedisConnection connection) -> {
            for (int i = 0; i < cacheName.length; i++) {
                connection.sAdd(serialize(cacheName[i] + CacheConstant.CACHE_SUFFIX), serialize(key));
                //设置cache超时时间，如果剩余时间小于新的超时时间则进行重新设置
                connection.expire(serialize(cacheName[i] + CacheConstant.CACHE_SUFFIX), expireTime);
            }
            return null;
        });
    }

    /**
     * TODO 需要考虑命名空间为空的问题
     * 根据命名空间,批量删除缓存
     *
     * @param cacheName
     */
    protected void delCacheByKeySpace(String[] cacheName) {
        if (cacheName == null || cacheName.length == 0) {
            return;
        }
        List<byte[]> keys = new ArrayList();

        for (int i = 0; i < cacheName.length; i++) {
            final int j = i;
            Set<byte[]> key = (Set<byte[]>) redisTemplate.execute((RedisConnection connection) ->
                    connection.sMembers(serialize(cacheName[j] + CacheConstant.CACHE_SUFFIX))
            );
            keys.addAll(key);
            keys.add(serialize(cacheName[j] + CacheConstant.CACHE_SUFFIX));
        }
        redisTemplate.executePipelined((RedisConnection connection) -> {
            for (int i = 0; i < keys.size(); i++) {
                connection.del(keys.get(i));
            }
            return null;
        });
    }

    /**
     * 根据key删除cache
     *
     * @param key
     */
    protected void delCache(String key) {
        redisUtils.del(key);
    }

    protected byte[] serialize(Object value) {
        return redisTemplate.getStringSerializer().serialize(value);
    }

    protected String deSerialize(byte[] value) {
        return (String) redisTemplate.getStringSerializer().deserialize(value);
    }
    protected <T> T getBean(String beanName, Class<T> expectedType) {
        return BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.beanFactory, expectedType, beanName);
    }

}
