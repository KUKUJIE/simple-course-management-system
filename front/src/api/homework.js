// homework API
import axios from 'axios'

const API_BASE = '/api/homeworks'

export function createHomework(payload) {
  return axios.post(API_BASE, payload);
}

export function getHomework(id) {
  return axios.get(`${API_BASE}/${id}`);
}

export function listHomeworks(params) {
  return axios.get(API_BASE, { params });
}

export function submitHomework(id, formData) {
  return axios.post(`${API_BASE}/${id}/submissions`, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
}

export function listSubmissions(homeworkId, params) {
  return axios.get(`${API_BASE}/${homeworkId}/submissions`, { params });
}

export function getSubmission(submissionId) {
  return axios.get(`/api/homeworks/submissions/${submissionId}`)
}

export function gradeSubmission(submissionId, payload) {
  return axios.post(`/api/submissions/${submissionId}/grade`, payload)
}

export function exportHomework(homeworkId) {
  return axios.get(`/api/homeworks/${homeworkId}/export`, { responseType: 'blob' })
}

export function getHomeworkStats(homeworkId) {
  return axios.get(`/api/statistics/homework/${homeworkId}/overview`)
}
