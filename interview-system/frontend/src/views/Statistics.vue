<template>
  <div class="page">
    <div class="page-header">
      <h2>统计分析</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-item" v-for="card in statCards" :key="card.label">
        <div class="stat-value" :style="{color:card.color}">{{ card.value }}</div>
        <div class="stat-label">{{ card.label }}</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-title">掌握情况</div>
        <div ref="masteryChart" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-title">各分类题目分布</div>
        <div ref="categoryChart" class="chart-box"></div>
      </div>
    </div>

    <!-- 下半区 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-title">🔥 错题排行榜</div>
        <div v-if="stats?.wrongRank?.length">
          <div v-for="(item,i) in stats.wrongRank" :key="item.id" class="rank-row">
            <span class="rank-num" :class="'r'+(i+1)">{{ i+1 }}</span>
            <span class="rank-q">{{ item.question }}</span>
            <span class="rank-cnt">{{ item.wrongCount }}次</span>
          </div>
        </div>
        <el-empty v-else description="暂无错题" :image-size="60" />
      </div>
      <div class="chart-card">
        <div class="chart-title">📋 最近复习记录</div>
        <el-table v-if="stats?.recentReviews?.length" :data="stats.recentReviews" stripe size="small">
          <el-table-column prop="question" label="题目" min-width="200" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="110">
            <template #default="{row}"><span class="cell-cat">{{ row.category }}</span></template>
          </el-table-column>
          <el-table-column prop="lastReviewTime" label="复习时间" width="170" />
        </el-table>
        <el-empty v-else description="暂无复习记录" :image-size="60" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { getStatistics } from '../api'
import * as echarts from 'echarts'

const stats = ref(null)
const masteryChart = ref(null)
const categoryChart = ref(null)

const statCards = computed(() => {
  if (!stats.value) return []
  return [
    { label:'总题目', value:stats.value.totalQuestions, color:'#6366f1' },
    { label:'已掌握', value:stats.value.masteredQuestions, color:'#10b981' },
    { label:'未掌握', value:stats.value.unmasteredQuestions, color:'#ef4444' },
    { label:'收藏', value:stats.value.favoriteQuestions, color:'#f59e0b' },
    { label:'今日复习', value:stats.value.todayReviewCount, color:'#64748b' },
  ]
})

const fetchData = async () => {
  try { const res = await getStatistics(); stats.value = res.data; await nextTick(); renderCharts() } catch(e){}
}

const renderCharts = () => {
  if (!stats.value) return

  if (masteryChart.value) {
    const c = echarts.init(masteryChart.value)
    c.setOption({
      tooltip:{trigger:'item'},
      series:[{
        type:'pie', radius:['50%','78%'], center:['50%','48%'],
        label:{show:true,formatter:'{b}\n{d}%'},
        emphasis:{label:{fontSize:16,fontWeight:'bold'}},
        data:[
          {value:stats.value.masteredQuestions,name:'已掌握',itemStyle:{color:'#10b981'}},
          {value:stats.value.unmasteredQuestions,name:'未掌握',itemStyle:{color:'#ef4444'}},
        ]
      }]
    })
  }

  if (categoryChart.value) {
    const c = echarts.init(categoryChart.value)
    const dist = stats.value.categoryDistribution||{}
    const keys = Object.keys(dist), vals = Object.values(dist)
    c.setOption({
      tooltip:{trigger:'axis'},
      grid:{left:'3%',right:'5%',bottom:'3%',containLabel:true},
      xAxis:{type:'category',data:keys,axisLabel:{rotate:35,fontSize:11}},
      yAxis:{type:'value',minInterval:1},
      series:[{
        type:'bar', data:vals,
        itemStyle:{borderRadius:[6,6,0,0],color:new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#a5b4fc'},{offset:1,color:'#6366f1'}])}
      }]
    })
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page { max-width: 1400px; margin: 0 auto; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1e293b; }

.stat-cards { display: flex; gap: 14px; margin-bottom: 20px; }
.stat-item {
  flex:1; background:#fff; border-radius:16px; padding:20px; text-align:center;
  box-shadow:0 1px 3px rgba(0,0,0,.04); transition: transform .15s;
}
.stat-item:hover { transform: translateY(-3px); }
.stat-value { font-size: 34px; font-weight: 800; line-height: 1.2; }
.stat-label { font-size: 13px; color: #94a3b8; margin-top: 6px; font-weight: 500; }

.charts-row { display: flex; gap: 16px; margin-bottom: 16px; }
.chart-card {
  flex:1; background:#fff; border-radius:16px; padding:20px;
  box-shadow:0 1px 3px rgba(0,0,0,.04);
}
.chart-title { font-size:15px; font-weight:700; color:#1e293b; margin-bottom:12px; }
.chart-box { height: 280px; }

.rank-row {
  display:flex; align-items:center; gap:8px; padding:10px 0; border-bottom:1px solid #f1f5f9; font-size:13px;
}
.rank-row:last-child { border-bottom:none; }
.rank-num {
  width:24px; height:24px; border-radius:7px; display:flex; align-items:center; justify-content:center;
  font-size:11px; font-weight:700; background:#e2e8f0; color:#64748b; flex-shrink:0;
}
.rank-num.r1 { background:#fef2f2; color:#ef4444; }
.rank-num.r2 { background:#fff7ed; color:#f97316; }
.rank-num.r3 { background:#fefce8; color:#eab308; }
.rank-q { flex:1; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; color:#475569; }
.rank-cnt { font-weight:600; color:#ef4444; flex-shrink:0; }
.cell-cat { font-size:11px; font-weight:600; padding:2px 8px; border-radius:5px; background:#eef2ff; color:#6366f1; }
</style>
