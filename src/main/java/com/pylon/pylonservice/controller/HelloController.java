package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/hello")
    public ResponseEntity<String> hello(@RequestHeader(value = "Authorization") final String authorizationHeader) {
        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        return ResponseEntity.ok(String.format("Hello %s!", jwtTokenUtil.getUsernameFromToken(jwt)));
    }
}
