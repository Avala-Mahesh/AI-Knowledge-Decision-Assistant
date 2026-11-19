package com.aiassistant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2")
public class ContextLoadsTest {

    @Test
    void contextLoads() {
        // Verifies Spring ApplicationContext starts up cleanly with all bean dependencies satisfied
    }
}
