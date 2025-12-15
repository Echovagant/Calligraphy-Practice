package com.calligraphy.platform.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SimpleApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SimpleApplication.class, args);
    }
    
    @RestController
    public static class SimpleController {
        
        @GetMapping("/test")
        public String test() {
            return "Hello, World!";
        }
    }
}


