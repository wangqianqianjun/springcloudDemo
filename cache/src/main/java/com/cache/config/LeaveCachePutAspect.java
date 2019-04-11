package com.cache.config;

import com.cache.config.annotation.LeaveCachePut;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 缓存更新切面
 *
 * @author wangqianjun
 *         Created by wangqianjun on 2019/3/29.
 */
@Aspect
@Component
public class LeaveCachePutAspect extends LeaveCacheOperate {

    @Pointcut("@annotation(com.cache.config.annotation.LeaveCachePut)")
    public void cachePutPoinCut() {
    }

    @Around("cachePutPoinCut()")
    public Object doCache(ProceedingJoinPoint joinPoint) {
        Object value = null;
        try {

            //1.获取当前方法上注解的内容
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = joinPoint.getTarget().getClass().getMethod(signature.getName(), signature.getMethod().getParameterTypes());

            Object object = joinPoint.getTarget();
            //添加参数
            Object[] args = joinPoint.getArgs();

            LeaveCachePut cacheable = method.getAnnotation(LeaveCachePut.class);

            String keyEl = cacheable.key();
            String[] cacheName = cacheable.cacheNames();
            String keyGeneratorName = cacheable.keyGenerator();
            String conditionEl = cacheable.condition();
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

            //如果超时时间没设置过期
            if (expireTime != -1) {
                expireTime = expireTime(expireTime, expireRandom);
            }
            //首先删除key
            delCache(key);
            //进行一次缓存操作
            value = doValue(joinPoint, typeEnum, cacheName, key, expireTime);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return value;
    }
}
