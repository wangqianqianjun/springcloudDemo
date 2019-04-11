package com.cache.config.annotation;

import java.lang.annotation.*;

/**
 * @author wangqianjun
 * Created by wangqianjun on 2019/3/29.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LeaveCacheEvict {

    /**
     * 可以指定哪些缓存同属于一个缓存块
     * 但是redis还是通过key来获取数据
     *
     * @return
     */
    String[] cacheNames() default {};

    /**
     * 指定key，支持el表达式
     *
     * @return
     */
    String key() default "";

    /**
     * 指定key生成方式，与key不能共存
     *
     * @return
     */
    String keyGenerator() default "";

    /**
     * 缓存条件，支持el表达式
     *
     * @return
     */
    String condition() default "";

    /**
     * 是否删除所属命名空间所有缓存
     *
     * @return
     */
    boolean allEntries() default false;

    /**
     * 是否在方法执行前删除缓存
     *
     * @return
     */
    boolean beforeInvocation() default false;
}
