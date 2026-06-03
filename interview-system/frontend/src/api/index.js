import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 响应拦截器
api.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code === 200) {
      return data
    } else {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  error => {
    const msg = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

// ── 题目 CRUD ──
export const createQuestion = (data) => api.post('/questions', data)
export const updateQuestion = (id, data) => api.put(`/questions/${id}`, data)
export const deleteQuestion = (id) => api.delete(`/questions/${id}`)
export const batchDeleteQuestions = (ids) => api.delete('/questions/batch', { data: ids })
export const getQuestionDetail = (id) => api.get(`/questions/${id}`)
export const getQuestionList = (params) => api.get('/questions', { params })

// ── 抽题 ──
export const getRandomQuestion = (params) => api.get('/questions/random', { params })
export const getDueTodayQuestions = (params) => api.get('/questions/due-today', { params })
export const getReviewOverview = () => api.get('/questions/review-overview')

// ── 复习反馈 ──
export const submitReview = (id, data) => api.post(`/questions/${id}/review`, data)

// ── 答案 ──
export const updateAnswer = (id, answer) => api.put(`/questions/${id}/answer`, { answer })

// ── 收藏 ──
export const toggleFavorite = (id) => api.post(`/questions/${id}/favorite`)
export const getFavorites = (params) => api.get('/questions/favorites', { params })

// ── 未掌握 ──
export const getUnmastered = (params) => api.get('/questions/unmastered', { params })

// ── 统计 ──
export const getStatistics = () => api.get('/questions/statistics')

// ── 备份管理 ──
export const getBackups = () => api.get('/questions/backup/list')
export const createBackup = () => api.post('/questions/backup/create')
export const restoreBackup = (id) => api.post(`/questions/backup/${encodeURIComponent(id)}/restore`)

// ── 导入导出 ──
export const exportJson = () => api.get('/questions/export/json')
export const importJson = (data) => api.post('/questions/import/json', data)

// ── 分类与标签 ──
export const getCategories = () => api.get('/questions/categories')
export const getTags = () => api.get('/tags')
export const createTag = (name) => api.post('/tags', { name })
export const deleteTag = (id) => api.delete(`/tags/${id}`)
export const deleteTagByName = (name) => api.delete(`/tags/name/${encodeURIComponent(name)}`)
export const batchAddTag = (questionIds, tagName) => api.post('/tags/batch-add', { questionIds, tagName })
export const getEmptyTagQuestions = () => api.get('/tags/empty-tag-questions')

export default api
