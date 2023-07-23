package com.pylon.pylonservice.beans;

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TikaBean {
    @Bean
    public Tika tika() {
        return new Tika();
    }
}
