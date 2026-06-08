<template>
  <div class="special-page">
    <header class="page-header">
      <div>
        <h1>专项抽题</h1>
        <p>算法题按天去重抽取，SQL 题可无限随机练习。</p>
      </div>
      <el-button :loading="overviewLoading || algorithmLoading" @click="refreshSpecialPractice">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </header>

    <section class="practice-grid">
      <div class="practice-panel algorithm-panel">
        <div class="panel-head">
          <div>
            <h2>算法题</h2>
            <p>每次抽取 5 道；当天抽过的题不会再次出现。</p>
          </div>
          <el-button type="primary" :loading="algorithmLoading" @click="drawAlgorithmQuestions()">
            抽 5 道
          </el-button>
        </div>

        <div class="metric-row">
          <div>
            <strong>{{ overview.algorithmTotal }}</strong>
            <span>算法题总数</span>
          </div>
          <div>
            <strong>{{ overview.algorithmDrawnToday }}</strong>
            <span>今日已抽</span>
          </div>
          <div>
            <strong>{{ overview.algorithmRemainingToday }}</strong>
            <span>今日剩余</span>
          </div>
        </div>

        <el-empty
          v-if="!currentAlgorithmQuestion"
          :description="algorithmEmptyText"
          :image-size="72"
        />

        <div v-else class="algorithm-session">
          <div class="session-progress">
            <span>第 {{ algorithmIndex + 1 }} / {{ algorithmQuestions.length }} 题</span>
            <span>本轮抽取 {{ currentBatchSize }} 道</span>
          </div>

          <article class="question-item">
            <div class="question-topline">
              <span class="question-index">{{ algorithmIndex + 1 }}</span>
              <span class="question-id">#{{ currentAlgorithmQuestion.id }}</span>
              <span class="question-category">{{ currentAlgorithmQuestion.category }}</span>
              <span v-if="currentAlgorithmQuestion.mastered" class="question-status done">已掌握</span>
            </div>

            <h3>{{ currentAlgorithmQuestion.question }}</h3>

            <div v-if="!algorithmAnswerVisible" class="review-actions">
              <button class="btn-mastered" :disabled="algorithmReviewing" @click="handleAlgorithmReview(true)">
                <el-icon><Check /></el-icon>
                <span>掌握了</span>
              </button>
              <button class="btn-failed" :disabled="algorithmReviewing" @click="handleAlgorithmReview(false)">
                <el-icon><Close /></el-icon>
                <span>没掌握</span>
              </button>
            </div>

            <div v-else class="answer-section">
              <div class="answer-title">参考答案</div>
              <MarkdownView :content="currentAlgorithmQuestion.answer" empty-text="当前题目暂无答案" />

              <div v-if="!algorithmCompleted" class="answer-actions">
                <el-button type="primary" @click="goNextAlgorithmQuestion">
                  下一题
                  <el-icon><Right /></el-icon>
                </el-button>
              </div>
            </div>

            <div v-if="algorithmCompleted" class="completion-panel">
              <h3>已经做完 {{ algorithmQuestions.length }} 道题目了</h3>
              <p v-if="overview.algorithmRemainingToday">还需要抽取几道题目？</p>
              <p v-else>今天算法题已经抽完。</p>
              <div v-if="overview.algorithmRemainingToday" class="extra-draw-options">
                <el-button
                  v-for="count in extraDrawOptions"
                  :key="count"
                  :disabled="algorithmLoading || count > overview.algorithmRemainingToday"
                  @click="drawAlgorithmQuestions(count)"
                >
                  {{ count }} 道
                </el-button>
              </div>
            </div>
          </article>
        </div>
      </div>

      <div class="practice-panel sql-panel">
        <div class="panel-head">
          <div>
            <h2>SQL 题</h2>
            <p>随机抽取 1 道，不限制当天抽取次数。</p>
          </div>
          <el-button type="primary" :loading="sqlLoading" @click="drawSqlQuestion">
            抽 SQL
          </el-button>
        </div>

        <div class="metric-row compact">
          <div>
            <strong>{{ overview.sqlTotal }}</strong>
            <span>SQL 题总数</span>
          </div>
          <div>
            <strong>不限</strong>
            <span>抽取次数</span>
          </div>
        </div>

        <el-empty
          v-if="!sqlQuestion"
          :description="sqlEmptyText"
          :image-size="72"
        />

        <article v-else class="question-item sql-question">
          <div class="question-topline">
            <span class="question-id">#{{ sqlQuestion.id }}</span>
            <span class="question-category">{{ sqlQuestion.category }}</span>
          </div>
          <h3>{{ sqlQuestion.question }}</h3>
          <details>
            <summary>查看答案</summary>
            <MarkdownView :content="sqlQuestion.answer" empty-text="当前题目暂无答案" />
          </details>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  drawSpecialAlgorithm,
  drawSpecialSql,
  getSpecialAlgorithmState,
  getSpecialPracticeOverview,
  submitSpecialAlgorithmReview
} from '../api'
import MarkdownView from '../components/MarkdownView.vue'

const overviewLoading = ref(false)
const algorithmLoading = ref(false)
const algorithmReviewing = ref(false)
const sqlLoading = ref(false)
const algorithmQuestions = ref([])
const algorithmIndex = ref(0)
const algorithmAnswerVisible = ref(false)
const algorithmCompleted = ref(false)
const algorithmSessionDate = ref('')
const algorithmBatchNo = ref(null)
const currentBatchSize = ref(5)
const sqlQuestion = ref(null)
const extraDrawOptions = [1, 2, 3, 4, 5]
const algorithmStoragePrefix = 'special-algorithm-session'
const overview = ref({
  algorithmTotal: 0,
  algorithmDrawnToday: 0,
  algorithmRemainingToday: 0,
  algorithmDrawSize: 5,
  sqlTotal: 0,
  sqlUnlimited: true
})

const algorithmEmptyText = computed(() => {
  if (!overview.value.algorithmTotal) return '算法分类下暂无题目'
  if (!overview.value.algorithmRemainingToday) return '今天算法题已经抽完'
  return '点击“抽 5 道”开始'
})

const sqlEmptyText = computed(() => {
  if (!overview.value.sqlTotal) return 'SQL 分类下暂无题目'
  return '点击“抽 SQL”开始'
})

const currentAlgorithmQuestion = computed(() => algorithmQuestions.value[algorithmIndex.value] || null)
const isLastAlgorithmQuestion = computed(() => {
  return algorithmQuestions.value.length > 0 && algorithmIndex.value >= algorithmQuestions.value.length - 1
})

const fetchOverview = async () => {
  overviewLoading.value = true
  try {
    overview.value = (await getSpecialPracticeOverview()).data
  } catch (e) {
  } finally {
    overviewLoading.value = false
  }
}

const sessionKey = (date = algorithmSessionDate.value) => `${algorithmStoragePrefix}:${date}`

const questionIds = (questions = algorithmQuestions.value) => questions.map(q => q.id)

const sameIds = (a = [], b = []) => {
  return a.length === b.length && a.every((id, index) => id === b[index])
}

const safeIndex = (index, length) => Math.min(Math.max(Number(index) || 0, 0), Math.max(length - 1, 0))

const readSavedAlgorithmSession = (date, batchNo, ids) => {
  if (!date || !ids.length) return null
  try {
    const saved = JSON.parse(localStorage.getItem(sessionKey(date)) || 'null')
    if (!saved || saved.batchNo !== batchNo || !sameIds(saved.questionIds, ids)) return null
    return saved
  } catch (e) {
    return null
  }
}

const saveAlgorithmSession = () => {
  if (!algorithmSessionDate.value || !algorithmQuestions.value.length) return
  localStorage.setItem(sessionKey(), JSON.stringify({
    batchNo: algorithmBatchNo.value,
    questionIds: questionIds(),
    currentIndex: algorithmIndex.value,
    answerVisible: algorithmAnswerVisible.value,
    completed: algorithmCompleted.value
  }))
}

const applyAlgorithmSession = (data, restoreSaved = false) => {
  const questions = data.questions || []
  const ids = questionIds(questions)
  const batchNo = data.batchNo ?? null
  const date = data.drawDate || new Date().toISOString().slice(0, 10)
  const saved = restoreSaved ? readSavedAlgorithmSession(date, batchNo, ids) : null
  const serverCompleted = Boolean(data.completed)

  algorithmQuestions.value = questions
  algorithmSessionDate.value = date
  algorithmBatchNo.value = batchNo
  currentBatchSize.value = data.returnedCount || questions.length || data.requestedCount || 5

  if (!questions.length) {
    algorithmIndex.value = 0
    algorithmAnswerVisible.value = false
    algorithmCompleted.value = false
    return
  }

  algorithmIndex.value = safeIndex(
    saved ? saved.currentIndex : data.currentIndex,
    questions.length
  )
  algorithmCompleted.value = serverCompleted || Boolean(saved?.completed)
  algorithmAnswerVisible.value = algorithmCompleted.value || Boolean(saved?.answerVisible)
  saveAlgorithmSession()
}

const restoreAlgorithmSession = async () => {
  algorithmLoading.value = true
  try {
    const data = (await getSpecialAlgorithmState()).data
    applyAlgorithmSession(data, true)
    overview.value = {
      ...overview.value,
      algorithmTotal: data.total,
      algorithmDrawnToday: data.drawnToday,
      algorithmRemainingToday: data.remainingToday
    }
  } catch (e) {
  } finally {
    algorithmLoading.value = false
  }
}

const refreshSpecialPractice = async () => {
  await fetchOverview()
  await restoreAlgorithmSession()
}

const normalizeDrawCount = (count) => {
  const value = Number(count)
  return Number.isInteger(value) ? Math.min(5, Math.max(1, value)) : 5
}

const drawAlgorithmQuestions = async (count = 5) => {
  const drawCount = normalizeDrawCount(count)
  algorithmLoading.value = true
  try {
    const data = (await drawSpecialAlgorithm(drawCount)).data
    applyAlgorithmSession(data, Boolean(data.restored))
    overview.value = {
      ...overview.value,
      algorithmTotal: data.total,
      algorithmDrawnToday: data.drawnToday,
      algorithmRemainingToday: data.remainingToday
    }
    if (!data.returnedCount) {
      ElMessage.info('今天算法题已经抽完')
    } else if (data.returnedCount < data.requestedCount) {
      ElMessage.warning(`今天仅剩 ${data.returnedCount} 道算法题`)
    }
  } catch (e) {
    algorithmQuestions.value = []
    algorithmIndex.value = 0
    algorithmAnswerVisible.value = false
    algorithmCompleted.value = false
  } finally {
    algorithmLoading.value = false
  }
}

const handleAlgorithmReview = async (mastered) => {
  if (!currentAlgorithmQuestion.value || algorithmReviewing.value) return
  algorithmReviewing.value = true
  try {
    const res = await submitSpecialAlgorithmReview(currentAlgorithmQuestion.value.id, { mastered })
    algorithmQuestions.value[algorithmIndex.value] = res.data
    algorithmAnswerVisible.value = true
    if (isLastAlgorithmQuestion.value) {
      algorithmCompleted.value = true
      ElMessage.success(`已经做完 ${algorithmQuestions.value.length} 道题目了`)
    } else {
      ElMessage.success(mastered ? '已标记为掌握' : '已标记为没掌握，下次加油！')
    }
    saveAlgorithmSession()
  } catch (e) {
  } finally {
    algorithmReviewing.value = false
  }
}

const goNextAlgorithmQuestion = () => {
  if (isLastAlgorithmQuestion.value) {
    algorithmCompleted.value = true
    saveAlgorithmSession()
    return
  }
  algorithmIndex.value += 1
  algorithmAnswerVisible.value = false
  saveAlgorithmSession()
}

const drawSqlQuestion = async () => {
  sqlLoading.value = true
  try {
    const data = (await drawSpecialSql()).data
    sqlQuestion.value = data.question
    overview.value = {
      ...overview.value,
      sqlTotal: data.total
    }
    if (!data.question) ElMessage.info('SQL 分类下暂无题目')
  } catch (e) {
    sqlQuestion.value = null
  } finally {
    sqlLoading.value = false
  }
}

onMounted(refreshSpecialPractice)
</script>

<style scoped>
.special-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 28px;
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: var(--color-ink-strong);
  font-family: var(--font-heading);
  font-size: 32px;
  line-height: 1.2;
}

.page-header p {
  margin-top: 6px;
  color: var(--color-ink-muted);
  font-family: var(--font-ui);
}

.practice-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 0.85fr);
  gap: 18px;
}

.practice-panel {
  min-width: 0;
  padding: 20px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.panel-head h2 {
  margin: 0;
  color: var(--color-ink-strong);
  font-family: var(--font-heading);
  font-size: 22px;
}

.panel-head p {
  margin-top: 4px;
  color: var(--color-ink-muted);
  font-family: var(--font-ui);
  font-size: 13px;
}

.metric-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin: 18px 0;
}

.metric-row.compact {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.metric-row > div {
  min-width: 0;
  padding: 10px 12px;
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.metric-row strong {
  display: block;
  color: var(--color-accent);
  font-family: var(--font-ui);
  font-size: 22px;
  line-height: 1.2;
}

.metric-row span {
  display: block;
  margin-top: 2px;
  color: var(--color-ink-muted);
  font-family: var(--font-ui);
  font-size: 12px;
}

.question-list,
.algorithm-session {
  display: grid;
  gap: 12px;
}

.session-progress {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 12px;
  color: var(--color-ink-muted);
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 13px;
  font-weight: 700;
}

.question-item {
  padding: 14px;
  background: var(--color-surface-warm);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.question-topline {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: var(--color-ink-muted);
  font-family: var(--font-ui);
  font-size: 12px;
  font-weight: 700;
}

.question-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  color: #fff;
  background: var(--color-accent);
  border-radius: 50%;
}

.question-id,
.question-category,
.question-status {
  padding: 2px 7px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.question-status.done {
  color: var(--color-green);
  background: var(--color-green-soft);
  border-color: rgba(122, 154, 126, 0.25);
}

.question-item h3 {
  margin: 0;
  color: var(--color-ink-strong);
  font-size: 16px;
  line-height: 1.55;
}

.question-item details {
  margin-top: 12px;
  font-family: var(--font-ui);
}

.question-item summary {
  width: fit-content;
  cursor: pointer;
  color: var(--color-accent);
  font-weight: 700;
}

.review-actions,
.answer-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.btn-mastered,
.btn-failed {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-width: 112px;
  height: 40px;
  padding: 0 16px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 14px;
  font-weight: 800;
  transition: all 0.2s var(--ease-out);
}

.btn-mastered:disabled,
.btn-failed:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.btn-mastered {
  color: var(--color-green);
  background: var(--color-green-soft);
  border-color: rgba(122, 154, 126, 0.25);
}

.btn-mastered:not(:disabled):hover {
  background: #d8f0e2;
  transform: translateY(-1px);
}

.btn-failed {
  color: var(--color-red);
  background: var(--color-red-soft);
  border-color: rgba(192, 114, 99, 0.25);
}

.btn-failed:not(:disabled):hover {
  background: #fde8e5;
  transform: translateY(-1px);
}

.answer-section {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--color-border);
}

.answer-title {
  margin-bottom: 10px;
  color: var(--color-ink-muted);
  font-family: var(--font-ui);
  font-size: 13px;
  font-weight: 800;
}

.question-item :deep(.markdown-view) {
  margin-top: 10px;
  color: var(--color-ink);
  font-family: var(--font-body);
}

.completion-panel {
  margin-top: 16px;
  padding: 14px;
  background: var(--color-accent-soft);
  border: 1px solid rgba(122, 147, 168, 0.26);
  border-radius: var(--radius-md);
}

.completion-panel h3 {
  margin: 0;
  color: var(--color-ink-strong);
  font-family: var(--font-heading);
  font-size: 17px;
  line-height: 1.35;
}

.completion-panel p {
  margin: 6px 0 0;
  color: var(--color-ink-muted);
  font-family: var(--font-ui);
  font-size: 13px;
}

.extra-draw-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.sql-question {
  margin-top: 18px;
}

@media (max-width: 860px) {
  .special-page {
    padding: 18px;
  }

  .page-header,
  .panel-head {
    align-items: stretch;
    flex-direction: column;
  }

  .practice-grid {
    grid-template-columns: 1fr;
  }

  .metric-row,
  .metric-row.compact {
    grid-template-columns: 1fr;
  }

  .review-actions,
  .answer-actions,
  .extra-draw-options {
    flex-direction: column;
  }

  .btn-mastered,
  .btn-failed,
  .extra-draw-options .el-button {
    width: 100%;
    margin-left: 0;
  }
}
</style>
