# 书法练习平台后端 (Spring Boot)

这是书法练习平台的Spring Boot后端实现，包含用户管理和认证功能。

## 项目结构
SpringBoot-Platform/
├── src/
│   ├── main/
│   │   ├── java/com/calligraphy/platform/
│   │   │   ├── config/           # 配置类
│   │   │   ├── controller/       # 控制器
│   │   │   ├── model/            # 数据模型
│   │   │   ├── repository/       # 数据访问层
│   │   │   ├── security/         # 安全相关
│   │   │   ├── service/          # 业务逻辑层
│   │   │   │   └── impl/         # 服务实现
│   │   │   ├── util/             # 工具类
│   │   │   └── CalligraphyPlatformApplication.java # 应用入口
│   │   └── resources/
│   │       └── application.properties # 应用配置
│   └── test/                     # 测试代码
├── pom.xml                       # Maven配置
└── README.md                     # 项目说明

## 技术栈

- Spring Boot 3.2.4
- Spring Security
- Spring Data JPA
- MySQL
- JWT (JSON Web Token)
- Lombok

## 功能特性

### 用户管理
- 用户注册
- 用户登录
- 获取用户信息
- 更新用户信息
- 删除用户
- 检查用户名/邮箱是否存在

### 认证与授权
- JWT令牌认证
- 角色基础的访问控制 (USER, ADMIN, SUPER_ADMIN)
- 令牌刷新
- 用户登出

## 快速开始

### 前提条件

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本

### 配置数据库

1. 创建MySQL数据库：

```sql
CREATE DATABASE calligraphy_platform;
CREATE USER 'calligraphy_user'@'localhost' IDENTIFIED BY 'calligraphy_password';
GRANT ALL PRIVILEGES ON calligraphy_platform.* TO 'calligraphy_user'@'localhost';
FLUSH PRIVILEGES;
```

1. 修改 `application.properties` 中的数据库连接信息（如果需要）

### 运行项目

```bash
# 编译并运行
cd SpringBoot-Platform
mvn spring-boot:run

# 或构建JAR包后运行
cd SpringBoot-Platform
mvn clean package
java -jar target/SpringBoot-Platform-1.0.0.jar
```

### 默认用户

应用启动时会自动创建以下默认用户：

- 管理员: `admin` / `admin123`
- 超级管理员: `superadmin` / `superadmin123`

## API端点

### 认证相关

- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册
- `POST /auth/refresh` - 刷新令牌
- `POST /auth/logout` - 用户登出

### 用户相关

- `GET /users` - 获取所有用户（需要管理员权限）
- `GET /users/me` - 获取当前用户信息
- `GET /users/{id}` - 根据ID获取用户
- `PUT /users/{id}` - 更新用户信息
- `DELETE /users/{id}` - 删除用户（需要管理员权限）
- `GET /users/check-username/{username}` - 检查用户名是否存在
- `GET /users/check-email/{email}` - 检查邮箱是否存在

## 安全配置

- 除了 `/auth/**` 和 `/public/**` 路径外，所有请求都需要认证
- 使用JWT令牌进行认证，令牌需要在请求头中以 `Bearer {token}` 形式提供
- 不同的API端点需要不同的角色权限

## 开发说明

### 添加新功能

1. 在 `model` 包中添加新的数据模型
2. 在 `repository` 包中添加相应的Repository接口
3. 在 `service` 包中定义服务接口，并在 `service/impl` 包中实现
4. 在 `controller` 包中添加相应的控制器

### 测试

使用JUnit和Spring Boot Test进行单元测试和集成测试。测试代码应该放在 `src/test/java` 目录下。

## 注意事项

1. 在生产环境中，请修改默认的管理员密码
2. 确保更新 `application.properties` 中的JWT密钥为安全的随机字符串
3. 考虑为生产环境配置HTTPS
4. 实现更完善的错误处理和日志记录
