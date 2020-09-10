package com.pylon.pylonservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import static java.security.Security.setProperty;

@SpringBootApplication
public class PylonServiceApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        setProperty("networkaddress.cache.ttl" , "60");
    	SpringApplication.run(PylonServiceApplication.class, args);
    }
}
