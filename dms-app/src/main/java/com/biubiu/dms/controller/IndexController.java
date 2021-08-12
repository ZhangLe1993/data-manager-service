package com.biubiu.dms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class IndexController {

    @RequestMapping(value = {"/", "/home"})
    public String index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //TODO 权限校验
        return "dist/index";
    }
}
