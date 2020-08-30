package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.services.AccessTokenService;
import org.assertj.core.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HelloControllerTests {
    @Mock
    private AccessTokenService accessTokenService;

    @InjectMocks
    final private HelloController helloController;

    HelloControllerTests() {
        helloController = new HelloController();
    }

    @Test
    void testHello() {
        final String testUsername = "Jason";
        final String testAccessToken = "testAccessToken";
        when(accessTokenService.getUsernameFromAccessToken(anyString())).thenReturn(testUsername);
        Assertions.assertThat(helloController.hello(testAccessToken).getBody())
            .isEqualTo(String.format("Hello %s!", testUsername));
        verify(accessTokenService).getUsernameFromAccessToken(testAccessToken);
    }
}
