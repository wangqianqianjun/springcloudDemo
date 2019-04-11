package com.cache.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 */
@Component
public class RedisUtils<T> {

    /**
     * lua 脚本 加锁
     */
    private static final String lockScript = "local key     = KEYS[1]\n" +
            "local ttl     = KEYS[2]\n" +
            "local content = KEYS[3]\n" +
            "\n" +
            "local lockSet = redis.call('setnx', key, content)\n" +
            "\n" +
            "if lockSet == 1 then\n" +
            "    return  redis.call('pexpire', key, ttl)\n" +
            "end\n" +
            "    return nil";
    /**
     * lua 脚本 释放锁
     */
    private static final String unlockScript = "local key  = KEYS[1]\n" +
            "local signature   = KEYS[2]\n" +
            "if redis.call('get',key) == signature then\n" +
            "    redis.call('del',key)\n" +
            "    return true\n" +
            "else\n" +
            "    return nil\n" +
            "end";
    @Autowired
    private RedisTemplate redisTemplate;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * 如果redis已有该数据则不保存并且返回false,如果redis没有则保存并且返回true
     *
     * @param key
     * @param value
     * @return
     * @author wangqianjun
     */
    public boolean setIfAbsent(Object key, T value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }


    /**
     * 锁
     *
     * @param key
     * @param ttl
     * @return
     */
    public Object lock(String key, long ttl, String signature) {
        return redisTemplate.execute(connection -> ((Jedis) connection.getNativeConnection()).eval(lockScript, 3, key, ttl + "", signature)
                , false);
    }

    /**
     * 释放锁
     *
     * @param key
     * @return
     */
    public Object unlock(String key, String signature) {
        return redisTemplate.execute(connection -> ((Jedis) connection.getNativeConnection()).eval(unlockScript, 2, key, signature)
                , false);
    }

    /**
     * 设置key-value
     *
     * @param key
     * @param value
     */
    public void set(Object key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置key-value-过期时间
     *
     * @param key
     * @param value
     * @param expireTime
     */
    public void set(Object key, T value, long expireTime) {
        redisTemplate.opsForValue().set(key, value);
        expire(key, expireTime);
    }

    /**
     * 对已存在的key更新过期时间
     *
     * @param key
     * @param expireTime
     */
    public void expire(Object key, long expireTime) {
        redisTemplate.expire(key, expireTime, timeUnit);
    }

    /**
     * 根据key获取值
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> Object get(Object key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 根据key删除
     *
     * @param key
     */
    public void del(Object key) {
        redisTemplate.delete(key);
    }

    /**
     * 将哈希表 key 中的域 field 的值设为 value
     *
     * @param key
     * @param hk
     * @param hv
     */
    public void hSet(Object key, Object hk, Object hv) {
        redisTemplate.opsForHash().put(key, hk, hv);
    }

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中
     *
     * @param key
     * @param dataMap
     */
    public void hMSet(String key, Map<T, T> dataMap) {
        if (null != dataMap) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    public void hMSet(String key, Map<T, T> dataMap, long expireTime) {
        if (null != dataMap) {
            redisTemplate.opsForHash().putAll(key, dataMap);
            expire(key, expireTime);
        }
    }

    public void rightPushAll(String key, List<T> list, long expireTime) {
        if (null != list) {
            redisTemplate.opsForList().rightPushAll(key, list);
            expire(key, expireTime);
        }
    }

    public void sadd(String key, Set<T> set, long expireTime) {
        if (null != set) {
            redisTemplate.opsForSet().add(key, set.toArray());
            expire(key, expireTime);
        }
    }


    public <T> Object hGet(Object key, Object hk) {
        return redisTemplate.opsForHash().get(key, hk);
    }

    /**
     * 返回哈希表 key 中，所有的域和值
     *
     * @param key
     * @return
     */
    public <T> Map<T, T> hGetAll(Object key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略
     *
     * @param key
     * @param hk
     */
    public void hDel(Object key, Object hk) {
        redisTemplate.opsForHash().delete(key, hk);
    }

    /**
     * 检查key是否存在
     *
     * @param key
     * @return
     */
    public boolean exists(Object key) {
        return redisTemplate.hasKey(key);
    }


    public void multi() {
        redisTemplate.multi();
    }

    public void exec() {
        redisTemplate.exec();
    }

    public byte[] serialize(Object obj) {
        return redisTemplate.getStringSerializer().serialize(obj);
    }

    public Object deserialize(byte[] bytes) {
        return redisTemplate.getStringSerializer().deserialize(bytes);
    }

    public Long ttl(final String key) {
        return (Long) redisTemplate.execute((RedisCallback) redisConnection -> redisConnection.ttl(key.getBytes()));
    }

    public void flushAll() {
        redisTemplate.execute((RedisCallback) redisConnection -> {
            redisConnection.flushAll();
            return null;
        });
    }
}
