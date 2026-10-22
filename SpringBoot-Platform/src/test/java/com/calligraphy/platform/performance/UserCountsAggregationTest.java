package com.calligraphy.platform.performance;

import com.calligraphy.platform.model.Comment;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.repository.CommentRepository;
import com.calligraphy.platform.repository.UserRepository;
import com.calligraphy.platform.repository.WorkRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
 * P1：用户资料页的计数必须是**聚合查询**算出来的，不能把作品与评论整批加载进内存。
 *
 * <p>{@code GET /api/users/{userId}} 与 {@code PUT /api/users/profile} 都要经过
 * {@code UserServiceImpl.getUserCounts}。以前它把该用户的全部作品（含 TEXT content 列）
 * 和全部评论查出来在内存里求和 / size()。
 *
 * <p>断言方式：打开 Hibernate 统计（{@code hibernate.generate_statistics}），
 * 请求前后看 **实体加载数**（{@code getLoadCount}）——
 * 聚合查询不加载任何 Work / Comment 实体，所以必须是 0。
 * 修复前这个数等于该用户的作品数与评论数，测试会红。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserCountsAggregationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SessionFactory sessionFactory;

    private String userId;
    private String username;
    private String password;
    private String token;

    private final List<Long> createdWorkIds = new ArrayList<>();
    private final List<Long> createdCommentIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        password = "Passw0rd" + suffix;
        username = "uc_" + suffix;
        userId = userRepository.save(User.builder()
                .username(username)
                .email("uc_" + suffix + "@example.com")
                .password(passwordEncoder.encode(password))
                .nickname("聚合计数测试")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();

        token = login(username, password);

        User author = userRepository.findById(userId).orElseThrow();
        // 3 个作品，点赞 1/2/3、浏览 2/5/8 —— 总和 6 与 15
        createWork(author, 1, 2);
        createWork(author, 2, 5);
        createWork(author, 3, 8);

        // 2 条评论
        createComment(author, "第一条评论");
        createComment(author, "第二条评论");
    }

    @AfterEach
    void tearDown() {
        for (Long id : createdCommentIds) {
            commentRepository.findById(id).ifPresent(c -> commentRepository.deleteById(c.getId()));
        }
        createdCommentIds.clear();
        for (Long id : createdWorkIds) {
            workRepository.findById(id).ifPresent(w -> workRepository.deleteById(w.getId()));
        }
        createdWorkIds.clear();
        if (userId != null) {
            userRepository.findById(userId).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
    }

    @Test
    void userProfileCountsAreCorrect() throws Exception {
        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workCount").value(3))
                .andExpect(jsonPath("$.commentCount").value(2))
                .andExpect(jsonPath("$.totalLikesReceived").value(6))
                .andExpect(jsonPath("$.totalViewsReceived").value(15));
    }

    /**
     * 计数必须来自聚合查询 —— 请求过程中一个 Work / Comment 实体都不该被加载。
     */
    @Test
    void userProfileCountsDoNotLoadWorkOrCommentEntities() throws Exception {
        sessionFactory.getStatistics().clear();

        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Statistics stats = sessionFactory.getStatistics();
        long workLoads = stats.getEntityStatistics(Work.class.getName()).getLoadCount();
        long commentLoads = stats.getEntityStatistics(Comment.class.getName()).getLoadCount();

        assertEquals(0L, workLoads,
                "统计计数不该把作品实体整批加载进内存（加载了 " + workLoads + " 条）");
        assertEquals(0L, commentLoads,
                "统计计数不该把评论实体整批加载进内存（加载了 " + commentLoads + " 条）");
    }

    // ------------------------------------------------------------ 工具

    private void createWork(User author, int likeCount, int viewCount) {
        Work work = workRepository.save(Work.builder()
                .title("聚合计数测试作品")
                .description("描述")
                .content("内容")
                .imageUrl("/api/files/uc-test.png")
                .user(author)
                .userId(author.getId())
                .status(Work.WorkStatus.PUBLISHED)
                .reviewStatus(Work.ReviewStatus.APPROVED)
                .likeCount(likeCount)
                .commentCount(0)
                .viewCount(viewCount)
                .isFeatured(false)
                .build());
        createdWorkIds.add(work.getId());
    }

    private void createComment(User author, String content) {
        Work work = workRepository.findById(createdWorkIds.get(0)).orElseThrow();
        Comment comment = commentRepository.save(Comment.builder()
                .content(content)
                .user(author)
                .work(work)
                .likeCount(0)
                .status(Comment.CommentStatus.APPROVED)
                .isFeatured(false)
                .build());
        createdCommentIds.add(comment.getId());
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
