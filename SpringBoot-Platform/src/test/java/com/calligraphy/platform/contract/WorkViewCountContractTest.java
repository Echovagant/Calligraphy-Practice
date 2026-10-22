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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 作品浏览量的落库测试。
 *
 * <p>以前 {@code getWorkById} 跑在类级的只读事务里，「改实体再 save」的 UPDATE
 * 不会 flush（FlushMode.MANUAL）—— 接口返回的 viewCount 在涨，
 * {@code works.view_count} 却永远是 0。
 *
 * <p>所以这里**必须查库**断言，不能只看响应体。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class WorkViewCountContractTest {

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

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private String authorId;
    private String username;
    private String password;
    private String token;

    private Long workId;

    private final List<Path> createdFiles = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        password = "Passw0rd" + suffix;
        username = "wv_user_" + suffix;
        authorId = userRepository.save(User.builder()
                .username(username)
                .email("wv_user_" + suffix + "@example.com")
                .password(passwordEncoder.encode(password))
                .nickname("浏览量测试")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();

        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        token = body(login).get("accessToken").asText();

        User author = userRepository.findById(authorId).orElseThrow();
        workId = workRepository.save(Work.builder()
                .title("计浏览量的作品")
                .description("描述")
                .content("内容")
                .imageUrl("/api/files/wv-contract-test.png")
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
                // 清理失败不影响断言
            }
        }
        createdFiles.clear();

        workRepository.findById(workId).ifPresent(work -> workRepository.deleteById(work.getId()));
        if (authorId != null) {
            userRepository.findById(authorId).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
    }

    // ------------------------------------------------------------ A5 浏览量必须落库

    @Test
    void readingTheDetailPersistsTheViewCount() throws Exception {
        assertEquals(0, viewCountInDb(), "前置条件：作品初始浏览量是 0");

        mockMvc.perform(get("/api/works/" + workId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewCount").value(1));

        assertEquals(1, viewCountInDb(), "浏览一次之后 works.view_count 必须是 1（以前永远是 0）");
    }

    @Test
    void everyDetailRequestAddsOneToThePersistedViewCount() throws Exception {
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(get("/api/works/" + workId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.viewCount").value(i));
            assertEquals(i, viewCountInDb(), "第 " + i + " 次浏览后库里必须是 " + i);
        }
    }

    @Test
    void changingTheMainImageDoesNotCountAsAView() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/works/" + workId + "/image")
                        .file(new MockMultipartFile("image", "test.png", MediaType.IMAGE_PNG_VALUE, TINY_PNG))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        trackUploadedFile(body(result).get("fileUrl").asText());

        assertEquals(0, viewCountInDb(), "换主图不是浏览作品详情，不该把浏览量涨上去");
    }

    // ------------------------------------------------------------ 工具

    /** 直接从库里读浏览量 —— 响应体不算数。 */
    private int viewCountInDb() {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT view_count FROM works WHERE id = ?", Integer.class, workId);
        return value == null ? 0 : value;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        assertFalse(content.isBlank(), "响应体不能为空");
        return objectMapper.readTree(content);
    }

    private void trackUploadedFile(String fileUrl) {
        String[] parts = fileUrl.split("/");
        if (parts.length < 9 || !"work".equals(parts[3])) {
            return;
        }
        Path file = Paths.get("uploads", "work", parts[4], parts[5], parts[6], parts[7], parts[8]);
        if (Files.exists(file)) {
            createdFiles.add(file);
        }
    }
}
