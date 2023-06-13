package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executors;

@SpringBootApplication
@RestController
public class Demo1Application {

    private static final Logger log = LoggerFactory.getLogger(Demo1Application.class);

    @Bean
    @Profile("virtual-threads")
    public TomcatProtocolHandlerCustomizer<?> virtualThreads() {
        return protocolHandler -> protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

    @GetMapping("/hello")
    public String getNumber() {
        log.info("Start to sleep {}", Thread.currentThread());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "Hello world";
    }

    public static void main(String[] args) {
        SpringApplication.run(Demo1Application.class, args);
    }

}
