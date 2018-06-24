package com.cd.springbootdemo.mybatisplusdemo.controller;

import com.cd.springbootdemo.mybatisplusdemo.annotation.ParamCheck;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {


    /**
     * https://blog.csdn.net/ysk_xh_521/article/details/80354544
     * https://blog.csdn.net/beauxie/article/details/78989730
     * @param name
     * @return
     */
    @GetMapping("hi")
    public String sayHello (@ParamCheck String name) {

        return "hello";
    }
}
