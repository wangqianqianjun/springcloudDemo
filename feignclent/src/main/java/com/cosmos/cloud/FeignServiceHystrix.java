package com.cosmos.cloud;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by wangqianjun on 2019/3/1.
 */
@Component
public class FeignServiceHystrix implements FeignExampleService{

    @Override
    public String hello(@RequestParam(value = "name") String name) {
        return "sorry "+name+"，service has fail!";
    }
}
