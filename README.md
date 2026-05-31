# 🎓 家校协同学生信息管理系统

## 📌 项目简介

本系统是一个基于 **Spring Boot + Vue 3** 的前后端分离家校协同平台，旨在解决传统家校沟通（家长会、电话、短信）中存在的信息滞后、沟通渠道分散等问题。

系统整合了以下核心功能：

* 学生管理
* 成绩管理
* 考勤管理
* 通知公告
* 实时聊天

支持 **管理员、教师、学生、家长** 四种角色，并实现了：

* 细粒度权限控制（RBAC）
* 数据隔离机制

---

## 🛠 技术栈

### 后端

* Spring Boot 3.2.4
* Spring Security 6.2.4
* MyBatis 3.0.3
* MySQL 8.0
* JWT（JJWT 0.11.5）
* WebSocket（STOMP）
* Maven 3.8.8

### 前端

* Vue 3.4
* Vite 5.0
* Element Plus 2.6
* Pinia（状态管理）
* Axios
* StompJS + SockJS（WebSocket）

---

## 🚀 核心功能

| 角色        | 功能                       |
| --------- | ------------------------ |
| 👨‍💼 管理员 | 用户管理、班级管理、全局统计           |
| 👩‍🏫 教师  | 学生管理、成绩管理、考勤管理、通知发布、在线聊天 |
| 👨‍🎓 学生  | 查看成绩、考勤、通知、在线聊天          |
| 👪 家长     | 查看孩子成绩、考勤、通知、在线聊天        |

---

## ✨ 系统亮点

* 🔹 前后端分离：Vue + Spring Boot 独立部署
* 🔹 JWT 无状态认证：支持分布式系统
* 🔹 权限控制：RBAC + 自定义拦截器
* 🔹 数据隔离：不同角色数据严格隔离
* 🔹 实时聊天：WebSocket + STOMP（支持单聊/群聊/未读计数）
* 🔹 工作台统计：动态展示角色数据

---

## ⚡ 快速开始

### 🔧 环境要求

* JDK 17+
* Node.js 16+
* MySQL 8.0
* Maven 3.6+

---

### 1️⃣ 克隆项目

```bash
git clone <repository-url>
cd edulink-system
```

---

### 2️⃣ 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE edulink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入数据
mysql -u root -p edulink < database/init.sql
mysql -u root -p edulink < database/sample_data.sql
```

---

### 3️⃣ 启动后端

```bash
cd edulink-backend
mvn clean compile
mvn spring-boot:run
```

📍 后端地址：http://localhost:8080/api

---

### 4️⃣ 启动前端

```bash
cd edulink-frontend
npm install
npm run dev
```

📍 前端地址：http://localhost:3000

---

### 5️⃣ 测试账号

| 角色  | 用户名        | 密码     |
| --- | ---------- | ------ |
| 管理员 | admin      | 123456 |
| 教师  | teacher001 | 123456 |
| 学生  | student001 | 123456 |
| 家长  | parent001  | 123456 |

---

## 📂 项目结构

### 后端结构

```
edulink-backend/
├── controller/      # 接口层
├── service/         # 业务逻辑
├── mapper/          # 数据访问
├── entity/          # 实体类
├── dto/             # 数据传输对象
├── vo/              # 视图对象
├── config/          # 配置类
├── interceptor/     # 权限控制
└── utils/           # 工具类
```

---

### 前端结构

```
edulink-frontend/
├── api/             # 接口封装
├── views/           # 页面
├── components/      # 公共组件
├── router/          # 路由
├── store/           # 状态管理
└── utils/           # 工具
```

---

## 🔗 主要接口示例

| 功能   | 方法        | 路径                  | 权限     |
| ---- | --------- | ------------------- | ------ |
| 登录   | POST      | /user/login         | 公开     |
| 学生列表 | GET       | /student/list       | 教师、管理员 |
| 录入成绩 | POST      | /score              | 教师     |
| 考勤打卡 | POST      | /attendance/checkin | 教师     |
| 发布通知 | POST      | /notice             | 教师、管理员 |
| 聊天消息 | WebSocket | /app/chat.send      | 已登录    |

---

## ❓ 常见问题

### 1. 跨域问题

* 后端已配置 CORS
* 前端使用 Vite 代理

---

### 2. WebSocket 连接失败

* 检查后端是否启动
* 检查 `VITE_WS_URL` 配置

---

### 3. 学生无法查看数据

* 检查 `student.id` 与 `sys_user.id` 是否一致
* 检查数据隔离逻辑

---

## 📄 许可证

本项目仅供学习交流使用。

---

## 📬 联系方式

* 👤 作者：林敏聪
* 📧 邮箱：[3338928728@qq.com](mailto:3338928728@qq.com)

---

## ⭐ 如果觉得不错

欢迎 Star ⭐ 支持一下！
