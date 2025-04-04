# UserCenter 用户中心系统

## 项目介绍

UserCenter是一个功能完善的用户管理系统，提供用户注册、登录、信息管理等核心功能。本项目采用前后端分离架构，包含完整的前端界面和后端API，可用作独立的用户中心系统，也可集成到其他业务系统中。

## 功能特性

- **用户账号管理**：注册、登录、注销
- **个人信息管理**：查看和修改个人资料、上传头像
- **账号安全管理**：用户密码修改
- **用户权限控制**：普通用户/管理员角色区分
- **管理员功能**：
  - 用户列表查看
  - 用户信息编辑（包括头像上传）
  - 用户删除
- **逻辑删除**：支持账号注销后重新注册
- **界面定制**：支持自定义网站图标

## 系统架构

项目采用前后端分离的架构：

- **前端**：基于Next.js构建的React应用，使用Ant Design组件库
- **后端**：基于Spring Boot的Java应用，提供RESTful API
- **数据库**：MySQL数据库存储用户数据

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
- **数据库**：MySQL 8.x

## 快速开始

### 环境要求
- Node.js 18+
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 后端部署

1. 克隆仓库
```bash
git clone https://github.com/Weiki886/UserCenter.git
cd UserCenter
```

2. 配置数据库
```sql
# 创建数据库
CREATE DATABASE user_center;
```

3. 修改配置
```bash
# 编辑配置文件
vim usercenter-backend/src/main/resources/application.yml
# 修改数据库连接信息
```

4. 执行数据库脚本
```bash
# 运行初始化SQL脚本
mysql -u username -p user_center < usercenter-backend/sql/init_table.sql
# 运行索引更新脚本
mysql -u username -p user_center < usercenter-backend/sql/update_unique_index.sql
```

5. 启动后端服务
```bash
cd usercenter-backend
mvn spring-boot:run
```

### 前端部署

1. 安装依赖
```bash
cd usercenter-fronted
npm install
```

2. 修改API配置
```bash
# 编辑API配置
vim src/services/api.ts
# 确保baseURL指向正确的后端地址
```

3. 开发环境启动
```bash
npm run dev
```

4. 生产环境构建
```bash
npm run build
npm start
```

## 开发指南

### 目录结构

```
UserCenter/
├── usercenter-backend/         # 后端代码
│   ├── src/                    
│   │   ├── main/java/com/weiki/usercenterbackend/
│   │   │   ├── annotation/     # 自定义注解
│   │   │   │   └── AuthCheck.java  # 权限校验注解
│   │   │   ├── aop/            # 面向切面编程
│   │   │   │   └── AuthInterceptor.java  # 权限校验拦截器
│   │   │   ├── common/         # 公共组件
│   │   │   ├── config/         # 配置类
│   │   │   ├── constant/       # 常量定义
│   │   │   ├── controller/     # 控制器
│   │   │   ├── exception/      # 异常处理
│   │   │   ├── mapper/         # 数据访问层
│   │   │   ├── model/          # 数据模型
│   │   │   ├── service/        # 业务逻辑层
│   │   │   └── utils/          # 工具类
│   │   └── resources/          # 资源文件
│   │       ├── mappers/        # MyBatis映射文件
│   │       └── application.yml # 应用配置
│   └── sql/                    # SQL脚本
├── usercenter-fronted/         # 前端代码
│   ├── public/                 # 静态资源文件
│   │   ├── icon/              # 网站图标文件夹
│   │   │   ├── favicon.ico    # 网站图标
│   │   │   ├── icon.png       # 网站图标备用格式
│   │   │   └── apple-icon.png # 苹果设备图标
│   │   ├── src/
│   │   │   ├── app/                # 页面组件
│   │   │   │   ├── auth/           # 认证相关页面
│   │   │   │   ├── dashboard/      # 仪表盘页面
│   │   │   │   │   ├── settings/   # 个人设置页面
│   │   │   │   │   ├── users/      # 用户管理页面
│   │   │   │   │   └── change-password/ # 密码修改页面
│   │   │   │   └── page.tsx        # 首页
│   │   │   ├── components/         # 公共组件
│   │   │   ├── contexts/           # React Context
│   │   │   ├── services/           # API服务
│   │   │   └── utils/              # 工具函数
│   │   └── package.json            # 依赖配置
│   └── README.md                   # 项目说明
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

### 用户头像管理
- 直接上传本地图片（自动转换为Base64编码存储）
- 实时预览头像效果
- 支持大图查看

### 数据安全
- 密码加密存储（使用MD5+盐值加密）
- 密码修改需验证旧密码，防止非法操作
- 密码修改后强制重新登录
- 敏感信息脱敏

## 常见问题

### Q: 注册账号提示已存在，但在用户列表中看不到
A: 可能该账号已被逻辑删除。系统现已支持重新注册已删除的账号。

### Q: 忘记密码怎么办？
A: 目前系统暂不支持找回密码功能，可自行添加相关功能。

### Q: 上传的头像大小有限制吗？
A: 建议上传小于1MB的图片文件，过大的文件可能会导致传输和存储问题。

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