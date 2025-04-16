# UserCenter 用户中心系统

## 最新更新

### 前端优化
1. 认证机制优化
   - 实现了完整的 Token 自动刷新机制
   - 添加请求队列管理，避免多个请求同时刷新 Token
   - 优化了认证状态管理和用户会话处理

2. 页面性能优化
   - 改进了加载状态的显示
   - 优化了页面切换时的用户体验
   - 添加了友好的错误提示机制

3. 技术栈升级
   - 升级到 Next.js App Router
   - 使用 'use client' 指令优化客户端组件
   - 采用 CSS Modules 进行样式管理

4. 用户体验改进
   - 优化了表单验证和提示
   - 改进了页面加载状态的展示
   - 增强了错误处理和用户反馈

5. 用户自助账号注销功能
   - 新增账号自助注销页面，用户可主动删除自己的账号
   - 提供双重确认机制，防止误操作
   - 账号注销后自动清除登录状态和相关缓存

### 后端优化
1. 认证系统增强
   - 完善了 Token 刷新机制
   - 优化了用户会话管理
   - 增强了安全性检查

2. 性能优化
   - 改进了请求处理流程
   - 优化了数据库查询
   - 增加了缓存机制

3. 用户账号管理增强
   - 支持用户自助注销账号功能
   - 完善用户状态和封禁状态管理
   - 优化封禁状态显示和处理逻辑

4. 新增示例代码
   - 提供基础Web示例代码供参考学习
   - 包含基本HTTP请求处理
   - 路径变量处理示例

## 项目介绍

UserCenter是一个功能完善的用户管理系统，提供用户注册、登录、信息管理等核心功能。本项目采用前后端分离架构，包含完整的前端界面和后端API，可用作独立的用户中心系统，也可集成到其他业务系统中。

## 功能特性

- **用户账号管理**：
  - 注册、登录、退出登录
  - **账号自助注销**：用户可自行删除账号
- **个人信息管理**：查看和修改个人资料、上传头像
- **账号安全管理**：用户密码修改
- **用户权限控制**：普通用户/管理员角色区分
- **用户状态系统**：
  - **基本状态**：正常(0)、已停用(1)
  - **封禁状态**：未封禁(0)、已封禁(1)
  - **封禁类型**：永久封禁、临时封禁(指定天数)
  - **状态显示**：直观的状态标签显示系统
- **管理员功能**：
  - 用户列表查看
  - 用户信息编辑（包括头像上传）
  - 用户删除
  - 用户封禁/解封管理
- **封禁系统**：
  - 支持临时封禁（指定天数）
  - 支持永久封禁（设置封禁天数为0）
  - 封禁时自动记录封禁原因
  - 临时封禁自动解除（到期后自动解封）
- **逻辑删除**：支持账号注销后重新注册
- **界面定制**：支持自定义网站图标
- **分布式锁**：支持Redis分布式锁和本地锁两种模式，保护关键操作
- **消息队列**：支持RabbitMQ消息队列处理用户事件
- **API限流系统**：
  - 三级限流策略（全局/接口/用户级）
  - 支持预热模式和突发流量处理
  - 分布式限流（Redis+Lua脚本）
  - 自动降级和监控指标收集
- **监控系统**：
  - Spring Boot Actuator 端点监控
  - 自定义健康检查（数据库、Redis、短信服务） 
  - Micrometer + Prometheus 指标收集
  - 安全防护的监控端点访问

## 系统架构

项目采用前后端分离的架构：

- **前端**：基于Next.js构建的React应用，使用Ant Design组件库
- **后端**：基于Spring Boot的Java应用，提供RESTful API
- **数据库**：MySQL数据库存储用户数据
- **缓存&锁**：可选配置Redis用于分布式锁
- **消息队列**：可选配置RabbitMQ用于异步消息处理
- **限流**：基于Guava RateLimiter和Redis的API限流系统

## 技术栈

### 前端
- **框架**：Next.js 14 (React框架)
- **UI组件库**：Ant Design 5.x
- **状态管理**：React Context API
- **路由**：Next.js App Router
- **HTTP客户端**：Axios
- **图片处理**：Base64编码传输

### 后端
- **框架**：Spring Boot 2.x
- **ORM**：MyBatis
- **API文档**：Knife4j
- **权限控制**：Session认证
- **安全框架**：Spring Security
- **数据库**：MySQL 8.x
- **分布式锁**：Redisson/本地JVM锁
- **消息队列**：RabbitMQ
- **AOP**：Spring AOP实现功能增强
- **限流系统**：Guava RateLimiter + Redis
- **监控**：Spring Boot Actuator + Micrometer + Prometheus

## 快速开始

### 环境要求
- Node.js 16+
- JDK 1.8+
- MySQL 5.7+
- Redis 6.0+

### 安装依赖
前端：
```bash
cd usercenter-fronted
npm install
```

后端：
```bash
cd usercenter-backend
mvn install
```

### 启动服务
前端开发服务器：
```bash
cd usercenter-fronted
npm run dev         # 标准模式启动
# 或使用优化的启动方式
npm run dev:fast    # Turbo模式启动（更快的编译速度）
npm run dev:win     # Windows环境优化的Turbo模式
```

后端服务器：
```bash
cd usercenter-backend
mvn spring-boot:run
```

### 配置说明
1. 前端配置（.env.development）：
```env
# 启用 Turbo 模式
NEXT_TURBO=1

# 调试信息（默认注释状态）
# NEXT_TELEMETRY_DEBUG=1
# TURBOPACK_DEV_SERVER_CHUNK=1
```

2. 后端配置（application.yml）：
```yaml
server:
  port: 8083                 # 服务端口号
  servlet:
    session:
      timeout: 86400         # Session超时时间（秒）

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/usercenter_db?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true
    username: root           # 数据库用户名
    password: 123456         # 数据库密码
  
  # Redis配置
  redis:
    enabled: true            # 是否启用Redis功能
    host: localhost
    port: 6379
    database: 0
    timeout: 10000
    lettuce:
      pool:
        max-active: 20       # 连接池最大连接数
        max-idle: 8          # 连接池最大空闲连接数
        min-idle: 2          # 连接池最小空闲连接数
        max-wait: 3000ms     # 连接池最大等待时间
    # Redis键事件通知配置，用于监听键过期
    notify-keyspace-events: Ex
  
  # RabbitMQ配置
  rabbitmq:
    enabled: true            # 是否启用RabbitMQ功能
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    # 消息确认配置
    publisher-confirm-type: correlated
    publisher-returns: true
    template:
      mandatory: true
    # 消费者配置
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 1
        concurrency: 3        # 最小消费者数
        max-concurrency: 10   # 最大消费者数

# Redisson配置（仅在 redis.enabled=true 时生效）
redisson:
  address: redis://localhost:6379
  database: 0
  pool:
    max-active: 8
    max-idle: 8
    min-idle: 2
  timeout: 3000

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath:mappers/*.xml
  type-aliases-package: com.weiki.usercenterbackend.model.domain
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      # 全局逻辑删除的字段名
      logic-delete-field: isDelete
      # 逻辑已删除值(1)
      logic-delete-value: 1
      # 逻辑未删除值(0)
      logic-not-delete-value: 0

# 管理端点配置
management:
  endpoints:
    web:
      exposure:
        include: "*"                # 暴露所有端点
        exclude: "shutdown"         # 禁用敏感端点
      base-path: /actuator          # 端点基础路径
  endpoint:
    health:
      show-details: always          # 显示详细健康信息
      probes:
        enabled: true               # 启用Kubernetes探针
    prometheus:
      enabled: true                 # 启用Prometheus端点

# 限流系统配置
rate:
  limit:
    global:
      qps: 10.0               # 全局默认QPS限制，所有API共享此限流器
      warmup: 0               # 全局默认预热时间（秒），0表示无预热期
      timeout: 0              # 全局默认超时时间（毫秒），0表示非阻塞模式
    user:
      qps: 5.0                # 用户级别默认QPS限制，每个用户单独计数
    distributed:
      enabled: true           # 是否启用分布式限流（基于Redis实现）

# 缓存配置
cache:
  user:
    expire: 3600              # 用户缓存过期时间（秒）
    prefix: "user:"
    enable: true              # 是否启用用户缓存
  auth:
    token-expire: 1800        # 访问令牌过期时间（秒）
    refresh-token-expire: 604800  # 刷新令牌过期时间（秒，7天）
```

#### 配置说明详解

1. **服务器配置**
   - 默认端口: 8083
   - Session超时: 24小时（86400秒）
   
2. **数据库配置**
   - 使用MySQL数据库，支持MySQL 5.7+
   - 默认数据库名: usercenter_db
   - 字符集: UTF-8
   - 时区: UTC
   
3. **Redis配置**
   - 可通过`spring.redis.enabled`控制是否启用Redis功能
   - 如设置为false，系统将使用本地缓存和锁，适合开发环境
   - 生产环境建议启用Redis以支持分布式功能
   
4. **RabbitMQ配置**
   - 可通过`spring.rabbitmq.enabled`控制是否启用消息队列功能
   - 支持消息确认机制和手动ACK模式，确保消息可靠性
   - 配置了消费者并发数，可根据实际负载调整
   
5. **限流配置**
   - 支持全局限流和用户级别限流
   - 可配置预热期、超时时间和突发流量处理
   - 支持分布式限流，依赖Redis实现
   
6. **缓存配置**
   - 用户信息缓存: 默认1小时过期
   - 认证令牌: 访问令牌30分钟，刷新令牌7天
   - 所有缓存均支持自定义过期时间

#### 环境特定配置

在生产环境中，建议调整以下配置:

1. **数据库配置**
   - 使用更安全的数据库凭证
   - 配置数据库连接池参数以优化性能
   
2. **Redis配置**
   - 启用密码认证
   - 配置适当的连接池大小
   - 考虑使用Redis集群提高可用性
   
3. **日志级别**
   - 将日志级别从`debug`调整为`info`或`warn`
   
4. **安全配置**
   - 禁用Knife4j API文档或启用生产模式
   - 限制Actuator端点访问

#### 前端环境变量配置

前端支持以下环境配置文件:

1. `.env.development` - 开发环境配置
2. `.env.production` - 生产环境配置
3. `.env.local` - 本地覆盖配置（不提交到版本控制）

常用环境变量:

- `NEXT_TURBO` - 控制是否启用Turbo模式，提升开发体验
- `NEXT_PUBLIC_API_BASE_URL` - API基础URL，可配置为后端服务地址

如果需要添加自定义环境变量，请确保:
- 客户端使用的变量需以`NEXT_PUBLIC_`开头
- 服务端专用变量无需特殊前缀
- 敏感信息应仅配置在服务端变量中

## 开发指南

### 目录结构
```
UserCenter/
├── usercenter-backend/                   # 后端代码
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/weiki/usercenterbackend/
│   │   │   │   ├── annotation/           # 自定义注解
│   │   │   │   │   ├── AuthCheck.java          # 权限校验注解
│   │   │   │   │   ├── DistributedLock.java    # 分布式锁注解
│   │   │   │   │   └── RateLimit.java          # 限流注解
│   │   │   │   │
│   │   │   │   ├── aop/                  # 面向切面编程
│   │   │   │   │   ├── AuthInterceptor.java       # 权限校验拦截器
│   │   │   │   │   ├── DistributedLockAspect.java # 分布式锁拦截器
│   │   │   │   │   └── RateLimiterAspect.java     # 限流拦截器
│   │   │   │   │
│   │   │   │   ├── common/               # 公共组件
│   │   │   │   │   ├── BaseResponse.java         # 旧版响应类（已废弃）
│   │   │   │   │   └── Fallback.java             # 限流降级接口
│   │   │   │   │
│   │   │   │   ├── config/               # 配置类
│   │   │   │   │   ├── RedissonConfig.java       # Redis分布式锁配置
│   │   │   │   │   ├── RedisConfig.java          # Redis配置类
│   │   │   │   │   ├── RedisCacheConfig.java     # Redis缓存配置
│   │   │   │   │   ├── RateLimitConfig.java      # 限流配置类
│   │   │   │   │   ├── MetricsConfig.java        # 监控指标配置
│   │   │   │   │   └── WebSecurityConfig.java    # 安全配置
│   │   │   │   │
│   │   │   │   ├── constant/             # 常量定义
│   │   │   │   │   ├── JwtConstant.java          # JWT相关常量
│   │   │   │   │   └── RedisConstant.java        # Redis缓存相关常量
│   │   │   │   │
│   │   │   │   ├── controller/           # 控制器
│   │   │   │   │   ├── AuthController.java       # 认证控制器（JWT）
│   │   │   │   │   ├── UserController.java       # 用户相关控制器
│   │   │   │   │   ├── OrderController.java      # 订单控制器（含监控指标示例）
│   │   │   │   │   └── ExampleRateLimitController.java # 限流示例控制器
│   │   │   │   │
│   │   │   │   ├── demos/                # 示例代码
│   │   │   │   │   └── web/                      # Web示例
│   │   │   │   │       ├── BasicController.java  # 基础控制器示例
│   │   │   │   │       ├── PathVariableController.java # 路径变量示例
│   │   │   │   │       └── User.java             # 用户示例类
│   │   │   │   │
│   │   │   │   ├── exception/            # 异常处理
│   │   │   │   │   ├── GlobalExceptionHandler.java     # 全局异常处理器
│   │   │   │   │   ├── JwtAuthenticationException.java # JWT认证异常
│   │   │   │   │   ├── RateLimitException.java         # 限流异常
│   │   │   │   │   └── RateLimitExceptionHandler.java  # 限流异常处理器
│   │   │   │   │
│   │   │   │   ├── filter/               # 过滤器
│   │   │   │   │   └── JwtAuthenticationFilter.java    # JWT认证过滤器
│   │   │   │   │
│   │   │   │   ├── health/               # 健康检查
│   │   │   │   │   └── SmsServiceHealthIndicator.java  # 短信服务健康检查器
│   │   │   │   │
│   │   │   │   ├── mapper/               # 数据访问层
│   │   │   │   │   └── UserMapper.java           # 用户数据访问接口
│   │   │   │   │
│   │   │   │   ├── metrics/              # 监控指标
│   │   │   │   │   ├── CacheMetrics.java         # 缓存监控指标
│   │   │   │   │   └── RateLimitMetrics.java     # 限流器监控指标
│   │   │   │   │
│   │   │   │   ├── model/                # 数据模型
│   │   │   │   │   ├── domain/                   # 领域模型
│   │   │   │   │   │   └── User.java             # 用户实体类
│   │   │   │   │   ├── dto/                      # 数据传输对象
│   │   │   │   │   │   ├── TokenDTO.java         # 令牌DTO
│   │   │   │   │   │   ├── UserUpdateDTO.java    # 用户更新DTO
│   │   │   │   │   │   ├── AuthRequestDTO.java   # 认证请求DTO
│   │   │   │   │   │   └── RefreshTokenRequestDTO.java # 刷新令牌请求DTO
│   │   │   │   │   ├── request/                  # 请求模型
│   │   │   │   │   │   └── UserRegisterRequest.java   # 用户注册请求
│   │   │   │   │   └── response/                 # 响应模型
│   │   │   │   │       ├── BaseResponse.java     # 标准响应类（新版本）
│   │   │   │   │       └── UserVO.java           # 用户视图对象
│   │   │   │   │
│   │   │   │   ├── mq/                   # 消息队列相关代码
│   │   │   │   │   ├── config/                   # MQ配置
│   │   │   │   │   ├── consumer/                 # 消息消费者
│   │   │   │   │   ├── producer/                 # 消息生产者
│   │   │   │   │   └── model/                    # 消息模型
│   │   │   │   │
│   │   │   │   ├── service/              # 业务逻辑层
│   │   │   │   │   ├── UserService.java          # 用户服务接口
│   │   │   │   │   ├── DistributedLockService.java   # 分布式锁接口
│   │   │   │   │   ├── DistributedRateLimiter.java   # 分布式限流器
│   │   │   │   │   ├── AuthService.java          # 认证服务接口
│   │   │   │   │   ├── RedisCacheService.java    # Redis缓存服务接口
│   │   │   │   │   └── impl/                     # 服务实现
│   │   │   │   │       ├── UserServiceImpl.java  # 用户服务实现
│   │   │   │   │       ├── DistributedLockServiceImpl.java # Redis分布式锁实现
│   │   │   │   │       ├── LocalLockServiceImpl.java       # 本地锁实现
│   │   │   │   │       ├── RedisCacheServiceImpl.java      # Redis缓存服务实现
│   │   │   │   │       └── AuthServiceImpl.java   # 认证服务实现
│   │   │   │   │
│   │   │   │   └── UserCenterBackendApplication.java # 应用程序入口
│   │   │   │
│   │   │   └── utils/                # 工具类
│   │   │       ├── JwtUtils.java             # JWT工具类
│   │   │       ├── RedisUtils.java           # Redis操作工具类
│   │   │       └── PasswordUtils.java        # 密码加密工具类
│   │   │
│   │   └── resources/                # 资源文件
│   │       ├── mappers/                      # MyBatis映射文件
│   │       ├── scripts/                      # 脚本文件
│   │       │   └── rate_limiter.lua          # 分布式限流Lua脚本
│   │       └── application.yml               # 应用配置
│   │
│   └── sql/                              # SQL脚本
│
├── usercenter-fronted/                   # 前端代码
│   ├── public/                           # 静态资源文件
│   │   └── icon/                         # 网站图标文件夹
│   │       ├── favicon.ico               # 网站图标
│   │       ├── icon.png                  # 网站图标备用格式
│   │       └── apple-icon.png            # 苹果设备图标
│   │
│   ├── src/                              # 源代码目录
│   │   ├── app/                          # 页面组件 (Next.js App Router)
│   │   │   ├── auth/                     # 认证相关页面
│   │   │   ├── dashboard/                # 仪表盘页面
│   │   │   │   ├── settings/             # 个人设置页面
│   │   │   │   ├── users/                # 用户管理页面
│   │   │   │   └── change-password/      # 密码修改页面
│   │   │   └── page.tsx                  # 首页
│   │   │
│   │   ├── components/                   # 公共组件
│   │   ├── contexts/                     # React Context
│   │   ├── services/                     # API服务
│   │   │   ├── api.ts                    # API配置
│   │   │   ├── userApi.ts                # 用户相关API
│   │   │   └── authApi.ts                # 认证相关API
│   │   │
│   │   └── utils/                        # 工具函数
│   │       ├── request.ts                # HTTP请求封装
│   │       ├── storage.ts                # 存储工具
│   │       └── token.ts                  # 令牌管理工具
│   │
│   ├── turbo-dev.js                      # Turbo模式启动脚本（Windows优化）
│   ├── next.config.mjs                   # Next.js配置（含性能优化）
│   ├── cache-handler.js                  # 缓存处理器
│   ├── .env.development                  # 开发环境配置
│   ├── tsconfig.json                     # TypeScript配置
│   ├── package.json                      # 依赖配置
│   └── .gitignore                        # Git忽略配置
│
│
├── update_baseresponse_imports.ps1       # BaseResponse导入修复脚本 (Windows版)
├── update_baseresponse_imports.sh        # BaseResponse导入修复脚本 (Unix版)
├── package.json                          # 根目录依赖配置
├── LICENSE                               # 许可证文件
└── .gitignore                            # Git忽略配置
```

### 自定义网站图标

系统支持自定义网站图标（favicon），只需替换以下文件：

1. 准备以下图标文件：
   - `favicon.ico` - 主要网站图标（ICO格式）
   - `icon.png` - PNG格式图标（32x32像素）
   - `apple-icon.png` - 苹果设备图标（180x180像素）

2. 将这些文件放置在目录：
   ```
   usercenter-fronted/public/icon/
   ```

3. 图标将自动应用于整个网站，无需额外配置

### 分布式锁配置与使用

系统支持两种锁模式：基于Redis的分布式锁和基于JVM的本地锁。通过配置切换使用哪种模式。

#### 配置说明

在`application.yml`中配置是否启用Redis分布式锁：

```yaml
spring:
  redis:
    enabled: true  # 设置为true启用Redis分布式锁，false使用本地锁
    host: localhost
    port: 6379
    # 其他Redis配置...

# Redisson配置（仅在redis.enabled=true时生效）
redisson:
  address: redis://localhost:6379
  database: 0
  # 其他Redisson配置...
```

#### 使用方式

1. **通过注解使用**：
   在需要加锁的方法上添加`@DistributedLock`注解
   ```java
   @DistributedLock(lockKey = "'userRegister:' + #userAccount", waitTime = 5000, leaseTime = 30000)
   public User userRegister(String userAccount, String userPassword, String checkPassword, HttpServletRequest request) {
       // 方法实现...
   }
   ```

2. **注解参数说明**：
   - `lockKey`: 锁的键，支持SpEL表达式
   - `lockPrefix`: 锁键前缀，默认为"weiki:lock:"
   - `waitTime`: 等待获取锁的最大时间（毫秒）
   - `leaseTime`: 持有锁的最大时间（毫秒）
   - `timeUnit`: 时间单位，默认为毫秒
   - `isFair`: 是否为公平锁，默认为false

3. **直接使用锁服务**：
   也可以通过注入`DistributedLockService`直接使用锁服务
   ```java
   @Autowired
   private DistributedLockService distributedLockService;
   
   public void someMethod() {
       String lockKey = "weiki:lock:someOperation";
       boolean locked = false;
       try {
           locked = distributedLockService.tryLock(lockKey, 3000, 30000, TimeUnit.MILLISECONDS, false);
           if (locked) {
               // 执行需要加锁的操作
           }
       } finally {
           if (locked) {
               distributedLockService.unlock(lockKey);
           }
       }
   }
   ```

#### 实现原理

- **Redis模式**：基于Redisson实现的分布式锁，适用于集群环境
- **本地模式**：基于JVM内的ReentrantLock实现，仅适用于单机部署
- **自动切换**：根据配置自动选择使用哪种锁实现，无需修改业务代码
- **优雅降级**：Redis服务不可用时，可通过配置切换到本地锁模式保证应用可用性

### 消息队列配置与使用

系统支持通过RabbitMQ处理用户相关事件消息。与Redis类似，可以通过配置文件控制是否启用此功能。

#### 配置说明

在`application.yml`中配置是否启用RabbitMQ消息队列：

```yaml
spring:
  rabbitmq:
    enabled: true  # 设置为true启用RabbitMQ功能，false则不使用消息队列
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    # 消息确认配置
    publisher-confirm-type: correlated
    publisher-returns: true
    template:
      mandatory: true
    # 消费者配置
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 1
        concurrency: 3
        max-concurrency: 10
```

#### 功能说明

消息队列主要处理以下类型的消息：

1. **用户注册事件**：用户完成注册后的异步处理
2. **用户活动事件**：记录用户登录、操作等活动信息

#### 使用方式

1. **发送消息**：
   
   通过注入`UserMessageProducer`来发送消息：
   ```java
   @Autowired
   private UserMessageProducer userMessageProducer;
   
   // 发送用户注册消息
   userMessageProducer.sendUserRegisterMessage(userId, "用户注册成功");
   
   // 发送用户活动消息
   userMessageProducer.sendUserActivityMessage(userId, "用户登录", extraData);
   ```

2. **消费消息**：
   
   系统已内置消息消费者`UserMessageConsumer`，会自动处理队列中的消息。
   如需添加自定义处理逻辑，可以修改消费者中的相关方法：
   ```java
   @RabbitListener(queues = RabbitMQConfig.USER_REGISTER_QUEUE)
   public void handleUserRegisterMessage(UserMessage userMessage, Message message, Channel channel) {
       // 实现具体业务逻辑...
   }
   ```

3. **测试接口**：
   
   系统提供测试接口用于验证消息队列功能：
   - 发送注册消息: `POST /api/mq/sendRegisterMessage?userId={userId}&content={content}`
   - 发送活动消息: `POST /api/mq/sendActivityMessage?userId={userId}&content={content}&extraData={extraData}`

### API文档

启动后端服务后，可通过以下地址访问API文档：
```
http://localhost:8083/doc.html
```

也可以直接访问特定接口文档，例如更新用户接口：
```
http://localhost:8083/doc.html#/default/%E7%94%A8%E6%88%B7%E7%9B%B8%E5%85%B3%E6%8E%A5%E5%8F%A3/updateUserUsingPOST
```

## API权限控制

系统使用AOP（面向切面编程）实现了统一的API接口权限控制：

1. **@AuthCheck注解**：用于标记需要进行权限校验的API接口
   - 位于`annotation/AuthCheck.java`
   - 支持设置所需的用户角色级别（普通用户/管理员）

2. **权限拦截器**：自动拦截并校验带有@AuthCheck注解的接口
   - 位于`aop/AuthInterceptor.java`
   - 检查用户登录状态
   - 检查用户状态（是否被禁用）
   - 检查用户角色是否满足接口要求

3. **使用方式**：
   - 在Controller方法上添加`@AuthCheck(mustRole = 1)`注解表示需要管理员权限
   - 在Controller方法上添加`@AuthCheck()`注解表示需要登录但不限制角色

4. **权限级别**：
   - 0：普通用户（默认）
   - 1：管理员

## 主要功能说明

### 用户注册
- 支持用户自主注册账号
- 账号长度至少4位，密码长度至少8位
- 系统会自动生成默认用户名（格式：用户_账号）
- 注册成功后自动登录并跳转至首页
- 使用分布式锁保护，防止并发注册冲突

### 用户登录
- 基于Session的用户认证
- 登录状态保持

### 个人信息管理
- 更新个人资料（用户名、头像、性别、联系方式等）
- 仅能修改自己的信息

### 密码修改
- 支持用户在个人设置菜单中修改密码
- 需要验证当前密码确保安全
- 新密码需符合安全要求（至少8位）
- 密码修改成功后自动退出，需重新登录
- 使用分布式锁保护，防止并发修改

### 管理员功能
- 查看所有用户列表
  - 支持按账号和角色筛选用户
  - 表格分页支持快速跳转到指定页码
  - 系统自动处理无效页码（数字过大或过小）
- 编辑任意用户信息
  - 修改用户名、性别、联系方式等基本信息
  - 直接上传用户头像（支持本地图片上传）
  - 预览头像效果
- 删除用户（逻辑删除）
- 封禁/解封用户
  - 临时封禁：指定封禁天数，到期自动解封
  - 永久封禁：设置封禁天数为0
  - 封禁时必须提供封禁原因
  - 可随时手动解封被封禁的用户
  - 用户管理界面显示封禁状态和原因

### 用户头像管理
- 直接上传本地图片（自动转换为Base64编码存储）
- 实时预览头像效果
- 支持大图查看

### 数据安全
- 密码加密存储（使用MD5+盐值加密）
- 密码修改需验证旧密码，防止非法操作
- 密码修改后强制重新登录
- 敏感信息脱敏

### 用户封禁系统
- **封禁机制**：
  - 管理员可封禁任何用户账号
  - 支持指定封禁天数（临时封禁）或永久封禁
  - 必须提供明确的封禁原因
- **封禁效果**：
  - 被封禁用户无法登录系统
  - 系统会显示封禁原因和解封时间（如果是临时封禁）
  - 永久封禁用户显示永久封禁标识
- **自动解封**：
  - 临时封禁的用户在封禁期满后自动解除封禁状态
  - 下次登录时系统自动检测并更新封禁状态
- **手动解封**：
  - 管理员可随时手动解封任何用户
  - 解封后用户可立即正常登录系统
- **封禁记录**：
  - 系统保留封禁原因记录，方便管理员查看

## 常见问题

### Q: 注册账号提示已存在，但在用户列表中看不到
A: 可能该账号已被逻辑删除。系统现已支持重新注册已删除的账号。

### Q: 忘记密码怎么办？
A: 目前系统暂不支持找回密码功能，可自行添加相关功能。

### Q: 上传的头像大小有限制吗？
A: 建议上传小于1MB的图片文件，过大的文件可能会导致传输和存储问题。

### Q: 临时封禁的用户什么时候会自动解封？
A: 系统会在用户尝试登录时检查封禁状态，如果已过封禁期限，则自动解除封禁状态。这意味着临时封禁用户的解封是在下次登录时触发的，而不是到期后立即解封。

### Q: 永久封禁的用户可以解封吗？
A: 可以。管理员可以在用户管理界面通过"解封"按钮手动解除任何用户的封禁状态，包括永久封禁的用户。

### Q: 如何查看用户的封禁原因和封禁时间？
A: 在用户管理界面，封禁状态列会显示用户的封禁状态。鼠标悬停在"临时封禁"或"永久封禁"标签上时，会显示封禁原因和解封日期（对于临时封禁用户）。

### Q: 密码修改后为什么需要重新登录？
A: 出于安全考虑，修改密码后会自动注销登录状态，强制用户使用新密码重新登录，以确保密码修改的安全性。

### Q: 用户列表的"跳至"功能输入无效页码会怎样？
A: 系统对输入内容有保护机制：如果输入小于1的数字，会跳转到第1页；如果输入大于最大页数的数字，会跳转到最后一页；如果输入非数字字符，不会触发跳转。这样设计可以防止用户误操作导致的问题。

### Q: 如何修改网站图标？
A: 替换 `usercenter-fronted/public/icon/` 目录下的 `favicon.ico`、`icon.png` 和 `apple-icon.png` 文件即可。建议保持文件名不变。

## 联系方式

如有问题或建议，请联系：
- 邮箱：weiki886@163.com

## 许可证

本项目采用MIT许可证。详见LICENSE文件。

## Java版本兼容性

本项目已优化至支持Java 11，推荐使用Java 11 LTS（如11.0.25）进行构建和部署。主要优化包括：

1. 更新了POM文件中的Java版本配置为Java 11
2. 添加了Java 11所需的JAXB、Jakarta XML和注解API依赖（从JDK 9开始这些组件不再包含在JDK中）
3. 配置了Maven编译器插件使用Java 11的release标志
4. 更新了Lombok和MapStruct依赖以确保与Java 11兼容

如有构建问题，请确保使用兼容的Maven版本（3.6+）并正确设置JAVA_HOME环境变量指向Java 11 JDK。

## JWT认证流程

### 概述

本项目实现了基于JWT（JSON Web Token）的认证机制，提供安全的用户认证和授权功能。JWT是一种开放标准，它定义了一种紧凑且自包含的方式，用于在各方之间以JSON对象的形式安全地传输信息。

### 特性

- 使用HS256算法进行签名
- 包含标准的声明（issuer, expiration, subject）
- 访问令牌包含用户ID
- 提供刷新令牌机制
- 中间件验证请求头中的Bearer Token
- 黑名单机制处理令牌注销
- Redis存储刷新令牌，提升安全性

### 核心组件

- **JwtConstant**：定义JWT相关常量
- **JwtUtils**：JWT工具类，处理令牌生成和验证
- **JwtAuthenticationFilter**：JWT认证过滤器，验证请求中的令牌
- **AuthService**：认证服务接口
- **AuthServiceImpl**：认证服务实现，处理登录、刷新令牌和注销
- **AuthController**：认证控制器，提供RESTful API接口

### 认证流程

1. **用户登录**：
   - 用户提供用户名和密码
   - 服务器验证用户身份
   - 生成访问令牌（短期有效）和刷新令牌（长期有效）
   - 返回令牌给客户端

2. **访问受保护资源**：
   - 客户端在请求头中添加访问令牌
   - 服务器验证令牌的有效性
   - 允许或拒绝访问请求

3. **刷新令牌**：
   - 当访问令牌过期时，客户端使用刷新令牌获取新的访问令牌
   - 服务器验证刷新令牌
   - 生成新的访问令牌和刷新令牌
   - 返回新令牌给客户端

4. **用户注销**：
   - 客户端请求注销
   - 服务器将用户的刷新令牌从Redis移除
   - 可以选择将访问令牌加入黑名单

### API接口

#### 用户登录

```
POST /api/auth/login
```

请求体：
```json
{
  "username": "用户名",
  "password": "密码"
}
```

响应：
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

#### 刷新令牌

```
POST /api/auth/refresh
```

请求体：
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

响应：
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

#### 用户注销

```
POST /api/auth/logout
```

请求头：
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

响应：
```
状态码: 200 OK
```

### 使用方式

1. **前端存储令牌**：
   - 将访问令牌和刷新令牌存储在安全的位置（如HttpOnly Cookie或localStorage）

2. **请求头添加令牌**：
   ```javascript
   // 示例：使用Axios添加认证头
   axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
   ```

3. **令牌过期处理**：
   - 当请求返回401状态码时，使用刷新令牌获取新的访问令牌
   - 更新存储的令牌
   - 重新发送原始请求

4. **安全建议**：
   - 使用HTTPS传输令牌
   - 访问令牌设置较短的过期时间
   - 敏感操作需要额外验证

### 配置说明

JWT相关配置位于`com.weiki.usercenterbackend.constant.JwtConstant.java`中：

```java
public class JwtConstant {
    // 密钥
    public static final String JWT_SECRET_KEY = "WeiKiUserCenterSecretKey12345678901234567890";
    
    // token前缀
    public static final String TOKEN_PREFIX = "Bearer ";
    
    // 请求头中token的key
    public static final String TOKEN_HEADER = "Authorization";
    
    // 发行者
    public static final String ISSUER = "weiki-user-center";
    
    // 访问令牌过期时间（毫秒） - 30分钟
    public static final long ACCESS_TOKEN_EXPIRATION = 30 * 60 * 1000L;
    
    // 刷新令牌过期时间（毫秒） - 7天
    public static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
    
    // 其他配置...
}
```

可以根据实际需要调整这些配置。

### 常见问题与注意事项

#### 密钥安全

在生产环境中，应当妥善保护JWT密钥：
- 不要在代码中硬编码密钥
- 考虑使用环境变量或加密的配置文件存储密钥
- 定期轮换密钥以增强安全性

#### Redis配置

确保Redis服务器已正确配置：
- 对于生产环境，启用密码保护
- 配置适当的持久化策略，避免令牌数据丢失
- 考虑使用Redis集群以提高可用性

#### 令牌失效处理

- 当用户更改密码或出现安全问题时，应立即使其所有令牌失效
- 可以通过更新用户的"tokenVersion"字段并在验证令牌时检查版本来实现

#### 跨域问题

如果前端与后端部署在不同域名下，需要配置CORS：
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*"); // 在生产中应指定确切的前端域名
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

#### 故障排除

1. **令牌验证失败**
   - 检查密钥是否正确
   - 确认令牌格式（是否包含"Bearer "前缀）
   - 验证令牌是否已过期
   - 检查令牌是否在黑名单中

2. **刷新令牌不工作**
   - 确认Redis连接正常
   - 检查刷新令牌是否过期
   - 验证刷新令牌是否为正确类型

3. **性能问题**
   - 对频繁访问的路径考虑添加缓存
   - 监控令牌黑名单大小，定期清理过期记录
   - 考虑使用JWT压缩算法减小令牌大小

### API限流系统

系统实现了一个基于Guava RateLimiter的高性能API限流系统，支持多种限流策略。

#### 限流配置

在`application.yml`中配置限流参数：

```yaml
rate:
  limit:
    global:
      qps: 10.0               # 全局默认QPS限制
      warmup: 0               # 预热时间（秒）
      timeout: 0              # 超时时间（毫秒）
    user:
      qps: 5.0                # 用户级别QPS限制
    cache:
      expire: 30              # 限流器缓存过期时间（分钟）
      maximum:
        size: 10000           # 限流器缓存最大数量
    distributed:
      enabled: false          # 是否启用分布式限流
      lua:
        path: classpath:scripts/rate_limiter.lua # Lua脚本路径
    burst:
      factor: 2.0             # 突发流量处理系数
```

#### 使用方式

1. **基本限流**：在需要限流的接口上添加`@RateLimit`注解：

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    // 默认接口限流（5 QPS）
    @RateLimit
    @GetMapping("/info")
    public BaseResponse<UserVO> getUserInfo() {
        // ...
    }
    
    // 用户级别限流（每用户5 QPS）
    @RateLimit(limitType = RateLimit.LimitType.USER)
    @GetMapping("/profile")
    public BaseResponse<UserProfileVO> getUserProfile() {
        // ...
    }
    
    // 自定义QPS和预热期
    @RateLimit(qps = 20.0, warmupPeriod = 5, timeout = 100)
    @PostMapping("/upload")
    public BaseResponse<String> uploadAvatar() {
        // ...
    }
}
```

2. **降级处理**：配置服务降级，当触发限流时返回替代结果：

```java
@RateLimit(fallbackClass = UserServiceFallback.class)
@GetMapping("/dashboard")
public BaseResponse<DashboardVO> getDashboard() {
    // 正常逻辑
}

@Component
public class UserServiceFallback implements Fallback {
    @Override
    public Object fallback(ProceedingJoinPoint joinPoint, RateLimitInfo rateLimitInfo) {
        // 返回缓存数据或默认值
        return BaseResponse.error(ResultCode.TOO_MANY_REQUESTS, "服务繁忙，请稍后再试");
    }
}
```

3. **分布式限流**：在集群环境中启用Redis分布式限流：

```yaml
rate:
  limit:
    distributed:
      enabled: true  # 启用分布式限流
```

#### 令牌获取模式

系统支持三种获取令牌的模式：

1. **阻塞式**：`timeout = -1`，请求会一直等待直到获取到令牌
2. **非阻塞式**：`timeout = 0`，立即返回成功或失败
3. **超时等待**：`timeout > 0`，等待指定时间，超时后返回失败

#### 监控指标

系统自动收集以下Prometheus指标：

- `http_requests_limiter_available_permits`：可用令牌数
- `http_requests_limiter_wait_time_seconds`：等待时间
- `http_requests_limiter_rejected_total`：被拒绝请求数
- `http_requests_limiter_removed_total`：从缓存中移除的限流器数量

这些指标可以通过Spring Boot Actuator暴露，并被Prometheus采集用于监控和告警。

#### 高级限流场景示例

以下是针对不同场景的限流配置示例：

##### 支付接口（严格限流）

支付类关键业务接口，需要稳定的处理能力：

```java
@RequestMapping(value = "/api/v1/payment", method = {RequestMethod.GET, RequestMethod.POST})
@RateLimit(
    qps = 100.0,                     // 限制为100 QPS
    warmupPeriod = 10,               // 10秒预热期
    warmupUnit = TimeUnit.SECONDS,   
    timeout = 200,                   // 最多等待200ms
    timeoutUnit = TimeUnit.MILLISECONDS
)
public BaseResponse<Map<String, Object>> paymentEndpoint() {
    // 支付处理逻辑
}
```

##### 商品接口（弹性限流）

高流量产品接口，需要应对突发流量：

```java
@RequestMapping(value = "/api/v1/product", method = {RequestMethod.GET, RequestMethod.POST})
@RateLimit(
    qps = 500.0,    // 基础QPS为500
    priority = 8    // 高优先级，配合突发流量系数(2.0)可实现1000 QPS的突发处理能力
)
public BaseResponse<Map<String, Object>> productEndpoint() {
    // 商品查询逻辑
}
```

##### 用户级别限流（会员优先）

基于用户级别的差异化限流策略：

```java
// 付费用户 - 高优先级
@GetMapping("/user/premium")
@RateLimit(
    limitType = RateLimit.LimitType.USER,  // 用户级别限流
    priority = 10                          // 高优先级(10)
)
public BaseResponse<Map<String, Object>> premiumUserEndpoint() {
    // 付费用户服务
}

// 免费用户 - 低优先级
@GetMapping("/user/free")
@RateLimit(
    limitType = RateLimit.LimitType.USER,  // 用户级别限流
    priority = 3                           // 低优先级(3)
)
public BaseResponse<Map<String, Object>> freeUserEndpoint() {
    // 免费用户服务
}
```

#### 存储优化

限流器缓存使用Guava的LoadingCache实现，具有以下特性：

1. **自动失效**：限流器30分钟未使用自动从缓存中移除
2. **容量限制**：最多缓存10,000个不同的限流器
3. **指标监控**：缓存项移除时会触发监控指标上报
4. **线程安全**：使用AtomicReference+版本号解决高并发下的ABA问题

#### 常见问题

1. **限流器创建太频繁导致性能问题**

   - 系统使用LoadingCache缓存和延迟加载策略，同一接口或用户只会创建一个限流器
   - 限流器30分钟不活跃会被自动回收，避免内存泄漏

2. **高并发下限流不准确**

   - 使用AtomicReference+版本号解决并发创建限流器时的ABA问题
   - 确保在高并发场景下限流器的创建和获取是线程安全的

3. **如何调优限流参数**

   - 根据接口的实际承载能力设置合理的QPS值
   - 对于冷启动场景，建议配置适当的预热时间
   - 通过监控指标观察接口的实际限流情况，动态调整参数

## 代码规范与类型问题修复

### BaseResponse 类型标准化

为了解决项目中存在的BaseResponse类型兼容性问题，我们进行了以下标准化：

1. **问题背景**
   - 项目中同时存在两个不同的BaseResponse类：
     - `com.weiki.usercenterbackend.common.BaseResponse`（旧版本）
     - `com.weiki.usercenterbackend.model.response.BaseResponse`（新版本）
   - 这导致了编译错误和类型不匹配问题

2. **解决方案**
   - 统一使用`com.weiki.usercenterbackend.model.response.BaseResponse`作为标准响应类
   - 更新所有控制器中的导入语句，使用新版本
   - 修改异常处理类中的返回类型为正确的泛型版本
   - 创建自动化脚本以批量处理相关文件

3. **预防措施**
   - 项目标准：使用`com.weiki.usercenterbackend.model.response.BaseResponse`作为唯一的响应类
   - 避免使用`com.weiki.usercenterbackend.common.BaseResponse`
   - 建议在代码审查阶段检查导入语句，确保使用正确的类
   - 考虑启用更严格的编译时类型检查

## Redis缓存用户信息

### 缓存策略

系统使用Redis缓存用户信息，提高查询性能并减轻数据库负担：

1. **缓存内容**
   - 用户基本信息（ID、账号、角色等）
   - 用户详细信息（名称、性别、头像等，不包含敏感数据）
   - 用户登录状态和会话信息

2. **缓存机制**
   - 采用"查询时缓存"策略：首次访问时缓存，后续直接从缓存获取
   - 更新机制：用户信息更新时自动更新或失效相关缓存
   - 缓存过期时间：默认30分钟，可在配置文件中调整

3. **配置说明**

```yaml
spring:
  redis:
    cache:
      user:
        enabled: true          # 是否启用用户信息缓存
        expiration: 1800       # 缓存过期时间（秒）
        prefix: "user:info:"   # 缓存键前缀
      session:
        expiration: 86400      # 用户会话缓存过期时间（秒）
        prefix: "user:session:"
```

4. **使用示例**

```java
// 用户服务自动使用Redis缓存
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public User getUserById(Long userId) {
        // 先尝试从缓存获取
        String cacheKey = "user:info:" + userId;
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedUser != null) {
            return cachedUser;
        }
        
        // 缓存未命中，从数据库查询
        User user = userMapper.selectById(userId);
        
        // 存入缓存
        if (user != null) {
            redisTemplate.opsForValue().set(cacheKey, user, 1800, TimeUnit.SECONDS);
        }
        
        return user;
    }
    
    @Override
    public boolean updateUser(User user) {
        // 更新数据库
        int result = userMapper.updateById(user);
        
        // 更新缓存
        if (result > 0) {
            String cacheKey = "user:info:" + user.getId();
            redisTemplate.opsForValue().set(cacheKey, user, 1800, TimeUnit.SECONDS);
        }
        
        return result > 0;
    }
}
```

5. **缓存一致性保障**
   - 更新操作：先更新数据库，再更新缓存
   - 删除操作：先删除数据库记录，再删除缓存
   - 批量操作：通过Redis管道(pipeline)提高性能
   - 缓存预热：系统启动时可选择性预加载活跃用户数据

6. **监控与维护**
   - 通过Spring Boot Actuator暴露缓存命中率指标
   - 提供手动清理缓存的管理接口（仅限管理员）
   - 定期清理过期和无效缓存数据

## Actuator 监控系统

UserCenter 集成了 Spring Boot Actuator 监控系统，提供了丰富的生产级监控和管理功能。

### 监控功能概览

- **健康检查**：监控数据库、Redis 和自定义短信服务的健康状态
- **指标收集**：通过 Micrometer + Prometheus 提供丰富的系统指标
- **自定义指标**：统计 API 调用次数、系统状态和用户活跃度
- **安全防护**：基于 Spring Security 的 Actuator 端点访问控制

### 关键配置

#### Actuator 端点配置

在 `application.yml` 中配置了 Actuator 端点：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"                # 暴露所有端点
        exclude: "shutdown"         # 禁用敏感端点
      base-path: /actuator          # 端点基础路径
  endpoint:
    health:
      show-details: always          # 显示详细健康信息
      probes:
        enabled: true               # 启用Kubernetes探针
      group:
        readiness:
          include: db, redis        # 包含数据库和Redis健康检查
    prometheus:
      enabled: true                 # 启用Prometheus端点
```

#### 安全配置

Actuator 端点通过 Spring Security 进行安全防护：

- `/actuator/health/**` - 允许公开访问，无需认证
- 其他 `/actuator/**` 端点 - 需要 ADMIN 角色和 HTTP Basic 认证

#### 自定义健康检查

实现了自定义短信服务健康检查器 `SmsServiceHealthIndicator`，用于监控第三方短信服务的可用性状态。

##### 真实SMS服务健康检查实现指南

当前的短信服务健康检查是模拟实现，在实际生产环境中，您需要改造代码以连接真实的SMS服务。以下是实现步骤：

1. **配置SMS服务端点**

   在`application.yml`中添加SMS服务配置：

   ```yaml
   sms:
     service:
       enabled: true
       endpoint: https://api.sms-provider.com/status
       timeout: 3000  # 毫秒
       healthcheck:
         interval: 60  # 秒
   ```

2. **引入HTTP客户端依赖**

   选择RestTemplate或WebClient：

   ```java
   // RestTemplate方案 - 已包含在spring-boot-starter-web中
   
   // 或WebClient方案 - 需要添加以下依赖
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-webflux</artifactId>
   </dependency>
   ```

3. **修改SmsServiceHealthIndicator实现**

   使用RestTemplate实现方式：

   ```java
   @Component
   @ConfigurationProperties(prefix = "sms.service")
   @Data
   public class SmsServiceHealthIndicator extends AbstractHealthIndicator {
   
       private String endpoint = "https://api.sms-provider.com/status";
       private int timeout = 3000;
       private boolean enabled = true;
   
       private final RestTemplate restTemplate;
   
       public SmsServiceHealthIndicator() {
           // 配置超时
           HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
           factory.setConnectTimeout(timeout);
           factory.setReadTimeout(timeout);
           this.restTemplate = new RestTemplate(factory);
       }
   
       @Override
       protected void doHealthCheck(Health.Builder builder) {
           if (!enabled) {
               builder.up().withDetail("service", "SMS Service")
                   .withDetail("status", "UP")
                   .withDetail("details", "Service check disabled");
               return;
           }
           
           try {
               // 发送GET请求到SMS服务状态端点
               ResponseEntity<Map> response = restTemplate.getForEntity(endpoint, Map.class);
               
               if (response.getStatusCode().is2xxSuccessful()) {
                   // 解析响应体，判断服务状态
                   Map<String, Object> body = response.getBody();
                   boolean isHealthy = body != null && "UP".equals(body.get("status"));
                   
                   if (isHealthy) {
                       builder.up()
                           .withDetail("service", "SMS Service")
                           .withDetail("status", "UP")
                           .withDetail("url", endpoint)
                           .withDetail("responseTime", body.get("responseTime"));
                   } else {
                       builder.down()
                           .withDetail("service", "SMS Service")
                           .withDetail("status", "DOWN")
                           .withDetail("url", endpoint)
                           .withDetail("error", "Service reports unhealthy state");
                   }
               } else {
                   builder.down()
                       .withDetail("service", "SMS Service")
                       .withDetail("status", "DOWN")
                       .withDetail("url", endpoint)
                       .withDetail("error", "Status code: " + response.getStatusCodeValue());
               }
           } catch (Exception e) {
               builder.down()
                   .withDetail("service", "SMS Service")
                   .withDetail("status", "DOWN")
                   .withDetail("url", endpoint)
                   .withDetail("error", e.getMessage());
           }
       }
   }
   ```

   使用WebClient实现方式（响应式）：

   ```java
   @Component
   @ConfigurationProperties(prefix = "sms.service")
   @Data
   public class SmsServiceHealthIndicator extends AbstractHealthIndicator {
   
       private String endpoint = "https://api.sms-provider.com/status";
       private int timeout = 3000;
       private boolean enabled = true;
   
       private final WebClient webClient;
   
       public SmsServiceHealthIndicator() {
           this.webClient = WebClient.builder()
               .clientConnector(new ReactorClientHttpConnector(
                   HttpClient.create().responseTimeout(Duration.ofMillis(timeout))
               ))
               .build();
       }
   
       @Override
       protected void doHealthCheck(Health.Builder builder) {
           if (!enabled) {
               builder.up().withDetail("service", "SMS Service")
                   .withDetail("status", "UP")
                   .withDetail("details", "Service check disabled");
               return;
           }
           
           try {
               // 发送GET请求，阻塞等待结果（健康检查上下文）
               Map response = webClient.get()
                   .uri(endpoint)
                   .retrieve()
                   .bodyToMono(Map.class)
                   .block(Duration.ofMillis(timeout));
               
               if (response != null && "UP".equals(response.get("status"))) {
                   builder.up()
                       .withDetail("service", "SMS Service")
                       .withDetail("status", "UP")
                       .withDetail("url", endpoint)
                       .withDetail("responseTime", response.get("responseTime"));
               } else {
                   builder.down()
                       .withDetail("service", "SMS Service")
                       .withDetail("status", "DOWN")
                       .withDetail("url", endpoint)
                       .withDetail("error", "Service reports unhealthy state");
               }
           } catch (Exception e) {
               builder.down()
                   .withDetail("service", "SMS Service")
                   .withDetail("status", "DOWN")
                   .withDetail("url", endpoint)
                   .withDetail("error", e.getMessage());
           }
       }
   }
   ```

4. **处理响应格式**

   您需要根据实际的SMS服务响应格式调整代码。假设服务返回如下JSON：

   ```json
   {
     "status": "UP",
     "responseTime": 45,
     "details": {
       "messagesQueued": 12,
       "activeConnections": 5
     }
   }
   ```

   则可以提取和展示这些有用信息：

   ```java
   Map<String, Object> details = (Map<String, Object>) response.get("details");
   builder.up()
       .withDetail("service", "SMS Service")
       .withDetail("status", "UP")
       .withDetail("url", endpoint)
       .withDetail("responseTime", response.get("responseTime"))
       .withDetail("messagesQueued", details.get("messagesQueued"))
       .withDetail("activeConnections", details.get("activeConnections"));
   ```

5. **添加容错机制**

   为了避免SMS服务不可用导致健康检查失败影响整个系统，可以添加以下容错机制：

   ```java
   // 在application.yml中添加
   management:
     health:
       sms:
         enabled: true
         required: false  # SMS服务故障不会导致整个健康检查失败
   ```

6. **测试健康检查**

   可以使用以下步骤测试SMS服务健康检查：

   1. 使用工具如Wiremock模拟SMS服务响应
   2. 访问`/actuator/health`端点查看SMS服务健康状态
   3. 模拟SMS服务故障，验证健康检查正确报告DOWN状态

#### 自定义指标监控

1. **API 调用计数器**：监控 `/api/orders` 接口的调用次数
   ```java
   Counter.builder("api.orders.calls")
       .description("Number of calls to orders API")
       .tag("application", "usercenter")
       .tag("endpoint", "/api/orders")
       .register(meterRegistry);
   ```

2. **系统状态指标**：监控系统运行状态
   ```java
   Gauge.builder("system.status", systemStatus, AtomicInteger::get)
       .description("System operational status (0=maintenance, 1=operational)")
       .tag("type", "availability")
       .register(registry);
   ```

3. **活跃用户指标**：统计系统活跃用户数量
   ```java
   Gauge.builder("users.active", activeUsers, AtomicInteger::get)
       .description("Number of currently active users")
       .tag("type", "user")
       .register(registry);
   ```

4. **服务调用计时器**：测量服务调用耗时
   ```java
   Timer.builder("application.service.time")
       .description("Time taken by application service calls")
       .tag("service", "user-service")
       .publishPercentiles(0.5, 0.95, 0.99)
       .register(registry);
   ```

### 访问监控数据

1. **健康状态**：`/actuator/health` (公开访问)
2. **指标概览**：`/actuator/metrics` (需 ADMIN 角色)
3. **Prometheus 格式指标**：`/actuator/prometheus` (需 ADMIN 角色)

### Actuator 身份认证

使用基本认证 (HTTP Basic) 访问受保护的 Actuator 端点：
- 用户名：`admin`
- 密码：`adminPassword`

### 集成到监控系统

Actuator 系统可以与以下监控工具轻松集成：

1. **Prometheus**：采集指标数据
2. **Grafana**：创建监控仪表盘
3. **Spring Boot Admin**：可视化管理界面
4. **Kubernetes**：利用健康检查进行容器管理

### 监控实践建议

1. 在生产环境中设置强密码，避免使用默认配置
2. 考虑使用专用端口和网络隔离暴露 Actuator 端点
3. 定期查看健康状态和关键指标
4. 为关键指标设置告警阈值

## 前端性能优化

为提升前端应用的编译速度和运行效率，项目进行了以下优化：

### Next.js Turbo 模式优化

在 `next.config.mjs` 中添加了性能优化配置：

```javascript
const nextConfig = {
  // 启用图片优化
  images: {
    domains: ['localhost'],
    formats: ['image/avif', 'image/webp'],
  },
  // 启用增量编译
  experimental: {
    // 优化配置
    optimizeCss: true,
    optimizePackageImports: ['antd', '@ant-design/icons'],
  },
  // 优化构建
  swcMinify: true,
  poweredByHeader: false,
  // 禁用严格模式以减少重复渲染
  reactStrictMode: false,
  // 优化输出
  output: 'standalone',
};
```

### 启动脚本优化

在 `package.json` 中添加了快速启动脚本：

```json
"scripts": {
  "dev": "next dev",
  "dev:fast": "next dev --turbo",
  "dev:win": "node turbo-dev.js",
  "build": "next build",
  "start": "next start",
  "lint": "next lint"
}
```

- `dev:fast` - 使用 Turbo 模式启动开发服务器
- `dev:win` - 专为 Windows 环境优化的启动方式

### Ant Design 组件优化

修复了所有 Spin 组件的使用方式，避免不必要的警告：

```jsx
// 优化前
<Spin size="large" tip="加载中..." />

// 优化后
<Spin spinning={true} size="large">
  <div style={{ padding: '50px', textAlign: 'center' }}>加载中...</div>
</Spin>
```

### 性能提升效果

- 大幅减少编译时间
- 提高热重载速度
- 减少不必要的控制台警告
- 优化组件导入，减少打包体积