package com.cache.config;

import com.cache.config.annotation.LeaveCacheable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 缓存核心实现
 * @author wangqianjun
 *         Created by wangqianjun on 2019/3/26.
 */
@Aspect
@Component
public class LeaveCacheableAspect extends LeaveCacheOperate {


    @Pointcut("@annotation(com.cache.config.annotation.LeaveCacheable)")
    public void cacheablePoinCut() {
    }

    @Around("cacheablePoinCut()")
    public Object doCache(ProceedingJoinPoint joinPoint) {
        Object value = null;
        RedisLock lock = null;
        try {

            //1.获取当前方法上注解的内容
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = joinPoint.getTarget().getClass().getMethod(signature.getName(), signature.getMethod().getParameterTypes());

            Object object = joinPoint.getTarget();
            //添加参数
            Object[] args = joinPoint.getArgs();

            LeaveCacheable cacheable = method.getAnnotation(LeaveCacheable.class);

            String keyEl = cacheable.key();
            String[] cacheName = cacheable.cacheNames();
            String keyGeneratorName = cacheable.keyGenerator();
            String conditionEl = cacheable.condition();
            boolean sync = cacheable.sync();
            RedisTypeEnum typeEnum = cacheable.type();
            long expireTime = cacheable.expireTime();
            boolean expireRandom = cacheable.expireRandom();

            //判断是否满足缓存条件
            boolean condition = checkCondition(conditionEl, method, args);
            //不满足缓存条件则不进行下面操作
            if (!condition) {
                return joinPoint.proceed();
            }

            String key = getKey(keyEl, keyGeneratorName, object, method, args);

            //去缓存中查询
            value = getValueFromCache(typeEnum, key);
            if (value != null) {
                return value;
            }
            //如果超时时间没设置过期
            if (expireTime != -1) {
                expireTime = expireTime(expireTime, expireRandom);
            }
            /**
             * cas 进行cache操作数据
             *
             */
            if (!sync) {
                value = doValue(joinPoint, typeEnum, cacheName, key, expireTime);
            } else {
                //对相同的key，进行cas控制，同一个key只有拿到分布式锁的才能进行访问数据库
                lock = new RedisLock(key, redisUtils);
                if (lock.lock(CacheConstant.LOCK_TIME_EXPIRE, CacheConstant.LOCK_TIME_OUT)) {
                    value = doValue(joinPoint, typeEnum, cacheName, key, expireTime);
                    lock.unLock();
                } else {
                    //没有拿到分布式锁的进入cas等待
                    long startTime = System.currentTimeMillis();
                    while (true) {
                        //sleep，防止死循环导致cpu资源占用太大
                        Thread.sleep(5);
                        value = getValueFromCache(typeEnum, key);
                        //如果获取到值就代表redis中已经存在了。可以跳出
                        if (value != null) {
                            break;
                        }
                        long endTime = System.currentTimeMillis();
                        //进行超时判断,如果超时redis中还没有值,不管咋滴,直接操作数据库
                        if (endTime - startTime > CacheConstant.CAS_TIME_OUT) {
                            value = doValue(joinPoint, typeEnum, cacheName, key, expireTime);
                            break;
                        }
                    }
                }
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (lock != null) {
                lock.unLock();
            }
        }
        return value;
    }



}
