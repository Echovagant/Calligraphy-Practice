package com.calligraphy.platform.contract;

import com.calligraphy.platform.model.PracticeRecord;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.repository.BackendUserRepository;
import com.calligraphy.platform.repository.PracticeRecordRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
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
 * 服务端搜索 / 筛选 / 排序的契约测试。
 *
 * <p>四个端点：{@code GET /api/works}、{@code GET /api/works/user/{userId}}、
 * {@code GET /api/admin/works/pending}、{@code GET /api/copybooks/practice/history}。
 *
 * <p><b>红线</b>：筛选与排序必须在数据库层做 —— 查出全量再在内存里过滤/排序
 * 是换汤不换药。这里用「结果集只含命中的那几条」与「顺序正确」来证明条件真的下推到了 SQL。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class WorkSearchFilterContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private PracticeRecordRepository practiceRecordRepository;

    @Autowired
    private BackendUserRepository backendUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 作者（普通用户）。 */
    private String authorId;
    private String authorUsername;

    /** 另一个作者 —— 用来证明「按作者名搜」不会把别人的作品也捞出来。 */
    private String otherAuthorId;

    /** 管理员（管理端令牌必须是 userType=ADMIN）。 */
    private String adminUsername;
    private String adminToken;

    /** 三部已发布作品，viewCount / commentCount 各不相同。 */
    private Long workByTitle;
    private Long workByDescription;
    private Long workPlain;
    private Long workEnglish;
    /** 待审作品（DRAFT + PENDING），标题含「待审标记作品」。 */
    private Long pendingWork;
    /** 同一个作者的另一部待审作品，标题不含关键词。 */
    private Long pendingWorkSameAuthor;
    /** 另一个作者的待审作品。 */
    private Long pendingWorkOtherAuthor;

    private final List<Long> createdWorkIds = new ArrayList<>();
    private final List<Long> createdPracticeRecordIds = new ArrayList<>();
    private final List<String> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        String authorPassword = "Passw0rd" + suffix + "a";
        authorUsername = "ws_author_" + suffix;
        authorId = userRepository.save(User.builder()
                .username(authorUsername)
                .email(authorUsername + "@example.com")
                .password(passwordEncoder.encode(authorPassword))
                .nickname("搜索测试作者")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();
        createdUserIds.add(authorId);

        String adminPassword = "Passw0rd" + suffix + "b";
        adminUsername = "ws_admin_" + suffix;
        String adminId = userRepository.save(User.builder()
                .username(adminUsername)
                .email(adminUsername + "@example.com")
                .password(passwordEncoder.encode(adminPassword))
                .nickname("搜索测试管理员")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.SUPER_ADMIN)
                .build()).getId();
        // 管理员也要登记进清理名单 —— 否则每次跑完都留下一行孤儿用户
        createdUserIds.add(adminId);
        adminToken = loginAdmin(adminUsername, adminPassword);

        User author = userRepository.findById(authorId).orElseThrow();

        workByTitle = saveWork(author, "青山绿水标记", "描述甲", 50, 1);
        workByDescription = saveWork(author, "另一幅作品", "含标记的描述", 10, 9);
        workPlain = saveWork(author, "第三幅普通作品", "描述丙", 30, 5);
        workEnglish = saveWork(author, "Ink Brush Study", "english description", 5, 0);
        pendingWork = saveWork(author, "待审标记作品", "待审描述", 1, 0,
                Work.WorkStatus.DRAFT, Work.ReviewStatus.PENDING);
        pendingWorkSameAuthor = saveWork(author, "另一个待审作品", "另一个待审描述", 1, 0,
                Work.WorkStatus.DRAFT, Work.ReviewStatus.PENDING);

        // 另一个作者：按作者名搜时不该把他的作品也捞出来
        String otherAuthorPassword = "Passw0rd" + suffix + "c";
        otherAuthorId = userRepository.save(User.builder()
                .username("ws_other_" + suffix)
                .email("ws_other_" + suffix + "@example.com")
                .password(passwordEncoder.encode(otherAuthorPassword))
                .nickname("另一个作者")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();
        createdUserIds.add(otherAuthorId);
        User otherAuthor = userRepository.findById(otherAuthorId).orElseThrow();
        pendingWorkOtherAuthor = saveWork(otherAuthor, "别人的待审作品", "别人的待审描述", 1, 0,
                Work.WorkStatus.DRAFT, Work.ReviewStatus.PENDING);
    }

    @AfterEach
    void tearDown() {
        for (Long id : createdPracticeRecordIds) {
            practiceRecordRepository.findById(id).ifPresent(r -> practiceRecordRepository.deleteById(r.getId()));
        }
        createdPracticeRecordIds.clear();
        for (Long id : createdWorkIds) {
            workRepository.findById(id).ifPresent(w -> workRepository.deleteById(w.getId()));
        }
        createdWorkIds.clear();
        backendUserRepository.findByUsername(adminUsername)
                .ifPresent(reviewer -> backendUserRepository.delete(reviewer));
        for (String id : createdUserIds) {
            userRepository.findById(id).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
        createdUserIds.clear();
    }

    // ------------------------------------------------------------ GET /api/works

    @Test
    void keywordMatchesTitleOrDescription() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/works")
                        .param("keyword", "标记")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();

        List<String> ids = idsIn(body(result).get("content"));
        assertTrue(ids.contains(String.valueOf(workByTitle)), "标题命中的作品应当在结果里，实际=" + ids);
        assertTrue(ids.contains(String.valueOf(workByDescription)), "描述命中的作品应当在结果里，实际=" + ids);
        assertFalse(ids.contains(String.valueOf(workPlain)), "没命中的作品不该出现，实际=" + ids);
        assertFalse(ids.contains(String.valueOf(pendingWork)), "未通过审核的作品不该出现在公开列表，实际=" + ids);
    }

    @Test
    void keywordIsCaseInsensitive() throws Exception {
        MvcResult lower = mockMvc.perform(get("/api/works")
                        .param("keyword", "ink")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();
        List<String> lowerIds = idsIn(body(lower).get("content"));
        assertTrue(lowerIds.contains(String.valueOf(workEnglish)),
                "小写关键词应当命中大写标题，实际=" + lowerIds);

        MvcResult upper = mockMvc.perform(get("/api/works")
                        .param("keyword", "INK")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();
        List<String> upperIds = idsIn(body(upper).get("content"));
        assertTrue(upperIds.contains(String.valueOf(workEnglish)),
                "大写关键词应当命中小写标题，实际=" + upperIds);
    }

    @Test
    void sortPopularOrdersByViewCountDescending() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/works")
                        .param("sort", "popular")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();

        List<Long> myIds = myWorkIdsInOrder(body(result).get("content"));
        assertEquals(List.of(workByTitle, workPlain, workByDescription), myIds.subList(0, 3),
                "sort=popular 必须按浏览量倒序（50 / 30 / 10），实际=" + myIds);
    }

    @Test
    void sortCommentedOrdersByCommentCountDescending() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/works")
                        .param("sort", "commented")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();

        List<Long> myIds = myWorkIdsInOrder(body(result).get("content"));
        assertEquals(List.of(workByDescription, workPlain, workByTitle), myIds.subList(0, 3),
                "sort=commented 必须按评论数倒序（9 / 5 / 1），实际=" + myIds);
    }

    @Test
    void sortLatestIsTheDefaultAndKeepsIdAsTieBreaker() throws Exception {
        MvcResult explicitLatest = mockMvc.perform(get("/api/works")
                        .param("sort", "latest")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult byDefault = mockMvc.perform(get("/api/works")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();

        List<Long> explicitIds = myWorkIdsInOrder(body(explicitLatest).get("content"));
        List<Long> defaultIds = myWorkIdsInOrder(body(byDefault).get("content"));
        assertEquals(explicitIds, defaultIds, "不传 sort 时应当与 sort=latest 一致");
        assertEquals(List.of(workEnglish, workPlain, workByDescription, workByTitle), explicitIds,
                "sort=latest 必须按创建时间倒序，实际=" + explicitIds);
    }

    // ------------------------------------------------------------ GET /api/works/user/{userId}

    @Test
    void userWorksSupportKeywordAndSort() throws Exception {
        MvcResult byKeyword = mockMvc.perform(get("/api/works/user/" + authorId)
                        .header("Authorization", "Bearer " + authorToken())
                        .param("keyword", "标记")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();
        List<String> ids = idsIn(body(byKeyword).get("content"));
        assertTrue(ids.contains(String.valueOf(workByTitle)), "作者作品列表按关键词过滤，实际=" + ids);
        assertFalse(ids.contains(String.valueOf(workPlain)), "没命中的作品不该出现，实际=" + ids);
        // 待审作品也在这个作者名下，只是作者本人能看到
        assertTrue(ids.contains(String.valueOf(pendingWork)),
                "作者自己的列表应当能看到自己的待审作品，实际=" + ids);

        MvcResult byPopular = mockMvc.perform(get("/api/works/user/" + authorId)
                        .header("Authorization", "Bearer " + authorToken())
                        .param("sort", "popular")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();
        List<Long> myIds = myWorkIdsInOrder(body(byPopular).get("content"));
        assertEquals(workByTitle, myIds.get(0), "作者作品列表也要支持 sort=popular");
    }

    // ------------------------------------------------------------ GET /api/admin/works/pending

    @Test
    void adminPendingMatchesWorkTitle() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/works/pending")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "待审标记作品")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();

        List<String> ids = idsIn(body(result).get("content"));
        assertTrue(ids.contains(String.valueOf(pendingWork)), "标题命中的待审作品应当在结果里，实际=" + ids);
        assertFalse(ids.contains(String.valueOf(pendingWorkSameAuthor)),
                "标题没命中的待审作品不该出现，实际=" + ids);
        assertFalse(ids.contains(String.valueOf(pendingWorkOtherAuthor)),
                "别的作者的待审作品不该出现，实际=" + ids);
        assertFalse(ids.contains(String.valueOf(workByTitle)), "已发布的作品不该出现在待审队列，实际=" + ids);
    }

    @Test
    void adminPendingMatchesAuthorName() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/works/pending")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", authorUsername)
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();

        List<String> ids = idsIn(body(result).get("content"));
        assertTrue(ids.contains(String.valueOf(pendingWork)), "作者名命中的待审作品应当在结果里，实际=" + ids);
        assertTrue(ids.contains(String.valueOf(pendingWorkSameAuthor)),
                "同一个作者的待审作品也该命中，实际=" + ids);
        assertFalse(ids.contains(String.valueOf(pendingWorkOtherAuthor)),
                "别的作者的待审作品不该出现，实际=" + ids);
    }

    @Test
    void adminPendingKeywordThatMatchesNothingReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/admin/works/pending")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "绝对不会命中的关键词xyz")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.total").value(0));
    }

    // ------------------------------------------------------------ GET /api/copybooks/practice/history

    @Test
    void practiceHistoryFiltersByDateRangeIncludingBothBounds() throws Exception {
        savePractice("三月练习", "楷书", "楷书备注", LocalDateTime.of(2026, 3, 10, 10, 0));
        savePractice("四月中旬练习", "行书", "行书备注", LocalDateTime.of(2026, 4, 15, 10, 0));
        savePractice("五月下旬练习", "楷书", "另一份备注", LocalDateTime.of(2026, 5, 20, 10, 0));
        // 月末深夜：用来验证 to 是「含当天」而不是「当天 00:00:00」
        savePractice("月末深夜练习", "楷书", "月末备注", LocalDateTime.of(2026, 3, 31, 23, 30));

        MvcResult march = mockMvc.perform(get("/api/copybooks/practice/history")
                        .header("Authorization", "Bearer " + authorToken())
                        .param("from", "2026-03-01")
                        .param("to", "2026-03-31")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        List<String> marchIds = idsIn(body(march).get("content"));
        assertEquals(2, marchIds.size(), "三月的练习应当正好两条（含月末深夜那条），实际=" + marchIds);

        MvcResult all = mockMvc.perform(get("/api/copybooks/practice/history")
                        .header("Authorization", "Bearer " + authorToken())
                        .param("from", "2026-03-01")
                        .param("to", "2026-05-31")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(4, body(all).get("total").asInt(), "三月到五月的练习应当有 4 条");
    }

    @Test
    void practiceHistoryFiltersByCategory() throws Exception {
        savePractice("楷书练习一", "楷书", "备注一", LocalDateTime.of(2026, 6, 1, 9, 0));
        savePractice("行书练习一", "行书", "备注二", LocalDateTime.of(2026, 6, 2, 9, 0));

        MvcResult result = mockMvc.perform(get("/api/copybooks/practice/history")
                        .header("Authorization", "Bearer " + authorToken())
                        .param("category", "楷书")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(1, body(result).get("total").asInt(), "按分类过滤后应当只剩一条");
        assertEquals("楷书练习一", body(result).get("content").get(0).get("title").asText());
    }

    @Test
    void practiceHistoryFiltersByKeywordOnTitleContentOrNotes() throws Exception {
        savePractice("标题里的关键词", "楷书", "备注甲", LocalDateTime.of(2026, 7, 1, 9, 0));
        savePractice("普通标题", "楷书", "备注里的关键词", LocalDateTime.of(2026, 7, 2, 9, 0));

        MvcResult result = mockMvc.perform(get("/api/copybooks/practice/history")
                        .header("Authorization", "Bearer " + authorToken())
                        .param("keyword", "关键词")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(2, body(result).get("total").asInt(), "标题与备注命中关键词的都该被筛出来");
    }

    @Test
    void practiceHistoryRejectsMalformedDateWith400() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/copybooks/practice/history")
                        .header("Authorization", "Bearer " + authorToken())
                        .param("from", "2026/03/01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andReturn();
        assertNotNull(body(result).get("message"), "日期格式错误必须给可读的错误信息");
    }

    // ------------------------------------------------------------ 工具

    private String authorToken;

    private String authorToken() throws Exception {
        if (authorToken == null) {
            String suffix = authorUsername.substring(authorUsername.length() - 8);
            authorToken = loginFront(authorUsername, "Passw0rd" + suffix + "a");
        }
        return authorToken;
    }

    private Long saveWork(User author, String title, String description,
                           int viewCount, int commentCount) {
        return saveWork(author, title, description, viewCount, commentCount,
                Work.WorkStatus.PUBLISHED, Work.ReviewStatus.APPROVED);
    }

    private Long saveWork(User author, String title, String description,
                           int viewCount, int commentCount,
                           Work.WorkStatus status, Work.ReviewStatus reviewStatus) {
        Work work = Work.builder()
                .title(title)
                .description(description)
                .content("内容")
                .imageUrl("/api/files/ws-contract-test.png")
                .user(author)
                .userId(author.getId())
                .status(status)
                .reviewStatus(reviewStatus)
                .likeCount(0)
                .commentCount(commentCount)
                .viewCount(viewCount)
                .isFeatured(false)
                .build();
        Long id = workRepository.save(work).getId();
        createdWorkIds.add(id);
        return id;
    }

    private void savePractice(String title, String category, String notes, LocalDateTime practiceDate) {
        User author = userRepository.findById(authorId).orElseThrow();
        PracticeRecord record = PracticeRecord.builder()
                .user(author)
                .title(title)
                .content("练习内容")
                .category(category)
                .notes(notes)
                .practiceDate(practiceDate)
                .duration(600L)
                .characterCount(20)
                .isCompleted(true)
                .build();
        createdPracticeRecordIds.add(practiceRecordRepository.save(record).getId());
    }

    private String loginFront(String username, String password) throws Exception {
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

    private List<String> idsIn(JsonNode content) {
        List<String> ids = new ArrayList<>();
        if (content == null || !content.isArray()) {
            return ids;
        }
        for (JsonNode node : content) {
            JsonNode id = node.get("id");
            if (id != null) {
                ids.add(id.asText());
            }
        }
        return ids;
    }

    /** 只留本测试造的那几部作品，保持它们在结果里的相对顺序。 */
    private List<Long> myWorkIdsInOrder(JsonNode content) {
        List<Long> ids = new ArrayList<>();
        if (content == null || !content.isArray()) {
            return ids;
        }
        for (JsonNode node : content) {
            JsonNode id = node.get("id");
            if (id == null) {
                continue;
            }
            long value = Long.parseLong(id.asText());
            if (value == workByTitle || value == workByDescription || value == workPlain
                    || value == workEnglish || value == pendingWork
                    || value == pendingWorkSameAuthor || value == pendingWorkOtherAuthor) {
                ids.add(value);
            }
        }
        return ids;
    }
}
