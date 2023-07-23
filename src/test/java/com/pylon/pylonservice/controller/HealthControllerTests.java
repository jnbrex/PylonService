package com.pylon.pylonservice.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class HealthControllerTests {
    final private HealthController healthController;

    HealthControllerTests() {
        healthController = new HealthController();
    }

    @Test
    void testHealthReturnsString() {
        Assertions.assertThat(healthController.health().getBody()).contains("Healthy at ");
    }
}
