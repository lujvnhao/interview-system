<template>
  <div class="quiz-page">
    <!-- Hero -->
    <header class="quiz-hero">
      <div class="hero-copy">
        <h1>面试抽背</h1>
        <p v-if="stats" class="hero-stat-line">
          今日已复习 <strong>{{ stats.todayReviewCount }}</strong> 题 ·
          题库共 <strong>{{ stats.totalQuestions }}</strong> 题
        </p>
      </div>
      <div class="hero-metrics" v-if="stats">
        <div class="metric-chip mastered">
          <span class="metric-value">{{ stats.masteredQuestions }}</span>
          <span class="metric-label">已掌握</span>
        </div>
        <div class="metric-chip unmastered">
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
      <!-- Main -->
      <div class="quiz-main">
        <!-- Control bar -->
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
            :loading="loading"
            @click="nextQuestion"
            class="draw-btn"
          >
            <el-icon><Refresh /></el-icon> {{ drawButtonText }}
          </el-button>
        </div>

        <!-- Empty state -->
        <div v-if="!currentQuestion && !loading" class="empty-state">
          <div class="empty-icon">
            <el-icon><Document /></el-icon>
          </div>
          <h3>{{ emptyTitle }}</h3>
          <p>{{ emptyDescription }}</p>
        </div>

        <!-- Question card -->
        <div v-if="currentQuestion" class="question-card">
          <!-- Meta tags -->
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

          <!-- Question -->
          <div class="question-body">
            <span class="q-number">#{{ currentQuestion.id }}</span>
            <p class="q-text">{{ currentQuestion.question }}</p>
          </div>

          <!-- Action buttons (before answer) -->
          <div v-if="!answerVisible" class="action-row">
            <button class="btn-mastered" :disabled="submitting" @click="handleMastered(true)">
              <el-icon><Check /></el-icon>
              <span>掌握了</span>
              <kbd>1</kbd>
            </button>
            <button class="btn-failed" :disabled="submitting" @click="handleMastered(false)">
              <el-icon><Close /></el-icon>
              <span>没掌握</span>
              <kbd>2</kbd>
            </button>
            <button
              :class="['btn-fav', { active: currentQuestion.favorite }]"
              @click="handleFavorite"
            >
              <el-icon><StarFilled v-if="currentQuestion.favorite" /><Star v-else /></el-icon>
              <span>{{ currentQuestion.favorite ? '已收藏' : '收藏' }}</span>
              <kbd>F</kbd>
            </button>
          </div>

          <!-- Answer -->
          <div v-if="answerVisible" class="answer-section">
            <div class="answer-divider">
              <span class="divider-line"></span>
              <span class="divider-label">
                <el-icon><Tickets /></el-icon> 参考答案
              </span>
              <span class="divider-line"></span>
            </div>

            <MarkdownView :content="currentQuestion.answer" empty-text="当前题目暂无答案" />

            <div class="answer-actions">
              <button class="btn-edit-answer" @click="openAnswerDialog">
                <el-icon><EditPen /></el-icon>
                {{ currentQuestion.answer ? '修改答案' : '补充答案' }}
                <kbd>E</kbd>
              </button>
              <button class="btn-next" @click="nextQuestion">
                下一题
                <el-icon><Right /></el-icon>
                <kbd>⏎</kbd>
              </button>
            </div>

            <div class="review-meta">
              <span>复习 {{ currentQuestion.reviewCount }} 次</span>
              <span class="dot">·</span>
              <span>对 {{ currentQuestion.correctCount }}</span>
              <span class="dot">·</span>
              <span>错 {{ currentQuestion.wrongCount }}</span>
              <span class="dot">·</span>
              <span>权重 {{ currentQuestion.weight }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="quiz-sidebar">
        <div class="stats-panel">
          <h3 class="panel-title">
            <el-icon><DataAnalysis /></el-icon> 复习概览
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
              <div class="stat-lbl">待巩固</div>
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
            >开始今日复习</el-button>
          </div>

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

    <!-- Answer editor dialog -->
    <el-dialog
      v-model="showAnswerDialog"
      title="补充 / 修改答案"
      width="620px"
      :close-on-click-modal="false"
    >
      <AnswerEditor v-model="answerForm" :rows="10" placeholder="请输入参考答案..." @escape="showAnswerDialog = false" />
      <template #footer>
        <el-button @click="showAnswerDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAnswer" :loading="submitting">保存答案</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onActivated, onDeactivated, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getRandomQuestion, submitReview, toggleFavorite,
  updateAnswer, getStatistics, getCategories, getReviewOverview
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
  const rm = route.query.mode
  if (['all', 'dueToday', 'favorites', 'unmastered'].includes(rm)) mode.value = rm
}

const tagList = computed(() => {
  if (!currentQuestion.value?.tags) return []
  return currentQuestion.value.tags.split(',').map(t => t.trim()).filter(Boolean)
})

const drawButtonText = computed(() => mode.value === 'dueToday' ? '抽待复习题' : '随机抽题')
const emptyTitle = computed(() => mode.value === 'dueToday' ? '今天没有待复习题' : '暂无题目')
const emptyDescription = computed(() =>
  mode.value === 'dueToday'
    ? '当前范围内暂无到期题，可切换分类或返回全部题目继续随机抽背'
    : '前往题库管理添加面试题，或点击上方按钮切换抽题范围'
)

const nextQuestion = async () => {
  loading.value = true
  answerVisible.value = false
  try {
    const params = { mode: mode.value }
    if (filterCategory.value) params.category = filterCategory.value
    const res = await getRandomQuestion(params)
    currentQuestion.value = res.data
  } catch (e) {
    currentQuestion.value = null
  } finally { loading.value = false }
}

const handleMastered = async (mastered) => {
  if (!currentQuestion.value) return
  submitting.value = true
  try {
    const res = await submitReview(currentQuestion.value.id, { mastered })
    currentQuestion.value = res.data
    answerVisible.value = true
    ElMessage.success(mastered ? '已标记为掌握' : '已标记为没掌握，下次加油！')
    fetchStats(); fetchReviewOverview()
  } catch (e) {} finally { submitting.value = false }
}

const handleFavorite = async () => {
  if (!currentQuestion.value) return
  try {
    const res = await toggleFavorite(currentQuestion.value.id)
    currentQuestion.value = res.data
    fetchStats(); fetchReviewOverview()
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

const fetchStats = async () => { try { stats.value = (await getStatistics()).data } catch (e) {} }
const fetchReviewOverview = async () => { try { reviewOverview.value = (await getReviewOverview()).data } catch (e) {} }
const fetchCategories = async () => { try { categories.value = (await getCategories()).data } catch (e) {} }
const startTodayReview = async () => { mode.value = 'dueToday'; await nextQuestion() }

const isEditableTarget = (target) => {
  if (!(target instanceof HTMLElement)) return false
  const t = target.tagName.toLowerCase()
  return t === 'input' || t === 'textarea' || t === 'select' || target.isContentEditable || !!target.closest('[contenteditable="true"]')
}

const handleShortcut = (event) => {
  if (route.path !== '/' || event.ctrlKey || event.metaKey || event.altKey) return
  if (showAnswerDialog.value) { if (event.key === 'Escape') showAnswerDialog.value = false; return }
  if (isEditableTarget(event.target)) return

  const key = event.key.toLowerCase()
  if (key === 'enter') { event.preventDefault(); if (!loading.value) nextQuestion(); return }
  if (!currentQuestion.value || submitting.value) return
  if (key === '1') { event.preventDefault(); if (!answerVisible.value) handleMastered(true) }
  else if (key === '2') { event.preventDefault(); if (!answerVisible.value) handleMastered(false) }
  else if (key === 'f') { event.preventDefault(); handleFavorite() }
  else if (key === 'e') { event.preventDefault(); openAnswerDialog() }
}

const bindShortcuts = () => {
  if (shortcutBound) return
  window.addEventListener('keydown', handleShortcut, true)
  shortcutBound = true
}
const unbindShortcuts = () => {
  if (!shortcutBound) return
  window.removeEventListener('keydown', handleShortcut, true)
  shortcutBound = false
}

onMounted(() => {
  syncModeFromRoute()
  fetchCategories(); fetchStats(); fetchReviewOverview()
  if (!currentQuestion.value) nextQuestion()
  bindShortcuts()
})
onActivated(bindShortcuts)
onDeactivated(unbindShortcuts)
onBeforeUnmount(unbindShortcuts)
watch(() => route.query.mode, async () => { syncModeFromRoute(); await nextQuestion() })
</script>

<style scoped>
.quiz-page { max-width: 1320px; margin: 0 auto; }

/* ── Hero ── */
.quiz-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding-bottom: 22px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--color-border);
}
.hero-copy h1 {
  font-family: var(--font-heading);
  font-size: 32px;
  font-weight: 800;
  color: var(--color-ink-strong);
  letter-spacing: 0.02em;
  line-height: 1.15;
}
.hero-stat-line { margin-top: 8px; font-family: var(--font-ui); font-size: 13.5px; color: var(--color-ink-muted); }
.hero-stat-line strong { color: var(--color-accent); font-weight: 700; }

.hero-metrics { display: flex; gap: 10px; }
.metric-chip {
  min-width: 90px;
  padding: 12px 14px;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}
.metric-chip.mastered { border-left: 3px solid var(--color-green); }
.metric-chip.unmastered { border-left: 3px solid var(--color-red); }
.metric-chip.saved { border-left: 3px solid var(--color-amber); }
.metric-value { display: block; font-family: var(--font-mono); font-size: 22px; font-weight: 700; color: var(--color-ink-strong); line-height: 1; }
.metric-label { display: block; margin-top: 6px; font-family: var(--font-ui); font-size: 11px; font-weight: 600; color: var(--color-ink-muted); }

/* ── Layout ── */
.quiz-layout { display: flex; gap: 20px; align-items: flex-start; }
.quiz-main { flex: 1; min-width: 0; }

/* ── Control bar ── */
.control-bar {
  display: flex; flex-wrap: wrap; gap: 14px; align-items: flex-end;
  padding: 18px; margin-bottom: 18px;
  background: var(--color-bg-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg);
}
.control-left { display: flex; flex: 1; flex-wrap: wrap; gap: 12px; min-width: 280px; }
.control-group { display: flex; flex: 1; flex-direction: column; gap: 6px; min-width: 180px; }
.control-group label { font-family: var(--font-ui); font-size: 11px; font-weight: 700; color: var(--color-ink-muted); letter-spacing: 0.04em; text-transform: uppercase; }
.draw-btn { height: 44px !important; min-width: 130px; font-size: 14px !important; }

/* ── Empty state ── */
.empty-state {
  padding: 80px 32px; text-align: center;
  background: var(--color-bg-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg);
}
.empty-icon {
  display: flex; align-items: center; justify-content: center;
  width: 64px; height: 64px; margin: 0 auto 20px;
  color: var(--color-ink-muted); background: var(--color-bg-elevated);
  border: 1px solid var(--color-border); border-radius: var(--radius-md); font-size: 28px;
}
.empty-state h3 { font-family: var(--font-heading); font-size: 18px; color: var(--color-ink-strong); margin-bottom: 8px; }
.empty-state p { font-size: 14px; color: var(--color-ink-muted); }

/* ── Question card ── */
.question-card {
  padding: 26px;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.card-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 20px; }
.tag-category, .tag-item, .tag-status {
  display: inline-flex; align-items: center;
  height: 26px; padding: 0 10px;
  font-family: var(--font-ui); font-size: 11px; font-weight: 700; border-radius: var(--radius-sm);
}
.tag-category { color: var(--color-blue); background: var(--color-blue-soft); border: 1px solid rgba(122,147,168,0.2); }
.tag-item { color: var(--color-ink-muted); background: var(--color-bg-elevated); border: 1px solid var(--color-border); }
.tag-hot { color: var(--color-red); background: var(--color-red-soft); border: 1px solid rgba(192,114,99,0.2); }
.tag-status.done { color: var(--color-green); background: var(--color-green-soft); border: 1px solid rgba(122,154,126,0.2); }
.tag-status.fav { color: var(--color-amber); background: var(--color-amber-soft); border: 1px solid rgba(196,154,92,0.2); }

/* Question body */
.question-body {
  position: relative;
  padding: 32px 30px 34px;
  margin-bottom: 24px;
  overflow: hidden;
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border);
  border-left: 3px solid var(--color-accent);
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}
.question-body::after {
  content: "";
  position: absolute;
  right: 24px; bottom: 18px;
  width: 80px; height: 80px;
  border: 2px solid var(--color-border);
  border-radius: 50%;
  opacity: 0.3;
}
.q-number {
  display: inline-block; margin-bottom: 14px;
  font-family: var(--font-mono); font-size: 11px; font-weight: 700;
  color: var(--color-accent); letter-spacing: 0.08em;
  text-transform: uppercase;
  padding: 2px 10px;
  background: var(--color-accent-soft);
  border-radius: var(--radius-sm);
}
.q-text {
  position: relative; z-index: 1;
  font-family: var(--font-heading);
  font-size: 21px; font-weight: 700; line-height: 1.8;
  color: var(--color-ink-strong);
  letter-spacing: 0.01em;
}

/* Action buttons */
.action-row { display: flex; flex-wrap: wrap; justify-content: center; gap: 12px; }

.btn-mastered, .btn-failed, .btn-fav,
.btn-edit-answer, .btn-next {
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  height: 44px; padding: 0 20px;
  font-family: var(--font-ui); font-size: 14px; font-weight: 700;
  cursor: pointer; border: 1px solid transparent; border-radius: var(--radius-md);
  transition: all 0.2s var(--ease-out);
}
.btn-mastered:disabled, .btn-failed:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-mastered {
  color: var(--color-green); background: var(--color-green-soft); border-color: rgba(122,154,126,0.25);
}
.btn-mastered:hover { background: #d8f0e2; transform: translateY(-1px); }

.btn-failed {
  color: var(--color-red); background: var(--color-red-soft); border-color: rgba(192,114,99,0.25);
}
.btn-failed:hover { background: #fde8e5; transform: translateY(-1px); }

.btn-fav {
  color: var(--color-ink-muted); background: var(--color-bg-elevated); border-color: var(--color-border);
}
.btn-fav:hover, .btn-fav.active { color: var(--color-amber); background: var(--color-amber-soft); border-color: rgba(196,154,92,0.25); }

kbd {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 20px; height: 20px; padding: 0 5px;
  font-family: var(--font-mono); font-size: 10px; font-weight: 700;
  color: var(--color-ink-muted); background: var(--color-surface-soft);
  border: 1px solid var(--color-border); border-radius: 3px;
}

/* Answer */
.answer-section { margin-top: 4px; }
.answer-divider { display: flex; align-items: center; gap: 14px; margin-bottom: 18px; }
.divider-line { flex: 1; height: 1px; background: var(--color-border); }
.divider-label {
  display: flex; align-items: center; gap: 6px;
  font-family: var(--font-ui); font-size: 12px; font-weight: 700; color: var(--color-ink-muted); white-space: nowrap;
}

.answer-actions { display: flex; justify-content: center; gap: 12px; margin-top: 20px; }
.btn-edit-answer { color: var(--color-blue); background: var(--color-blue-soft); border-color: rgba(122,147,168,0.25); }
.btn-edit-answer:hover { background: #e2ebf5; }
.btn-next { color: #fff; background: var(--color-accent); border-color: var(--color-accent); }
.btn-next:hover { background: var(--color-accent-strong); transform: translateY(-1px); }

.review-meta {
  display: flex; flex-wrap: wrap; justify-content: center; gap: 8px;
  margin-top: 18px; font-family: var(--font-ui); font-size: 12px; color: var(--color-ink-muted);
}
.dot { color: var(--color-ink-faint); }

/* ── Sidebar ── */
.quiz-sidebar { flex-shrink: 0; width: 280px; }
.stats-panel {
  position: sticky; top: 32px;
  padding: 20px;
  background: var(--color-bg-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg);
}
.panel-title {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 16px;
  font-family: var(--font-heading); font-size: 15px; font-weight: 800; color: var(--color-ink-strong);
}
.panel-title .el-icon { color: var(--color-accent); }

.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.stat-card {
  padding: 14px 12px; text-align: center;
  background: var(--color-bg-elevated); border: 1px solid var(--color-border); border-radius: var(--radius-md);
}
.stat-num { font-family: var(--font-mono); font-size: 24px; font-weight: 700; line-height: 1.15; }
.stat-card.total .stat-num { color: var(--color-blue); }
.stat-card.mastered .stat-num { color: var(--color-green); }
.stat-card.unmastered .stat-num { color: var(--color-red); }
.stat-card.favorite .stat-num { color: var(--color-amber); }
.stat-lbl { margin-top: 4px; font-family: var(--font-ui); font-size: 11px; font-weight: 600; color: var(--color-ink-muted); }

.today-badge {
  padding: 10px; margin-top: 12px;
  text-align: center; font-family: var(--font-ui); font-size: 13px; color: var(--color-ink-muted);
  background: var(--color-bg-elevated); border: 1px solid var(--color-border); border-radius: var(--radius-md);
}
.today-badge strong { color: var(--color-accent); }

.due-overview { margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--color-border); }
.due-grid { display: flex; flex-direction: column; gap: 8px; }
.due-item {
  display: flex; align-items: center; justify-content: space-between; gap: 10px;
  min-height: 42px; padding: 10px 12px;
  background: var(--color-bg-elevated); border: 1px solid var(--color-border); border-radius: var(--radius-md);
}
.due-item.primary { border-left: 3px solid var(--color-accent); }
.due-item.risk { border-left: 3px solid var(--color-red); }
.due-item.stale { border-left: 3px solid var(--color-amber); }
.due-num { font-family: var(--font-mono); font-size: 20px; font-weight: 700; color: var(--color-ink-strong); }
.due-label { font-family: var(--font-ui); font-size: 12px; font-weight: 600; color: var(--color-ink-muted); }
.start-today-btn { width: 100%; margin-top: 12px; }

.rank-section { margin-top: 20px; }
.rank-section h4 {
  margin-bottom: 10px;
  font-family: var(--font-heading); font-size: 13px; font-weight: 800; color: var(--color-ink-strong);
}
.rank-row {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 0; font-size: 13px; border-bottom: 1px solid var(--color-border);
}
.rank-row:last-child { border-bottom: 0; }
.rank-pos {
  display: flex; flex-shrink: 0; align-items: center; justify-content: center;
  width: 22px; height: 22px;
  font-family: var(--font-mono); font-size: 11px; font-weight: 800;
  background: var(--color-bg-elevated); border-radius: var(--radius-sm); color: var(--color-ink-muted);
}
.rank-pos.top1 { color: var(--color-red); background: var(--color-red-soft); }
.rank-pos.top2 { color: var(--color-amber); background: var(--color-amber-soft); }
.rank-pos.top3 { color: var(--color-accent); background: var(--color-accent-glow); }
.rank-q { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--color-ink); }
.rank-count { flex-shrink: 0; font-family: var(--font-mono); font-size: 12px; font-weight: 700; color: var(--color-red); }

/* ── Responsive ── */
@media (max-width: 1080px) {
  .quiz-layout { flex-direction: column; }
  .quiz-sidebar { width: 100%; }
  .stats-panel { position: static; }
}
@media (max-width: 760px) {
  .quiz-hero { flex-direction: column; align-items: flex-start; }
  .hero-copy h1 { font-size: 26px; }
  .question-card, .control-bar { padding: 16px; }
  .question-body { padding: 24px 18px; }
  .q-text { font-size: 18px; }
  .action-row, .answer-actions { flex-direction: column; align-items: stretch; }
  .btn-mastered, .btn-failed, .btn-fav, .btn-edit-answer, .btn-next { width: 100%; }
}
</style>
