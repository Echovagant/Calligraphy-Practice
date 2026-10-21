package com.calligraphy.platform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoadsWithPasswordEncoder() {
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder.encode("secret").startsWith("$2"));
    }

    @Test
    void protectedPathWithoutTokenReturns401() throws Exception {
        // GET /api/users/{userId} 需要令牌（公开的只有 /api/works 画廊与 /api/stats/overview）
        mockMvc.perform(get("/api/users/some-user-id"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicPathIsNotRejectedBySecurity() throws Exception {
        // /api/frontend/auth/login is in the permit list. The controller only maps
        // POST on it, so a GET must pass the security filter and be rejected by
        // the dispatcher with 405 Method Not Allowed -- not 401.
        mockMvc.perform(get("/api/frontend/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }
}
