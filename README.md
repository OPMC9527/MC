家校协同学生信息管理系统
项目简介
本系统是一个基于 Spring Boot + Vue 3 的前后端分离家校协同平台，旨在解决传统家校沟通（家长会、电话、短信）信息滞后、渠道分散等问题。系统整合了学生管理、成绩管理、考勤管理、通知公告、实时聊天等功能，支持管理员、教师、学生、家长四种角色，并实现了细粒度的权限控制与数据隔离。

技术栈
后端
Spring Boot 3.2.4

MyBatis 3.0.3

MySQL 8.0

JWT (JJWT 0.11.5)

Spring Security 6.2.4

WebSocket (STOMP)

Maven 3.8.8

前端
Vue 3.4

Vite 5.0

Element Plus 2.6

Pinia (状态管理)

Axios

StompJS + SockJS (WebSocket)

核心功能
角色	功能
管理员	用户管理（增删改查、重置密码、禁用/启用）、班级管理、全局统计
教师	学生管理、成绩管理（录入/修改/删除）、考勤管理（打卡/修改/删除）、通知发布、在线聊天（单聊/群聊）
学生	查看个人成绩、考勤、通知，与教师在线聊天
家长	查看关联孩子的成绩、考勤、通知，与教师聊天
系统亮点
前后端分离：Vue + Spring Boot 独立开发部署

JWT 无状态认证：安全可靠，支持分布式

权限控制：自定义拦截器实现 RBAC，学生仅允许 GET 请求

数据隔离：学生只看自己，家长看自己孩子，教师看自己班级

实时聊天：WebSocket + STOMP，支持单聊、群聊、未读计数

工作台统计：根据角色动态展示统计数据

快速开始
环境要求
JDK 17+

Node.js 16+

MySQL 8.0

Maven 3.6+

1. 克隆项目
bash
git clone <repository-url>
cd edulink-system
2. 数据库初始化
bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE edulink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构及测试数据
mysql -u root -p edulink < database/init.sql
mysql -u root -p edulink < database/sample_data.sql
3. 后端启动
bash
cd edulink-backend
mvn clean compile
mvn spring-boot:run
后端运行在 http://localhost:8080/api

4. 前端启动
bash
cd edulink-frontend
npm install
npm run dev
前端运行在 http://localhost:3000

5. 测试账号
角色	用户名	密码
管理员	admin	123456
教师	teacher001	123456
学生	student001	123456
家长	parent001	123456
项目结构
text
edulink-backend/
├── src/main/java/com/edulink
│   ├── controller/      # REST API
│   ├── service/         # 业务逻辑
│   ├── mapper/          # MyBatis 数据访问
│   ├── entity/          # 实体类
│   ├── dto/             # 数据传输对象
│   ├── vo/              # 视图对象
│   ├── config/          # 配置类（WebSocket、CORS、拦截器）
│   ├── interceptor/     # 权限拦截器
│   └── utils/           # 工具类（JWT、UserContext、Result）
└── src/main/resources/
    ├── application.yml  # 配置文件
    └── mapper/          # XML 映射文件

edulink-frontend/
├── src/
│   ├── api/             # 接口封装
│   ├── views/           # 页面组件
│   ├── components/      # 公共组件
│   ├── router/          # 路由配置
│   ├── store/           # Pinia 状态管理
│   └── utils/           # 工具（axios、websocket）
├── index.html
├── package.json
└── vite.config.js
主要接口示例
功能	方法	路径	权限
登录	POST	/user/login	公开
学生列表	GET	/student/list	教师、管理员
录入成绩	POST	/score	教师
打卡	POST	/attendance/checkin	教师
发布通知	POST	/notice	教师、管理员
发送聊天消息	WebSocket	/app/chat.send	已登录用户
常见问题
1. 跨域问题
后端已配置 CorsConfig，前端使用代理（vite.config.js 中已配置）

2. WebSocket 连接失败
检查后端是否启动，前端环境变量 VITE_WS_URL 是否正确

3. 学生无法看到成绩/考勤
确认数据库 student 表中该学生的 id 与 sys_user.id 一致

检查后端 ScoreServiceImpl.listScores 数据隔离逻辑

许可证
本项目仅供学习交流使用。

联系方式
作者：林敏聪

邮箱：3338928728@qq.com
