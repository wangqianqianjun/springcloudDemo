package com.cosmos.cloud;

/**
 * @author wangqianjun
 * Created by wangqianjun on 2019/3/1.
 */

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "service-producer",fallback = FeignServiceHystrix.class)
public interface FeignExampleService {
    /**
     * feign 测试
     * @param name
     * @return
     */
    @GetMapping(value = "/hello")
    String hello(@RequestParam(value = "name") String name);
}
