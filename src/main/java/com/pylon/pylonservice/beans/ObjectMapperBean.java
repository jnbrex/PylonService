package com.pylon.pylonservice.beans;

import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONMapper;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapperBean {
    @Bean
    public ObjectMapper objectMapper() {
        return GraphSONMapper.build().create().createMapper();
    }
}
