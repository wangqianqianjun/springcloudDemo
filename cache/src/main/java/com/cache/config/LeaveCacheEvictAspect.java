package com.cache.config;

import com.cache.config.annotation.LeaveCacheEvict;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author wangqianjun
 *         缓存删除切面
 *         Created by wangqianjun on 2019/3/29.
**/
@Aspect
@Component
public class LeaveCacheEvictAspect extends LeaveCacheOperate {

    @Pointcut("@annotation(com.cache.config.annotation.LeaveCacheEvict)")
    public void cacheEvictPoinCut() {
    }

    @Around("cacheEvictPoinCut()")
    public Object doCache(ProceedingJoinPoint joinPoint) {
        Object value = null;
        try {

            //1.获取当前方法上注解的内容
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = joinPoint.getTarget().getClass().getMethod(signature.getName(), signature.getMethod().getParameterTypes());

            Object object = joinPoint.getTarget();
            //添加参数
            Object[] args = joinPoint.getArgs();

            LeaveCacheEvict cacheEvict = method.getAnnotation(LeaveCacheEvict.class);

            String keyEl = cacheEvict.key();
            String[] cacheName = cacheEvict.cacheNames();
            String keyGeneratorName = cacheEvict.keyGenerator();
            String conditionEl = cacheEvict.condition();
            boolean allEntries = cacheEvict.allEntries();
            boolean beforeInvocation = cacheEvict.beforeInvocation();
            //判断是否满足缓存条件
            boolean condition = checkCondition(conditionEl, method, args);
            //不满足缓存条件则不进行下面操作
            if (!condition) {
                return joinPoint.proceed();
            }

            String key = getKey(keyEl, keyGeneratorName, object, method, args);
            //是否在方法执行前情况缓存
            if (beforeInvocation) {
                delCache(allEntries, cacheName, key);
                value = joinPoint.proceed();
            } else {
                value = joinPoint.proceed();
                delCache(allEntries, cacheName, key);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return value;
    }


    private void delCache(boolean allEntries, String[] cacheName, String key) {
        if (allEntries) {
            delCacheByKeySpace(cacheName);
        }
        delCache(key);
    }
}
