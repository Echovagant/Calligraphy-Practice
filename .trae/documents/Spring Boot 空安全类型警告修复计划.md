## 修复计划

### 1. 统一 Null 注解策略
- 添加 jsr305 依赖，统一使用 javax.annotation.Nonnull
- 创建统一的注解配置类 `NullSafetyConfig.java`
- 替换所有不同的 Null 注解为统一的 @Nonnull

### 2. 修复依赖问题
- 添加 Caffeine 缓存依赖，解决 `com.github` 无法解析问题
- 创建 `TokenCacheConfig.java`，配置 `tokenValidationCache`
- 修复 `JwtServiceImpl.java` 中的错误，特别是 `requireSignature()` 方法和 `tokenValidationCache` 相关问题

### 3. 修复方法签名不一致
- 修复 `CommentServiceImpl` 中 `getCommentsByWorkId` 方法的返回类型
- 确保接口和实现类的注解一致

### 4. 替换过时的 API
- 将 `SignatureException` 替换为 `java.security.SignatureException` 或 `io.jsonwebtoken.security.SignatureException`
- 重构 `AuthController`，移除废弃的 `RefreshTokenRequest` 内部类，创建独立的 DTO

### 5. 更新构建配置
- 更新 Maven 编译器配置，添加注解处理器
- 配置编译器参数，启用 Null 检查

### 6. 运行测试验证
- 执行 `mvn clean compile` 验证编译
- 执行 `mvn test` 验证测试
- 执行 `mvn spotbugs:check` 进行静态分析

## 执行顺序
1. 备份当前代码
2. 更新依赖配置（pom.xml）
3. 创建统一的注解配置
4. 修复 JwtServiceImpl 中的错误
5. 修复 CommentServiceImpl 中的方法签名问题
6. 替换过时的 API
7. 更新构建配置
8. 运行测试验证

## 注意事项
- 确保同步更新 IDE 配置
- 分模块逐步修复，避免一次性全改
- 更新项目文档，说明新的 Null 安全策略
- 确保所有修复都通过测试验证