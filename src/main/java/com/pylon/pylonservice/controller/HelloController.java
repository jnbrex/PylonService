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

    /**
     * Call to receive a hello message personalized to the calling User.
     *
     * @param authorizationHeader A key-value header with key "Authorization" and value like "Bearer exampleJwtToken".
     * @return HTTP 200 OK - String like "Hello Jason!".
     */
    @GetMapping("/hello")
    public ResponseEntity<?> hello(@RequestHeader(value = "Authorization") final String authorizationHeader) {
        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        return ResponseEntity.ok(String.format("Hello %s!", jwtTokenUtil.getUsernameFromToken(jwt)));
    }
}
