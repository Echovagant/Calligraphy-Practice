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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户资料的隐私边界与改密码的强度规则。
 *
 * <ul>
 *   <li>登录 IP / 失败次数 / 锁定时间属于账号安全信息，
 *       查别人时必须置空（以前任何登录用户都能查到别人的登录 IP）；</li>
 *   <li>改密码要走与注册同一套复杂度规则（以前注册拦住了、改密码却能设弱密码）。</li>
 * </ul>
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserPrivacyContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String ownerId;
    private String ownerUsername;
    private String ownerPassword;
    private String ownerToken;

    private String otherId;
    private String otherUsername;
    private String otherPassword;
    private String otherToken;

    private String adminId;
    private String adminUsername;
    private String adminPassword;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        ownerPassword = "Passw0rd" + suffix;
        ownerUsername = "up_owner_" + suffix;
        ownerId = createUser(ownerUsername, "up_owner_" + suffix + "@example.com", ownerPassword, User.Role.USER);
        ownerToken = loginUser(ownerUsername, ownerPassword);

        otherPassword = "Passw0rd" + suffix;
        otherUsername = "up_other_" + suffix;
        otherId = createUser(otherUsername, "up_other_" + suffix + "@example.com", otherPassword, User.Role.USER);
        otherToken = loginUser(otherUsername, otherPassword);

        adminPassword = "Passw0rd" + suffix;
        adminUsername = "up_admin_" + suffix;
        adminId = createUser(adminUsername, "up_admin_" + suffix + "@example.com", adminPassword, User.Role.SUPER_ADMIN);
        adminToken = loginAdmin(adminUsername, adminPassword);

        // 给「本人」造出可断言的敏感字段：
        // lastLoginIp + failedLoginAttempts + accountLockedUntil。
        // 注意不设 accountLocked —— 那会把账号真锁上，登不了录。
        User owner = userRepository.findById(ownerId).orElseThrow();
        owner.setLastLoginIp("203.0.113.7");
        owner.setFailedLoginAttempts(3);
        owner.setAccountLockedUntil(LocalDateTime.now().plusHours(2));
        userRepository.save(owner);
    }

    @AfterEach
    void tearDown() {
        if (ownerId != null) {
            userRepository.findById(ownerId).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
        if (otherId != null) {
            userRepository.findById(otherId).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
        if (adminId != null) {
            userRepository.findById(adminId).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
    }

    // ------------------------------------------------------------ A9 敏感字段脱敏

    @Test
    void otherUserCannotSeeMyLoginIpAndLockState() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/" + ownerId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = body(result);
        assertNull(textOrNull(body, "lastLoginIp"),
                "别人的登录 IP 不能暴露，实际=" + body.get("lastLoginIp"));
        assertTrue(body.get("failedLoginAttempts").isNull(),
                "别人的登录失败次数不能暴露，实际=" + body.get("failedLoginAttempts"));
        assertTrue(body.get("lockedUntil").isNull(),
                "别人的账号锁定状态不能暴露，实际=" + body.get("lockedUntil"));
    }

    @Test
    void ownerStillSeesTheirOwnLoginIpAndLockState() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/" + ownerId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = body(result);
        assertEquals("203.0.113.7", textOrNull(body, "lastLoginIp"), "本人必须能看到自己的登录 IP");
        assertEquals(3, body.get("failedLoginAttempts").asInt(), "本人必须能看到自己的失败次数");
        assertNotNull(textOrNull(body, "lockedUntil"), "本人必须能看到自己的锁定时间");
    }

    @Test
    void adminCanSeeTheLoginIpAndLockStateOfOtherUsers() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/" + ownerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = body(result);
        assertEquals("203.0.113.7", textOrNull(body, "lastLoginIp"), "管理员可以看到用户的登录 IP");
        assertEquals(3, body.get("failedLoginAttempts").asInt(), "管理员可以看到用户的失败次数");
    }

    // ------------------------------------------------------------ A12 改密码的复杂度

    @Test
    void weakNewPasswordIsRejectedAndTheOldOneStillWorks() throws Exception {
        mockMvc.perform(put("/api/users/password")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "oldPassword", ownerPassword,
                                "newPassword", "abcdef",
                                "confirmPassword", "abcdef"))))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 400 || status == 422,
                            "弱密码必须被拒（400/422），实际=" + status);
                })
                .andExpect(jsonPath("$.message").isNotEmpty());

        // 密码必须没被改掉：旧密码仍然能登录，新密码不能
        mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", ownerUsername, "password", ownerPassword))))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", ownerUsername, "password", "abcdef"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void strongNewPasswordIsAccepted() throws Exception {
        String newPassword = "N3wPassw0rd" + UUID.randomUUID().toString().substring(0, 6);

        mockMvc.perform(put("/api/users/password")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "oldPassword", ownerPassword,
                                "newPassword", newPassword,
                                "confirmPassword", newPassword))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", ownerUsername, "password", newPassword))))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------ 工具

    private String createUser(String username, String email, String rawPassword, User.Role role) {
        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .nickname(username)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(role)
                .build()).getId();
    }

    private String loginUser(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return body(login).get("accessToken").asText();
    }

    private String loginAdmin(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return body(login).get("accessToken").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        assertFalse(content.isBlank(), "响应体不能为空");
        return objectMapper.readTree(content);
    }

    /** 取字符串字段：字段缺失或为 null 都返回 null。 */
    private String textOrNull(JsonNode body, String field) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }
}
