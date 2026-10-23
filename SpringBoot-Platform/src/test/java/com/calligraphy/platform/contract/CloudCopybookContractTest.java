package com.calligraphy.platform.contract;

import com.calligraphy.platform.model.Copybook;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.repository.CopybookRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 云端字帖的契约测试：{@code POST /api/copybooks} 与 {@code GET /api/copybooks/mine}。
 *
 * <p>背景：「字帖生成」页生成的自定义字帖以前只落在浏览器 localStorage 里，
 * 换设备就没了，界面却像存下来了。这两个端点把它存进账号。
 *
 * <p>两条硬约束：
 * <ul>
 *   <li>自建字帖**不能**混进公开字帖列表 / 热门榜 —— 那是某个人的草稿；</li>
 *   <li>{@code /api/copybooks/mine} 与 {@code /api/copybooks/{copybookId}} 同级，
 *       两者互不吞掉。</li>
 * </ul>
 *
 * <p>断言落到数据库状态（copybooks 表的行），不只看响应体。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class CloudCopybookContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CopybookRepository copybookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String ownerId;
    private String ownerToken;
    private String otherId;
    private String otherToken;

    private final List<String> createdCopybookIds = new ArrayList<>();
    private final List<String> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String ownerPassword = "Passw0rd" + suffix;
        ownerId = saveUser("cc_owner_" + suffix, ownerPassword);
        ownerToken = loginFront("cc_owner_" + suffix, ownerPassword);

        String otherPassword = "Passw0rd" + suffix + "x";
        otherId = saveUser("cc_other_" + suffix, otherPassword);
        otherToken = loginFront("cc_other_" + suffix, otherPassword);
    }

    @AfterEach
    void tearDown() {
        // 自建字帖先删（copybooks.user_id 上没有外键，但先删更干净）
        for (String id : createdCopybookIds) {
            copybookRepository.findById(id).ifPresent(c -> copybookRepository.deleteById(c.getId()));
        }
        createdCopybookIds.clear();
        for (String id : createdUserIds) {
            userRepository.findById(id).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
        createdUserIds.clear();
    }

    // ------------------------------------------------------------ POST /api/copybooks

    @Test
    void createCopybookStoresItForTheCurrentUser() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/copybooks")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createBody("我的永字帖", "永和九年"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("我的永字帖"))
                .andExpect(jsonPath("$.content").value("永和九年"))
                .andExpect(jsonPath("$.isCustom").value(true))
                .andExpect(jsonPath("$.font").value("kai"))
                .andExpect(jsonPath("$.layout").value("grid"))
                .andExpect(jsonPath("$.charactersPerPage").value(20))
                .andExpect(jsonPath("$.fontSize").value(48))
                .andExpect(jsonPath("$.paperColor").value("#fffdf7"))
                .andExpect(jsonPath("$.gridColor").value("#d9d2c5"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();

        String copybookId = body(result).get("id").asText();
        createdCopybookIds.add(copybookId);

        // 断言落到数据库：归属、标记、计数都要对
        Copybook saved = copybookRepository.findById(copybookId).orElseThrow();
        assertEquals(ownerId, saved.getUserId(), "自建字帖必须归属当前登录用户");
        assertEquals(Boolean.TRUE, saved.getIsCustom(), "自建字帖的 is_custom 必须是 true");
        assertEquals(Boolean.TRUE, saved.getIsActive(), "自建字帖默认启用");
        assertEquals(0, saved.getUsageCount(), "刚建好的字帖使用次数是 0");
        assertEquals("永和九年", saved.getContent(), "正文必须落库");
        assertEquals(4, saved.getCharacterCount(), "字数按去空白后的字符数算");
        assertFalse(saved.getImageUrl() == null || saved.getImageUrl().isBlank(),
                "image_url 是 NOT NULL，自建字帖也必须有封面图");
    }

    @Test
    void createCopybookRejectsAnonymous() throws Exception {
        mockMvc.perform(post("/api/copybooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createBody("匿名字帖", "内容"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCopybookRejectsBlankTitleOrContent() throws Exception {
        Map<String, Object> noTitle = new LinkedHashMap<>();
        noTitle.put("title", "   ");
        noTitle.put("content", "永和九年");
        mockMvc.perform(post("/api/copybooks")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(noTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());

        Map<String, Object> noContent = new LinkedHashMap<>();
        noContent.put("title", "有标题");
        noContent.put("content", "");
        mockMvc.perform(post("/api/copybooks")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(noContent)))
                .andExpect(status().isBadRequest());

        // 库里不能留下半截数据
        assertEquals(3, copybookRepository.count(),
                "失败的创建请求不该在 copybooks 表留下行（库里应当仍是 3 行播种字帖）");
    }

    @Test
    void createCopybookRejectsOversizedContent() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", "超长正文");
        body.put("content", "永".repeat(10001));
        mockMvc.perform(post("/api/copybooks")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------ GET /api/copybooks/mine

    @Test
    void myCopybooksReturnsOnlyTheCurrentUsersCopybooks() throws Exception {
        String mineId = createCopybookViaApi(ownerToken, "甲的字帖", "甲的内容");
        String otherCopybookId = createCopybookViaApi(otherToken, "乙的字帖", "乙的内容");
        createCopybookViaApi(ownerToken, "甲的第二份", "甲的第二份内容");

        MvcResult mine = mockMvc.perform(get("/api/copybooks/mine")
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.total").value(2))
                .andReturn();

        List<String> ids = idsIn(body(mine).get("content"));
        assertTrue(ids.contains(mineId), "自己的字帖应当在列表里，实际=" + ids);
        assertFalse(ids.contains(otherCopybookId), "别人的字帖不该出现在我的列表里，实际=" + ids);

        MvcResult others = mockMvc.perform(get("/api/copybooks/mine")
                        .header("Authorization", "Bearer " + otherToken)
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        List<String> otherIds = idsIn(body(others).get("content"));
        assertEquals(List.of(otherCopybookId), otherIds, "另一个用户只应看到自己那一份");
    }

    @Test
    void myCopybooksIsPaged() throws Exception {
        createCopybookViaApi(ownerToken, "第一份", "内容一");
        createCopybookViaApi(ownerToken, "第二份", "内容二");
        createCopybookViaApi(ownerToken, "第三份", "内容三");

        mockMvc.perform(get("/api/copybooks/mine")
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void myCopybooksRequiresToken() throws Exception {
        mockMvc.perform(get("/api/copybooks/mine"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * {@code /mine} 与 {@code /{copybookId}} 同级：字面量路径优先于模板路径，
     * 两者互不吞掉。契约里明确写了这条，要靠测试锁住。
     */
    @Test
    void minePathAndTemplatePathDoNotSwallowEachOther() throws Exception {
        // /mine 走字面量路径 → 分页信封
        MvcResult mine = mockMvc.perform(get("/api/copybooks/mine")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();
        assertNotNull(body(mine).get("content"), "/mine 必须返回分页信封");
        assertNotNull(body(mine).get("total"), "/mine 必须返回分页信封");

        // /{copybookId} 走模板路径 → 裸资源（没有分页信封字段）
        String featuredId = copybookRepository.findAll().stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsCustom()))
                .map(Copybook::getId)
                .findFirst()
                .orElseThrow();
        MvcResult detail = mockMvc.perform(get("/api/copybooks/" + featuredId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(featuredId))
                .andReturn();
        assertNotNull(body(detail).get("id"), "/{copybookId} 必须返回裸字帖");
        assertTrue(body(detail).get("total") == null, "/{copybookId} 不该返回分页信封");
    }

    // ------------------------------------------------------------ 自建字帖不进公开列表

    @Test
    void publicListDoesNotContainCustomCopybooks() throws Exception {
        String customId = createCopybookViaApi(ownerToken, "不该出现在公开列表的字帖", "内容");

        MvcResult result = mockMvc.perform(get("/api/copybooks").param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();

        List<String> ids = idsIn(body(result).get("content"));
        assertFalse(ids.contains(customId), "自建字帖不该混进公开字帖列表，实际=" + ids);
        assertTrue(ids.size() >= 3, "公开列表里应当还有 3 行播种字帖，实际=" + ids);
        for (JsonNode node : body(result).get("content")) {
            assertFalse(node.get("isCustom").asBoolean(),
                    "公开列表里出现了自建字帖: " + node.get("title").asText());
        }
    }

    @Test
    void popularCopybooksDoNotContainCustomCopybooks() throws Exception {
        String customId = createCopybookViaApi(ownerToken, "不该出现在热门榜的字帖", "内容");

        MvcResult result = mockMvc.perform(get("/api/copybooks/popular").param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();

        List<String> ids = idsIn(body(result).get("content"));
        assertFalse(ids.contains(customId), "自建字帖不该混进热门字帖榜，实际=" + ids);
        // 榜单不能是空的：3 行播种字帖必须在里面
        assertTrue(ids.size() >= 3, "热门榜里应当还有 3 行播种字帖，实际=" + ids);
    }

    @Test
    void featuredCopybooksAreMarkedAsNotCustom() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/copybooks").param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode content = body(result).get("content");
        assertTrue(content.size() > 0, "公开列表不该是空的");
        for (JsonNode node : content) {
            assertFalse(node.get("isCustom").asBoolean(),
                    "公开列表里的字帖都必须 isCustom=false: " + node.get("title").asText());
        }
    }

    // ------------------------------------------------------------ 工具

    private String saveUser(String username, String password) {
        String id = userRepository.save(User.builder()
                .username(username)
                .email(username + "@example.com")
                .password(passwordEncoder.encode(password))
                .nickname("云端字帖测试")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();
        createdUserIds.add(id);
        return id;
    }

    private String loginFront(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return body(login).get("accessToken").asText();
    }

    /** 走真实端点建一份自建字帖，返回它的 id。 */
    private String createCopybookViaApi(String token, String title, String content) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/copybooks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createBody(title, content))))
                .andExpect(status().isCreated())
                .andReturn();
        String id = body(result).get("id").asText();
        createdCopybookIds.add(id);
        return id;
    }

    private Map<String, Object> createBody(String title, String content) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        body.put("content", content);
        body.put("description", "云端字帖测试");
        body.put("font", "kai");
        body.put("layout", "grid");
        body.put("charactersPerPage", 20);
        body.put("fontSize", 48);
        body.put("paperColor", "#fffdf7");
        body.put("gridColor", "#d9d2c5");
        body.put("category", "楷书");
        body.put("difficulty", "beginner");
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
}
