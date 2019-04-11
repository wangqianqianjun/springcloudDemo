package com.cosmos.cloud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * @author wangqianjun
 * Created by wangqianjun on 2019/3/1.
 */
@Controller
public class FeignExampleController {
    @Resource
    private FeignExampleService feignExampleService;

    @Value("${spring.application.name}")
    String applicationName;

    @RequestMapping("/hello/{name}")
    public String index(@PathVariable("name") String name) {
        return feignExampleService.hello(name+", from "+applicationName);
    }
}
