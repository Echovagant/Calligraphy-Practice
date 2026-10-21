package com.calligraphy.platform.service;

import com.calligraphy.platform.entity.AdminUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JwtServiceImplTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generatedTokenCarriesTheIdentityClaims() {
        String token = jwtService.generateToken("user-1", "USER", "alice");

        assertEquals("user-1", jwtService.extractUserId(token));
        assertEquals("USER", jwtService.extractUserType(token));
        assertEquals("alice", jwtService.extractUsername(token));
        assertTrue(jwtService.validateToken(token), "新生成的令牌必须通过校验");
    }

    @Test
    void revokedTokenFailsValidation() {
        String token = jwtService.generateToken("user-2", "USER", "bob");
        assertTrue(jwtService.validateToken(token), "吊销前的令牌必须通过校验");

        jwtService.invalidateToken(token);

        // 断言黑名单本身，这样测试不会因为别的原因（比如令牌根本解不开）而假绿
        assertTrue(jwtService.isBlacklisted(token), "令牌必须被写入 Redis 黑名单");
        assertFalse(jwtService.validateToken(token), "已吊销的令牌必须校验失败");
    }

    @Test
    void adminTokenRoundTrips() {
        AdminUser admin = AdminUser.builder()
                .username("root")
                .roles("ROLE_SUPER_ADMIN")
                .build();

        String token = jwtService.generateAdminToken(admin);

        assertTrue(jwtService.validateAdminToken(token), "管理员令牌必须通过校验");
        assertEquals("root", jwtService.getUsernameFromAdminToken(token));
        assertTrue(jwtService.getAdminJwtExpiration() > 0, "管理员令牌必须有有效期");
    }
}
