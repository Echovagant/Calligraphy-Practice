package com.calligraphy.platform.performance;

import com.calligraphy.platform.model.Copybook;
import com.calligraphy.platform.model.PracticeRecord;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.repository.CopybookRepository;
import com.calligraphy.platform.repository.PracticeRecordRepository;
import com.calligraphy.platform.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P4：练习历史不该为了算连续天数而把该用户的**全部**练习记录拉进内存。
 *
 * <p>连续天数只需要「练过哪几天」，不需要「每次练习的时间戳」。
 * 以前是 {@code SELECT practice_date ... ORDER BY practice_date DESC} 全量拉回，
 * 同一天练 10 次就回 10 行；现在是 {@code SELECT DISTINCT DATE(practice_date)}，
 * 一天一行。
 *
 * <p>另外，练习历史列表里的每个元素只带 totalPractices / totalDuration /
 * averageDuration / streakDays 四个统计字段 —— 以前每次翻页还为
 * characterCount 与 copybookCount 这两个**用不上的**字段多跑一次聚合查询。
 *
 * <p>断言方式：对仓储打桩，验证历史接口走的是「按天去重的投影」，
 * 而不是那条全量查询；并且 {@code GET /api/copybooks/statistics/user}
 * 仍然返回**完整**统计。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class PracticeHistoryStatisticsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.hibernate.SessionFactory sessionFactory;

    @Autowired
    private CopybookRepository copybookRepository;

    /** 打桩用：验证服务层走的是哪条查询。 */
    @SpyBean
    private PracticeRecordRepository practiceRecordRepository;

    private String userId;
    private String username;
    private String password;
    private String token;

    private final List<Long> createdRecordIds = new ArrayList<>();
    private final List<String> copybookIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        password = "Passw0rd" + suffix;
        username = "ph_" + suffix;
        userId = userRepository.save(User.builder()
                .username(username)
                .email("ph_" + suffix + "@example.com")
                .password(passwordEncoder.encode(password))
                .nickname("练习历史测试")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();

        token = login(username, password);

        List<Copybook> copybooks = copybookRepository.findAll();
        assertFalse(copybooks.isEmpty(), "字帖表里应当有播种数据");
        copybookIds.add(copybooks.get(0).getId());
        copybookIds.add(copybooks.get(1 % copybooks.size()).getId());

        // 5 条记录落在 3 个不同的日子：今天 2 条、昨天 2 条、前天 1 条
        // （前天与昨天之间断一天，所以连续天数是 2）
        LocalDate today = LocalDate.now();
        record(today, 60, 10, copybookIds.get(0));
        record(today, 60, 10, copybookIds.get(0));
        record(today.minusDays(1), 120, 10, copybookIds.get(1));
        record(today.minusDays(1), 120, 10, copybookIds.get(1));
        record(today.minusDays(3), 60, 10, copybookIds.get(0));
    }

    @AfterEach
    void tearDown() {
        Mockito.clearInvocations(practiceRecordRepository);
        for (Long id : createdRecordIds) {
            practiceRecordRepository.deleteById(id);
        }
        createdRecordIds.clear();
        if (userId != null) {
            userRepository.findById(userId).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
    }

    /**
     * 练习历史走的是「按天去重的投影」，不是那条全量日期查询。
     */
    @Test
    void practiceHistoryUsesDistinctDayProjection() throws Exception {
        Mockito.clearInvocations(practiceRecordRepository);

        mockMvc.perform(get("/api/copybooks/practice/history")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0").param("size", "20"))
                .andExpect(status().isOk());

        verify(practiceRecordRepository, never()).findPracticeDatesByUserId(anyString());
        verify(practiceRecordRepository, atLeastOnce()).findDistinctPracticeDatesByUserId(anyString());
    }

    /**
     * 去重投影一天只回一行：5 条记录落在 3 天上 → 3 行。
     */
    @Test
    void distinctDayProjectionReturnsOneRowPerDay() {
        List<java.sql.Date> days = practiceRecordRepository.findDistinctPracticeDatesByUserId(userId);

        assertEquals(3, days.size(),
                "5 条练习记录落在 3 天上，去重投影应当只回 3 行，实际=" + days.size());
        assertTrue(days.contains(java.sql.Date.valueOf(LocalDate.now())), "应当包含今天");
        assertTrue(days.contains(java.sql.Date.valueOf(LocalDate.now().minusDays(1))), "应当包含昨天");
    }

    /**
     * 练习历史的作者信息一次 JOIN 取齐，不该每条记录再查一次 users。
     */
    @Test
    void practiceHistoryDoesNotFetchUserPerRecord() throws Exception {
        sessionFactory.getStatistics().clear();

        mockMvc.perform(get("/api/copybooks/practice/history")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0").param("size", "20"))
                .andExpect(status().isOk());

        long fetches = sessionFactory.getStatistics()
                .getEntityStatistics(User.class.getName())
                .getFetchCount();
        assertEquals(0L, fetches,
                "练习历史不该逐条记录再查一次 users（抓取了 " + fetches + " 次）");
    }

    /**
     * 连续天数的算法没变：今天与昨天连着 → 2 天。
     */
    @Test
    void practiceHistoryKeepsStreakDaysCorrect() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/copybooks/practice/history")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].streakDays").value(2))
                .andExpect(jsonPath("$.content[0].totalPractices").value(5))
                .andReturn();
        assertNotNull(body(result));
    }

    /**
     * 统计端点必须仍然返回**完整**统计 —— 优化不能把字段删了。
     */
    @Test
    void statisticsEndpointStillReturnsFullStatistics() throws Exception {
        mockMvc.perform(get("/api/copybooks/statistics/user")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPractices").value(5))
                .andExpect(jsonPath("$.totalDuration").value(420))
                .andExpect(jsonPath("$.averageDuration").value(84.0))
                .andExpect(jsonPath("$.streakDays").value(2))
                .andExpect(jsonPath("$.characterCount").value(50))
                .andExpect(jsonPath("$.copybookCount").value(2));
    }

    // ------------------------------------------------------------ 工具

    private void record(LocalDate day, long duration, int characters, String copybookId) {
        PracticeRecord record = practiceRecordRepository.save(PracticeRecord.builder()
                .user(userRepository.findById(userId).orElseThrow())
                .userId(userId)
                .copybookId(copybookId)
                .copybookTitle("测试字帖")
                .title("练习记录")
                .content("内容")
                .category("楷书")
                .practiceType("临摹")
                .duration(duration)
                .characterCount(characters)
                .practiceDate(LocalDateTime.of(day, LocalTime.NOON))
                .isCompleted(true)
                .build());
        createdRecordIds.add(record.getId());
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
