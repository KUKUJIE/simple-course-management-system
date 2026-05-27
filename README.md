# 学生选课管理系统

## 项目简介

本项目是一个面向高校课程管理场景的学生选课管理系统，前端基于 Vue 3 + Vite + Element Plus，后端基于 Spring Boot 2.6.13 + MyBatis-Plus + MySQL。

系统支持学生、教师、管理员三类角色，围绕核心业务链 `course -> course_section -> enrollment -> score` 完成课程目录、教学班、选课记录和成绩记录的分离管理。项目重点补齐了数据库实体关系、外键约束、索引、视图、存储过程、触发器和大数据测试数据，满足数据库课程项目的设计与实现要求。

## 项目地址

[GitHub 仓库](https://github.com/jrywzk/course-manage-system.git)

## 功能特点

- 前端基于 Vue 3 + Vite + Element Plus 构建，页面按学生、教师、管理员三类角色拆分。
- 后端基于 Spring Boot 2.6.13 + MyBatis-Plus 实现接口服务，默认端口为 `9090`。
- 数据库采用 MySQL，默认数据库名为 `sms`。
- 正式业务链采用 `course -> course_section -> enrollment -> score`，不再将成绩表兼作选课记录。
- 管理员负责学生、教师、课程、教学班、院系、专业、教室等基础数据维护。
- 学生围绕教学班完成选课、退课、已选课程查询和成绩查询。
- 教师围绕本人教学班查看学生名单，并完成成绩录入和修改。
- 前端通过路由守卫和本地角色信息控制页面访问，后端提供基础接口校验和统一业务返回结构。
- 数据库包含外键、唯一约束、索引、视图、存储过程、触发器和大规模测试数据，可支撑课程报告与答辩说明。

### 项目部分效果图

#### 登录页面

![登录页面](assets/README/登录页面.png)

#### 管理员系统

![管理员工作台](assets/README/管理员工作台.png)

![学生管理](assets/README/学生管理.png)

![教师管理](assets/README/教师管理.png)

![课程管理](assets/README/课程管理.png)

![教学班管理](assets/README/教学班管理.png)

![院系管理](assets/README/院系管理.png)

![专业管理](assets/README/专业管理.png)

![教室管理](assets/README/教室管理.png)

#### 学生系统

![学生工作台](assets/README/学生工作台.png)

![选课中心](assets/README/选课中心.png)

![我的课程](assets/README/我的课程.png)

![成绩查询](assets/README/成绩查询.png)

![个人信息](assets/README/个人信息.png)

#### 教师系统

![教师工作台](assets/README/教师工作台.png)

![教学班列表](assets/README/教学班列表.png)

![成绩管理](assets/README/成绩管理.png)

![教师个人信息](assets/README/教师个人信息.png)

### 技术栈

#### 后端技术

- Spring Boot 2.6.13
- MyBatis-Plus 3.5.9
- PageHelper 2.1.0
- Knife4j 4.0.0
- MySQL Connector/J
- Lombok
- Maven

#### 前端技术

- Vue 3.5.12
- Vite 5.4.10
- Element Plus 2.9.1
- Pinia 2.0.33
- Vue Router 4.4.5
- Axios 1.3.4
- ECharts 5.5.1
- Sass

## 开发视图

```mermaid
graph LR
    subgraph Login[登录模块]
        L[登录页面] --> |认证| R{角色判断}
    end

    subgraph StudentSystem[学生系统]
        R -->|学生| SD[学生工作台]
        SD --> SC[选课中心]
        SD --> SM[我的课程]
        SD --> SG[成绩查询]
        SD --> SP[个人信息]

        SC -->|按教学班选课| EN[选课记录]
        SM -->|退课| EN
        EN -->|成绩结果| SG
    end

    subgraph TeacherSystem[教师系统]
        R -->|教师| TD[教师工作台]
        TD --> TS[本人教学班]
        TS --> TL[学生名单]
        TL --> TG[成绩管理]
        TD --> TP[个人信息]
    end

    subgraph AdminSystem[管理员系统]
        R -->|管理员| AD[管理员工作台]
        AD --> AU[学生/教师管理]
        AD --> AC[课程管理]
        AD --> AS[教学班管理]
        AD --> AB[院系/专业/教室管理]

        AC --> AS
        AB --> AS
        AS --> EN
    end
```

### 页面说明

#### 学生模块

- 首页：展示学生当前选课、学分、成绩等概览信息。
- 选课中心：查看可选教学班，按教学班完成选课和退课。
- 我的课程：查看当前已选教学班、课程、教师、上课时间和选课状态。
- 成绩查询：查看平时成绩、考试成绩、总评成绩、绩点和通过状态。
- 个人信息：查看和维护个人基础信息。

#### 教师模块

- 首页：展示本人教学班、学生人数、成绩录入进度等概览信息。
- 课程/教学班：查看本人负责的教学班，不承担开课或开班职责。
- 成绩管理：按教学班查看学生名单，录入或修改学生成绩。
- 个人信息：查看和维护教师基础信息。

#### 管理员模块

- 首页：展示学生、教师、课程、教学班和成绩等系统数据概览。
- 学生管理：维护学生档案，并同步维护登录账号状态。
- 教师管理：维护教师档案，并同步维护登录账号状态。
- 课程管理：维护课程目录。
- 教学班管理：维护具体开课教学班、教师、教室、学期、容量和状态。
- 院系管理：维护院系基础数据。
- 专业管理：维护专业基础数据，并关联所属院系。
- 教室管理：维护教室基础数据、容量和可用状态。

## 系统架构

```mermaid
graph TB
    subgraph Client[浏览器端]
        Pages[Vue 页面]
        Router[Vue Router 路由守卫]
        Store[Pinia 状态管理]
        ApiClient[Axios 请求封装]
    end

    subgraph Server[Spring Boot 后端]
        AuthApi[认证接口]
        StudentApi[学生接口]
        TeacherApi[教师接口]
        AdminApi[管理员接口]
        SectionApi[教学班接口]
        EnrollmentApi[选课接口]
        ScoreApi[成绩接口]
    end

    subgraph Database[MySQL 数据库]
        Tables[业务表]
        Indexes[索引]
        Views[视图]
        Procedures[存储过程]
        Triggers[触发器]
    end

    Pages --> Router
    Pages --> Store
    Pages --> ApiClient
    ApiClient --> AuthApi
    ApiClient --> StudentApi
    ApiClient --> TeacherApi
    ApiClient --> AdminApi
    ApiClient --> SectionApi
    ApiClient --> EnrollmentApi
    ApiClient --> ScoreApi
    Server --> Database
```

## 表设计

### ER 图

```mermaid
erDiagram
    t_department ||--o{ t_major : contains
    t_department ||--o{ t_teacher : owns
    t_department ||--o{ t_course : offers
    t_major ||--o{ t_student : contains
    t_course ||--o{ t_course_section : opens
    t_teacher ||--o{ t_course_section : teaches
    t_classroom ||--o{ t_course_section : hosts
    t_student ||--o{ t_enrollment : selects
    t_course_section ||--o{ t_enrollment : selected_by
    t_enrollment ||--o| t_score : has
    t_user ||--o| t_student : binds
    t_user ||--o| t_teacher : binds
    t_user ||--o| t_admin : binds
```

### 表结构

#### t_user

| 字段名 | 说明 |
| --- | --- |
| id | 用户账号主键 |
| username | 登录账号 |
| password | 登录密码 |
| role | 用户角色：student、teacher、admin |
| status | 账号状态 |
| created_at | 创建时间 |

#### t_student

| 字段名 | 说明 |
| --- | --- |
| student_id | 学生主键 |
| user_id | 关联用户账号 |
| major_id | 所属专业 |
| student_no | 学号 |
| student_name | 学生姓名 |
| gender | 性别 |
| phone | 联系电话 |
| email | 邮箱 |
| enrollment_year | 入学年份 |
| status | 学生状态 |

#### t_teacher

| 字段名 | 说明 |
| --- | --- |
| teacher_id | 教师主键 |
| user_id | 关联用户账号 |
| department_id | 所属院系 |
| teacher_no | 工号 |
| teacher_name | 教师姓名 |
| title | 职称 |
| phone | 联系电话 |
| email | 邮箱 |
| status | 教师状态 |

#### t_admin

| 字段名 | 说明 |
| --- | --- |
| admin_id | 管理员主键 |
| user_id | 关联用户账号 |
| admin_no | 管理员工号 |
| admin_name | 管理员姓名 |
| phone | 联系电话 |
| email | 邮箱 |
| status | 管理员状态 |

#### t_department

| 字段名 | 说明 |
| --- | --- |
| department_id | 院系主键 |
| department_code | 院系编码 |
| department_name | 院系名称 |
| office_phone | 办公电话 |
| status | 院系状态 |

#### t_major

| 字段名 | 说明 |
| --- | --- |
| major_id | 专业主键 |
| department_id | 所属院系 |
| major_code | 专业编码 |
| major_name | 专业名称 |
| status | 专业状态 |

#### t_classroom

| 字段名 | 说明 |
| --- | --- |
| classroom_id | 教室主键 |
| building | 教学楼 |
| room_no | 教室号 |
| capacity | 容量 |
| status | 教室状态 |
| remark | 备注 |

#### t_course

| 字段名 | 说明 |
| --- | --- |
| course_id | 课程主键 |
| department_id | 开课院系 |
| course_code | 课程编码 |
| course_name | 课程名称 |
| credit | 学分 |
| total_hours | 总学时 |
| course_type | 课程类型 |
| description | 课程说明 |
| status | 课程状态 |

#### t_course_section

| 字段名 | 说明 |
| --- | --- |
| section_id | 教学班主键 |
| course_id | 所属课程 |
| teacher_id | 授课教师 |
| classroom_id | 上课教室 |
| section_code | 教学班编号 |
| semester | 学期 |
| schedule_text | 上课时间描述 |
| capacity_limit | 容量上限 |
| selected_count | 当前选课人数 |
| status | 教学班状态 |

#### t_enrollment

| 字段名 | 说明 |
| --- | --- |
| enrollment_id | 选课记录主键 |
| student_id | 学生 |
| section_id | 教学班 |
| select_time | 选课时间 |
| status | 选课状态 |
| source | 选课来源 |
| remark | 备注 |

#### t_score

| 字段名 | 说明 |
| --- | --- |
| score_id | 成绩主键 |
| enrollment_id | 关联选课记录 |
| usual_score | 平时成绩 |
| exam_score | 考试成绩 |
| final_score | 总评成绩 |
| gpa_point | 绩点 |
| is_passed | 是否通过 |
| graded_at | 录入时间 |
| graded_by | 录入人 |

### 数据库实现要点

- `sql/sms.sql`：完整数据库脚本，包含建库、建表、初始化数据和大规模测试数据。
- `sql/sms-struct.sql`：结构脚本，包含正式表结构、索引、视图、存储过程和触发器定义。
- 通过外键约束维护院系、专业、学生、教师、课程、教学班、选课记录和成绩之间的关系。
- 通过唯一约束限制登录账号、学号、工号、课程编码、教学班编号等关键数据重复。
- 通过索引优化登录、列表筛选、学生查已选课程、教师查教学班、成绩统计等高频查询。
- 通过视图支持学生课程查询、教学班统计和学生绩点统计。
- 通过存储过程封装选课、退课和成绩录入逻辑。
- 通过触发器维护教学班已选人数，并自动计算成绩绩点和通过状态。

## 角色权限

| 角色 | 权限描述 |
| --- | --- |
| 学生 | 查看教学班、选课、退课、查看已选课程、查询成绩、维护个人信息 |
| 教师 | 查看本人教学班、查看学生名单、录入和修改成绩、查看教学统计、维护个人信息 |
| 管理员 | 管理学生、教师、课程、教学班、院系、专业、教室和系统基础数据 |

## 环境要求

- JDK 8+，项目 `pom.xml` 中 `java.version` 为 17，编译插件 source/target 为 1.8。
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+
- npm 8+

## 运行说明

### 1. 后端环境配置

#### 1.1 数据库配置

1. 创建并导入数据库脚本：

```bash
mysql -u user -p < sql/sms.sql
```

2. 根据本机 MySQL 环境修改 `backend/src/main/resources/application.yml`：

```yaml
server:
  port: 9090
  servlet:
    context-path: /api

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/sms?useUnicode=true&characterEncoding=utf8
    username: user
    password: 123
```

默认数据库名为 `sms`，默认用户名为 `user`，默认密码为 `123`。如果本机数据库账号不同，需要同步修改配置。

### 2. 启动项目

#### 2.1 后端启动

1. 进入后端目录：

```bash
cd backend
```

2. 使用 Maven 启动：

```bash
mvn spring-boot:run
```

3. 可选：打包后运行：

```bash
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

#### 2.2 前端启动

1. 进入前端目录：

```bash
cd front
```

2. 安装依赖：

```bash
npm install
```

3. 启动开发服务：

```bash
npm run dev
```

#### 2.3 快速启动

完成一次环境构建后，后续可直接使用项目根目录下的启动脚本快速启动前后端服务：

```bash
start-dev.bat
```

或在 PowerShell 中执行：

```powershell
.\start-dev.ps1
```

使用快速启动脚本前，应先确保已经完成以下准备：

- MySQL 已启动，且已导入 `sql/sms.sql`。
- `backend/src/main/resources/application.yml` 中的数据库连接信息正确。
- 前端依赖已在 `front/` 目录下通过 `npm install` 安装完成。
- 本机已安装可用的 Maven、Node.js 和 npm。

### 3. 访问项目

- 前端页面：`http://localhost:5173`
- 后端接口基地址：`http://localhost:9090/api`
- Knife4j 接口文档：`http://localhost:9090/api/doc.html`

### 4. 登录入口

前端登录调用：

```text
POST /api/auth/login
```

## 项目结构

```text
course-manage-system/
├── backend/                         # Spring Boot 后端项目
│   ├── src/main/java/com/agiantii/backend/
│   │   ├── common/                  # 通用返回结构和常量
│   │   ├── config/                  # Web、跨域等配置
│   │   ├── controller/              # 接口控制器
│   │   ├── mapper/                  # MyBatis-Plus Mapper
│   │   ├── pojo/                    # 实体对象
│   │   ├── pojo/vo/                 # 视图对象
│   │   └── utils/                   # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml          # 后端主配置
│   │   └── mapper/                  # MyBatis XML 映射文件
│   └── pom.xml                      # Maven 配置
├── front/                           # Vue 3 前端项目
│   ├── src/api/                     # Axios 与接口封装
│   ├── src/layouts/                 # 三类角色布局
│   ├── src/router/                  # 路由和访问控制
│   ├── src/styles/                  # 全局样式
│   └── src/views/                   # 页面实现
├── sql/
│   ├── sms.sql                      # 完整数据库导入脚本
│   └── sms-struct.sql               # 数据库结构脚本
├── assets/README/                   # README 截图资源目录
├── docs/leader-a/                   # 组长 A 文档、ER 图和报告材料
├── start-dev.bat                    # Windows 启动脚本
├── start-dev.ps1                    # PowerShell 启动脚本
├── README.md
└── README.en.md
```

## API 文档

项目集成 Knife4j，启动后访问：

```text
http://localhost:9090/api/doc.html
```

主要接口分组包括：

- 认证接口：`/api/auth/login`、`/api/auth/info`
- 学生选课接口：`/api/sections`、`/api/enrollments`、`/api/students/{studentId}/enrollments`
- 教师教学接口：`/api/teachers/{teacherId}/sections`、`/api/sections/{sectionId}/students`、`/api/scores`
- 管理员接口：`/api/admin/courses`、`/api/admin/sections`、`/api/admin/departments`、`/api/admin/majors`、`/api/admin/classrooms`、`/api/admin/students`、`/api/admin/teachers`

## 部署说明

1. 在 MySQL 中导入 `sql/sms.sql`，确认数据库名为 `sms`。
2. 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息。
3. 在 `backend/` 下执行 `mvn clean package` 打包后端项目。
4. 在 `front/` 下执行 `npm install` 和 `npm run build` 构建前端项目。
5. 部署时可使用 Nginx 托管前端静态资源，并将 `/api` 请求反向代理到后端服务 `http://localhost:9090/api`。
6. 后端服务可通过 `java -jar`、`systemd` 或其他进程管理方式启动。
