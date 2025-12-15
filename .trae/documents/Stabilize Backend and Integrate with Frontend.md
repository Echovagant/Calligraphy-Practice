## Priority 1: Fix Syntax and Type Errors
- Align `AdminService` and `AdminServiceImpl`
  - Update interface to include `existsByUsername(String)`, `existsByEmail(String)`, and return `AdminRegisterResponseDto` from `registerAdmin` (src/main/java/com/calligraphy/platform/service/AdminService.java:12).
  - Change `AdminServiceImpl.logoutAdmin` to use `jwtService.revokeToken(token)` (src/main/java/com/calligraphy/platform/service/impl/AdminServiceImpl.java:200).
  - Extend `AdminRegisterResponseDto` to include `accessToken`, `refreshToken`, `tokenType`, `expiresIn`, and optional `permissions` so builder calls compile (src/main/java/com/calligraphy/platform/dto/AdminRegisterResponseDto.java:10).
- Fix `AuthController` to use DTOs
  - Login: call `authService.login(LoginRequestDto)` and return `LoginResponseDto` (src/main/java/com/calligraphy/platform/controller/AuthController.java:35–106).
  - Register: accept `RegisterRequestDto` and return `RegisterResponseDto` (src/main/java/com/calligraphy/platform/controller/AuthController.java:109–113).
  - Refresh: return `LoginResponseDto` instead of `Map<String,String>` (src/main/java/com/calligraphy/platform/controller/AuthController.java:116–120).
- Correct `WebSecurityConfig`/`JwtAuthenticationFilter` wiring
  - Remove manual `new JwtAuthenticationFilter()` and setter calls; inject the component bean (src/main/java/com/calligraphy/platform/security/WebSecurityConfig.java:69–74).
  - Use a single `JwtTokenProvider` class (prefer `com.calligraphy.platform.security.JwtTokenProvider`) consistently.
- Resolve `EmailService` duplicate signatures
  - Remove duplicate methods in interface: `isEmailOpened(String)`, `isEmailClicked(String)`, `getEmailStatus(String)`; keep one set with consistent return types (src/main/java/com/calligraphy/platform/service/EmailService.java:116–181).
  - If required, add lightweight stub implementations in `EmailServiceImpl` for stats/config methods to compile.
- Repair `AuthServiceImpl` type mismatches
  - Replace `.orElseThrow()` usages with null checks per `UserService` contracts (src/main/java/com/calligraphy/platform/service/impl/AuthServiceImpl.java:69–71, 146–151).
  - Import `FrontendUserUpdateDto` and remove use of missing builder fields (`registrationIp`); use `lastLoginIp`/existing fields (src/main/java/com/calligraphy/platform/service/impl/AuthServiceImpl.java:328–338, 230–244).
- Standardize `CommentServiceImpl`
  - Convert `userId` and `workId` to `String`/`Long` as required by repositories; e.g., `UserService.findById(String)` and `WorkService.getWorkEntityById(Long)` (src/main/java/com/calligraphy/platform/service/impl/CommentServiceImpl.java:42–44).
  - Use `FrontendUserService` to load `FrontendUser` for `Comment.user(...)` (src/main/java/com/calligraphy/platform/service/impl/CommentServiceImpl.java:33–48).
  - Add missing `WorkService.updateCommentCount(Long,int)` to the interface (src/main/java/com/calligraphy/platform/service/WorkService.java:46–66) matching `WorkServiceImpl` (src/main/java/com/calligraphy/platform/service/impl/WorkServiceImpl.java:270–278).
- Fix `CommentMapper` entity mismatch
  - Map from `com.calligraphy.platform.model.Comment` instead of `com.calligraphy.platform.entity.Comment` (src/main/java/com/calligraphy/platform/mapper/CommentMapper.java:6, 23).
- Complete `CommentRepository` signatures
  - Add pageable variants and missing counters: `findByStatus(String, Pageable)`, `countByUserIdAndStatusNot(String,String)`, `countByParentIdAndStatusNot(Long,String)` to match service usage (src/main/java/com/calligraphy/platform/repository/CommentRepository.java:92–101).

## Priority 2: Refactor Deprecated/Outdated Code
- Consolidate security configuration
  - Prefer `config/SecurityConfig` (bean-based SecurityFilterChain for Spring Security 6) and remove or disable legacy `security/WebSecurityConfig` to avoid conflicts (src/main/java/com/calligraphy/platform/security/WebSecurityConfig.java:28–181).
  - Ensure JWT filter is registered once and sessions are stateless for APIs.
- Unify JWT service API
  - Implement admin helper methods in `JwtServiceImpl` (`generateAdminToken(AdminUser)`, `generateAdminRefreshToken(AdminUser)`, `validateAdminToken`, `getUsernameFromAdminToken`, `revokeAdminToken` delegating to existing methods) so `AdminServiceImpl` compiles cleanly (src/main/java/com/calligraphy/platform/service/impl/JwtServiceImpl.java:190–205).
  - Prune `JwtService` interface to actual needed set; keep `getTokenRemainingTime` and `getTokenExpiryTime` which are already implemented.
- RESTful controllers
  - Normalize request/response DTOs across controllers; remove nested deprecated inner DTOs in `AuthController` (src/main/java/com/calligraphy/platform/controller/AuthController.java:166–219).

## Priority 3: Complete Functional Logic
- Frontend auth endpoints
  - `FrontendAuthController` `/api/frontend/auth/login|register|refresh|logout` return `accessToken`, `refreshToken`, and `user` payload aligning with frontend expectations (src/main/java/com/calligraphy/platform/controller/FrontendAuthController.java:40–133).
  - Mirror endpoints in `BackendAuthController` for admin users (src/main/java/com/calligraphy/platform/controller/BackendAuthController.java).
- Comment workflows
  - Ensure create/update/delete flow updates `Work.commentCount` via `WorkService.updateCommentCount` and uses consistent status enums (`APPROVED`, `DELETED`).
  - Implement search stubs minimally or mark TODO, keeping compile success.
- Repository support
  - Add/adjust JPA query method signatures to match model IDs (`Work.id` as `Long`, `Comment.userId` as `String`).

## Priority 4: Frontend–Backend Integration
- Response shapes
  - Confirm all auth endpoints return `{ accessToken, refreshToken, user }` where used by frontend (`Practice-Platform/src/utils/api.js:321–433`).
  - Works/comments endpoints: ensure `/works`, `/community/comments/...` match the frontend routes and accept IDs with correct types (`Work.id` Long, `User.id` String).
- CORS configuration
  - Add `http://localhost:5173` and proxy origin `http://localhost:8080` to allowed origins (src/main/java/com/calligraphy/platform/config/SecurityConfig.java:91–101).
- Swagger/OpenAPI
  - With `springdoc-openapi` present in `pom.xml`, annotate controllers for tags and descriptions; verify `/swagger-ui/index.html` exposes the API for frontend developers.
- Verification plan
  - Build and run backend; fix residual compile errors iteratively.
  - Exercise endpoints with sample payloads that mirror `src/utils/api.js` expectations.
  - Validate JWT flows: login → attach `Authorization: Bearer` → refresh via body `{ refreshToken }` → logout invalidates.

## Deliverables
- Compiling backend (Java 17, Spring Boot 3.3.4).
- Unified security and JWT service.
- Controllers and services aligned with DTOs and repository types.
- Swagger UI covering auth, users, works, comments.
- CORS working with Vite dev server at `http://localhost:5173` and proxy at `http://localhost:8080`.

If you approve, I will implement these edits file-by-file, run a build to verify, and adjust any remaining discrepancies until the backend compiles cleanly and integrates with the frontend.