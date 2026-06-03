<template>
  <div class="quiz-page">
    <header class="quiz-hero">
      <div class="hero-copy">
        <h1>面试抽背</h1>
        <p v-if="stats">今日 {{ stats.todayReviewCount }} 题 · 题库 {{ stats.totalQuestions }} 题</p>
        <p v-else>题库加载中</p>
      </div>
      <div class="hero-metrics" v-if="stats">
        <div class="metric-chip">
          <span class="metric-value">{{ stats.masteredQuestions }}</span>
          <span class="metric-label">已掌握</span>
        </div>
        <div class="metric-chip warn">
          <span class="metric-value">{{ stats.unmasteredQuestions }}</span>
          <span class="metric-label">待巩固</span>
        </div>
        <div class="metric-chip saved">
          <span class="metric-value">{{ stats.favoriteQuestions }}</span>
          <span class="metric-label">收藏</span>
        </div>
      </div>
    </header>

    <div class="quiz-layout">
      <!-- 左侧：抽题主区域 -->
      <div class="quiz-main">
        <!-- 顶部控制栏 -->
        <div class="control-bar">
          <div class="control-left">
            <div class="control-group">
              <label>抽题范围</label>
              <el-select v-model="mode" size="large" class="mode-select">
                <el-option label="全部题目" value="all" />
                <el-option label="今日待复习" value="dueToday" />
                <el-option label="收藏题目" value="favorites" />
                <el-option label="未掌握" value="unmastered" />
              </el-select>
            </div>
            <div class="control-group">
              <label>分类筛选</label>
              <el-select v-model="filterCategory" size="large" clearable placeholder="不限分类">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </div>
          </div>
          <el-button
            type="primary"
            size="large"
            title="快捷键：Enter"
            @click="nextQuestion"
            :loading="loading"
            class="draw-btn"
          >
            <el-icon><Refresh /></el-icon> {{ drawButtonText }}
          </el-button>
        </div>

        <!-- 空状态 -->
        <div v-if="!currentQuestion && !loading" class="empty-state">
          <div class="empty-icon">
            <el-icon><Document /></el-icon>
          </div>
          <h3>{{ emptyTitle }}</h3>
          <p>{{ emptyDescription }}</p>
        </div>

        <!-- 题目卡片 -->
        <div v-if="currentQuestion" class="question-card">
          <!-- 元信息标签 -->
          <div class="card-tags">
            <span class="tag-category">{{ currentQuestion.category }}</span>
            <span
              v-for="tag in tagList"
              :key="tag"
              class="tag-item"
              :class="{ 'tag-hot': tag === '高频' || tag === '重点' || tag === '必背' }"
            >{{ tag }}</span>
            <span v-if="currentQuestion.mastered" class="tag-status done">已掌握</span>
            <span v-if="currentQuestion.favorite" class="tag-status fav">收藏</span>
          </div>

          <!-- 问题区 -->
          <div class="question-body">
            <span class="q-number">Q{{ currentQuestion.id }}</span>
            <p class="q-text">{{ currentQuestion.question }}</p>
          </div>

          <!-- 操作按钮（答案显示前） -->
          <div v-if="!answerVisible" class="action-row">
            <button class="btn-mastered" title="快捷键：1" @click="handleMastered(true)" :disabled="submitting">
              <el-icon><Check /></el-icon>
              <span>掌握了</span>
            </button>
            <button class="btn-failed" title="快捷键：2" @click="handleMastered(false)" :disabled="submitting">
              <el-icon><Close /></el-icon>
              <span>没掌握</span>
            </button>
            <button
              :class="['btn-fav', { active: currentQuestion.favorite }]"
              title="快捷键：F"
              @click="handleFavorite"
            >
              <el-icon>
                <StarFilled v-if="currentQuestion.favorite" />
                <Star v-else />
              </el-icon>
              <span>{{ currentQuestion.favorite ? '已收藏' : '收藏' }}</span>
            </button>
          </div>

          <!-- 答案区域 -->
          <div v-if="answerVisible" class="answer-section">
            <div class="answer-divider">
              <span class="divider-line"></span>
              <span class="divider-label">
                <el-icon><Tickets /></el-icon>
                参考答案
              </span>
              <span class="divider-line"></span>
            </div>

            <MarkdownView :content="currentQuestion.answer" empty-text="当前题目暂无答案" />

            <div class="answer-actions">
              <button class="btn-edit-answer" title="快捷键：E" @click="openAnswerDialog">
                <el-icon><EditPen /></el-icon>
                {{ currentQuestion.answer ? '修改答案' : '补充答案' }}
              </button>
              <button class="btn-next" title="快捷键：Enter" @click="nextQuestion">
                下一题
                <el-icon><Right /></el-icon>
              </button>
            </div>

            <!-- 复习统计条 -->
            <div class="review-meta">
              <span>复习 {{ currentQuestion.reviewCount }} 次</span>
              <span class="dot">·</span>
              <span>掌握 {{ currentQuestion.correctCount }}</span>
              <span class="dot">·</span>
              <span>未掌握 {{ currentQuestion.wrongCount }}</span>
              <span class="dot">·</span>
              <span>权重 {{ currentQuestion.weight }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：统计面板 -->
      <div class="quiz-sidebar">
        <div class="stats-panel">
          <h3 class="panel-title">
            <el-icon><DataAnalysis /></el-icon>
            复习概览
          </h3>

          <div class="stats-grid" v-if="stats">
            <div class="stat-card total">
              <div class="stat-num">{{ stats.totalQuestions }}</div>
              <div class="stat-lbl">总题目</div>
            </div>
            <div class="stat-card mastered">
              <div class="stat-num">{{ stats.masteredQuestions }}</div>
              <div class="stat-lbl">已掌握</div>
            </div>
            <div class="stat-card unmastered">
              <div class="stat-num">{{ stats.unmasteredQuestions }}</div>
              <div class="stat-lbl">未掌握</div>
            </div>
            <div class="stat-card favorite">
              <div class="stat-num">{{ stats.favoriteQuestions }}</div>
              <div class="stat-lbl">收藏</div>
            </div>
          </div>

          <div class="today-badge" v-if="stats">
            今日已复习 <strong>{{ stats.todayReviewCount }}</strong> 题
          </div>

          <div class="due-overview" v-if="reviewOverview">
            <div class="due-grid">
              <div class="due-item primary">
                <span class="due-num">{{ reviewOverview.dueTodayCount }}</span>
                <span class="due-label">今日待复习</span>
              </div>
              <div class="due-item risk">
                <span class="due-num">{{ reviewOverview.highRiskCount }}</span>
                <span class="due-label">高风险题</span>
              </div>
              <div class="due-item stale">
                <span class="due-num">{{ reviewOverview.longUnreviewedCount }}</span>
                <span class="due-label">长期未复习</span>
              </div>
            </div>
            <el-button
              type="primary"
              class="start-today-btn"
              :disabled="loading"
              @click="startTodayReview"
            >
              开始今日复习
            </el-button>
            <p v-if="reviewOverview.dueTodayCount === 0" class="due-empty-tip">
              今天暂无到期题，可继续随机抽题
            </p>
          </div>

          <!-- 错题排行 -->
          <div class="rank-section" v-if="stats?.wrongRank?.length">
            <h4>错题排行</h4>
            <div v-for="(item, i) in stats.wrongRank.slice(0, 5)" :key="item.id" class="rank-row">
              <span class="rank-pos" :class="'top' + (i + 1)">{{ i + 1 }}</span>
              <span class="rank-q">{{ item.question }}</span>
              <span class="rank-count">{{ item.wrongCount }}次</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 补充答案弹窗 -->
    <el-dialog
      v-model="showAnswerDialog"
      title="补充 / 修改答案"
      width="620px"
      :close-on-click-modal="false"
    >
      <AnswerEditor
        v-model="answerForm"
        :rows="10"
        placeholder="请输入参考答案..."
        @escape="showAnswerDialog = false"
      />
      <template #footer>
        <el-button @click="showAnswerDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAnswer" :loading="submitting">保存答案</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {
  ref,
  computed,
  onMounted,
  onBeforeUnmount,
  onActivated,
  onDeactivated,
  watch
} from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getRandomQuestion, submitReview, toggleFavorite,
  updateAnswer, getStatistics, getCategories,
  getDueTodayQuestions, getReviewOverview
} from '../api'
import AnswerEditor from '../components/AnswerEditor.vue'
import MarkdownView from '../components/MarkdownView.vue'

const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const currentQuestion = ref(null)
const answerVisible = ref(false)
const showAnswerDialog = ref(false)
const answerForm = ref('')
const stats = ref(null)
const reviewOverview = ref(null)
const categories = ref([])
const mode = ref('all')
const filterCategory = ref('')
let shortcutBound = false

const syncModeFromRoute = () => {
  const routeMode = route.query.mode
  if (['all', 'dueToday', 'favorites', 'unmastered'].includes(routeMode)) {
    mode.value = routeMode
  }
}

const tagList = computed(() => {
  if (!currentQuestion.value?.tags) return []
  return currentQuestion.value.tags.split(',').map(t => t.trim()).filter(Boolean)
})

const drawButtonText = computed(() => mode.value === 'dueToday' ? '抽待复习题' : '随机抽题')

const emptyTitle = computed(() => mode.value === 'dueToday' ? '今天没有待复习题' : '暂无题目')

const emptyDescription = computed(() => {
  if (mode.value === 'dueToday') {
    return '当前范围内暂无到期题，可切换分类或返回全部题目继续随机抽背'
  }
  return '前往题库管理添加面试题，或点击上方按钮切换抽题范围'
})

const nextQuestion = async () => {
  loading.value = true
  answerVisible.value = false
  try {
    const params = { mode: mode.value }
    if (filterCategory.value) params.category = filterCategory.value
    if (mode.value === 'dueToday') {
      const dueParams = { page: 1, size: 1 }
      if (filterCategory.value) dueParams.category = filterCategory.value
      const dueRes = await getDueTodayQuestions(dueParams)
      if (!dueRes.data?.totalElements) {
        currentQuestion.value = null
        return
      }
    }
    const res = await getRandomQuestion(params)
    currentQuestion.value = res.data
  } catch (e) {
    currentQuestion.value = null
  } finally {
    loading.value = false
  }
}

const handleMastered = async (mastered) => {
  if (!currentQuestion.value) return
  submitting.value = true
  try {
    const res = await submitReview(currentQuestion.value.id, { mastered })
    currentQuestion.value = res.data
    answerVisible.value = true
    ElMessage.success(mastered ? '已标记为掌握 ✓' : '已标记为没掌握，自动收藏，下次加油！')
    fetchStats()
    fetchReviewOverview()
  } catch (e) {} finally { submitting.value = false }
}

const handleFavorite = async () => {
  if (!currentQuestion.value) return
  try {
    const res = await toggleFavorite(currentQuestion.value.id)
    currentQuestion.value = res.data
    fetchStats()
    fetchReviewOverview()
  } catch (e) {}
}

const openAnswerDialog = () => {
  if (!currentQuestion.value) return
  answerForm.value = currentQuestion.value.answer || ''
  showAnswerDialog.value = true
}

const submitAnswer = async () => {
  if (!currentQuestion.value) return
  submitting.value = true
  try {
    const res = await updateAnswer(currentQuestion.value.id, answerForm.value)
    currentQuestion.value = res.data
    showAnswerDialog.value = false
    ElMessage.success('答案已保存')
  } catch (e) {} finally { submitting.value = false }
}

const fetchStats = async () => {
  try {
    const res = await getStatistics()
    stats.value = res.data
  } catch (e) {}
}

const fetchReviewOverview = async () => {
  try {
    const res = await getReviewOverview()
    reviewOverview.value = res.data
  } catch (e) {}
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (e) {}
}

const startTodayReview = async () => {
  mode.value = 'dueToday'
  await nextQuestion()
}

const isEditableTarget = (target) => {
  if (!(target instanceof HTMLElement)) return false
  const tagName = target.tagName.toLowerCase()
  return tagName === 'input'
    || tagName === 'textarea'
    || tagName === 'select'
    || target.isContentEditable
    || !!target.closest('[contenteditable="true"]')
}

const handleShortcut = (event) => {
  if (event.__quizShortcutHandled) return
  event.__quizShortcutHandled = true
  if (route.path !== '/' || event.defaultPrevented) return
  if (event.ctrlKey || event.metaKey || event.altKey) return

  const key = event.key.toLowerCase()

  if (showAnswerDialog.value) {
    if (key === 'escape' || key === 'esc') {
      event.preventDefault()
      showAnswerDialog.value = false
    }
    return
  }

  if (isEditableTarget(event.target)) return

  if (key === 'enter') {
    event.preventDefault()
    if (!loading.value) nextQuestion()
    return
  }

  if (!currentQuestion.value || submitting.value) return

  if (key === '1') {
    event.preventDefault()
    if (!answerVisible.value) handleMastered(true)
    return
  }

  if (key === '2') {
    event.preventDefault()
    if (!answerVisible.value) handleMastered(false)
    return
  }

  if (key === 'f') {
    event.preventDefault()
    handleFavorite()
    return
  }

  if (key === 'e') {
    event.preventDefault()
    openAnswerDialog()
  }
}

const bindShortcuts = () => {
  if (shortcutBound) return
  window.addEventListener('keydown', handleShortcut, true)
  document.addEventListener('keydown', handleShortcut, true)
  shortcutBound = true
}

const unbindShortcuts = () => {
  if (!shortcutBound) return
  window.removeEventListener('keydown', handleShortcut, true)
  document.removeEventListener('keydown', handleShortcut, true)
  shortcutBound = false
}

onMounted(() => {
  syncModeFromRoute()
  fetchCategories()
  fetchStats()
  fetchReviewOverview()
  if (!currentQuestion.value) nextQuestion()
  bindShortcuts()
})

onActivated(bindShortcuts)
onDeactivated(unbindShortcuts)
onBeforeUnmount(unbindShortcuts)

watch(() => route.query.mode, async () => {
  syncModeFromRoute()
  await nextQuestion()
})
</script>

<style scoped>
.quiz-page {
  max-width: 1360px;
  margin: 0 auto;
}

.quiz-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 4px 0 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #dfe5e8;
}

.hero-copy h1 {
  color: #1f2933;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.2;
}

.hero-copy p {
  margin-top: 8px;
  color: #667085;
  font-size: 14px;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(92px, 1fr));
  gap: 10px;
}

.metric-chip {
  min-width: 92px;
  padding: 11px 12px;
  background: #ffffff;
  border: 1px solid #dfe5e8;
  border-left: 3px solid #16a34a;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
}

.metric-chip.warn {
  border-left-color: #dc2626;
}

.metric-chip.saved {
  border-left-color: #d97706;
}

.metric-value {
  display: block;
  color: #1f2933;
  font-size: 20px;
  font-weight: 800;
  line-height: 1;
}

.metric-label {
  display: block;
  margin-top: 6px;
  color: #667085;
  font-size: 12px;
  font-weight: 600;
}

.quiz-layout {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}

.quiz-main {
  flex: 1;
  min-width: 0;
}

.control-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: flex-end;
  padding: 16px;
  margin-bottom: 16px;
  background: #ffffff;
  border: 1px solid #dfe5e8;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
}

.control-left {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  gap: 12px;
  min-width: 280px;
}

.control-group {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 6px;
  min-width: 180px;
}

.control-group label {
  color: #667085;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.draw-btn {
  height: 44px !important;
  min-width: 132px;
  padding: 0 20px !important;
  font-size: 15px !important;
  border-radius: 8px !important;
}

.empty-state {
  padding: 72px 32px;
  text-align: center;
  background: #ffffff;
  border: 1px solid #dfe5e8;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
}

.empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  margin: 0 auto 18px;
  color: #0f766e;
  font-size: 30px;
  background: #e6f4f1;
  border-radius: 8px;
}

.empty-state h3 {
  margin-bottom: 8px;
  color: #344054;
  font-size: 18px;
}

.empty-state p {
  color: #667085;
  font-size: 14px;
}

.question-card {
  padding: 24px;
  background: #ffffff;
  border: 1px solid #dfe5e8;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04), 0 14px 30px rgba(16, 24, 40, 0.05);
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 18px;
}

.tag-category,
.tag-item,
.tag-status {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 3px 9px;
  font-size: 12px;
  font-weight: 700;
  border-radius: 6px;
}

.tag-category {
  color: #2563eb;
  background: #eaf2ff;
}

.tag-item {
  color: #5f6f7d;
  background: #f0f3f5;
}

.tag-hot {
  color: #dc2626;
  background: #fff1f0;
}

.tag-status.done {
  color: #0f766e;
  background: #e6f4f1;
}

.tag-status.fav {
  color: #b45309;
  background: #fff4df;
}

.question-body {
  position: relative;
  padding: 26px 28px 28px;
  margin-bottom: 22px;
  overflow: hidden;
  color: #ffffff;
  background:
    linear-gradient(135deg, rgba(37, 99, 235, 0.18), rgba(217, 119, 6, 0.08)),
    linear-gradient(135deg, #0f766e 0%, #1f2933 100%);
  border-radius: 8px;
}

.question-body::after {
  position: absolute;
  right: 18px;
  bottom: 12px;
  width: 120px;
  height: 1px;
  content: "";
  background: rgba(255, 255, 255, 0.32);
}

.q-number {
  display: inline-block;
  margin-bottom: 12px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
}

.q-text {
  position: relative;
  z-index: 1;
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.75;
  letter-spacing: 0;
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.btn-mastered,
.btn-failed,
.btn-fav,
.btn-edit-answer,
.btn-next {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-height: 40px;
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  border: 1px solid transparent;
  border-radius: 8px;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.btn-mastered:disabled,
.btn-failed:disabled,
.btn-fav:disabled {
  cursor: not-allowed;
  opacity: 0.62;
}

.btn-mastered {
  color: #0f766e;
  background: #e6f4f1;
  border-color: #b8ded8;
}

.btn-mastered:hover {
  background: #d5eee9;
  transform: translateY(-1px);
}

.btn-failed {
  color: #dc2626;
  background: #fff1f0;
  border-color: #ffd4d0;
}

.btn-failed:hover {
  background: #ffe4e1;
  transform: translateY(-1px);
}

.btn-fav {
  color: #5f6f7d;
  background: #f6f8f9;
  border-color: #dfe5e8;
}

.btn-fav:hover,
.btn-fav.active {
  color: #b45309;
  background: #fff4df;
  border-color: #f8d49a;
}

.answer-section {
  margin-top: 4px;
}

.answer-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: #dfe5e8;
}

.divider-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #667085;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.answer-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 18px;
}

.btn-edit-answer {
  color: #2563eb;
  background: #eaf2ff;
  border-color: #c9ddff;
}

.btn-edit-answer:hover {
  background: #deebff;
}

.btn-next {
  color: #ffffff;
  background: #0f766e;
  border-color: #0f766e;
}

.btn-next:hover {
  background: #0b5f59;
  border-color: #0b5f59;
}

.review-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
  color: #667085;
  font-size: 13px;
}

.dot {
  color: #b7c0c7;
}

.quiz-sidebar {
  flex-shrink: 0;
  width: 292px;
}

.stats-panel {
  position: sticky;
  top: 28px;
  padding: 18px;
  background: #ffffff;
  border: 1px solid #dfe5e8;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: #1f2933;
  font-size: 15px;
  font-weight: 800;
}

.panel-title .el-icon {
  color: #0f766e;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.stat-card {
  padding: 13px 10px;
  text-align: center;
  background: #f8faf9;
  border: 1px solid #edf1f3;
  border-radius: 8px;
}

.stat-num {
  font-size: 24px;
  font-weight: 800;
  line-height: 1.15;
}

.stat-card.total .stat-num {
  color: #2563eb;
}

.stat-card.mastered .stat-num {
  color: #16a34a;
}

.stat-card.unmastered .stat-num {
  color: #dc2626;
}

.stat-card.favorite .stat-num {
  color: #d97706;
}

.stat-lbl {
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
  font-weight: 600;
}

.today-badge {
  padding: 10px;
  margin-top: 12px;
  color: #667085;
  font-size: 13px;
  text-align: center;
  background: #f6f8f9;
  border-radius: 8px;
}

.today-badge strong {
  color: #0f766e;
}

.due-overview {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #edf1f3;
}

.due-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.due-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 38px;
  padding: 8px 10px;
  background: #f8faf9;
  border: 1px solid #edf1f3;
  border-left: 3px solid #0f766e;
  border-radius: 8px;
}

.due-item.risk {
  border-left-color: #dc2626;
}

.due-item.stale {
  border-left-color: #d97706;
}

.due-num {
  color: #1f2933;
  font-size: 20px;
  font-weight: 800;
  line-height: 1;
}

.due-label {
  color: #667085;
  font-size: 12px;
  font-weight: 700;
  text-align: right;
}

.start-today-btn {
  width: 100%;
  margin-top: 10px;
  border-radius: 8px !important;
}

.due-empty-tip {
  margin-top: 8px;
  color: #667085;
  font-size: 12px;
  line-height: 1.5;
  text-align: center;
}

.rank-section {
  margin-top: 18px;
}

.rank-section h4 {
  margin-bottom: 10px;
  color: #1f2933;
  font-size: 13px;
  font-weight: 800;
}

.rank-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 0;
  font-size: 13px;
  border-bottom: 1px solid #edf1f3;
}

.rank-row:last-child {
  border-bottom: 0;
}

.rank-pos {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: #667085;
  font-size: 11px;
  font-weight: 800;
  background: #edf1f3;
  border-radius: 6px;
}

.rank-pos.top1 {
  color: #dc2626;
  background: #fff1f0;
}

.rank-pos.top2 {
  color: #d97706;
  background: #fff4df;
}

.rank-pos.top3 {
  color: #ca8a04;
  background: #fef9c3;
}

.rank-q {
  flex: 1;
  overflow: hidden;
  color: #475467;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-count {
  flex-shrink: 0;
  color: #dc2626;
  font-weight: 700;
}

@media (max-width: 1080px) {
  .quiz-layout {
    flex-direction: column;
  }

  .quiz-sidebar {
    width: 100%;
  }

  .stats-panel {
    position: static;
  }
}

@media (max-width: 760px) {
  .quiz-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-copy h1 {
    font-size: 26px;
  }

  .hero-metrics {
    width: 100%;
    grid-template-columns: repeat(3, 1fr);
  }

  .question-card,
  .control-bar {
    padding: 14px;
  }

  .question-body {
    padding: 22px 18px;
  }

  .q-text {
    font-size: 18px;
  }

  .action-row,
  .answer-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .btn-mastered,
  .btn-failed,
  .btn-fav,
  .btn-edit-answer,
  .btn-next,
  .draw-btn {
    width: 100%;
  }
}
</style>
