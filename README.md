# 📝 面试题抽背系统

<p align="center">
  <strong>全栈面试题背诵/抽背管理平台 — 智能间隔重复算法，高效备战面试</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?logo=spring-boot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vue.js&logoColor=white" alt="Vue 3"/>
  <img src="https://img.shields.io/badge/Vite-5.2-646CFF?logo=vite&logoColor=white" alt="Vite"/>
  <img src="https://img.shields.io/badge/Element_Plus-2.7-409EFF?logo=element&logoColor=white" alt="Element Plus"/>
  <img src="https://img.shields.io/badge/ECharts-5.5-AA344D?logo=apache-echarts&logoColor=white" alt="ECharts"/>
  <img src="https://img.shields.io/badge/H2-Database-004B8D?logo=h2&logoColor=white" alt="H2"/>
  <img src="https://img.shields.io/badge/license-MIT-green" alt="License"/>
</p>

---

## 📋 目录

- [功能概览](#-功能概览)
- [技术栈](#-技术栈)
- [项目结构](#-项目结构)
- [快速开始](#-快速开始)
- [功能详解](#-功能详解)
  - [1. 智能间隔重复抽题](#1-智能间隔重复抽题)
  - [2. 题库管理](#2-题库管理)
  - [3. 复习反馈机制](#3-复习反馈机制)
  - [4. 收藏系统](#4-收藏系统)
  - [5. 错题集](#5-错题集)
  - [6. 学习诊断](#6-学习诊断)
  - [7. 标签管理](#7-标签管理)
  - [8. 数据备份与恢复](#8-数据备份与恢复)
  - [9. 导入导出](#9-导入导出)
- [API 接口文档](#-api-接口文档)
- [默认数据](#-默认数据)
- [常见问题](#-常见问题)
- [优化建议](#-优化建议)

---

## 🎯 功能概览

| 模块 | 说明 |
|------|------|
| **抽背首页** | 间隔重复加权抽题，快捷键操作（Enter/1/2/F/E），今日待复习概览 |
| **题库管理** | 增删改查、多条件组合筛选（关键词/分类/标签/掌握/收藏/无答案/高频/久未复习）、排序、批量操作 |
| **收藏题目** | 查看所有收藏题目，支持取消收藏、查看答案、一键跳转收藏题专项抽背 |
| **错题集** | 按错题次数从高到低排列所有未掌握题目，一键跳转错题专项抽背 |
| **学习诊断** | ECharts 图表：分类掌握率、7 天复习趋势、错题增长趋势、弱项排行、错题排行榜 |
| **备份管理** | 自动备份 + 手动快照 + 一键恢复，支持从任意备份还原题库和标签 |
| **标签系统** | 系统预定义标签 + 用户自定义标签，支持批量给题目添加/清除标签 |
| **Markdown 答案** | 富文本答案编辑器（H1-H3/粗体/列表/代码块/链接），Markdown 渲染展示 |
| **导入导出** | 支持 JSON 格式导入导出题库数据 |

---

## 🛠 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| **Java** | 17 | 开发语言 |
| **Spring Boot** | 3.2.5 | 应用框架 |
| **Spring Data JPA** | — | ORM 与持久化 |
| **H2 Database** | — | 内嵌文件数据库，零配置 |
| **Lombok** | — | 简化 Java 代码 |
| **Jackson** | — | JSON 序列化与备份导出 |
| **Spring Validation** | — | 请求参数校验 |
| **Spring Scheduling** | — | 定时备份任务 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| **Vue 3** | 3.4 | 前端框架（Composition API） |
| **Vite** | 5.2 | 构建工具 |
| **Vue Router** | 4.3 | 前端路由 |
| **Element Plus** | 2.7 | UI 组件库 |
| **Axios** | 1.6 | HTTP 请求库 |
| **ECharts** | 5.5 | 数据可视化图表 |
| **markdown-it** | 14.2 | Markdown 渲染 |
| **DOMPurify** | 3.4 | HTML 安全过滤 |

---

## 📁 项目结构

```
interview-system/
├── backend/                          # Spring Boot 后端 (2380 行 Java)
│   ├── pom.xml                       # Maven 依赖配置
│   └── src/main/
│       ├── java/com/interview/
│       │   ├── InterviewApplication.java      # 启动入口
│       │   ├── common/
│       │   │   ├── Result.java                # 统一响应封装
│       │   │   ├── GlobalExceptionHandler.java # 全局异常处理
│       │   │   ├── DataInitializer.java       # 首次启动数据初始化 + 备份恢复
│       │   │   ├── BackupShutdownHandler.java # 关闭时自动备份
│       │   │   └── ScheduledBackupTask.java   # 定时备份任务
│       │   ├── controller/
│       │   │   ├── QuestionController.java    # 题目管理 API（/api/questions）
│       │   │   └── TagController.java         # 标签管理 API（/api/tags）
│       │   ├── dto/
│       │   │   ├── QuestionDTO.java           # 题目请求/响应 DTO
│       │   │   ├── ReviewResultDTO.java       # 复习反馈 DTO
│       │   │   └── backup/
│       │   │       ├── QuestionsBackup.java   # 题目备份数据结构
│       │   │       └── TagsBackup.java        # 标签备份数据结构
│       │   ├── entity/
│       │   │   ├── Question.java              # 题目实体（question_bank 表）
│       │   │   └── TagConfig.java             # 标签配置实体（tag_config 表）
│       │   ├── repository/
│       │   │   ├── QuestionRepository.java    # 题目数据访问层
│       │   │   └── TagConfigRepository.java   # 标签数据访问层
│       │   ├── service/
│       │   │   ├── QuestionService.java       # 题目服务接口
│       │   │   ├── BackupService.java         # 备份服务（快照/恢复/导出）
│       │   │   └── impl/
│       │   │       └── QuestionServiceImpl.java # 题目服务实现（间隔重复算法）
│       │   └── vo/
│       │       ├── StatisticsVO.java          # 统计视图对象（含趋势/弱项/分类掌握率）
│       │       ├── WrongRankItem.java         # 错题排行子项
│       │       ├── RecentReviewItem.java      # 最近复习子项
│       │       ├── BackupInfoVO.java          # 备份文件信息
│       │       └── ReviewOverviewVO.java      # 今日复习概览
│       └── resources/
│           └── application.yml                # 应用配置文件
│
├── frontend/                         # Vue 3 前端
│   ├── index.html                    # HTML 入口
│   ├── package.json                  # 前端依赖
│   ├── vite.config.js                # Vite 配置（含 API 代理）
│   └── src/
│       ├── main.js                   # 应用入口（注册 Element Plus + 图标）
│       ├── App.vue                   # 根组件（侧边栏导航 + keep-alive 路由出口）
│       ├── api/
│       │   └── index.js              # API 接口层（Axios 封装 + 拦截器）
│       ├── router/
│       │   └── index.js              # 路由配置（6 个页面）
│       ├── components/
│       │   ├── AnswerEditor.vue      # Markdown 富文本编辑器
│       │   └── MarkdownView.vue      # Markdown 渲染组件（代码块美化）
│       └── views/
│           ├── Home.vue              # 抽背首页（间隔重复 + 快捷键）
│           ├── QuestionBank.vue      # 题库管理（多维度筛选 + 排序）
│           ├── Favorites.vue         # 收藏题目
│           ├── Unmastered.vue        # 错题集
│           ├── Statistics.vue        # 学习诊断（ECharts 图表）
│           └── BackupManagement.vue  # 备份管理（快照/恢复）
│
├── sync-to-github.sh                 # 数据同步脚本
└── README.md                         # 项目文档
```

---

## 🚀 快速开始

### 环境要求

| 工具 | 最低版本 | 说明 |
|------|----------|------|
| **JDK** | 17+ | 后端运行环境 |
| **Maven** | 3.6+ | 后端构建工具 |
| **Node.js** | 18+ | 前端构建工具 |
| **npm** | 9+ | 前端包管理器 |

> **注意**：✅ H2 数据库为内嵌文件数据库，无需额外安装 MySQL/PostgreSQL。

### 第一步：启动后端

```bash
# 进入后端目录
cd interview-system/backend

# 编译并启动（首次启动会自动建表并插入 27 道默认面试题）
mvn spring-boot:run

# 或者先打包再运行
mvn clean package -DskipTests
java -jar target/interview-system-1.0.0.jar
```

后端启动后：
- API 服务地址：`http://localhost:8080`
- H2 数据库控制台：`http://localhost:8080/h2-console`（JDBC URL: `jdbc:h2:file:./data/interview`，用户名 `sa`，无密码）
- 数据文件位置：`backend/data/interview.mv.db`（自动创建，程序退出后数据不丢失）

### 第二步：启动前端

```bash
# 进入前端目录
cd interview-system/frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

前端启动后：
- 访问 `http://localhost:3000`
- Vite 自动将 `/api` 路径代理到后端 `http://localhost:8080`

### 一键启动

```bash
# 终端 1 — 启动后端
cd interview-system/backend && mvn spring-boot:run

# 终端 2 — 启动前端
cd interview-system/frontend && npm install && npm run dev
```

---

## 📖 功能详解

### 1. 智能间隔重复抽题

系统采用**间隔重复加权随机算法（Spaced Repetition）**，不同题目有不同的被抽中概率：

**动态权重计算**（`spacedRepetitionWeight`）：
- 基础权重 × 难度提升（错题次数 × 0.32） × 复习提升（log(复习次数) × 0.12）
- **已掌握题**：按复习间隔衰减——刚掌握的题短期压制（12 小时内几乎不出现），到期后随 overdue 程度线性提升
- **未掌握题**：错题次数越多权重越高，30 分钟内短期压制避免连续抽中
- **收藏题**：额外 × 1.15 提升

**复习间隔策略**：
- 目标间隔 = 1.0 + 答对次数 × 1.8 + 总复习 × 0.25 − 答错次数 × 0.8（1~30 天）
- 未掌握题：最短 30 分钟即可再次出现
- 高风险题：错题 ≥ 3 次 / 权重 ≥ 30 / 未掌握且错题率 ≥ 50%

**快捷键支持**：
| 快捷键 | 功能 |
|--------|------|
| `Enter` | 随机抽题 / 下一题 |
| `1` | 标记「掌握了」 |
| `2` | 标记「没掌握」 |
| `F` | 收藏/取消收藏 |
| `E` | 编辑答案 |

### 2. 题库管理

- **多维度筛选**：关键词（搜索问题+答案）、分类、标签、掌握状态、收藏状态
- **快速筛选按钮**：无答案、高频标签、久未复习
- **排序支持**：默认/错题次数/复习次数/最近复习/权重，升降序可切换
- **CRUD 操作**：新增、编辑、详情查看、删除、批量删除
- **分页浏览**：支持 10/20/50/100 条每页

### 3. 复习反馈机制

| 操作 | 效果 |
|------|------|
| 点击「掌握了」 | 标记为已掌握 ✅，正确次数 +1，权重 -（2 + 连续正确次数/2），展示答案 |
| 点击「没掌握」 | 标记为未掌握 ❌，错误次数 +1，权重 +（6 + 错误数×2），**自动收藏**，展示答案 |

- 回答后自动展示 Markdown 渲染的参考答案
- 支持在答案区直接**补充或修改答案**（富文本编辑器）
- 记录每次复习时间和累计复习次数
- 右侧面板实时展示复习概览（今日待复习/高风险/长期未复习）

### 4. 收藏系统

- 题目可收藏/取消收藏
- 收藏页面汇总所有已收藏题目
- 支持一键进入「收藏题专项抽背」模式
- 「没掌握」的题目会被自动收藏

### 5. 错题集

- 展示所有未掌握的题目，按错题次数**从高到低**排列
- 每个题目显示：错题次数、复习次数、当前权重、是否已收藏
- 支持一键进入「错题专项抽背」模式

### 6. 学习诊断

使用 ECharts 提供多维度可视化分析：
- **分类掌握率**：横向柱状图，按分类展示已掌握 vs 未掌握比例
- **整体掌握情况**：环形饼图展示已掌握/未掌握占比
- **7 天复习趋势**：折线图展示每日复习题数 + 错题增长趋势
- **弱项排行**：雷达图/排行榜展示薄弱分类（综合掌握率和错题次数计算风险分数）
- **错题排行榜**：Top 10 高频错题
- **最近复习记录**：最近 10 条复习记录时间线

### 7. 标签管理

- **系统预置标签**：高频、重点、易错、必背、项目相关、八股文、手撕代码、原理题、场景题
- **用户自定义标签**：支持新增和删除
- **批量添加标签**：选中多道题目后一键批量添加标签
- **删除标签**：删除标签时会自动清除所有题目中的该标签
- **空白标签筛选**：快速定位未打标签的题目

### 8. 数据备份与恢复

系统内置完整的**数据备份、快照和恢复机制**：

**自动备份**：
| 触发方式 | 说明 |
|----------|------|
| **关闭备份** | 应用正常关闭时（Ctrl+C），自动导出到 `data/backup/` |
| **定时备份** | 每 30 分钟自动导出一次（防止意外崩溃丢失数据） |
| **手动快照** | 前端备份管理页面一键创建带时间戳的快照 |

**备份文件结构**：
```
data/backup/
├── questions.json              # 当前自动备份（题目 + 复习数据 + 权重）
├── tags.json                   # 当前自动备份（标签配置）
└── snapshots/
    ├── 20260603-155027-094/    # 手动快照 1
    │   ├── questions.json
    │   └── tags.json
    └── 20260603-163632-521/    # 手动快照 2
        ├── questions.json
        └── tags.json
```

**数据恢复**：
- **自动恢复**：删除数据库文件后重启应用，自动从 JSON 备份恢复数据
- **手动恢复**：前端备份管理页面选择任意备份一键恢复（需二次确认）

**同步到 GitHub**：
```bash
# 将备份数据提交并推送到 GitHub
./sync-to-github.sh

# 自定义 commit 信息
./sync-to-github.sh "feat: 新增 10 道 Redis 面试题"
```

### 9. 导入导出

| 功能 | 格式 | 说明 |
|------|------|------|
| 导出 | JSON | 导出全部题目数据（含掌握状态、复习记录、权重） |
| 导入 | JSON | 批量导入题目，自动跳过重复题目 |

导入 JSON 格式示例：
```json
[
  {
    "question": "什么是面向对象编程的三大特性？",
    "answer": "封装、继承、多态...",
    "category": "Java 基础",
    "tags": "高频,重点,八股文",
    "weight": 10,
    "mastered": false,
    "favorite": false
  }
]
```

---

## 📡 API 接口文档

所有接口前缀：`/api`

### 题目管理 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/questions` | 新增题目 |
| `PUT` | `/questions/{id}` | 修改题目 |
| `DELETE` | `/questions/{id}` | 删除题目 |
| `DELETE` | `/questions/batch` | 批量删除（Body: `[1,2,3]`） |
| `GET` | `/questions/{id}` | 查询题目详情 |
| `GET` | `/questions` | 分页查询（参数：page, size, keyword, category, tag, mastered, favorite, emptyTag, noAnswer, hotTag, longUnreviewed, staleDays, sortBy, sortDir） |

### 抽题 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/questions/random` | 加权随机抽题（参数：mode=[all\|favorites\|unmastered\|dueToday], category） |
| `GET` | `/questions/due-today` | 今日应复习题目列表（分页） |
| `GET` | `/questions/review-overview` | 首页复习概览（今日待复习/高风险/长期未复习数量） |

### 复习反馈 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/questions/{id}/review` | 提交复习结果（Body: `{"mastered": true/false}`） |

### 收藏 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/questions/{id}/favorite` | 切换收藏状态 |
| `GET` | `/questions/favorites` | 收藏题目列表（分页） |

### 答案管理 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `PUT` | `/questions/{id}/answer` | 补充/修改答案（Body: `{"answer": "..."}`） |

### 未掌握 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/questions/unmastered` | 未掌握题目列表（分页，按错题次数降序） |

### 统计 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/questions/statistics` | 获取统计信息（总数、掌握率、分类分布、趋势、错题排行等） |

### 导入导出 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/questions/export/json` | 导出全部题目为 JSON |
| `POST` | `/questions/import/json` | 从 JSON 批量导入题目 |

### 分类与标签 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/questions/categories` | 获取所有分类列表 |
| `GET` | `/questions/tags` | 获取所有标签列表 |

### 备份管理 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/questions/backup/list` | 获取所有备份列表（自动备份 + 手动快照） |
| `POST` | `/questions/backup/create` | 创建手动备份快照 |
| `POST` | `/questions/backup/{backupId}/restore` | 从指定备份恢复题库和标签 |
| `POST` | `/questions/backup/restore` | 从当前自动备份恢复数据 |

### 标签管理 `/api/tags`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/tags` | 获取所有标签 |
| `POST` | `/tags` | 新增标签 |
| `DELETE` | `/tags/{id}` | 删除标签（同时清除所有题目中的该标签） |
| `DELETE` | `/tags/name/{name}` | 按名称删除标签 |
| `POST` | `/tags/batch-add` | 批量给题目添加标签 |
| `GET` | `/tags/empty-tag-questions` | 获取所有未打标签的题目 |

### 统一响应格式

```json
{
  "code": 200,
  "message": "成功",
  "data": { ... }
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 500 | 服务器错误 |

---

## 📚 默认数据

首次启动时，系统会自动初始化 **27 道** 面试题和 **9 个** 默认标签。

### 默认题目覆盖

| 分类 | 题目数 |
|------|--------|
| Java 基础 | 5 题 |
| JVM | 3 题 |
| Java 并发 | 3 题 |
| MySQL | 2 题 |
| Redis | 2 题 |
| Spring | 3 题 |
| Spring Boot | 1 题 |
| MyBatis | 1 题 |
| 分布式 | 3 题 |
| 消息队列 | 1 题 |
| 计算机网络 | 2 题 |
| 操作系统 | 1 题 |
| 算法 | 1 题 |
| 项目面试 | 1 题 |
| HR 面试 | 1 题 |

### 默认标签

`高频` `重点` `易错` `必背` `项目相关` `八股文` `手撕代码` `原理题` `场景题`

---

## ❓ 常见问题

<details>
<summary><strong>Q: 需要安装 MySQL 或 PostgreSQL 吗？</strong></summary>
不需要。本系统使用 H2 内嵌文件数据库，数据持久化在 <code>backend/data/</code> 目录下的文件中，无需额外安装任何数据库。
</details>

<details>
<summary><strong>Q: 如何重置数据？</strong></summary>

删除数据库文件后重启即可：
```bash
rm -rf interview-system/backend/data/interview.mv.db
# 重启应用，会自动从备份恢复或初始化默认数据
```
</details>

<details>
<summary><strong>Q: 前端端口被占用怎么办？</strong></summary>
修改 <code>frontend/vite.config.js</code> 中的 <code>server.port</code> 为其他端口即可。
</details>

<details>
<summary><strong>Q: 后端端口被占用怎么办？</strong></summary>
修改 <code>backend/src/main/resources/application.yml</code> 中的 <code>server.port</code> 配置项，同时记得同步修改 <code>frontend/vite.config.js</code> 中的代理目标端口。
</details>

<details>
<summary><strong>Q: 如何打包部署到生产环境？</strong></summary>

```bash
# 后端打包
cd interview-system/backend
mvn clean package -DskipTests
# 产物在 target/interview-system-1.0.0.jar

# 前端打包
cd interview-system/frontend
npm run build
# 产物在 dist/ 目录，可部署到 Nginx 等静态服务器
```
</details>

<details>
<summary><strong>Q: 支持切换其他数据库吗？</strong></summary>
支持。Spring Data JPA 可以无缝切换，只需在 <code>application.yml</code> 中修改数据源配置并添加对应的 JDBC 驱动依赖即可。例如切换到 MySQL 只需添加 <code>mysql-connector-j</code> 依赖并修改 URL、用户名和密码。
</details>

---

## 🔧 优化建议

以下是从代码审查中识别的优化机会，按优先级排列：

### 🟡 P1 — 性能优化

| # | 问题 | 位置 | 影响 | 建议 |
|---|------|------|------|------|
| 1 | **全表查询 + 内存过滤** | `QuestionServiceImpl.java` 多个方法 (`randomQuestion`, `dueToday`, `reviewOverview`, `allCategories`, `allTags`) | 题库超过 1000 题后性能显著下降 | 将过滤逻辑下推到数据库层，使用 JPA Specification 或 @Query |
| 2 | **缺失数据库索引** | `Question.java` 实体 | 全表扫描，查询慢 | 在 `category`, `mastered`, `favorite`, `wrongCount`, `lastReviewTime` 上添加 `@Index` |
| 3 | **标签存储为逗号分隔字符串** | `Question.tags` 字段 | LIKE 查询无法走索引 | 建议引入中间表 `question_tag` 做多对多关联，或用 H2 全文索引 |
| 4 | **标签操作全量加载** | `TagController.java` `removeTagFromAllQuestions`, `emptyTagQuestions`, `list` | 每次标签操作加载全部题目 | 使用 JPQL UPDATE 批量更新，或用 @Query 投影查询 |

### 🟡 P2 — 前端优化

| # | 问题 | 位置 | 影响 | 建议 |
|---|------|------|------|------|
| 5 | **Element Plus 全量图标注册** | `main.js` — `import * as ElementPlusIconsVue` | 打包体积增大 ~500KB | 使用 `unplugin-icons` 或手动按需引入图标组件 |
| 6 | **Element Plus 全量 CSS 导入** | `main.js` — `import 'element-plus/dist/index.css'` | 首次加载大 | 使用 `unplugin-element-plus` 实现组件级别按需导入 |
| 7 | **ECharts 全量引入** | `Statistics.vue` | ECharts ~1MB | 按需引入图表类型（`echarts/core` + 所需 chart/pie/line） |
| 8 | **缺失路由懒加载** | `router/index.js` | 首次 JS 加载体积大 | 已使用动态 `import()` ✅，但可添加加载状态/骨架屏 |

### 🟢 P3 — 健壮性

| # | 问题 | 位置 | 影响 | 建议 |
|---|------|------|------|------|
| 9 | **CORS 全开放** | `QuestionController.java`, `TagController.java` — `@CrossOrigin(origins = "*")` | 安全风险 | 改为配置 CORS 白名单，或在 `application.yml` 统一配置 |
| 10 | **缺失生产环境配置** | `application.yml` | 开发配置用于生产 | 添加 `application-prod.yml`（关闭 H2 console、调整 JPA ddl、配置日志级别） |
| 11 | **异常类型单一** | 全项目使用 `IllegalArgumentException` | 无法区分业务异常和系统异常 | 引入 `NotFoundException`, `BusinessException` 等自定义异常 |
| 12 | **缺失单元测试** | 整个项目 | 重构风险高、回归困难 | 添加 `QuestionServiceImplTest`（算法测试）、`BackupServiceTest` 等 |

### 🔵 P4 — 代码质量

| # | 问题 | 位置 | 影响 | 建议 |
|---|------|------|------|------|
| 13 | **StatisticsVO 过于庞大** | `StatisticsVO.java` — 含 4 个内部类 | 单一文件 70+ 行 | 将 `CategoryMasteryItem`, `TrendItem`, `WeakCategoryItem` 提取为独立 VO 文件 |
| 14 | **重复的分页包装逻辑** | `QuestionController.java` — 6 个方法都有相同的 `Map.of(...)` 模板 | 代码冗余 | 提取 `wrapPage(Page<Question>, page)` 私有方法 |
| 15 | **TagController 直接操作 Repository** | `TagController.java` | 违反分层原则（Controller 直接访问数据层） | 增加 `TagService` 中间层 |
| 16 | **`BackupService` 的 `restoreBackup` 和 `restoreFromBackup` 逻辑重复** | `BackupService.java` 两个恢复方法逻辑高度相似 | 维护成本 | 合并为一个方法，通过参数控制是否清空已有数据 |

### ⚪ P5 — 体验优化

| # | 问题 | 位置 | 影响 | 建议 |
|---|------|------|------|------|
| 17 | **统计页面每次切换都重新渲染图表** | `Statistics.vue` — `keep-alive` 下仍可能重新初始化 | 切换标签页时闪烁 | 使用 `v-show` 替代 `v-if` 或添加图表实例复用逻辑 |
| 18 | **"今日待复习"判断依赖 `randomQuestion` 先调用 `dueToday` 检查** | `Home.vue` `nextQuestion()` 方法 | 不必要的网络请求 | 从 `reviewOverview.dueTodayCount` 直接判断 |
| 19 | **缺少全局 Loading 和请求去重** | `api/index.js` | 重复点击导致重复请求 | 添加请求取消令牌 + 防重复提交指令 |

---

## 📄 License

MIT License

---

<p align="center">
  <strong>⭐ 如果这个项目对你有帮助，请给一个 Star！</strong>
</p>
