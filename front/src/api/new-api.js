/**
 * 新版业务链 API 封装
 *
 * 业务链: course → course_section → enrollment → score
 * 所有方法直接调用新版接口，旧链已清理。
 */

import http from '@/api/index.js'

// ======================== 1. 认证 ========================

export const authApi = {
  /**
   * 登录（统一入口）
   * POST /api/auth/login
   */
  async login(username, password) {
    return http.post('/api/auth/login', { username, password })
  },

  /**
   * 获取当前用户信息
   * GET /api/auth/info
   */
  async getInfo() {
    return http.get('/api/auth/info')
  }
}

// ======================== 2. 教学班 ========================

export const sectionApi = {
  /**
   * 教学班列表（学生选课列表页）
   * GET /api/sections?page=&pageSize=&status=
   */
  async getSections(params = {}) {
    return http.get('/api/sections', params)
  },

  /**
   * 教学班详情
   * GET /api/sections/{id}
   */
  async getSectionDetail(id) {
    return http.get(`/api/sections/${id}`)
  }
}

// ======================== 3. 选课 / 退课 ========================

export const enrollmentApi = {
  /**
   * 选课
   * POST /api/enrollments
   */
  async enroll(studentId, sectionId) {
    return http.post('/api/enrollments', { studentId, sectionId, source: '自主选课' })
  },

  /**
   * 退课
   * DELETE /api/enrollments/{enrollmentId}
   */
  async drop(enrollmentId) {
    return http.delete(`/api/enrollments/${enrollmentId}`)
  },

  /**
   * 学生已选教学班列表（我的课程）
   * GET /api/students/{studentId}/enrollments?semester=&status=
   */
  async getMyEnrollments(studentId, params = {}) {
    return http.get(`/api/students/${studentId}/enrollments`, params)
  }
}

// ======================== 4. 教师端 ========================

/**
 * 安全获取 teacherId：优先用传入值，否则从 localStorage 读取
 */
function getTeacherId(tid) {
  if (tid !== null && tid !== undefined && tid !== '' && tid !== 'undefined' && tid !== 'null') {
    return parseInt(tid, 10)
  }
  const stored = localStorage.getItem('teacherId')
  if (stored) return parseInt(stored, 10)
  return null
}

export const teacherNewApi = {
  /**
   * 教师授课教学班列表
   * GET /api/teachers/{teacherId}/sections  (Day2: 需要 Token)
   */
  async getSections(teacherId) {
    const tid = getTeacherId(teacherId)
    return http.get(`/api/teachers/${tid}/sections`)
  },

  /**
   * 教学班学生名单
   * GET /api/sections/{sectionId}/students?teacherId=
   * Day2: teacherId 必填 Query + Token 校验
   */
  async getStudents(sectionId, teacherId) {
    const tid = getTeacherId(teacherId)
    return http.get(`/api/sections/${sectionId}/students`, { teacherId: tid })
  },

  /**
   * 录入/修改成绩
   * POST /api/scores   (Day2: 需要 Token + teacherId 必填 Body)
   */
  async saveScore(enrollmentId, teacherId, usualScore, examScore) {
    const tid = getTeacherId(teacherId)
    return http.post('/api/scores', { enrollmentId, teacherId: tid, usualScore, examScore })
  },

  /**
   * 教学班成绩汇总
   * GET /api/sections/{sectionId}/scores?teacherId=
   * Day2: teacherId 必填 Query + Token 校验
   */
  async getSectionScores(sectionId, teacherId) {
    const tid = getTeacherId(teacherId)
    return http.get(`/api/sections/${sectionId}/scores`, { teacherId: tid })
  }
}

// ======================== 5. 管理员端 ========================

export const adminNewApi = {
  // ======================== 下拉选项 ========================

  async getCourseOptions() {
    return http.get('/api/admin/courses/options')
  },
  async getTeacherOptions() {
    return http.get('/api/admin/teachers/options')
  },
  async getClassroomOptions() {
    return http.get('/api/admin/classrooms/options')
  },

  // ======================== 课程管理 ========================

  async getCourseList() {
    return http.get('/api/admin/courses')
  },
  async getCourseById(id) {
    return http.get(`/api/admin/courses/${id}`)
  },
  async addCourse(data) {
    return http.post('/api/admin/courses', data)
  },
  async updateCourse(id, data) {
    return http.put(`/api/admin/courses/${id}`, data)
  },
  async disableCourse(id) {
    return http.put(`/api/admin/courses/${id}/disable`)
  },

  // ======================== 教学班管理 ========================

  async getSectionList() {
    return http.get('/api/admin/sections')
  },
  async getSectionById(id) {
    return http.get(`/api/admin/sections/${id}`)
  },
  async addSection(data) {
    return http.post('/api/admin/sections', data)
  },
  async updateSection(id, data) {
    return http.put(`/api/admin/sections/${id}`, data)
  },
  async closeSection(id) {
    return http.put(`/api/admin/sections/${id}/close`)
  },
  async deleteSection(id) {
    return http.delete(`/api/admin/sections/${id}`)
  },

  // ======================== 院系管理 ========================

  async getDepartmentList() {
    return http.get('/api/admin/departments')
  },
  async getDepartmentById(id) {
    return http.get(`/api/admin/departments/${id}`)
  },
  async addDepartment(data) {
    return http.post('/api/admin/departments', data)
  },
  async updateDepartment(id, data) {
    return http.put(`/api/admin/departments/${id}`, data)
  },
  async disableDepartment(id) {
    return http.put(`/api/admin/departments/${id}/disable`)
  },

  // ======================== 专业管理 ========================

  async getMajorList() {
    return http.get('/api/admin/majors')
  },
  async getMajorById(id) {
    return http.get(`/api/admin/majors/${id}`)
  },
  async addMajor(data) {
    return http.post('/api/admin/majors', data)
  },
  async updateMajor(id, data) {
    return http.put(`/api/admin/majors/${id}`, data)
  },
  async disableMajor(id) {
    return http.put(`/api/admin/majors/${id}/disable`)
  },

  // ======================== 教室管理 ========================

  async getClassroomList() {
    return http.get('/api/admin/classrooms')
  },
  async getClassroomById(id) {
    return http.get(`/api/admin/classrooms/${id}`)
  },
  async addClassroom(data) {
    return http.post('/api/admin/classrooms', data)
  },
  async updateClassroom(id, data) {
    return http.put(`/api/admin/classrooms/${id}`, data)
  },
  async disableClassroom(id) {
    return http.put(`/api/admin/classrooms/${id}/disable`)
  },

  // ======================== 学生管理 ========================

  async getStudentList(params = {}) {
    return http.get('/api/admin/students', params)
  },
  async getStudentById(id) {
    return http.get(`/api/admin/students/${id}`)
  },
  async getNextStudentNo(majorId, year) {
    return http.get('/api/admin/students/next-no', { majorId, year })
  },
  async addStudent(data) {
    return http.post('/api/admin/students', data)
  },
  async updateStudent(id, data) {
    return http.put(`/api/admin/students/${id}`, data)
  },
  async disableStudent(id) {
    return http.put(`/api/admin/students/${id}/disable`)
  },
  async deleteStudent(id) {
    return http.delete(`/api/admin/students/${id}`)
  },

  // ======================== 教师管理 ========================

  async getTeacherList(params = {}) {
    return http.get('/api/admin/teachers', params)
  },
  async getTeacherById(id) {
    return http.get(`/api/admin/teachers/${id}`)
  },
  async getNextTeacherNo(departmentId, year) {
    return http.get('/api/admin/teachers/next-no', { departmentId, year })
  },
  async addTeacher(data) {
    return http.post('/api/admin/teachers', data)
  },
  async updateTeacher(id, data) {
    return http.put(`/api/admin/teachers/${id}`, data)
  },
  async disableTeacher(id) {
    return http.put(`/api/admin/teachers/${id}/disable`)
  },
  async deleteTeacher(id) {
    return http.delete(`/api/admin/teachers/${id}`)
  },

  // ======================== 下拉选项补充 ========================

  async getMajorOptions() {
    return http.get('/api/admin/majors/options')
  },
  async getDepartmentOptions() {
    return http.get('/api/admin/departments/options')
  }
}

export default {
  auth: authApi,
  section: sectionApi,
  enrollment: enrollmentApi,
  teacher: teacherNewApi,
  admin: adminNewApi
}
