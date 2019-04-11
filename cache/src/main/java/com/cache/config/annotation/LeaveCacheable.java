package com.cache.config.annotation;


import com.cache.config.RedisTypeEnum;

import java.lang.annotation.*;

/**
 * @author wangqianjun
 *         Created by wangqianjun on 2019/3/26.
 * 自定义cache的实现。扩展了按类型存储，自定义超时时间
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LeaveCacheable {

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
     * 是否开启同步锁
     * 缓存失效多线程争夺是否阻塞。
     * 默认阻塞，防止流量涌入db层。
     *
     * @return
     */
    boolean sync() default true;

    /**
     * 表示存储在redis中的类型
     *
     * @return
     */
    RedisTypeEnum type() default RedisTypeEnum.String;

    /**
     * 过期时间,默认不过期
     *
     * @return
     */
    long expireTime() default -1;

    /**
     * 是否对过期时间进行随机,大批量key过期时间一致可能会导致缓存雪崩。建议开启
     * 随机范围 -5~+5秒
     *
     * @return
     */
    boolean expireRandom() default true;
}
