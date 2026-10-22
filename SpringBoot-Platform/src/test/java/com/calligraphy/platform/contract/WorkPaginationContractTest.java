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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 作品列表的分页与排序测试。
 *
 * <p>两件事：
 * <ul>
 *   <li>{@code GET /api/works/user/{userId}} 必须真的分页
 *       （以前是把全量作品塞进 PageImpl，content 里装着 3 条、size 却写着 1）；</li>
 *   <li>{@code GET /api/works} 必须有稳定排序（最新在前），
 *       否则翻页可能重复或漏掉。</li>
 * </ul>
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class WorkPaginationContractTest {

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
    private JdbcTemplate jdbcTemplate;

    private String authorId;
    private String username;
    private String password;
    private String token;

    private final List<Long> createdWorkIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        password = "Passw0rd" + suffix;
        username = "wp_user_" + suffix;
        authorId = userRepository.save(User.builder()
                .username(username)
                .email("wp_user_" + suffix + "@example.com")
                .password(passwordEncoder.encode(password))
                .nickname("分页测试")
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
    }

    @AfterEach
    void tearDown() {
        for (Long id : createdWorkIds) {
            workRepository.findById(id).ifPresent(work -> workRepository.deleteById(id));
        }
        createdWorkIds.clear();
        if (authorId != null) {
            userRepository.findById(authorId).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
    }

    // ------------------------------------------------------------ A4 分页真的生效

    @Test
    void userWorkListRespectsPageSize() throws Exception {
        createWork("第一件");
        createWork("第二件");
        createWork("第三件");

        MvcResult result = mockMvc.perform(get("/api/works/user/" + authorId)
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andReturn();

        JsonNode content = body(result).get("content");
        assertEquals(1, content.size(),
                "size=1 时 content 里只能有一条（以前装的是全部 3 条，size 字段却在撒谎）");
    }

    @Test
    void userWorkListPagesDoNotOverlap() throws Exception {
        createWork("第一件");
        createWork("第二件");
        createWork("第三件");

        String firstId = firstIdOfPage(0);
        String secondId = firstIdOfPage(1);

        assertNotEquals(firstId, secondId, "第 1 页和第 2 页不能是同一件作品");
    }

    @Test
    void userWorkListEnvelopeMatchesTheRealTotal() throws Exception {
        createWork("第一件");
        createWork("第二件");
        createWork("第三件");

        mockMvc.perform(get("/api/works/user/" + authorId)
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    // ------------------------------------------------------------ A13 排序稳定

    @Test
    void publicGalleryIsSortedNewestFirst() throws Exception {
        // created_at 由实体的 @PrePersist 强制写成 now()，只能事后用 SQL 改。
        // 时间戳取「未来」是为了保证这三件一定排在画廊最前面（不受别的测试数据影响）。
        LocalDateTime now = LocalDateTime.now();
        Long oldest = createPublishedWork("最早的一件", now.plusHours(1));
        Long middle = createPublishedWork("中间的一件", now.plusHours(2));
        Long newest = createPublishedWork("最新的一件", now.plusHours(3));

        MvcResult result = mockMvc.perform(get("/api/works")
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();

        List<String> ids = new ArrayList<>();
        for (JsonNode node : body(result).get("content")) {
            ids.add(node.get("id").asText());
        }

        int iOldest = ids.indexOf(String.valueOf(oldest));
        int iMiddle = ids.indexOf(String.valueOf(middle));
        int iNewest = ids.indexOf(String.valueOf(newest));

        assertTrue(iNewest >= 0 && iMiddle >= 0 && iOldest >= 0,
                "三件作品必须都在第一页里，实际=" + ids);
        assertTrue(iNewest < iMiddle && iMiddle < iOldest,
                "画廊必须按创建时间倒序（最新在前），实际顺序=" + ids);
    }

    // ------------------------------------------------------------ 工具

    private void createWork(String title) {
        User author = userRepository.findById(authorId).orElseThrow();
        Long id = workRepository.save(Work.builder()
                .title(title)
                .description("描述")
                .content("内容")
                .imageUrl("/api/files/wp-contract-test.png")
                .user(author)
                .userId(authorId)
                .status(Work.WorkStatus.DRAFT)
                .reviewStatus(Work.ReviewStatus.PENDING)
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .isFeatured(false)
                .build()).getId();
        createdWorkIds.add(id);
    }

    private Long createPublishedWork(String title, LocalDateTime createdAt) {
        User author = userRepository.findById(authorId).orElseThrow();
        Long id = workRepository.save(Work.builder()
                .title(title)
                .description("描述")
                .content("内容")
                .imageUrl("/api/files/wp-contract-test.png")
                .user(author)
                .userId(authorId)
                .status(Work.WorkStatus.PUBLISHED)
                .reviewStatus(Work.ReviewStatus.APPROVED)
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .isFeatured(false)
                .build()).getId();
        createdWorkIds.add(id);
        jdbcTemplate.update("UPDATE works SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(createdAt), id);
        return id;
    }

    private String firstIdOfPage(int page) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/works/user/" + authorId)
                        .header("Authorization", "Bearer " + token)
                        .param("page", String.valueOf(page))
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode content = body(result).get("content");
        assertFalse(content.isEmpty(), "第 " + page + " 页不能是空的");
        return content.get(0).get("id").asText();
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
