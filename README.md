# TechHub 技术社区

一个基于 **SpringBoot 3 + MyBatis-Plus + MySQL + Redis** 的技术问答/内容社区项目。

## 技术栈

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 17(编译目标) | 本机 21 也能编译 |
| SpringBoot | 3.2.5 | |
| MyBatis-Plus | 3.5.7 | |
| MySQL | 8.0+ | |
| Redis | 6+ | |
| Lombok / Validation | — | |

## 项目结构(分层)

```
TechHub
├── pom.xml
├── sql/schema.sql                 # 建库建表脚本
└── src/main
    ├── java/com/techhub
    │   ├── TechHubApplication.java      # 启动类
    │   ├── common                      # 通用层
    │   │   ├── Result.java             # 统一返回体
    │   │   ├── ResultCode.java         # 状态码枚举
    │   │   ├── PageResult.java         # 分页返回体
    │   │   └── exception
    │   │       ├── BusinessException.java
    │   │       └── GlobalExceptionHandler.java
    │   ├── config                      # 配置层
    │   │   ├── MybatisPlusConfig.java  # 分页插件
    │   │   └── MyMetaObjectHandler.java# 自动填充 created_at/updated_at
    │   ├── entity                      # 实体层(对应 t_ 表)
    │   ├── mapper                      # Mapper 层(继承 BaseMapper)
    │   ├── service                     # 业务层(后续按模块建)
    │   │   └── impl
    │   └── controller                  # 控制层
    └── resources
        ├── application.yml             # 通用配置
        ├── application-dev.yml         # 开发环境配置(数据库/Redis)
        └── mapper/                     # 自定义 SQL 的 XML(后续加)
```

## 一键启动本地环境(Docker)

需要先安装 Docker Desktop(Windows 上依赖 WSL2)。

```bash
cd D:/MyJavaProject/TechHub
docker compose up -d
```

这会自动:
- 启动 **MySQL 8**(端口 3306,账号 root / 密码 root,自动创建 `techhub` 库)
- 启动 **Redis 7**(端口 6379,无密码)
- 首次启动自动执行 `sql/schema.sql` 建表

停止:`docker compose down`(加 `-v` 会连数据一起删除)

## 如何运行

**方式一:IDEA(推荐,你本机没装 Maven)**

1. IDEA → File → Open → 选择 `TechHub` 目录(它会自动识别 Maven 工程)
2. 先执行 `sql/schema.sql` 建库建表(Navicat / DataGrip / 命令行都行)
3. 改 `application-dev.yml` 里的数据库账号密码
4. 右键 `TechHubApplication` → Run

**方式二:命令行(需先装 Maven 或用 IDEA 的 Maven)**

```bash
cd D:/MyJavaProject/TechHub
mvn spring-boot:run
```
