package com.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

/**
 * @author wangqianjun
 * Created by wangqianjun on 2019/3/20.
 */
@Controller
@RequestMapping("/cache")
public class CacheTestController {

    @Autowired
    private CacheService cacheService;


    @RequestMapping("/test")
    @ResponseBody
    public Object test(String param){
        Model m=new Model();
        m.setId(UUID.randomUUID().toString());
        m.setName("name-test");
        String ca=cacheService.initCache(param,m);
        return ca;
    }
}
