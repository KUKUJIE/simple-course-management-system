import axios from 'axios'

axios.defaults.baseURL = '/api'

// Request interceptor to attach token
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
}, error => Promise.reject(error))

// Response interceptor to handle auth errors globally
axios.interceptors.response.use(response => response, error => {
  if (error.response) {
    if (error.response.status === 401) {
      // redirect to login
      window.location.href = '/login'
    }
  }
  return Promise.reject(error)
})

export default axios
