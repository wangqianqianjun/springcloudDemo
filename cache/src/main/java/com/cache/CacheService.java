package com.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author wangqianjun
 * Created by wangqianjun on 2019/3/20.
 */
@Service
public class CacheService {

    @Cacheable
    public String initCache(String param,Model model){
        return "生成一条cache"+param;
    }
}
