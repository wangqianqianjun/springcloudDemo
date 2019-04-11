package com.cosmos.cloud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by wangqianjun on 2019/2/20.
 * @author wangqianjun
 */
@Controller
@RequestMapping("/test")
public class TestController {

    @Value("${server.port}")
    String port;

    @RequestMapping("/api-1")
    @ResponseBody
    public Object test(){
        return "api-1 success from "+port;
    }

    @RequestMapping("/api-2")
    @ResponseBody
    public Object test2(){
        return "api-2 success from "+port;
    }
}
