# 拾光集 · 二手交易平台

> 基于 **Spring Boot 3 + MyBatis-Plus + MySQL + Redis** 的前后端分离二手交易系统

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.9-green)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.18-blue)
![Redis](https://img.shields.io/badge/Redis-3.2-red)

---

## 项目简介

拾光集是一个面向社会用户的二手物品交易平台，提供商品发布、浏览、详情查看、用户注册登录等核心能力。

项目采用**多模块 Maven 架构**，在保证代码分层清晰的同时，重点实践了两项后端性能优化：

- **MySQL 索引优化**：针对高频查询设计联合索引，10 万数据下查询耗时由 **29.6ms 降至 0.073ms**
- **Redis 缓存**：商品详情采用 Cache Aside 模式，命中缓存时**完全不访问数据库**

---

## 技术栈

| 分类 | 技术 | 版本 |
|---|---|---|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.2.9 |
| ORM | MyBatis-Plus | 3.5.18 |
| 数据库 | MySQL | 8.4 |
| 缓存 | Redis + Lettuce | 3.2.12 |
| 认证 | JWT (jjwt) | 0.12.6 |
| 密码加密 | BCrypt (spring-security-crypto) | 6.2.6 |
| 接口文档 | Knife4j (OpenAPI 3) | 4.5.0 |
| 工具库 | Hutool | 5.8.32 |

---

## 模块结构

```
second-hand-market
├── shm-common      公共模块：统一返回 Result、全局异常、常量、JWT 工具
├── shm-pojo        实体模块：Entity / DTO / VO / Query
├── shm-mapper      数据访问层：Mapper 接口
├── shm-service     业务逻辑层：Service 接口与实现
└── shm-web         控制器层：Controller + 启动入口
```

**依赖方向严格单向**：`web → service → mapper → pojo`

---

## 已实现功能

### 用户模块 ✅
- 用户注册（BCrypt 加密存储，绝不存明文）
- 用户登录（JWT 签发 + Redis 存储登录态）
- 获取当前用户信息
- 退出登录（删除 Redis 中的 token，立即失效）

### 商品模块 ✅
- 发布商品（事务保证商品与图片双表一致）
- 商品列表（分页 + 分类/关键词/价格区间筛选 + 排序）
- 商品详情（Redis 缓存 + 防穿透 + 防雪崩）

### 分类模块 ✅
- 两级分类树查询（一次查询 + 内存组装，避免 N+1）

### 基础设施 ✅
- 统一返回体 `Result<T>`
- 全局异常处理器（业务异常 / 参数校验 / 路径不存在 / 系统异常）
- 登录鉴权拦截器（可选登录，游客也能访问公开接口）
- 公共字段自动填充（创建时间 / 更新时间 / 逻辑删除）

### 开发中 🚧
- 游客浏览限流
- 商品浏览量统计
- 收藏 / 评论
- 订单模块

---

## 核心设计说明

### 1. 认证方案：JWT + Redis

JWT 本身是无状态的，但**纯 JWT 无法主动让 token 失效**（用户退出登录后，旧 token 在过期前仍可用）。

因此将 token 同时写入 Redis：

```
登录  → 签发 JWT → Redis 存 shm:auth:token:{userId}（TTL 7 天）
请求  → 拦截器解析 JWT 取 userId → 与 Redis 中的 token 比对 → 一致才放行
退出  → 删除 Redis key → token 立即失效
```

### 2. 缓存策略：Cache Aside

```
查缓存 → 命中则返回
       → 未命中则查库 → 组装 VO → 写回缓存 → 返回
```

| 问题 | 解决方案 |
|---|---|
| 缓存穿透（查不存在的商品） | 缓存空值标记 `NULL`，TTL 5 分钟 |
| 缓存雪崩（大量 key 同时过期） | 过期时间 = 30 分钟 + 随机 0~300 秒 |
| 缓存击穿（热点 key 失效） | 暂用基础方案，后续引入互斥锁 |

### 3. 索引设计

商品表核心索引：

```sql
KEY idx_seller (seller_id, create_time)              -- 我的发布
KEY idx_category_status (category_id, status, create_time)  -- 分类页列表
KEY idx_status_time (status, create_time)            -- 首页最新
```

**设计原则**：`ORDER BY` 的列放在联合索引最后一位，使排序直接利用索引有序性，避免 `Using filesort`。

---

## 性能实测数据

### 索引优化（10 万条商品数据）

| 场景 | 无索引 | 有索引 | 提升 |
|---|---|---|---|
| 分类列表查询 | 29.6 ms（全表扫描 + filesort） | **0.073 ms** | **约 400 倍** |
| COUNT 统计 | 21.7 ms（全表扫描） | **1.01 ms**（覆盖索引） | **约 21 倍** |

### Redis 缓存

| 场景 | 耗时 | 数据库查询 |
|---|---|---|
| 详情首次访问（未命中） | 约 5ms | 3 次 |
| 详情再次访问（命中） | **约 0.5ms** | **0 次** |

---

## 快速开始

### 环境要求

- JDK 17
- Maven 3.6+
- MySQL 8.x
- Redis 5.x 及以上

### 1. 初始化数据库

```bash
mysql -uroot -p < sql/schema.sql
mysql -uroot -p < sql/data.sql
```

> `sql/` 目录包含建表语句与初始数据，未纳入版本控制，请从项目维护者处获取。

### 2. 修改配置

编辑 `shm-web/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shiguangji?...
    username: root
    password: 你的密码
  data:
    redis:
      host: 你的 Redis 地址
      port: 6379
      password: 你的 Redis 密码
```

### 3. 启动

```bash
mvn clean package -DskipTests
java -jar shm-web/target/shm-web.jar
```

或在 IDEA 中直接运行 `ShmApplication`。

### 4. 访问接口文档

```
http://localhost:8080/doc.html
```

---

## 接口一览

| 方法 | 路径 | 说明 | 需要登录 |
|---|---|---|---|
| POST | /api/user/register | 用户注册 | 否 |
| POST | /api/user/login | 用户登录 | 否 |
| GET | /api/user/me | 当前用户信息 | 是 |
| POST | /api/user/logout | 退出登录 | 是 |
| GET | /api/category/tree | 分类树 | 否 |
| POST | /api/product | 发布商品 | 是 |
| GET | /api/product/list | 商品列表 | 否 |
| GET | /api/product/{id} | 商品详情 | 否 |

---

## 开发规范

本项目遵循《阿里巴巴 Java 开发手册（黄山版）》：

- 分层职责单一，Controller 不写业务逻辑
- 统一返回体与全局异常处理，异常堆栈不返回给前端
- 禁止魔法值，使用常量类统一管理
- 金额使用 `BigDecimal`，禁止 `double`
- Redis key 命名 `业务:对象:标识`，且必须设置过期时间
- 事务注解必须指定 `rollbackFor = Exception.class`

---

## 许可证

本项目仅供学习与毕业设计使用。
