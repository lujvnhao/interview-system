<template>
  <div class="quiz-page">
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
                <el-option label="⭐ 收藏题目" value="favorites" />
                <el-option label="🔥 未掌握" value="unmastered" />
              </el-select>
            </div>
            <div class="control-group">
              <label>分类筛选</label>
              <el-select v-model="filterCategory" size="large" clearable placeholder="不限分类">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </div>
          </div>
          <el-button type="primary" size="large" @click="nextQuestion" :loading="loading" class="draw-btn">
            <el-icon><Refresh /></el-icon> 随机抽题
          </el-button>
        </div>

        <!-- 空状态 -->
        <div v-if="!currentQuestion && !loading" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2M9 5h6"/><line x1="9" y1="12" x2="15" y2="12"/><line x1="9" y1="16" x2="13" y2="16"/></svg>
          </div>
          <h3>暂无题目</h3>
          <p>前往题库管理添加面试题，或点击上方按钮切换抽题范围</p>
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
            <span v-if="currentQuestion.favorite" class="tag-status fav">⭐ 收藏</span>
          </div>

          <!-- 问题区 -->
          <div class="question-body">
            <span class="q-number">Q{{ currentQuestion.id }}</span>
            <p class="q-text">{{ currentQuestion.question }}</p>
          </div>

          <!-- 操作按钮（答案显示前） -->
          <div v-if="!answerVisible" class="action-row">
            <button class="btn-mastered" @click="handleMastered(true)" :disabled="submitting">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              <span>掌握了</span>
            </button>
            <button class="btn-failed" @click="handleMastered(false)" :disabled="submitting">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              <span>没掌握</span>
            </button>
            <button
              :class="['btn-fav', { active: currentQuestion.favorite }]"
              @click="handleFavorite"
            >
              <svg viewBox="0 0 24 24" :fill="currentQuestion.favorite ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
              <span>{{ currentQuestion.favorite ? '已收藏' : '收藏' }}</span>
            </button>
          </div>

          <!-- 答案区域 -->
          <div v-if="answerVisible" class="answer-section">
            <div class="answer-divider">
              <span class="divider-line"></span>
              <span class="divider-label">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="8" y1="8" x2="16" y2="8"/><line x1="8" y1="12" x2="16" y2="12"/><line x1="8" y1="16" x2="12" y2="16"/></svg>
                参考答案
              </span>
              <span class="divider-line"></span>
            </div>

            <div v-if="currentQuestion.answer" class="answer-content">
              {{ currentQuestion.answer }}
            </div>
            <div v-else class="answer-empty">
              <p>📭 当前题目暂无答案</p>
            </div>

            <div class="answer-actions">
              <button class="btn-edit-answer" @click="showAnswerDialog = true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                {{ currentQuestion.answer ? '修改答案' : '补充答案' }}
              </button>
              <button class="btn-next" @click="nextQuestion">
                下一题
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
              </button>
            </div>

            <!-- 复习统计条 -->
            <div class="review-meta">
              <span>📊 复习 {{ currentQuestion.reviewCount }} 次</span>
              <span class="dot">·</span>
              <span>✅ 掌握 {{ currentQuestion.correctCount }}</span>
              <span class="dot">·</span>
              <span>❌ 未掌握 {{ currentQuestion.wrongCount }}</span>
              <span class="dot">·</span>
              <span>⚖️ 权重 {{ currentQuestion.weight }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：统计面板 -->
      <div class="quiz-sidebar">
        <div class="stats-panel">
          <h3 class="panel-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
            复习概览
          </h3>

          <div class="stats-grid" v-if="stats">
            <div class="stat-card">
              <div class="stat-num" style="color:#6366f1">{{ stats.totalQuestions }}</div>
              <div class="stat-lbl">总题目</div>
            </div>
            <div class="stat-card">
              <div class="stat-num" style="color:#10b981">{{ stats.masteredQuestions }}</div>
              <div class="stat-lbl">已掌握</div>
            </div>
            <div class="stat-card">
              <div class="stat-num" style="color:#ef4444">{{ stats.unmasteredQuestions }}</div>
              <div class="stat-lbl">未掌握</div>
            </div>
            <div class="stat-card">
              <div class="stat-num" style="color:#f59e0b">{{ stats.favoriteQuestions }}</div>
              <div class="stat-lbl">收藏</div>
            </div>
          </div>

          <div class="today-badge" v-if="stats">
            📅 今日已复习 <strong>{{ stats.todayReviewCount }}</strong> 题
          </div>

          <!-- 错题排行 -->
          <div class="rank-section" v-if="stats?.wrongRank?.length">
            <h4>🔥 错题排行</h4>
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
    <el-dialog v-model="showAnswerDialog" title="补充 / 修改答案" width="620px" :close-on-click-modal="false">
      <el-input v-model="answerForm" type="textarea" :rows="10" placeholder="请输入参考答案..." />
      <template #footer>
        <el-button @click="showAnswerDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAnswer" :loading="submitting">保存答案</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getRandomQuestion, submitReview, toggleFavorite,
  updateAnswer, getStatistics, getCategories
} from '../api'

const loading = ref(false)
const submitting = ref(false)
const currentQuestion = ref(null)
const answerVisible = ref(false)
const showAnswerDialog = ref(false)
const answerForm = ref('')
const stats = ref(null)
const categories = ref([])
const mode = ref('all')
const filterCategory = ref('')

const tagList = computed(() => {
  if (!currentQuestion.value?.tags) return []
  return currentQuestion.value.tags.split(',').map(t => t.trim()).filter(Boolean)
})

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
  } catch (e) {} finally { submitting.value = false }
}

const handleFavorite = async () => {
  if (!currentQuestion.value) return
  try {
    const res = await toggleFavorite(currentQuestion.value.id)
    currentQuestion.value = res.data
    fetchStats()
  } catch (e) {}
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

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (e) {}
}

onMounted(() => {
  fetchCategories()
  fetchStats()
  if (!currentQuestion.value) nextQuestion()
})
</script>

<style scoped>
.quiz-page { max-width: 1340px; margin: 0 auto; }

.quiz-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* ── 左侧主区域 ── */
.quiz-main {
  flex: 1;
  min-width: 0;
}

/* 控制栏 */
.control-bar {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.control-left { display: flex; gap: 16px; }
.control-group { display: flex; flex-direction: column; gap: 4px; }
.control-group label {
  font-size: 12px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: .5px;
}
.draw-btn {
  height: 44px !important;
  padding: 0 28px !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  border-radius: 12px !important;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,.04);
}
.empty-icon {
  width: 72px; height: 72px;
  margin: 0 auto 20px;
  background: #f1f5f9; border-radius: 20px;
  display: flex; align-items: center; justify-content: center;
  color: #94a3b8;
}
.empty-icon svg { width: 32px; height: 32px; }
.empty-state h3 { font-size: 18px; color: #334155; margin-bottom: 8px; }
.empty-state p { color: #94a3b8; font-size: 14px; }

/* 题目卡片 */
.question-card {
  background: #fff;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0,0,0,.04), 0 1px 2px rgba(0,0,0,.03);
}

/* 标签 */
.card-tags {
  display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 24px;
}
.tag-category {
  font-size: 12px; font-weight: 600;
  padding: 4px 10px; border-radius: 6px;
  background: #eef2ff; color: #6366f1;
}
.tag-item {
  font-size: 12px; font-weight: 500;
  padding: 4px 10px; border-radius: 6px;
  background: #f1f5f9; color: #64748b;
}
.tag-hot { background: #fef2f2; color: #ef4444; }
.tag-status {
  font-size: 12px; font-weight: 600;
  padding: 4px 10px; border-radius: 6px;
}
.tag-status.done { background: #ecfdf5; color: #10b981; }
.tag-status.fav { background: #fffbeb; color: #f59e0b; }

/* 问题 */
.question-body {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 28px;
}
.q-number {
  display: inline-block;
  font-size: 12px; font-weight: 700; color: rgba(255,255,255,.5);
  letter-spacing: 1px; margin-bottom: 12px;
}
.q-text {
  font-size: 19px; font-weight: 600; line-height: 1.8; color: #fff;
  letter-spacing: .3px;
}

/* 操作按钮 */
.action-row {
  display: flex; justify-content: center; gap: 16px; flex-wrap: wrap;
}
.btn-mastered, .btn-failed, .btn-fav {
  display: flex; align-items: center; gap: 8px;
  padding: 14px 28px;
  border: none; border-radius: 14px;
  font-size: 15px; font-weight: 600; cursor: pointer;
  transition: all .2s;
}
.btn-mastered {
  background: #ecfdf5; color: #059669;
}
.btn-mastered:hover { background: #d1fae5; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(16,185,129,.2); }
.btn-mastered svg { width: 20px; height: 20px; }

.btn-failed {
  background: #fef2f2; color: #dc2626;
}
.btn-failed:hover { background: #fee2e2; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(239,68,68,.2); }
.btn-failed svg { width: 20px; height: 20px; }

.btn-fav {
  background: #f8fafc; color: #64748b;
}
.btn-fav:hover, .btn-fav.active { background: #fffbeb; color: #f59e0b; }
.btn-fav.active:hover { background: #fef3c7; }
.btn-fav svg { width: 20px; height: 20px; }

/* 答案区域 */
.answer-section { margin-top: 8px; }

.answer-divider {
  display: flex; align-items: center; gap: 12px; margin-bottom: 20px;
}
.divider-line { flex: 1; height: 1px; background: #e2e8f0; }
.divider-label {
  font-size: 13px; font-weight: 600; color: #94a3b8;
  display: flex; align-items: center; gap: 6px; white-space: nowrap;
}

.answer-content {
  background: #f8fafc;
  border-radius: 12px;
  padding: 24px;
  font-size: 15px; line-height: 2; color: #334155;
  white-space: pre-wrap; word-break: break-word;
  border: 1px solid #e2e8f0;
}

.answer-empty {
  text-align: center; padding: 32px;
  color: #94a3b8; font-size: 14px;
}

.answer-actions {
  display: flex; justify-content: center; gap: 12px; margin-top: 20px;
}
.btn-edit-answer, .btn-next {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 22px;
  border: none; border-radius: 10px;
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: all .2s;
}
.btn-edit-answer { background: #eef2ff; color: #6366f1; }
.btn-edit-answer:hover { background: #e0e7ff; }
.btn-next { background: #6366f1; color: #fff; }
.btn-next:hover { background: #4f46e5; box-shadow: 0 4px 12px rgba(99,102,241,.3); }

.review-meta {
  display: flex; justify-content: center; gap: 8px;
  margin-top: 18px; font-size: 13px; color: #94a3b8;
}
.dot { color: #cbd5e1; }

/* ── 右侧面板 ── */
.quiz-sidebar { width: 280px; flex-shrink: 0; }

.stats-panel {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,.04);
  position: sticky; top: 28px;
}
.panel-title {
  font-size: 15px; font-weight: 700; color: #1e293b;
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 20px;
}

.stats-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 10px;
}
.stat-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 14px 12px;
  text-align: center;
  transition: transform .15s;
}
.stat-card:hover { transform: translateY(-2px); }
.stat-num { font-size: 26px; font-weight: 800; line-height: 1.2; }
.stat-lbl { font-size: 12px; color: #94a3b8; margin-top: 4px; }

.today-badge {
  text-align: center; margin-top: 16px;
  padding: 10px; background: #f8fafc; border-radius: 10px;
  font-size: 13px; color: #64748b;
}
.today-badge strong { color: #6366f1; }

.rank-section { margin-top: 20px; }
.rank-section h4 { font-size: 13px; font-weight: 700; color: #1e293b; margin-bottom: 10px; }

.rank-row {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 0; border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
}
.rank-row:last-child { border-bottom: none; }
.rank-pos {
  width: 22px; height: 22px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700;
  background: #e2e8f0; color: #64748b; flex-shrink: 0;
}
.rank-pos.top1 { background: #fef2f2; color: #ef4444; }
.rank-pos.top2 { background: #fff7ed; color: #f97316; }
.rank-pos.top3 { background: #fefce8; color: #eab308; }
.rank-q {
  flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #475569;
}
.rank-count {
  font-weight: 600; color: #ef4444; flex-shrink: 0;
}

@media (max-width: 1024px) {
  .quiz-layout { flex-direction: column; }
  .quiz-sidebar { width: 100%; }
}
</style>
