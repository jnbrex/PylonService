package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.jwt.JwtRequest;
import com.pylon.pylonservice.model.jwt.JwtResponse;
import com.pylon.pylonservice.model.tables.Refresh;
import com.pylon.pylonservice.services.JwtUserDetailsService;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthenticateController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @PostMapping(value = "/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody final JwtRequest authenticationRequest)
        throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String jwtToken = jwtTokenUtil.generateJwtForUser(userDetails);

        final Refresh refresh = Refresh.builder()
            .refreshToken(UUID.randomUUID().toString())
            .userId(UserUtil.getUserIdForUsername(dynamoDBMapper, userDetails.getUsername()))
            .build();

        dynamoDBMapper.save(refresh);

        return ResponseEntity.ok(
            JwtResponse.builder()
                .jwtToken(jwtToken)
                .refreshToken(refresh.getRefreshToken())
                .build()
        );
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}