package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.util.JwtTokenUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HelloControllerTests {
    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    final private HelloController helloController;

    HelloControllerTests() {
        helloController = new HelloController();
    }

    @Test
    void testHello() {
        final String username = "Jason";
        when(jwtTokenUtil.getUsernameFromAuthorizationHeader(anyString())).thenReturn(username);
        Assertions.assertThat(helloController.hello("Bearer jwt").getBody())
            .isEqualTo(String.format("Hello %s!", username));
        verify(jwtTokenUtil).getUsernameFromAuthorizationHeader("jwt");
    }
}
