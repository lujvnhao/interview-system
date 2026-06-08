import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// ── 请求去重：防止同一请求并发重复发送 ──
const pendingRequests = new Map()

const requestKey = (config) => {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

api.interceptors.request.use(config => {
  const key = requestKey(config)
  if (pendingRequests.has(key)) {
    const cancel = pendingRequests.get(key)
    cancel('重复请求已取消')
    pendingRequests.delete(key)
  }
  config.cancelToken = new axios.CancelToken(cancel => {
    pendingRequests.set(key, cancel)
  })
  return config
})

api.interceptors.response.use(
  response => {
    const key = requestKey(response.config)
    pendingRequests.delete(key)
    const data = response.data
    if (data.code === 200) {
      return data
    } else {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  error => {
    if (error.config) {
      const key = requestKey(error.config)
      pendingRequests.delete(key)
    }
    if (axios.isCancel(error)) {
      return Promise.resolve()  // 被取消的请求静默处理
    }
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
export const getSpecialPracticeOverview = () => api.get('/questions/special/overview')
export const getSpecialAlgorithmState = () => api.get('/questions/special/algorithm/state')
const normalizeSpecialDrawCount = (count) => {
  const value = Number(count)
  return Number.isInteger(value) ? Math.min(5, Math.max(1, value)) : 5
}

export const drawSpecialAlgorithm = (count = 5) => {
  return api.post('/questions/special/algorithm/draw', null, {
    params: { count: normalizeSpecialDrawCount(count) }
  })
}
export const drawSpecialSql = () => api.post('/questions/special/sql/draw')

// ── 复习反馈 ──
export const submitReview = (id, data) => api.post(`/questions/${id}/review`, data)
export const submitSpecialAlgorithmReview = (id, data) => api.post(`/questions/special/algorithm/${id}/review`, data)

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
export const getBackupDir = () => api.get('/questions/backup/dir')
export const openBackupDir = () => api.post('/questions/backup/open-dir')

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
