package com.example.provider.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private Environment env;

    @Value("${version:0}")
    private String version;

    /**
     * http://localhost:9001/test/port
     *
     * @return
     */
    @GetMapping("/port")
    public Object port() {
        return String.format("port=%s, version=%s", env.getProperty("local.server.port"), version);
    }
}
