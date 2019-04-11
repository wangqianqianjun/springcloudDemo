package com.cache.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * @author wangqianjun
 * Created by wangqianjun on 2019/3/21.
 */
@Configuration
public class RedisConfig {

    private final static int NO_PARAM_KEY = 0;

    @Bean(name = "defaultKeyGenerator")
    public KeyGenerator keyGenerator() {
        return (Object target, Method method, Object... params) -> {
            char sp = ':';
            StringBuilder strBuilder = new StringBuilder(30);
            // 类名
            strBuilder.append(target.getClass().getName());
            strBuilder.append(sp);
            // 方法名
            strBuilder.append(method.getName());
            //TODO 需要考虑key过长的问题 不能让参数全部放到key上
            if (params.length > 0) {
                // 参数值
                for (Object object : params) {
                    if (object == null) {
                        continue;
                    }
                    strBuilder.append(sp);
                    if (BeanHelper.isSimpleValueType(object.getClass())) {
                        strBuilder.append(object);
                    } else {
                        //TODO object为空
                        strBuilder.append(object);
                    }
                }
            } else {
                strBuilder.append(NO_PARAM_KEY);
            }

            return strBuilder.toString();
        };

    }


}
