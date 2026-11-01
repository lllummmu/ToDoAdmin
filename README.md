# TodoApp / TodoAdmin —— Vue3 全栈任务管理系统

课程设计升级版：在原有「Vue3 CDN + Servlet + MySQL」课程项目基础上，新增工程化前端
**todo-admin**（Vite + Vue3 + TypeScript + Element Plus + Pinia + Vue Router），
后端在不破坏旧页面的前提下新增 `/api/*` REST JSON 接口层，并支持 SSE 实时推送与文件上传。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 前端（工程化） | Vue 3 Composition API、TypeScript、Vite、Element Plus、Pinia、Vue Router、Axios、ECharts、marked |
| 前端（课程版） | Vue 3 CDN、Bootstrap 5、Axios（保留原样，可直接提交作业） |
| 后端 | Java 8、Servlet 4.0、JDBC、Gson |
| 数据库 | MySQL 8（todo_app 库，users / tasks 表，utf8mb4 + InnoDB） |
| 部署 | Tomcat 8.5（端口 8081）+ Vite Dev Server（端口 5173） |

## 功能清单

- 登录 / 注册 / 退出，HttpSession 会话认证，路由守卫自动恢复登录态
- 任务增删改查：内容、优先级（高/中/低）、截止时间、执行人指派、备注、附件
- 关键词 + 状态 + 优先级叠加筛选，截止/优先级/创建时间排序，分页
- 统计面板：总任务、已完成、未完成、即将到期（24h 内）、已过期、个人完成率
- ECharts 看板：完成情况环形图、优先级分布柱状图
- 任务备注 Markdown 编辑与实时预览（marked 解析，HTML 转义防注入）
- 附件上传（10MB 内），图片附件在线预览，其他类型下载
- SSE 实时推送：任何任务增删改，所有在线页面收到通知并自动刷新
- 过期任务整行标红，已完成任务划线置灰

## 快速启动

### 1. 数据库

```bash
mysql -u root -p < sql/todo_app.sql
```

脚本会创建 `todo_app` 库、`users`/`tasks` 表和测试账号
**admin / alice / bob（密码均为 123456）**。
数据库连接配置在 `src/com/todoapp/util/DBUtil.java`。

### 2. 后端（Tomcat 8081）

```bat
scripts\deploy.bat
```

脚本完成两件事：编译 `src/` 到 `web/WEB-INF/classes`，并把 `web/` 镜像同步到
`D:\apache-tomcat-8.5.16\deploy\TodoApp`（Tomcat 上下文描述符
`conf/Catalina/localhost/TodoApp.xml` 指向该目录，因 Tomcat 会忽略 appBase 内的 docBase）。

随后启动 Tomcat：`D:\apache-tomcat-8.5.16\bin\startup.bat`。
课程版页面：http://localhost:8081/TodoApp/ （登录页）。

### 3. 前端（Vite 5173）

```bash
cd todo-admin
npm install
npm run dev
```

打开 http://localhost:5173/ ，用 admin / 123456 登录。
开发服务器把 `/api`、`/uploads` 代理到 `http://localhost:8081/TodoApp`，
并在 `vite.config.ts` 中改写 JSESSIONID 的 Path，保证代理后会话有效。

生产构建：`npm run build`（产物在 `todo-admin/dist/`）。

## API 一览（/api/* 统一返回 `{code, msg, data}`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /api/login | 登录（JSON：username/password），返回用户 id 与用户名 |
| POST | /api/register | 注册 |
| POST | /api/logout | 退出 |
| GET | /api/me | 当前登录用户（会话恢复） |
| GET | /api/users | 用户列表（指派下拉框） |
| GET | /api/tasks | 当前用户创建或被指派的任务 |
| POST | /api/tasks | 新建任务（JSON） |
| PUT | /api/tasks/{id} | 局部更新（completed/text/priority/dueDate/assigneeId/remark/attachment） |
| DELETE | /api/tasks/{id} | 删除任务 |
| POST | /api/upload | multipart 上传（字段名 file），返回可访问 URL |
| GET | /api/events | SSE 订阅，任务变更推送 `task-changed` 事件 |

旧版表单接口（/login、/register、/tasks、/users）保留原行为，供课程版页面使用。

## 目录结构

```
TodoApp/
├── src/com/todoapp/          # 后端源码（controller/dao/model/util）
│   └── controller/api/       # 新增 REST API 层（Auth/Task/User/Upload/Sse）
├── web/                      # 课程版前端（login/register/index.html）+ WEB-INF
├── sql/todo_app.sql          # 数据库初始化脚本（含测试数据）
├── scripts/deploy.bat        # 后端一键编译部署脚本
└── todo-admin/               # 工程化前端
    ├── src/api/              # axios 封装 + 类型化接口
    ├── src/store/            # Pinia：user（认证）、task（任务与统计）
    ├── src/router/           # Vue Router + 登录守卫
    ├── src/components/       # TaskStats/StatsCharts/TaskFilter/UserSelect/
    │                         # TodoItem/TaskEditDialog/MarkdownView
    ├── src/composables/      # useSse（EventSource 封装）
    └── src/views/            # LoginView/RegisterView/TodoListView
```

## 本次升级修复的历史问题

1. 旧版 `TaskServlet.doPut` 为空实现且 `/tasks` 映射不支持 `/tasks/{id}`，
   导致"完成"按钮永远失败 —— 已补实现并增加 `/tasks/*` 映射。
2. 旧版前端 `getUserId()` 从任务列表"猜"当前用户 id —— 新接口登录即返回真实用户 id。
3. JDBC 连接 `serverTimezone=UTC` 导致时间偏差 —— 调整为 Asia/Shanghai 并显式 UTF-8。
