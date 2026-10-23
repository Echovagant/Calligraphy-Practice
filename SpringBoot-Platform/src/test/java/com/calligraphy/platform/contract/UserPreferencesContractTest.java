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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 账号级偏好（{@code users.preferences}）的契约测试。
 *
 * <p>这一列早就存在、也一直被返回，但**从来没有写入路径** ——
 * 个人资料页的「保存设置」只落在浏览器本地，换设备就没了。
 *
 * <p>三条语义：
 * <ul>
 *   <li>传了合法 JSON → 落库并回显；</li>
 *   <li>传 null / 省略 → **不改动**（不能把已有值覆盖成 null）；</li>
 *   <li>传非法 JSON → 400，而且不能把坏数据写进库。</li>
 * </ul>
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserPreferencesContractTest {

    private static final String VALID_PREFERENCES = "{\"theme\":\"ink\",\"gridSize\":8}";

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
    private String token;

    private final List<String> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        username = "up_user_" + suffix;
        email = "up_user_" + suffix + "@example.com";
        String password = "Passw0rd" + suffix;
        userId = userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname("偏好测试")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();
        createdUserIds.add(userId);

        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        token = body(login).get("accessToken").asText();
    }

    @AfterEach
    void tearDown() {
        for (String id : createdUserIds) {
            userRepository.findById(id).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
        createdUserIds.clear();
    }

    @Test
    void preferencesArePersistedAndEchoedBack() throws Exception {
        MvcResult result = mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(profileBody(VALID_PREFERENCES))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences").value(VALID_PREFERENCES))
                .andReturn();

        assertNotNull(body(result).get("preferences"), "响应里必须回显 preferences");

        // 断言落到数据库
        String stored = userRepository.findById(userId).orElseThrow().getPreferences();
        assertEquals(VALID_PREFERENCES, stored, "preferences 必须真的写进 users 表");
    }

    @Test
    void omittingPreferencesKeepsTheExistingValue() throws Exception {
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(profileBody(VALID_PREFERENCES))))
                .andExpect(status().isOk());

        // 不带 preferences 再更新一次
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(profileBody(null))))
                .andExpect(status().isOk());

        String stored = userRepository.findById(userId).orElseThrow().getPreferences();
        assertEquals(VALID_PREFERENCES, stored, "不传 preferences 表示不改动，不能被覆盖成 null");
    }

    @Test
    void nullPreferencesMeansNoChange() throws Exception {
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(profileBody(VALID_PREFERENCES))))
                .andExpect(status().isOk());

        Map<String, Object> explicitNull = profileBody(null);
        explicitNull.put("preferences", null);
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(explicitNull)))
                .andExpect(status().isOk());

        String stored = userRepository.findById(userId).orElseThrow().getPreferences();
        assertEquals(VALID_PREFERENCES, stored, "显式传 null 也表示不改动");
    }

    @Test
    void invalidJsonIsRejectedWith400AndNotStored() throws Exception {
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(profileBody(VALID_PREFERENCES))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(profileBody("这不是 JSON"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());

        String stored = userRepository.findById(userId).orElseThrow().getPreferences();
        assertEquals(VALID_PREFERENCES, stored, "非法 JSON 不能写进库里");
    }

    @Test
    void profileResponseIncludesPreferences() throws Exception {
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(profileBody(VALID_PREFERENCES))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences").value(VALID_PREFERENCES));
    }

    @Test
    void profileUpdateRequiresToken() throws Exception {
        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(profileBody(VALID_PREFERENCES))))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------ 工具

    private Map<String, Object> profileBody(String preferences) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nickname", "偏好测试");
        body.put("email", email);
        if (preferences != null) {
            body.put("preferences", preferences);
        }
        return body;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    /**
     * 读响应体的**原始字节**。
     *
     * <p>MockHttpServletResponse 的 characterEncoding 默认是 ISO-8859-1，
     * getContentAsString() 会把 UTF-8 中文读成乱码 —— 中文断言会莫名其妙地失败。
     */
    private JsonNode body(MvcResult result) throws Exception {
        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 0, "响应体不能为空");
        JsonNode node = objectMapper.readTree(content);
        assertNotNull(node);
        return node;
    }
}
