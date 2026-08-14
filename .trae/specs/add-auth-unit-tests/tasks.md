# Tasks
- [x] Task 1: 添加测试依赖到 blog-backend/pom.xml
  - [x] SubTask 1.1: 新增 `org.springframework.boot:spring-boot-starter-test`（scope=test，由 parent 3.1.5 管理版本）
- [x] Task 2: 编写 LoginAttemptServiceTest
  - [x] SubTask 2.1: 搭建 Mockito 环境（@ExtendWith(MockitoExtension.class)，Mock StringRedisTemplate/ValueOperations/DefaultRedisScript/AlertNotifier，真实 SecurityProperties 设定阈值参数）
  - [x] SubTask 2.2: tryAcquireIp 4 个场景（允许/限流+告警/null 返回/空 IP 回退 unknown）
  - [x] SubTask 2.3: isLocked 3 个场景 + getRemainingLockMillis 3 个场景
  - [x] SubTask 2.4: onLoginFailure 4 个场景（未达阈值/首次失败 TTL/达到阈值锁定+告警/increment 返回 null）+ onLoginSuccess 清理场景
- [x] Task 3: 编写 JwtUtilsTest
  - [x] SubTask 3.1: 搭建环境（@ExtendWith(MockitoExtension.class)，Mock RedisUtils/StringRedisTemplate/SetOperations，真实 SecurityProperties，ReflectionTestUtils 注入 ≥64 字节 HS512 密钥）
  - [x] SubTask 3.2: AccessToken/RefreshToken 生成、类型区分、subject/有效期解析往返
  - [x] SubTask 3.3: validateToken（合法/篡改/乱码/黑名单）
  - [x] SubTask 3.4: isValidRefreshToken（成员/非成员/AccessToken/无效）
  - [x] SubTask 3.5: revokeAllRefreshTokens / revokeRefreshToken / addToBlacklist（剩余>0 与已过期）/ isBlacklisted
- [x] Task 4: 编写 AuthControllerTest
  - [x] SubTask 4.1: 搭建环境（Mock 5 个协作者 + MockHttpServletRequest/Response）
  - [x] SubTask 4.2: login 8 个场景（空用户名/锁定/限流/成功/BadCredentials 401/BadCredentials 锁定 423/其他异常 401/X-Forwarded-For IP 提取）
  - [x] SubTask 4.3: refresh 4 个场景（Token 缺失/无效清 Cookie/Header 兜底/有效轮换）
  - [x] SubTask 4.4: logout 2 个场景（带 Bearer/不带）
- [x] Task 5: 运行并验证全部测试通过
  - [x] SubTask 5.1: 在 blog-backend 执行 `mvn test -Dtest="LoginAttemptServiceTest,JwtUtilsTest,AuthControllerTest"`，确认全部通过（46 个测试，0 失败 0 错误，BUILD SUCCESS）

# Task Dependencies
- Task 2/3/4 依赖 Task 1（需要测试依赖才能编译）
- Task 2/3/4 相互独立，可并行
- Task 5 依赖 Task 2/3/4
