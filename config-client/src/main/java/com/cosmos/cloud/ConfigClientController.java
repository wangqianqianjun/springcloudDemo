package com.cosmos.cloud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by wangqianjun on 2019/3/1.
 */
@RestController
@RefreshScope
public class ConfigClientController {

    @Value("${writer}")
    String writer;

    @RequestMapping(value = "/writer")
    public String writer(){
        return writer;
    }

}
