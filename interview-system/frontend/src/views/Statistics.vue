<template>
  <div class="page">
    <div class="page-header">
      <div class="page-title">
        <span class="title-icon"><el-icon><DataAnalysis /></el-icon></span>
        <div>
          <h2>学习诊断</h2>
          <span v-if="stats" class="header-desc">
            掌握率 {{ stats.masteryRate || 0 }}% · 今日复习 {{ stats.todayReviewCount }} 题
          </span>
        </div>
      </div>
    </div>

    <div class="stat-cards">
      <div class="stat-item" v-for="card in statCards" :key="card.label" :class="card.tone">
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-label">{{ card.label }}</div>
      </div>
    </div>

    <div class="charts-row primary-row">
      <div class="chart-card wide">
        <div class="chart-head">
          <div>
            <div class="chart-title">分类掌握率</div>
            <div class="chart-subtitle">按分类统计已掌握占比</div>
          </div>
        </div>
        <div
          v-show="hasCategoryMastery"
          ref="categoryMasteryChart"
          class="chart-box category-box"
          :style="categoryChartStyle"
        ></div>
        <el-empty v-if="!hasCategoryMastery" description="暂无分类数据" :image-size="64" />
      </div>

      <div class="chart-card">
        <div class="chart-head">
          <div>
            <div class="chart-title">整体掌握情况</div>
            <div class="chart-subtitle">已掌握 / 未掌握</div>
          </div>
        </div>
        <div v-show="hasQuestions" ref="masteryChart" class="chart-box compact-box"></div>
        <el-empty v-if="!hasQuestions" description="暂无题目数据" :image-size="64" />
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card wide">
        <div class="chart-head">
          <div>
            <div class="chart-title">最近 7 天复习趋势</div>
            <div class="chart-subtitle">复习题数与错题增长</div>
          </div>
        </div>
        <div v-show="hasTrendData" ref="trendChart" class="chart-box"></div>
        <el-empty v-if="!hasTrendData" description="最近 7 天暂无复习数据" :image-size="64" />
      </div>

      <div class="chart-card">
        <div class="chart-head">
          <div>
            <div class="chart-title">薄弱分类排行</div>
            <div class="chart-subtitle">综合未掌握、错题数与掌握率</div>
          </div>
        </div>
        <div v-if="weakCategories.length" class="weak-list">
          <div v-for="(item, index) in weakCategories" :key="item.category" class="weak-row">
            <span class="weak-index" :class="'rank' + (index + 1)">{{ index + 1 }}</span>
            <div class="weak-main">
              <div class="weak-top">
                <span class="weak-name">{{ item.category }}</span>
                <span class="weak-score">风险 {{ item.riskScore }}</span>
              </div>
              <div class="weak-meta">
                {{ item.unmastered }} 未掌握 · {{ item.wrongCount }} 错题 · 掌握率 {{ item.masteryRate }}%
              </div>
              <div class="weak-bar">
                <span :style="{ width: `${Math.max(0, Math.min(100, item.masteryRate || 0))}%` }"></span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无薄弱分类" :image-size="64" />
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-head">
          <div>
            <div class="chart-title">错题排行榜</div>
            <div class="chart-subtitle">按累计未掌握次数排序</div>
          </div>
        </div>
        <div v-if="stats?.wrongRank?.length">
          <div v-for="(item, i) in stats.wrongRank" :key="item.id" class="rank-row">
            <span class="rank-num" :class="'r' + (i + 1)">{{ i + 1 }}</span>
            <span class="rank-q">{{ item.question }}</span>
            <span class="rank-cnt">{{ item.wrongCount }}次</span>
          </div>
        </div>
        <el-empty v-else description="暂无错题" :image-size="64" />
      </div>

      <div class="chart-card">
        <div class="chart-head">
          <div>
            <div class="chart-title">最近复习记录</div>
            <div class="chart-subtitle">最近一次复习的题目</div>
          </div>
        </div>
        <el-table v-if="stats?.recentReviews?.length" :data="stats.recentReviews" stripe size="small">
          <el-table-column prop="question" label="题目" min-width="200">
            <template #default="{row}">
              <span class="table-ellipsis-cell" :title="row.question || ''">{{ row.question }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="110">
            <template #default="{row}"><span class="cell-cat">{{ row.category }}</span></template>
          </el-table-column>
          <el-table-column prop="lastReviewTime" label="复习时间" width="170" />
        </el-table>
        <el-empty v-else description="暂无复习记录" :image-size="64" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { getStatistics } from '../api'
import * as echarts from 'echarts/core'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  GraphicComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([PieChart, BarChart, LineChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent, GraphicComponent, CanvasRenderer])

const stats = ref(null)
const masteryChart = ref(null)
const categoryMasteryChart = ref(null)
const trendChart = ref(null)
let masteryInstance = null
let categoryMasteryInstance = null
let trendInstance = null

const hasQuestions = computed(() => (stats.value?.totalQuestions || 0) > 0)
const categoryMastery = computed(() => stats.value?.categoryMastery || [])
const weakCategories = computed(() => stats.value?.weakCategoryRank || [])
const reviewTrend = computed(() => stats.value?.reviewTrend || [])
const wrongGrowthTrend = computed(() => stats.value?.wrongGrowthTrend || [])
const hasCategoryMastery = computed(() => categoryMastery.value.length > 0)
const categoryChartStyle = computed(() => ({
  height: `${Math.min(620, Math.max(320, categoryMastery.value.length * 24 + 90))}px`
}))
const hasTrendData = computed(() => {
  const reviewTotal = reviewTrend.value.reduce((sum, item) => sum + (item.value || 0), 0)
  const wrongTotal = wrongGrowthTrend.value.reduce((sum, item) => sum + (item.value || 0), 0)
  return reviewTotal + wrongTotal > 0
})

const statCards = computed(() => {
  if (!stats.value) return []
  return [
    { label: '总题目', value: stats.value.totalQuestions, tone: 'total' },
    { label: '掌握率', value: `${stats.value.masteryRate || 0}%`, tone: 'rate' },
    { label: '已掌握', value: stats.value.masteredQuestions, tone: 'mastered' },
    { label: '未掌握', value: stats.value.unmasteredQuestions, tone: 'unmastered' },
    { label: '今日复习', value: stats.value.todayReviewCount, tone: 'today' },
    { label: '收藏', value: stats.value.favoriteQuestions, tone: 'favorite' },
  ]
})

const fetchData = async () => {
  try {
    const res = await getStatistics()
    stats.value = res.data
    await nextTick()
    renderCharts()
  } catch (e) {}
}

const renderCharts = () => {
  if (!stats.value) return
  renderMasteryChart()
  renderCategoryMasteryChart()
  renderTrendChart()
  requestAnimationFrame(resizeCharts)
}

const renderMasteryChart = () => {
  if (!masteryChart.value || !hasQuestions.value) return
  masteryInstance = masteryInstance || echarts.init(masteryChart.value)
  masteryInstance.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['52%', '78%'],
      center: ['50%', '50%'],
      label: { show: true, formatter: '{b}\n{d}%', color: '#344054', fontWeight: 700 },
      data: [
        { value: stats.value.masteredQuestions, name: '已掌握', itemStyle: { color: '#16a34a' } },
        { value: stats.value.unmasteredQuestions, name: '未掌握', itemStyle: { color: '#dc2626' } },
      ]
    }]
  })
}

const renderCategoryMasteryChart = () => {
  if (!categoryMasteryChart.value || !hasCategoryMastery.value) return
  categoryMasteryInstance = categoryMasteryInstance || echarts.init(categoryMasteryChart.value)
  const items = [...categoryMastery.value].sort((a, b) => a.masteryRate - b.masteryRate)
  categoryMasteryInstance.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const item = items[params[0].dataIndex]
        return `${item.category}<br/>掌握率：${item.masteryRate}%<br/>已掌握：${item.mastered}/${item.total}<br/>错题：${item.wrongCount}`
      }
    },
    grid: { left: 90, right: 28, bottom: 18, top: 10, containLabel: false },
    xAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: { formatter: '{value}%', color: '#667085' },
      splitLine: { lineStyle: { color: '#edf1f3' } }
    },
    yAxis: {
      type: 'category',
      data: items.map(item => item.category),
      axisLabel: { color: '#667085', fontSize: 11 },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    series: [{
      type: 'bar',
      data: items.map(item => item.masteryRate),
      barMaxWidth: 18,
      label: { show: true, position: 'right', formatter: '{c}%', color: '#344054', fontWeight: 700 },
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#2dd4bf' },
          { offset: 1, color: '#0f766e' }
        ])
      }
    }]
  })
}

const renderTrendChart = () => {
  if (!trendChart.value || !hasTrendData.value) return
  trendInstance = trendInstance || echarts.init(trendChart.value)
  const labels = reviewTrend.value.map(item => item.label)
  trendInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, right: 8, textStyle: { color: '#667085', fontWeight: 600 } },
    grid: { left: 36, right: 20, bottom: 28, top: 42, containLabel: true },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { color: '#667085' },
      axisLine: { lineStyle: { color: '#dfe5e8' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#667085' },
      splitLine: { lineStyle: { color: '#edf1f3' } }
    },
    series: [
      {
        name: '复习题数',
        type: 'line',
        smooth: true,
        symbolSize: 7,
        data: reviewTrend.value.map(item => item.value),
        lineStyle: { width: 3, color: '#0f766e' },
        itemStyle: { color: '#0f766e' },
        areaStyle: { color: 'rgba(15, 118, 110, 0.12)' }
      },
      {
        name: '错题增长',
        type: 'bar',
        barMaxWidth: 24,
        data: wrongGrowthTrend.value.map(item => item.value),
        itemStyle: { borderRadius: [5, 5, 0, 0], color: '#dc2626' }
      }
    ]
  })
}

const resizeCharts = () => {
  masteryInstance?.resize()
  categoryMasteryInstance?.resize()
  trendInstance?.resize()
}

onMounted(async () => {
  await fetchData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  masteryInstance?.dispose()
  categoryMasteryInstance?.dispose()
  trendInstance?.dispose()
  masteryInstance = null
  categoryMasteryInstance = null
  trendInstance = null
})
</script>

<style scoped>
.page { max-width: 1400px; margin: 0 auto; }

.cell-cat {
  display: inline-flex; align-items: center; min-height: 22px;
  padding: 2px 8px;
  color: var(--color-blue); font-size: 11px; font-weight: 700;
  background: var(--color-blue-soft); border-radius: var(--radius-sm);
}

.stat-cards {
  display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; margin-bottom: 20px;
}
.stat-item {
  padding: 18px 16px;
  background: var(--color-bg-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md);
  text-align: center;
}
.stat-item.total { border-top: 3px solid var(--color-blue); }
.stat-item.rate { border-top: 3px solid var(--color-accent); }
.stat-item.mastered { border-top: 3px solid var(--color-green); }
.stat-item.unmastered { border-top: 3px solid var(--color-red); }
.stat-item.today { border-top: 3px solid var(--color-amber); }
.stat-item.favorite { border-top: 3px solid var(--color-accent-dim); }
.stat-value { display: block; font-family: var(--font-mono); font-size: 26px; font-weight: 700; color: var(--color-ink-strong); line-height: 1.1; }
.stat-label { display: block; margin-top: 6px; font-family: var(--font-ui); font-size: 12px; font-weight: 600; color: var(--color-ink-muted); }

.charts-row { display: flex; gap: 16px; margin-bottom: 16px; }
.charts-row.primary-row { margin-bottom: 16px; }
.chart-card {
  flex: 1; min-width: 0;
  padding: 20px;
  background: var(--color-bg-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg);
}
.chart-card.wide { flex: 2; }
.chart-head { margin-bottom: 14px; }
.chart-title { font-family: var(--font-heading); font-size: 15px; font-weight: 800; color: var(--color-ink-strong); }
.chart-subtitle { margin-top: 4px; font-size: 12px; color: var(--color-ink-muted); font-family: var(--font-ui); }
.chart-box { width: 100%; height: 292px; }
.category-box { height: 330px; }
.compact-box { height: 300px; }

.weak-list { display: flex; flex-direction: column; gap: 9px; }
.weak-row {
  display: flex; gap: 10px; padding: 10px;
  background: var(--color-bg-elevated); border: 1px solid var(--color-border); border-radius: var(--radius-md);
}
.weak-index, .rank-num {
  display: flex; flex-shrink: 0; align-items: center; justify-content: center;
  width: 24px; height: 24px;
  font-family: var(--font-mono); font-size: 11px; font-weight: 800;
  background: var(--color-bg-hover); border-radius: var(--radius-sm); color: var(--color-ink-muted);
}
.weak-index.rank1, .rank-num.r1 { color: var(--color-red); background: var(--color-red-soft); }
.weak-index.rank2, .rank-num.r2 { color: var(--color-amber); background: var(--color-amber-soft); }
.weak-index.rank3, .rank-num.r3 { color: var(--color-accent); background: var(--color-accent-glow); }
.weak-main { flex: 1; min-width: 0; }
.weak-top { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.weak-name { overflow: hidden; color: var(--color-ink-strong); font-size: 13px; font-weight: 800; text-overflow: ellipsis; white-space: nowrap; }
.weak-meta { display: flex; gap: 14px; margin-top: 4px; font-size: 12px; color: var(--color-ink-muted); }
.risk-badge { flex-shrink: 0; padding: 2px 8px; font-size: 11px; font-weight: 700; border-radius: var(--radius-sm); }
.risk-badge.high { color: var(--color-red); background: var(--color-red-soft); }
.risk-badge.medium { color: var(--color-amber); background: var(--color-amber-soft); }
.risk-badge.low { color: var(--color-green); background: var(--color-green-soft); }

.rank-list, .review-list { display: flex; flex-direction: column; gap: 6px; }
.rank-item, .review-item {
  display: flex; align-items: center; gap: 10px; padding: 10px;
  background: var(--color-bg-elevated); border: 1px solid var(--color-border); border-radius: var(--radius-md);
}
.rank-q, .review-q { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; color: var(--color-ink); }
.rank-val { flex-shrink: 0; font-family: var(--font-mono); font-size: 13px; font-weight: 700; color: var(--color-red); }
.review-time { flex-shrink: 0; font-size: 11px; color: var(--color-ink-muted); font-family: var(--font-mono); }

@media (max-width: 1080px) {
  .stat-cards { grid-template-columns: repeat(3, 1fr); }
  .charts-row { flex-direction: column; }
}
@media (max-width: 640px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
  .chart-card { padding: 14px; }
}
</style>
