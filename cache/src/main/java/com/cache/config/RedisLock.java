package com.cache.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.UUID;

/**
 * Created by wangqianjun on 2017/12/7.
 * <p>
 * redis分布式锁,主要原理利用redis.setnx()函数特性实现.key不存在返回1,key存在返回0
 * 在redisTemplate里面为setIfAbsent()方法
 * 使用说明:
 * 在分布式场景下对并发对资源进行修改的情况下,首先要获取一把锁,锁名称自定义
 * (在这里已经进行处理,直接传入需要加锁的名称即可),理论上应当和业务相关.
 * 获取成功的情况下再进行业务处理,处理完业务再释放当前锁.(确保锁释放或者设置锁超时时间!)
 */
public class RedisLock {

    /**
     * 毫秒与毫微秒的换算单位 1毫秒 = 1000000毫微秒
     */
    private static final long MILLI_NANO_CONVERSION = 1000 * 1000L;
    //key前缀
    private static final String EXPIRE_KEY_PREFIX = "lock:%s";
    private final Logger logger = LogManager.getLogger(this.getClass());
    private boolean locked;

    private String key;

    private RedisUtils redis;

    private String signature;


    public RedisLock(String key, RedisUtils redis) {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("Param key can not be null or empty");
        }
        this.key = String.format(EXPIRE_KEY_PREFIX, key);
        this.locked = false;
        this.redis = redis;
    }


    /**
     * 获取锁
     * 在timeout时间内会一直尝试获取锁，如果timeout=0，则表示获取失败后直接返回不再尝试
     * 锁的有效期expireSecs必须大于0，当超过有效期未被unlock时，系统将会强制释放
     *
     * @param timeout    获取锁的等待时间,单位毫秒
     * @param expireSecs 锁的有效时间，必须大于0,单位毫秒
     * @return true/false
     */
    public boolean lock(long expireSecs, long timeout) {
        if (expireSecs <= 0) {
            throw new IllegalArgumentException("Param expireSecs must lager than zero.");
        }
        long nano = System.nanoTime();
        timeout *= MILLI_NANO_CONVERSION;
        signature = UUID.randomUUID().toString();
        try {
            while ((System.nanoTime() - nano) < timeout) {
                if (redis.lock(key, expireSecs, signature) != null) {
                    locked = true;
                    logger.info("lock success :" + key + " signature is :" + signature);
                    return locked;
                }
                Random r = new Random();
                int time = 30 + r.nextInt(100);
                Thread.sleep(time);
            }
        } catch (Exception e) {
            logger.error("lock false!");
            return false;
        }
        return false;
    }

    /**
     * 释放锁
     *
     * @return true/false
     */
    public boolean unLock() {
        try {
            if (this.locked) {
                if (redis.unlock(key, signature) != null) {
                    this.locked = false;
                    logger.info(" UNLocking success! key:" + this.key + ". Delete the key!");
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(" UNLocking error! key:" + this.key);
        }
        logger.error(" UNLocking false! key:" + this.key + " signature is :" + signature);
        return false;
    }


}
