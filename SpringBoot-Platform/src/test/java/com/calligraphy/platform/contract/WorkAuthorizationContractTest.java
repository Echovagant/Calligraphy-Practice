package com.calligraphy.platform.contract;

import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.repository.BackendUserRepository;
import com.calligraphy.platform.repository.UserRepository;
import com.calligraphy.platform.repository.WorkRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

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
 * 作品域的越权与数据一致性测试。
 *
 * <p>覆盖四件事：
 * <ul>
 *   <li>换主图必须判归属（以前任何登录用户都能改别人的作品主图）；</li>
 *   <li>越权要返回 403 而不是 500（以前抛 JDK 的 SecurityException，掉进通用兜底）；</li>
 *   <li>管理员身份看 {@code users.role}（以前查 backend_users，真管理员反而没权限）；</li>
 *   <li>换主图只上传一次，响应里的 fileUrl 就是入库的 URL。</li>
 * </ul>
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class WorkAuthorizationContractTest {

    /** 1x1 像素的合法 PNG。 */
    private static final byte[] TINY_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private BackendUserRepository backendUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authorId;
    private String authorUsername;
    private String authorPassword;
    private String authorToken;

    private String otherId;
    private String otherUsername;
    private String otherPassword;
    private String otherToken;

    private String adminUsername;
    private String adminPassword;
    private String adminToken;

    private Long workId;

    /** 本测试上传出来的文件（@AfterEach 删掉，别在磁盘上留垃圾）。 */
    private final List<Path> createdFiles = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        authorPassword = "Passw0rd" + suffix;
        authorUsername = "wa_author_" + suffix;
        authorId = createUser(authorUsername, "wa_author_" + suffix + "@example.com", authorPassword, User.Role.USER);
        authorToken = loginUser(authorUsername, authorPassword);

        otherPassword = "Passw0rd" + suffix;
        otherUsername = "wa_other_" + suffix;
        otherId = createUser(otherUsername, "wa_other_" + suffix + "@example.com", otherPassword, User.Role.USER);
        otherToken = loginUser(otherUsername, otherPassword);

        adminPassword = "Passw0rd" + suffix;
        adminUsername = "wa_admin_" + suffix;
        createUser(adminUsername, "wa_admin_" + suffix + "@example.com", adminPassword, User.Role.SUPER_ADMIN);
        // 管理端接口只认 ADMIN 类型的令牌，必须走后台登录端点拿
        adminToken = loginAdmin(adminUsername, adminPassword);

        User author = userRepository.findById(authorId).orElseThrow();
        workId = workRepository.save(Work.builder()
                .title("作者的作品")
                .description("描述")
                .content("内容")
                .imageUrl("/api/files/wa-contract-test.png")
                .user(author)
                .userId(authorId)
                .status(Work.WorkStatus.DRAFT)
                .reviewStatus(Work.ReviewStatus.PENDING)
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .isFeatured(false)
                .build()).getId();
    }

    @AfterEach
    void tearDown() {
        for (Path file : createdFiles) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // 清理失败不影响断言结果
            }
        }
        createdFiles.clear();

        // works.reviewer_id 指向 backend_users，作品必须先删
        if (authorId != null) {
            workRepository.findByUserId(authorId).forEach(work -> workRepository.deleteById(work.getId()));
        }
        if (otherId != null) {
            workRepository.findByUserId(otherId).forEach(work -> workRepository.deleteById(work.getId()));
        }
        workRepository.findById(workId).ifPresent(work -> workRepository.deleteById(work.getId()));

        backendUserRepository.findByUsername(adminUsername)
                .ifPresent(reviewer -> backendUserRepository.delete(reviewer));

        if (authorId != null) {
            userRepository.findById(authorId).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
        if (otherId != null) {
            userRepository.findById(otherId).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
        userRepository.findByUsername(adminUsername).ifPresent(user -> userRepository.deleteById(user.getId()));
    }

    // ------------------------------------------------------------ A1 换主图越权

    @Test
    void otherUserCannotChangeTheMainImageOfSomebodyElsesWork() throws Exception {
        String originalImageUrl = workRepository.findById(workId).orElseThrow().getImageUrl();

        mockMvc.perform(multipart("/api/works/" + workId + "/image")
                        .file(png("image"))
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());

        Work work = workRepository.findById(workId).orElseThrow();
        assertEquals(originalImageUrl, work.getImageUrl(), "越权换图后作品主图不能被改掉");
    }

    // ------------------------------------------------------------ A2 越权返回 403 而不是 500

    @Test
    void updatingSomebodyElsesWorkReturns403Not500() throws Exception {
        mockMvc.perform(put("/api/works/" + workId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "被改掉的标题"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());

        Work work = workRepository.findById(workId).orElseThrow();
        assertEquals("作者的作品", work.getTitle(), "越权更新后标题不能变");
    }

    @Test
    void deletingSomebodyElsesWorkReturns403Not500() throws Exception {
        mockMvc.perform(delete("/api/works/" + workId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty());

        assertTrue(workRepository.findById(workId).isPresent(), "越权删除后作品必须还在");
    }

    // ------------------------------------------------------------ A3 管理员判定看 users.role

    @Test
    void adminCanUpdateSomebodyElsesWork() throws Exception {
        mockMvc.perform(put("/api/works/" + workId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "管理员改的标题"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("管理员改的标题"));

        Work work = workRepository.findById(workId).orElseThrow();
        assertEquals("管理员改的标题", work.getTitle(), "管理员改标题必须真的落库");
    }

    @Test
    void adminCanDeleteSomebodyElsesWork() throws Exception {
        mockMvc.perform(delete("/api/works/" + workId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertTrue(workRepository.findById(workId).isEmpty(), "管理员删作品必须真的删掉");
    }

    @Test
    void adminCanChangeTheMainImageOfSomebodyElsesWork() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/works/" + workId + "/image")
                        .file(png("image"))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").isNotEmpty())
                .andReturn();
        trackUploadedFile(body(result).get("fileUrl").asText());

        Work work = workRepository.findById(workId).orElseThrow();
        assertNotNull(work.getImageUrl());
        assertFalse(work.getImageUrl().isBlank(), "管理员换图后作品主图不能为空");
    }

    // ------------------------------------------------------------ A6 只上传一次，URL 一致

    @Test
    void changingTheMainImageUploadsTheFileOnlyOnceAndStoresThatUrl() throws Exception {
        Path uploadRoot = Paths.get("uploads", "work", authorId);
        long before = countFiles(uploadRoot);

        MvcResult result = mockMvc.perform(multipart("/api/works/" + workId + "/image")
                        .file(png("image"))
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").isNotEmpty())
                .andReturn();
        String fileUrl = body(result).get("fileUrl").asText();
        trackUploadedFile(fileUrl);

        long after = countFiles(uploadRoot);
        assertEquals(before + 1, after,
                "换一次主图只能落一个文件（以前 Controller 与 Service 各传一次，磁盘上会有两份）");

        Work work = workRepository.findById(workId).orElseThrow();
        assertEquals(fileUrl, work.getImageUrl(), "响应里的 fileUrl 必须就是入库的 imageUrl");

        // 作品详情接口读到的也必须是同一个 URL
        mockMvc.perform(get("/api/works/" + workId)
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(fileUrl));
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

    /** 普通用户令牌（userType=USER）。 */
    private String loginUser(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return body(login).get("accessToken").asText();
    }

    /** 管理端令牌（userType=ADMIN）。 */
    private String loginAdmin(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/backend/auth/login")
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

    private MockMultipartFile png(String field) {
        return new MockMultipartFile(field, "test.png", MediaType.IMAGE_PNG_VALUE, TINY_PNG);
    }

    /** 递归统计目录下的文件数（目录不存在算 0）。 */
    private long countFiles(Path root) throws IOException {
        if (!Files.exists(root)) {
            return 0L;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile).count();
        }
    }

    /** 记录上传出来的文件路径，teardown 时删掉。 */
    private void trackUploadedFile(String fileUrl) {
        // /api/files/work/{userId}/{yyyy}/{MM}/{dd}/{name}
        String[] parts = fileUrl.split("/");
        if (parts.length < 9 || !"work".equals(parts[3])) {
            return;
        }
        String userId = parts[4];
        String datePath = parts[5] + "/" + parts[6] + "/" + parts[7];
        Path file = Paths.get("uploads", "work", userId, datePath, parts[8]);
        if (Files.exists(file)) {
            createdFiles.add(file);
        }
    }
}
