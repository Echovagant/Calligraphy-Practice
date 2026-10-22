package com.calligraphy.platform.performance;

import com.calligraphy.platform.model.Copybook;
import com.calligraphy.platform.repository.CopybookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P3：首页概览只该取前 5 个热门字帖，不该把全部启用字帖加载进内存。
 *
 * <p>{@code GET /api/stats/overview} 是公开端点，首页每次打开都调它。
 * 以前是把全部启用字帖查出来在内存里 {@code limit(5)}。
 *
 * <p>断言方式：Hibernate 统计的**实体加载数**（{@code getLoadCount}）。
 * 本测试造 8 个启用字帖，修复前首页概览会把它们全部加载（≥ 8），
 * 修复后只加载 5 个。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class StatsOverviewLimitTest {

    /** 造的启用字帖数量，必须多于首页概览要返回的 5 条。 */
    private static final int CREATED_COPYBOOKS = 8;

    /** 首页概览返回的热门字帖条数。 */
    private static final int POPULAR_LIMIT = 5;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CopybookRepository copybookRepository;

    @Autowired
    private SessionFactory sessionFactory;

    private final List<String> createdCopybookIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        for (int i = 0; i < CREATED_COPYBOOKS; i++) {
            Copybook copybook = copybookRepository.save(Copybook.builder()
                    .title("概览限流测试字帖 " + i)
                    .description("描述")
                    .imageUrl("/sample/copybook-1.png")
                    .difficulty("beginner")
                    .category("楷书")
                    .characterCount(10)
                    .usageCount(100 + i)
                    .isActive(true)
                    .build());
            createdCopybookIds.add(copybook.getId());
        }
    }

    @AfterEach
    void tearDown() {
        for (String id : createdCopybookIds) {
            copybookRepository.deleteById(id);
        }
        createdCopybookIds.clear();
    }

    @Test
    void overviewReturnsFivePopularCopybooks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/stats/overview"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode popular = body(result).get("popularCopybooks");
        assertTrue(popular.isArray(), "popularCopybooks 必须是数组");
        assertEquals(POPULAR_LIMIT, popular.size(),
                "首页概览必须只返回 " + POPULAR_LIMIT + " 个热门字帖");
    }

    /**
     * 只加载前 5 条，不是把全部启用字帖加载进来再截断。
     */
    @Test
    void overviewLoadsOnlyTheTopFiveCopybooks() throws Exception {
        sessionFactory.getStatistics().clear();

        mockMvc.perform(get("/api/stats/overview"))
                .andExpect(status().isOk());

        long loads = sessionFactory.getStatistics()
                .getEntityStatistics(Copybook.class.getName())
                .getLoadCount();

        assertTrue(loads <= POPULAR_LIMIT,
                "首页概览只该加载 " + POPULAR_LIMIT + " 个字帖，实际加载了 " + loads + " 个");
    }

    private JsonNode body(MvcResult result) throws Exception {
        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 0, "响应体不能为空");
        return objectMapper.readTree(content);
    }
}
