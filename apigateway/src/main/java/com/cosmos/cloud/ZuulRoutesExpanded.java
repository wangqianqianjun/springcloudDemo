package com.cosmos.cloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by wangqianjun on 2019/2/21.
 * @author wangqianjun
 */
@Controller
public class ZuulRoutesExpanded {

    @Autowired
    private ZuulProperties zuulProperties;

    @RequestMapping("/getRoutes")
    @ResponseBody
    public Object getRoutes(){
        Map<String, ZuulProperties.ZuulRoute> routes= zuulProperties.getRoutes();
        return routes;
    }



    @RequestMapping("/addRoutes")
    @ResponseBody
    public Object addRoutes(){
        Map<String, ZuulProperties.ZuulRoute> routes= zuulProperties.getRoutes();
        String id="add-api";
        ZuulProperties.ZuulRoute route=new ZuulProperties.ZuulRoute("add-api","/add-api/**","service-producer",
                null,true,null,null);
        routes.put(id,route);
        zuulProperties.setRoutes(routes);
        return "add-api success";
    }

}
