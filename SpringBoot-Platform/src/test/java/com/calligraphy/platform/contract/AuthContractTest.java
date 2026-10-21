package com.calligraphy.platform.contract;

import com.calligraphy.platform.model.User;
import com.calligraphy.platform.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * auth 域的契约测试（spec 阶段 2 的验收项：契约测试 10 项绿）。
 *
 * <p>每个 operation 一个测试方法，断言 method / path / 状态码 / 响应字段集 / 错误码。
 * 这里刻意**只依赖契约**（路径、状态码、字段名），不引用实现层的 DTO 类 ——
 * 这样实现换了形状而契约没变时，测试会立刻失败。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userId;
    private String username;
    private String email;
    private String password;

    private String adminId;
    private String adminUsername;
    private String adminPassword;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        password = "Passw0rd" + suffix;
        username = "user_" + suffix;
        email = "user_" + suffix + "@example.com";
        userId = createUser(username, email, password, User.Role.USER);

        adminPassword = "Admin0" + suffix;
        adminUsername = "admin_" + suffix;
        adminId = createUser(adminUsername, "admin_" + suffix + "@example.com",
                adminPassword, User.Role.SUPER_ADMIN);
    }

    @AfterEach
    void tearDown() {
        if (userId != null) {
            userRepository.deleteById(userId);
        }
        if (adminId != null) {
            userRepository.deleteById(adminId);
        }
    }

    private String createUser(String name, String mail, String rawPassword, User.Role role) {
        User u = User.builder()
                .username(name)
                .email(mail)
                .password(passwordEncoder.encode(rawPassword))
                .nickname(name)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(role)
                .build();
        return userRepository.save(u).getId();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        assertFalse(content.isBlank(), "响应体不能为空");
        return objectMapper.readTree(content);
    }

    // ------------------------------------------------------------ 认证（10 个 operation）

    @Test
    void frontendAuthRegister() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String newName = "reg_" + suffix;
        String newMail = "reg_" + suffix + "@example.com";
        String newPass = "Passw0rd" + suffix;
        try {
            MvcResult result = mockMvc.perform(post("/api/frontend/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of(
                                    "username", newName,
                                    "email", newMail,
                                    "password", newPass,
                                    "nickname", "注册用户"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").isNotEmpty())
                    .andExpect(jsonPath("$.user.id").isNotEmpty())
                    .andExpect(jsonPath("$.user.username").value(newName))
                    .andExpect(jsonPath("$.user.email").value(newMail))
                    .andExpect(jsonPath("$.user.role").value("USER"))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.success").doesNotExist())
                    .andReturn();

            // 注册出来的账号必须能直接登录（防止密码被双重哈希）
            mockMvc.perform(post("/api/frontend/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("username", newName, "password", newPass))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty());
        } finally {
            userRepository.findByUsername(newName).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
    }

    @Test
    void frontendAuthLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.user.username").value(username))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result));
    }

    @Test
    void frontendAuthProfile() throws Exception {
        String token = loginAndGetToken(username, password);

        MvcResult result = mockMvc.perform(get("/api/frontend/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.email").value(email))
                // 裸资源：没有 user 包装层
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result));
    }

    @Test
    void frontendAuthRefresh() throws Exception {
        String refreshToken = loginAndGetRefreshToken(username, password);

        MvcResult result = mockMvc.perform(post("/api/frontend/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                // 裸资源：没有 data 信封
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result));
    }

    @Test
    void frontendAuthLogout() throws Exception {
        String accessToken = loginAndGetToken(username, password);
        String refreshToken = loginAndGetRefreshToken(username, password);

        mockMvc.perform(post("/api/frontend/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isNoContent());

        // 登出后令牌必须失效
        mockMvc.perform(get("/api/frontend/auth/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void backendAuthLogin() throws Exception {
        mockMvc.perform(post("/api/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", adminUsername, "password", adminPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value(adminUsername))
                .andExpect(jsonPath("$.user.role").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void backendAuthProfile() throws Exception {
        String token = loginAdminAndGetToken();

        mockMvc.perform(get("/api/backend/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(adminId))
                .andExpect(jsonPath("$.username").value(adminUsername))
                .andExpect(jsonPath("$.role").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void backendAuthRefresh() throws Exception {
        String refreshToken = loginAdminAndGetRefreshToken();

        mockMvc.perform(post("/api/backend/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void backendAuthLogout() throws Exception {
        String accessToken = loginAdminAndGetToken();
        String refreshToken = loginAdminAndGetRefreshToken();

        mockMvc.perform(post("/api/backend/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isNoContent());
    }

    @Test
    void authSendVerificationCode() throws Exception {
        mockMvc.perform(post("/api/auth/verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty())
                // 绝不能把验证码本身回传给客户端
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // ------------------------------------------------------------ 错误路径：{code,message}

    @Test
    void errorPathUsesCodeAndMessageShape() throws Exception {
        mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", "WrongPassw0rd"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.success").doesNotExist())
                .andExpect(jsonPath("$.timestamp").doesNotExist())
                .andExpect(jsonPath("$.path").doesNotExist());
    }

    @Test
    void validationErrorUsesCodeAndMessageShape() throws Exception {
        mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "a", "password", "x"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void missingTokenIsUnauthorizedWithCodeAndMessage() throws Exception {
        mockMvc.perform(get("/api/frontend/auth/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void nonAdminCannotUseBackendLogin() throws Exception {
        mockMvc.perform(post("/api/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ------------------------------------------------------------ 工具

    private String loginAndGetToken(String name, String pass) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", name, "password", pass))))
                .andExpect(status().isOk())
                .andReturn();
        return body(result).get("accessToken").asText();
    }

    private String loginAndGetRefreshToken(String name, String pass) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", name, "password", pass))))
                .andExpect(status().isOk())
                .andReturn();
        return body(result).get("refreshToken").asText();
    }

    private String loginAdminAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", adminUsername, "password", adminPassword))))
                .andExpect(status().isOk())
                .andReturn();
        return body(result).get("accessToken").asText();
    }

    private String loginAdminAndGetRefreshToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", adminUsername, "password", adminPassword))))
                .andExpect(status().isOk())
                .andReturn();
        return body(result).get("refreshToken").asText();
    }
}
