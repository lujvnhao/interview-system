# 📝 面试题抽背系统

<p align="center">
  <strong>一个全栈面试题背诵/抽背管理平台 — 智能加权抽题，高效备战面试</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?logo=spring-boot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vue.js&logoColor=white" alt="Vue 3"/>
  <img src="https://img.shields.io/badge/Vite-5.2-646CFF?logo=vite&logoColor=white" alt="Vite"/>
  <img src="https://img.shields.io/badge/Element_Plus-2.7-409EFF?logo=element&logoColor=white" alt="Element Plus"/>
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
  - [1. 智能加权抽题](#1-智能加权抽题)
  - [2. 题库管理](#2-题库管理)
  - [3. 复习反馈机制](#3-复习反馈机制)
  - [4. 收藏系统](#4-收藏系统)
  - [5. 错题集](#5-错题集)
  - [6. 统计分析](#6-统计分析)
  - [7. 标签管理](#7-标签管理)
  - [8. 导入导出](#8-导入导出)
- [API 接口文档](#-api-接口文档)
- [默认数据](#-默认数据)
- [常见问题](#-常见问题)

---

## 🎯 功能概览

| 模块 | 说明 |
|------|------|
| **抽背首页** | 按范围（全部/收藏/未掌握）和分类随机抽题，标记掌握/没掌握后自动展示答案 |
| **题库管理** | 增删改查、多条件组合筛选（关键词/分类/标签/掌握状态/收藏状态）、分页浏览 |
| **收藏题目** | 查看所有收藏题目，支持取消收藏、查看答案；一键跳转收藏题专项抽背 |
| **错题集** | 按错题次数从高到低排列所有未掌握题目，一键跳转错题专项抽背 |
| **统计分析** | 掌握率饼图、分类分布柱状图、错题排行榜、最近复习记录 |
| **标签系统** | 系统预定义标签 + 用户自定义标签，支持批量给题目添加/清除标签 |
| **导入导出** | 支持 JSON 格式导入导出题库数据，提供导入模板下载 |

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
| **Jackson CSV** | — | CSV 导出支持 |
| **Spring Validation** | — | 请求参数校验 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| **Vue 3** | 3.4 | 前端框架（Composition API） |
| **Vite** | 5.2 | 构建工具 |
| **Vue Router** | 4.3 | 前端路由 |
| **Element Plus** | 2.7 | UI 组件库 |
| **Axios** | 1.6 | HTTP 请求库 |
| **ECharts** | 5.5 | 数据可视化图表 |

---

## 📁 项目结构

```
interview-system/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml                       # Maven 依赖配置
│   └── src/main/
│       ├── java/com/interview/
│       │   ├── InterviewApplication.java      # 启动入口
│       │   ├── common/
│       │   │   ├── Result.java                # 统一响应封装
│       │   │   ├── GlobalExceptionHandler.java # 全局异常处理
│       │   │   └── DataInitializer.java       # 首次启动数据初始化
│       │   ├── controller/
│       │   │   ├── QuestionController.java    # 题目管理 API（/api/questions）
│       │   │   └── TagController.java         # 标签管理 API（/api/tags）
│       │   ├── dto/
│       │   │   ├── QuestionDTO.java           # 题目请求/响应 DTO
│       │   │   └── ReviewResultDTO.java       # 复习反馈 DTO
│       │   ├── entity/
│       │   │   ├── Question.java              # 题目实体（question_bank 表）
│       │   │   └── TagConfig.java             # 标签配置实体（tag_config 表）
│       │   ├── repository/
│       │   │   ├── QuestionRepository.java    # 题目数据访问层
│       │   │   └── TagConfigRepository.java   # 标签数据访问层
│       │   ├── service/
│       │   │   ├── QuestionService.java       # 题目服务接口
│       │   │   └── impl/QuestionServiceImpl.java # 题目服务实现
│       │   └── vo/
│       │       ├── StatisticsVO.java          # 统计视图对象
│       │       ├── WrongRankItem.java         # 错题排行子项
│       │       └── RecentReviewItem.java      # 最近复习子项
│       └── resources/
│           └── application.yml                # 应用配置文件
│
└── frontend/                         # Vue 3 前端
    ├── index.html                    # HTML 入口
    ├── package.json                  # 前端依赖
    ├── vite.config.js                # Vite 配置（含代理）
    └── src/
        ├── main.js                   # 应用入口（注册 Element Plus）
        ├── App.vue                   # 根组件（侧边栏 + 路由出口）
        ├── api/
        │   └── index.js              # API 接口层（Axios 封装）
        ├── router/
        │   └── index.js              # 路由配置（5 个页面）
        └── views/
            ├── Home.vue              # 抽背首页
            ├── QuestionBank.vue      # 题库管理
            ├── Favorites.vue         # 收藏题目
            ├── Unmastered.vue        # 错题集
            └── Statistics.vue        # 统计分析
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

### 一键启动（两者同时）

```bash
# 终端 1 — 启动后端
cd interview-system/backend && mvn spring-boot:run

# 终端 2 — 启动前端
cd interview-system/frontend && npm install && npm run dev
```

---

## 📖 功能详解

### 1. 智能加权抽题

系统采用**加权随机算法**，不同题目有不同的被抽中概率：

```
有效权重 = 原始权重(1-100)
           × 0.3  （如果已掌握，降低抽中概率）
           × 1.3  （如果收藏了，提高抽中概率）
           → 不低于 1
```

- 每次回答"没掌握" → 权重 **+5**，下次更容易被抽到
- 每次回答"掌握了" → 权重 **-2**，逐渐减少出现频率（最低为 1）
- 支持三种抽题范围：全部题目 / 收藏题目 / 未掌握题目
- 支持按分类进一步筛选

### 2. 题库管理

- **多维度筛选**：关键词（搜索问题+答案）、分类、标签、掌握状态、收藏状态
- **空白标签筛选**：快速找出未打标签的题目，方便批量打标
- **CRUD 操作**：新增、编辑、详情查看、删除、批量删除
- **分页浏览**：支持 10/20/50/100 条每页

### 3. 复习反馈机制

| 操作 | 效果 |
|------|------|
| 点击「掌握了」 | 标记为已掌握 ✅，掌握次数 +1，权重 -2，展示答案 |
| 点击「没掌握」 | 标记为未掌握 ❌，错题次数 +1，权重 +5，**自动收藏**，展示答案 |

- 回答后自动展示标准答案
- 支持在答案区直接**补充或修改答案**
- 记录每次复习时间和累计复习次数
- 右侧面板实时展示复习概览统计

### 4. 收藏系统

- 题目可收藏/取消收藏
- 收藏页面汇总所有已收藏题目
- 支持一键进入「收藏题专项抽背」模式
- 「没掌握」的题目会被自动收藏

### 5. 错题集

- 展示所有未掌握的题目，按错题次数**从高到低**排列
- 每个题目显示：错题次数、复习次数、当前权重、是否已收藏
- 支持一键进入「错题专项抽背」模式

### 6. 统计分析

- **掌握情况**：环形饼图展示已掌握 vs 未掌握比例
- **分类分布**：柱状图展示各分类题目数量
- **错题排行榜**：Top 10 高频错题
- **最近复习记录**：最近 10 条复习记录时间线
- 顶部统计卡片：总题目、已掌握、未掌握、收藏数、今日复习数

### 7. 标签管理

- **系统预置标签**：高频、重点、易错、必背、项目相关、八股文、手撕代码、原理题、场景题
- **用户自定义标签**：支持新增和删除
- **批量添加标签**：选中多道题目后一键批量添加标签
- **删除标签**：删除标签时会自动清除所有题目中的该标签
- **空白标签筛选**：快速定位未打标签的题目

### 8. 导入导出

| 功能 | 格式 | 说明 |
|------|------|------|
| 导出 | JSON / CSV | 导出全部题目数据 |
| 导入 | JSON | 批量导入题目，自动跳过重复题目 |
| 导入模板 | JSON | 下载标准模板文件，按格式填写后导入 |

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
| `GET` | `/questions` | 分页查询（参数：page, size, keyword, category, tag, mastered, favorite） |

### 抽题 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/questions/random` | 加权随机抽题（参数：mode=[all\|favorites\|unmastered], category） |

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
| `GET` | `/questions/statistics` | 获取统计信息（总数、掌握率、分类分布、错题排行等） |

### 导入导出 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/questions/export/json` | 导出全部题目为 JSON |
| `POST` | `/questions/import/json` | 从 JSON 批量导入题目 |

### 分类 `/api/questions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/questions/categories` | 获取所有分类列表 |
| `GET` | `/questions/tags` | 获取所有标签列表 |

### 标签管理 `/api/tags`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/tags` | 获取所有标签（系统标签 + 题目中已有的） |
| `POST` | `/tags` | 新增标签 |
| `DELETE` | `/tags/{id}` | 删除标签（同时清除所有题目中的该标签） |
| `DELETE` | `/tags/name/{name}` | 按名称删除标签 |
| `POST` | `/tags/batch-add` | 批量给题目添加标签（Body: `{"questionIds": [...], "tagName": "..."}`） |
| `GET` | `/tags/empty-tag-questions` | 获取所有未打标签的题目 |

### 统一响应格式

```json
{
  "code": 200,
  "message": "成功",
  "data": { ... }
}
```

---

## 📚 默认数据

首次启动时，系统会自动初始化 **27 道** 面试题，覆盖以下分类：

| 分类 | 题目数 | 示例题目 |
|------|--------|----------|
| **Java 基础** | 5 题 | `==` vs `equals`、String/StringBuilder/StringBuffer、集合框架、HashMap 原理、ArrayList vs LinkedList |
| **JVM** | 3 题 | JVM 内存划分、垃圾回收算法、类加载与双亲委派 |
| **Java 并发** | 3 题 | 多线程创建方式、synchronized vs ReentrantLock、线程池参数 |
| **MySQL** | 2 题 | B+ 树索引原理、ACID 事务特性 |
| **Redis** | 2 题 | 五种数据类型、缓存穿透/击穿/雪崩 |
| **Spring** | 3 题 | IoC 与 AOP、事务传播行为、设计模式 |
| **Spring Boot** | 1 题 | 自动配置原理 |
| **MyBatis** | 1 题 | `#{}` vs `${}` |
| **分布式** | 3 题 | CAP 与 BASE、分布式锁实现、微服务 |
| **消息队列** | 1 题 | MQ 的作用与场景 |
| **计算机网络** | 2 题 | HTTP vs HTTPS、TCP 三次握手四次挥手 |
| **操作系统** | 1 题 | 进程 vs 线程 |
| **算法** | 1 题 | 快速排序与归并排序 |
| **项目面试** | 1 题 | 项目最大挑战（开放题） |
| **HR 面试** | 1 题 | 自我介绍 |

同时初始化 9 个默认标签：`高频` `重点` `易错` `必背` `项目相关` `八股文` `手撕代码` `原理题` `场景题`。

---

## ❓ 常见问题

<details>
<summary><strong>Q: 需要安装 MySQL 或 PostgreSQL 吗？</strong></summary>
不需要。本系统使用 H2 内嵌文件数据库，数据持久化在 <code>backend/data/</code> 目录下的文件中，无需额外安装任何数据库。
</details>

<details>
<summary><strong>Q: 如何重置数据？</strong></summary>
删除 <code>backend/data/</code> 目录后重新启动即可。系统会重新建表并插入默认面试题。
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

## 📄 License

MIT License

---

<p align="center">
  <strong>⭐ 如果这个项目对你有帮助，请给一个 Star！</strong>
</p>
