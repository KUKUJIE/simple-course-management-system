# 组员C → 组员D 联调信息 — Day 3 管理员端接口（2026-05-18）

## 一、管理员端接口列表（今日新增，前缀 `/api/admin`）

所有管理员接口均需 Token 认证（`role=admin`），其他角色访问一律 401。

| 序号 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 1 | GET | `/api/admin/courses` | 课程列表 |
| 2 | GET | `/api/admin/courses/{id}` | 课程详情 |
| 3 | POST | `/api/admin/courses` | 新增课程 |
| 4 | PUT | `/api/admin/courses/{id}` | 修改课程 |
| 5 | PUT | `/api/admin/courses/{id}/disable` | 停用课程 |
| 6 | GET | `/api/admin/sections` | 教学班列表 |
| 7 | GET | `/api/admin/sections/{id}` | 教学班详情 |
| 8 | POST | `/api/admin/sections` | 新增教学班 |
| 9 | PUT | `/api/admin/sections/{id}` | 修改教学班 |
| 10 | PUT | `/api/admin/sections/{id}/close` | 关闭教学班 |
| 11 | GET | `/api/admin/departments` | 院系列表 |
| 12 | GET | `/api/admin/departments/{id}` | 院系详情 |
| 13 | POST | `/api/admin/departments` | 新增院系 |
| 14 | PUT | `/api/admin/departments/{id}` | 修改院系 |
| 15 | PUT | `/api/admin/departments/{id}/disable` | 停用院系 |
| 16 | GET | `/api/admin/majors` | 专业列表 |
| 17 | GET | `/api/admin/majors/{id}` | 专业详情 |
| 18 | POST | `/api/admin/majors` | 新增专业 |
| 19 | PUT | `/api/admin/majors/{id}` | 修改专业 |
| 20 | PUT | `/api/admin/majors/{id}/disable` | 停用专业 |
| 21 | GET | `/api/admin/classrooms` | 教室列表 |
| 22 | GET | `/api/admin/classrooms/{id}` | 教室详情 |
| 23 | POST | `/api/admin/classrooms` | 新增教室 |
| 24 | PUT | `/api/admin/classrooms/{id}` | 修改教室 |
| 25 | PUT | `/api/admin/classrooms/{id}/disable` | 停用教室 |
| 26 | GET | `/api/admin/courses/options` | 课程下拉列表 |
| 27 | GET | `/api/admin/teachers/options` | 教师下拉列表 |
| 28 | GET | `/api/admin/classrooms/options` | 教室下拉列表 |

---

## 二、各接口详细说明

### 2.1 课程管理

#### GET /api/admin/courses — 课程列表
```
Header: Authorization: Bearer <admin_token>
Response:
{
  "status": 200,
  "data": [{
    "id": 1,
    "courseCode": "CS101",
    "name": "程序设计基础",
    "credit": 4.0,
    "totalHours": 64,
    "courseType": "必修",
    "departmentId": 1,
    "departmentName": "计算机学院",
    "description": "...",
    "status": 1
  }]
}
```

#### POST /api/admin/courses — 新增课程
```json
Body:
{
  "courseCode": "CS205",
  "name": "数据结构",
  "credit": 4.0,
  "totalHours": 64,
  "courseType": "必修",
  "departmentId": 1,
  "description": "...",
  "status": 1
}
Response: { "status": 200, "msg": "课程新增成功", "data": {...} }
```

#### PUT /api/admin/courses/{id}/disable — 停用课程
```
校验：该课程下存在运行中的教学班 → 返回 400 "该课程下存在 N 个运行中的教学班，无法停用"
```

---

### 2.2 教学班管理

#### GET /api/admin/sections — 教学班列表
```
Response:
{
  "status": 200,
  "data": [{
    "sectionId": 1,
    "sectionCode": "CS101-01",
    "semester": "2025-2026-1",
    "courseId": 1,
    "courseName": "程序设计基础",
    "teacherId": 1,
    "teacherName": "王教授",
    "classroomId": 1,
    "classroomName": "教学楼A 101",
    "capacityLimit": 60,
    "selectedCount": 45,
    "scheduleText": "周一 1-2节",
    "status": 1
  }]
}
```

#### POST /api/admin/sections — 新增教学班
```json
Body:
{
  "sectionCode": "CS101-03",
  "courseId": 1,
  "teacherId": 1,
  "classroomId": 1,
  "semester": "2025-2026-1",
  "capacityLimit": 50,
  "scheduleText": "周三 5-6节",
  "status": 1
}
Response: { "status": 200, "msg": "教学班新增成功", "data": {...} }
```

**业务校验：**
- courseId/teacherId/classroomId 必须存在 → 否则 400
- capacityLimit > 0 → 否则 400
- capacityLimit >= selectedCount → 否则 400（修改时）

#### PUT /api/admin/sections/{id}/close — 关闭教学班
```
Response: { "status": 200, "msg": "教学班已关闭" }
关闭后学生端选课列表不再显示此教学班。
```

---

### 2.3 院系管理

#### GET /api/admin/departments — 院系列表
```
Response:
{
  "status": 200,
  "data": [{
    "departmentId": 1,
    "departmentCode": "CS",
    "departmentName": "计算机学院",
    "officePhone": "010-xxxx",
    "status": 1
  }]
}
```

#### POST /api/admin/departments — 新增院系
```json
Body: { "departmentCode": "MATH", "departmentName": "数学学院", "officePhone": "010-xxxx", "status": 1 }
```

#### PUT /api/admin/departments/{id}/disable — 停用院系
```
校验：存在引用的专业或教师 → 400 拒绝，返回 "该院系下存在 N 个专业、N 名教师，无法停用"
```

---

### 2.4 专业管理

#### GET /api/admin/majors — 专业列表（含所属院系名称）
```
Response:
{
  "status": 200,
  "data": [{
    "majorId": 1,
    "departmentId": 1,
    "majorCode": "CS-SE",
    "majorName": "软件工程",
    "status": 1,
    "departmentName": "计算机学院"
  }]
}
```

#### POST /api/admin/majors — 新增专业
```json
Body: { "departmentId": 1, "majorCode": "CS-AI", "majorName": "人工智能", "status": 1 }
校验：departmentId 必须存在 → 否则 400
```

#### PUT /api/admin/majors/{id}/disable — 停用专业
```
校验：存在引用的学生 → 400 拒绝，返回 "该专业下存在 N 名学生，无法停用"
```

---

### 2.5 教室管理

#### GET /api/admin/classrooms — 教室列表
```
Response:
{
  "status": 200,
  "data": [{
    "classroomId": 1,
    "building": "教学楼A",
    "roomNo": "101",
    "capacity": 60,
    "status": 1,
    "remark": null
  }]
}
```

#### POST /api/admin/classrooms — 新增教室
```json
Body: { "building": "教学楼A", "roomNo": "201", "capacity": 50, "remark": "多媒体", "status": 1 }
```

#### PUT /api/admin/classrooms/{id}/disable — 停用教室
```
校验：存在运行中的教学班 → 400 拒绝
```

---

### 2.6 下拉数据接口

#### GET /api/admin/courses/options — 课程下拉
```json
Response: [{ "id": 1, "courseCode": "CS101", "name": "程序设计基础" }]
```

#### GET /api/admin/teachers/options — 教师下拉
```json
Response: [{ "teacherId": 1, "teacherName": "王教授" }]
```

#### GET /api/admin/classrooms/options — 教室下拉
```json
Response: [{ "classroomId": 1, "building": "教学楼A", "roomNo": "101", "capacity": 60, "status": 1 }]
```

---

## 三、管理员端完整调用流程

```
1. POST /api/auth/login {"username":"admin01","password":"123456"}
   → 获取 token

2. GET /api/admin/courses/options    → 课程下拉数据
   GET /api/admin/teachers/options   → 教师下拉数据
   GET /api/admin/classrooms/options → 教室下拉数据

3. GET /api/admin/departments        → 院系列表
   POST /api/admin/departments       → 新增院系

4. GET /api/admin/majors             → 专业列表
   POST /api/admin/majors            → 新增专业（绑定院系）

5. GET /api/admin/classrooms         → 教室列表
   POST /api/admin/classrooms        → 新增教室

6. GET /api/admin/courses            → 课程列表
   POST /api/admin/courses           → 新增课程（绑定院系）

7. GET /api/admin/sections           → 教学班列表
   POST /api/admin/sections          → 新增教学班（选课程/教师/教室/学期/容量）
   PUT /api/admin/sections/{id}/close → 关闭教学班

8. 学生端 GET /api/sections          → 应能看到管理员创建的教学班
   教师端 GET /api/teachers/{id}/sections → 教师应能看到归属自己的教学班
```

---

## 四、全局约定

- **Base URL**: `http://localhost:9090/api`
- **认证方式**: `Authorization: Bearer <admin_token>`（仅 role=admin 可访问）
- **响应格式**: `{"status":200,"msg":"...","data":{...}}`
- **禁用/关闭**: 通过 `PUT /api/admin/{resource}/{id}/disable|close` 操作，不物理删除
- **引用保护**: 禁用/关闭会检查下级引用，存在引用时返回 400 拒绝

---

## 五、新建文件清单（供参考）

| 文件 | 说明 |
|------|------|
| `pojo/Department.java` | 院系实体 |
| `pojo/Major.java` | 专业实体 |
| `pojo/Classroom.java` | 教室实体 |
| `mapper/DepartmentMapper.java` | 院系 CRUD |
| `mapper/MajorMapper.java` | 专业 CRUD（含院系 JOIN）|
| `mapper/ClassroomMapper.java` | 教室 CRUD |
| `controller/AdminApiController.java` | 全部 28 个管理员端接口 |
| `mapper/CourseMapper.java` | 新增 7 个管理员端方法 |
| `mapper/CourseSectionMapper.java` | 新增 6 个管理员端方法 |
