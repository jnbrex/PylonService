package com.pylon.pylonservice.controller;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NeptuneTestController {
    @Qualifier("reader")
    @Autowired
    private GraphTraversalSource rG;

    @GetMapping(value = "/neptuneTest/vertexCount")
    public ResponseEntity<?> vertexCount() {
        long count = rG.V().count().next();

        return new ResponseEntity<>(
            count, HttpStatus.OK
        );
    }
}
