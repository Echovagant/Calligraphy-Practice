package com.calligraphy.platform.model;

import com.calligraphy.platform.repository.CopybookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CopybookRepositoryTest {

    @Autowired
    private CopybookRepository copybookRepository;

    @Test
    void existingCopybooksAreReadable() {
        assertEquals(3, copybookRepository.count(),
                "copybooks 表里已有 3 行播种数据，实体必须能读到它们");
    }

    @Test
    void copybookKeepsSeededValues() {
        Copybook copybook = copybookRepository.findAll().stream()
                .filter(c -> "楷书基础练习".equals(c.getTitle()))
                .findFirst()
                .orElse(null);
        assertNotNull(copybook, "应能按标题找到播种的字帖");
        assertEquals("beginner", copybook.getDifficulty());
        assertEquals("楷书", copybook.getCategory());
        assertEquals(100, copybook.getCharacterCount());
    }
}
