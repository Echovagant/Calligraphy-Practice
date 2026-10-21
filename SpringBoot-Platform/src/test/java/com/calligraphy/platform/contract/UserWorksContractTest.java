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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * user + works + stats 域的契约测试（spec 阶段 3）。
 *
 * <p>每个 operation 一个测试方法，断言 method / path / 状态码 / 响应字段集 / 错误码。
 * 刻意**只依赖契约**（路径、状态码、字段名），不引用实现层的 DTO 类 ——
 * 实现换了形状而契约没变时，测试会立刻失败。
 *
 * <p>主键字段一律叫 {@code id}（前端读的就是 {@code work.id} / {@code user.id}）。
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserWorksContractTest {

    /** 1x1 像素的合法 PNG，用来测 multipart 上传与取回。 */
    private static final byte[] TINY_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

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
    private String accessToken;

    private final List<String> createdWorkIds = new ArrayList<>();
    private final List<String> createdFileUrls = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        password = "Passw0rd" + suffix;
        username = "uw_" + suffix;
        email = "uw_" + suffix + "@example.com";
        userId = userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname("契约测试")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();

        // 走真实的登录端点拿令牌，认证链本身就是被测对象
        MvcResult login = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        accessToken = body(login).get("accessToken").asText();
        assertFalse(accessToken.isBlank(), "登录必须拿到 accessToken");
    }

    @AfterEach
    void tearDown() {
        if (userId != null) {
            userRepository.findById(userId).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        assertFalse(content.isBlank(), "响应体不能为空");
        return objectMapper.readTree(content);
    }

    private MockMultipartFile png(String field) {
        return new MockMultipartFile(field, "test.png", MediaType.IMAGE_PNG_VALUE, TINY_PNG);
    }

    // ------------------------------------------------------------ user（4 个 operation）

    @Test
    void userUpdateProfile() throws Exception {
        MvcResult result = mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nickname", "新昵称",
                                "email", email,
                                "bio", "简介"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.nickname").value("新昵称"))
                .andExpect(jsonPath("$.email").value(email))
                // 主键叫 id，不是 userId
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result));
    }

    @Test
    void userUploadAvatar() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/users/avatar")
                        .file(png("avatar"))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").isNotEmpty())
                .andExpect(jsonPath("$.fileName").isNotEmpty())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        String fileUrl = body(result).get("fileUrl").asText();
        createdFileUrls.add(fileUrl);
        assertTrue(fileUrl.startsWith("/api/files/"),
                "文件 URL 必须落在静态映射的 /api/files/ 下，实际=" + fileUrl);
    }

    @Test
    void userChangePassword() throws Exception {
        String newPassword = "NewPassw0rd" + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(put("/api/users/password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "oldPassword", password,
                                "newPassword", newPassword,
                                "confirmPassword", newPassword))))
                .andExpect(status().isNoContent());

        // 旧密码必须失效，新密码必须能登录
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", newPassword))))
                .andExpect(status().isOk());
    }

    @Test
    void userGetById() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andReturn();
        assertNotNull(body(result));
    }

    // ------------------------------------------------------------ works（7 个 operation）

    @Test
    void workCreate() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/works")
                        .file(png("image"))
                        .param("title", "契约测试作品")
                        .param("description", "描述")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("契约测试作品"))
                .andExpect(jsonPath("$.imageUrl").isNotEmpty())
                .andExpect(jsonPath("$.workId").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        JsonNode node = body(result);
        createdWorkIds.add(node.get("id").asText());
        createdFileUrls.add(node.get("imageUrl").asText());
    }

    @Test
    void workList() throws Exception {
        mockMvc.perform(get("/api/works")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void workListByUser() throws Exception {
        mockMvc.perform(get("/api/works/user/" + userId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").isNumber())
                .andExpect(jsonPath("$.size").isNumber())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    @Test
    void workGetById() throws Exception {
        String workId = createWork();

        MvcResult result = mockMvc.perform(get("/api/works/" + workId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workId))
                .andExpect(jsonPath("$.title").value("被查询的作品"))
                .andExpect(jsonPath("$.imageUrl").isNotEmpty())
                .andExpect(jsonPath("$.workId").doesNotExist())
                .andReturn();
        assertNotNull(body(result));
    }

    @Test
    void workUpdate() throws Exception {
        String workId = createWork();

        mockMvc.perform(put("/api/works/" + workId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "改过的标题",
                                "description", "改过的描述"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workId))
                .andExpect(jsonPath("$.title").value("改过的标题"));
    }

    @Test
    void workUpdateImage() throws Exception {
        String workId = createWork();

        MvcResult result = mockMvc.perform(multipart("/api/works/" + workId + "/image")
                        .file(png("image"))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").isNotEmpty())
                .andReturn();
        createdFileUrls.add(body(result).get("fileUrl").asText());
    }

    @Test
    void workDelete() throws Exception {
        String workId = createWork();

        mockMvc.perform(delete("/api/works/" + workId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/works/" + workId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadedImageIsRetrievable() throws Exception {
        String workId = createWork();
        String imageUrl = createdFileUrls.get(createdFileUrls.size() - 1);

        // 图片必须能取回 —— 静态映射 /api/files/** -> {file.upload-dir}
        MvcResult file = mockMvc.perform(get(imageUrl))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(file.getResponse().getContentAsByteArray().length > 0,
                "取回的图片不能是空字节");
        assertTrue(workId != null && !workId.isBlank());
    }

    // ------------------------------------------------------------ stats（1 个 operation）

    @Test
    void statsOverview() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/stats/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorks").isNumber())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.totalPracticeRecords").isNumber())
                .andExpect(jsonPath("$.popularCopybooks").isArray())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result));
    }

    // ------------------------------------------------------------ 错误路径

    @Test
    void missingTokenIsUnauthorizedWithCodeAndMessage() throws Exception {
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void validationErrorUsesCodeAndMessageShape() throws Exception {
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nickname", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void nonNumericWorkIdIsAClientErrorNotAServerError() throws Exception {
        mockMvc.perform(get("/api/works/not-a-number")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(result -> assertTrue(result.getResponse().getStatus() < 500,
                        "非数字 workId 不该是 5xx，实际=" + result.getResponse().getStatus()));
    }

    @Test
    void missingWorkReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/works/999999999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ------------------------------------------------------------ 工具

    private String createWork() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/works")
                        .file(png("image"))
                        .param("title", "被查询的作品")
                        .param("description", "描述")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = body(result);
        String id = node.get("id").asText();
        createdWorkIds.add(id);
        if (node.has("imageUrl") && !node.get("imageUrl").isNull()) {
            createdFileUrls.add(node.get("imageUrl").asText());
        }
        return id;
    }
}
