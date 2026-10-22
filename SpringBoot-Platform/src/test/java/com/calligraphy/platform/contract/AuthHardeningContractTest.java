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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证域的加固测试。
 *
 * <ul>
 *   <li>登出端点必须登录（以前匿名 POST logout 返回 204，契约要求 401）；</li>
 *   <li>令牌自己的 userType 声明必须被校验：
 *       「前端登录」发的 USER 令牌不能调管理端接口；</li>
 *   <li>注册必须校验邮箱验证码（以前 Redis 里存的码从来没被比对过）。</li>
 * </ul>
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthHardeningContractTest {

    private static final String CODE_KEY_PREFIX = "auth:verification:code:";
    private static final String LIMIT_KEY_PREFIX = "auth:verification:limit:";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String username;
    private String password;
    private String token;

    private String adminUsername;
    private String adminPassword;
    private String adminToken;

    /** 测试期间用到的邮箱（@AfterEach 清 Redis 里的验证码与限流 key）。 */
    private final List<String> emails = new ArrayList<>();

    /** 测试期间注册出来的用户名。 */
    private final List<String> registeredUsernames = new ArrayList<>();

    /** 测试期间额外建出来的账号（清理放在 @AfterEach，断言失败也不能留下数据）。 */
    private final List<String> extraUsernames = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        password = "Passw0rd" + suffix;
        username = "ah_user_" + suffix;
        createUser(username, "ah_user_" + suffix + "@example.com", password, User.Role.USER);
        token = loginUser(username, password);

        adminPassword = "Passw0rd" + suffix;
        adminUsername = "ah_admin_" + suffix;
        createUser(adminUsername, "ah_admin_" + suffix + "@example.com", adminPassword, User.Role.SUPER_ADMIN);
        adminToken = loginAdmin(adminUsername, adminPassword);
    }

    @AfterEach
    void tearDown() {
        for (String email : emails) {
            redisTemplate.delete(CODE_KEY_PREFIX + email);
            redisTemplate.delete(LIMIT_KEY_PREFIX + email);
        }
        emails.clear();
        for (String name : registeredUsernames) {
            userRepository.findByUsername(name).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
        registeredUsernames.clear();
        for (String name : extraUsernames) {
            userRepository.findByUsername(name).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
        extraUsernames.clear();

        userRepository.findByUsername(username).ifPresent(user -> userRepository.deleteById(user.getId()));
        userRepository.findByUsername(adminUsername).ifPresent(user -> userRepository.deleteById(user.getId()));
    }

    // ------------------------------------------------------------ A7 登出必须登录

    @Test
    void anonymousFrontendLogoutIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/frontend/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", "whatever"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test
    void anonymousBackendLogoutIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/backend/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", "whatever"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test
    void logoutWithATokenIsNoContent() throws Exception {
        mockMvc.perform(post("/api/frontend/auth/logout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of())))
                .andExpect(status().isNoContent());
    }

    // ------------------------------------------------------------ A8 令牌类型必须被校验

    @Test
    void frontendTokenCannotReachAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/works/pending")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    /**
     * 管理员用「前端登录」拿到的令牌也进不了管理端。
     *
     * <p>这条是 A8 的关键：以前 userType 声明完全没人看，
     * 管理员用前端登录拿到的 USER 令牌照样能进 /api/admin/**。
     */
    @Test
    void adminUsingAFrontendTokenCannotReachAdminEndpoints() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String adminUserName = "ah_adminuser_" + suffix;
        String adminUserPassword = "Passw0rd" + suffix;
        createUser(adminUserName, "ah_adminuser_" + suffix + "@example.com", adminUserPassword, User.Role.ADMIN);
        extraUsernames.add(adminUserName);
        String frontendToken = loginUser(adminUserName, adminUserPassword);

        mockMvc.perform(get("/api/admin/works/pending")
                        .header("Authorization", "Bearer " + frontendToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test
    void backendTokenCanReachAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/works/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void frontendTokenCannotReachBackendEndpoints() throws Exception {
        mockMvc.perform(get("/api/backend/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test
    void backendLoginOnlyIssuesAdminTokensToAdmins() throws Exception {
        // 普通用户走后台登录必须被拒（不能拿到 ADMIN 类型的令牌）
        mockMvc.perform(post("/api/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isForbidden());

        // 管理员走后台登录拿到的令牌必须能进管理端
        assertNotNull(adminToken);
        mockMvc.perform(get("/api/backend/auth/profile")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SUPER_ADMIN"));
    }

    // ------------------------------------------------------------ A11 注册校验验证码

    @Test
    void registerWithAWrongVerificationCodeIsRejected() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "ah_wrong_" + suffix + "@example.com";
        emails.add(email);
        String newName = "ah_wrong_" + suffix;
        // 即使断言失败也要清干净 —— 注册请求可能在「期望失败」时意外成功
        registeredUsernames.add(newName);
        extraUsernames.add(newName);

        sendVerificationCode(email);
        String realCode = readCodeFromRedis(email);
        assertNotNull(realCode, "验证码必须真的写进 Redis");

        mockMvc.perform(post("/api/frontend/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", newName,
                                "email", email,
                                "password", "Passw0rd" + suffix,
                                "verificationCode", "000000".equals(realCode) ? "111111" : "000000"))))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 400 || status == 422,
                            "验证码不对必须被拒（400/422），实际=" + status);
                })
                .andExpect(jsonPath("$.message").isNotEmpty());

        assertTrue(userRepository.findByUsername(newName).isEmpty(), "注册失败时不能留下用户");
    }

    @Test
    void registerWithTheRightVerificationCodeMarksTheEmailVerified() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "ah_right_" + suffix + "@example.com";
        emails.add(email);
        String newName = "ah_right_" + suffix;
        registeredUsernames.add(newName);

        sendVerificationCode(email);
        String realCode = readCodeFromRedis(email);
        assertNotNull(realCode, "验证码必须真的写进 Redis");

        mockMvc.perform(post("/api/frontend/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", newName,
                                "email", email,
                                "password", "Passw0rd" + suffix,
                                "verificationCode", realCode))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        User created = userRepository.findByUsername(newName).orElseThrow();
        assertTrue(Boolean.TRUE.equals(created.getEmailVerified()),
                "验证码校验通过后邮箱必须标记为已验证");
        assertNull(redisTemplate.opsForValue().get(CODE_KEY_PREFIX + email),
                "验证码必须一次性使用（校验通过后 Redis 里的码要删掉）");
    }

    @Test
    void theSameVerificationCodeCannotBeUsedTwice() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "ah_twice_" + suffix + "@example.com";
        emails.add(email);
        String firstName = "ah_twice1_" + suffix;
        String secondName = "ah_twice2_" + suffix;
        registeredUsernames.add(firstName);
        registeredUsernames.add(secondName);

        sendVerificationCode(email);
        String realCode = readCodeFromRedis(email);
        assertNotNull(realCode, "验证码必须真的写进 Redis");

        mockMvc.perform(post("/api/frontend/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", firstName,
                                "email", email,
                                "password", "Passw0rd" + suffix,
                                "verificationCode", realCode))))
                .andExpect(status().isCreated());

        // 把第一个用户删掉，腾出邮箱 —— 这样第二次失败只可能是因为验证码已失效
        userRepository.findByUsername(firstName).ifPresent(user -> userRepository.deleteById(user.getId()));

        // 同一个码再注册一次必须失败
        mockMvc.perform(post("/api/frontend/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", secondName,
                                "email", email,
                                "password", "Passw0rd" + suffix,
                                "verificationCode", realCode))))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 400 || status == 422,
                            "验证码用过一次就该失效，实际=" + status);
                });

        assertTrue(userRepository.findByUsername(secondName).isEmpty(), "第二次注册不能留下用户");
    }

    @Test
    void registerWithoutAVerificationCodeStillWorks() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "ah_none_" + suffix + "@example.com";
        emails.add(email);
        String newName = "ah_none_" + suffix;
        registeredUsernames.add(newName);

        mockMvc.perform(post("/api/frontend/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", newName,
                                "email", email,
                                "password", "Passw0rd" + suffix))))
                .andExpect(status().isCreated());

        User created = userRepository.findByUsername(newName).orElseThrow();
        assertFalse(Boolean.TRUE.equals(created.getEmailVerified()),
                "没填验证码时邮箱保持未验证");
    }

    // ------------------------------------------------------------ 工具

    private void sendVerificationCode(String email) throws Exception {
        mockMvc.perform(post("/api/auth/verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email))))
                .andExpect(status().isOk());
    }

    /** 从 Redis 里读回验证码（发信是占位配置，读库才是可靠来源）。 */
    private String readCodeFromRedis(String email) {
        return redisTemplate.opsForValue().get(CODE_KEY_PREFIX + email);
    }

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
}
