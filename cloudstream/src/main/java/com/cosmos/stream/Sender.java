package com.cosmos.stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wangqianjun
 * Created by wangqianjun on 2019/3/13.
 */
@Component
public class Sender {

    @Autowired
    private AmqpTemplate rabbitmqTemplate;

    public void send(){
        String content = "hello" + new Date();
        System.out.println("Sender:" +content);
        this.rabbitmqTemplate.convertAndSend("hello", content);
    }

}
