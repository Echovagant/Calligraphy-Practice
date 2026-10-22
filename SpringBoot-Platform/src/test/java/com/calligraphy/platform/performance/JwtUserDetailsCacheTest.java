package com.calligraphy.platform.performance;

import com.calligraphy.platform.model.User;
import com.calligraphy.platform.repository.UserRepository;
import com.calligraphy.platform.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P6：连续请求不该每次都去 users 表捞一次身份。
 *
 * <p>{@code JwtAuthenticationFilter} 每个带令牌的请求都要
 * {@code loadUserByUsername} 一次 —— 一次 users 查询 + 一次 BCrypt 密码比对。
 * 现在走 {@code UserDetailsCache}（短 TTL 本地缓存）。
 *
 * <p>断言方式：对 {@code UserDetailsServiceImpl} 打桩，
 * 统计它在一串请求里被调了几次。第一次请求会加载（缓存未命中），
 * 之后的同令牌请求必须**不再**调用它。
 *
 * <p>注意：这里断言的是「第二次请求不再访问 users 表」，
 * 不涉及缓存的 TTL 窗口 —— TTL 窗口在报告里说明（默认 60 秒）。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class JwtUserDetailsCacheTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 打桩用：数「从库里捞用户身份」这件事发生了几次。 */
    @SpyBean
    private UserDetailsServiceImpl userDetailsService;

    private String userId;
    private String username;
    private String password;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        password = "Passw0rd" + suffix;
        username = "jc_" + suffix;
        userId = userRepository.save(User.builder()
                .username(username)
                .email("jc_" + suffix + "@example.com")
                .password(passwordEncoder.encode(password))
                .nickname("身份缓存测试")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();

        token = login(username, password);
        // 登录本身也会 loadUserByUsername，计数前先清掉
        Mockito.clearInvocations(userDetailsService);
    }

    @AfterEach
    void tearDown() {
        Mockito.clearInvocations(userDetailsService);
        if (userId != null) {
            userRepository.findById(userId).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
    }

    /**
     * 第一次请求：缓存未命中，必须真的去加载一次身份。
     */
    @Test
    void firstRequestLoadsUserDetails() throws Exception {
        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        verify(userDetailsService, times(1)).loadUserByUsername(username);
    }

    /**
     * 第二个请求带同一个令牌：不该再查 users 表。
     */
    @Test
    void secondRequestWithSameTokenDoesNotHitTheDatabase() throws Exception {
        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Mockito.clearInvocations(userDetailsService);

        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    /**
     * 连续多次请求，身份只从库里捞一次，返回结果始终一致。
     */
    @Test
    void repeatedRequestsReuseTheCachedIdentity() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/users/" + userId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.username").value(username));
        }

        verify(userDetailsService, times(1)).loadUserByUsername(username);
    }

    private String login(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = body(login).get("accessToken").asText();
        assertFalse(accessToken.isBlank(), "登录必须拿到 accessToken");
        return accessToken;
    }

    private JsonNode body(MvcResult result) throws Exception {
        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 0, "响应体不能为空");
        JsonNode node = objectMapper.readTree(content);
        assertNotNull(node);
        return node;
    }
}
