<template>
  <div class="page">
    <div class="page-header">
      <div class="page-title">
        <span class="title-icon alert"><el-icon><WarningFilled /></el-icon></span>
        <div>
          <h2>错题集</h2>
          <span class="header-desc">共 {{ total }} 题未掌握，按错题次数从高到低排列</span>
        </div>
      </div>
      <div class="page-actions">
        <el-button type="primary" size="large" @click="$router.push('/?mode=unmastered')">
          <el-icon><VideoPlay /></el-icon> 错题专项抽背
        </el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe class="data-table" max-height="calc(100vh - 260px)">
        <el-table-column prop="id" label="ID" width="55" />
        <el-table-column prop="question" label="问题" min-width="280" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="105">
          <template #default="{row}"><span class="cell-cat">{{ row.category }}</span></template>
        </el-table-column>
        <el-table-column prop="wrongCount" label="错题次数" width="100" align="center">
          <template #default="{row}">
            <span class="wrong-badge" :class="{ heavy: row.wrongCount>=3 }">{{ row.wrongCount }} 次</span>
          </template>
        </el-table-column>
        <el-table-column prop="reviewCount" label="复习" width="60" align="center" />
        <el-table-column prop="weight" label="权重" width="60" align="center" />
        <el-table-column prop="favorite" label="收藏" width="60" align="center">
          <template #default="{row}">
            <el-icon v-if="row.favorite" class="favorite-icon"><StarFilled /></el-icon>
            <span v-else class="empty-mark">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastReviewTime" label="最近复习" width="145" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button size="small" text @click="showAnswer(row)">查看答案</el-button>
            <el-button size="small" text type="success" @click="markMastered(row)">标记掌握</el-button>
            <el-button size="small" text type="primary" @click="editAnswer(row)">编辑答案</el-button>
            <el-button v-if="!row.favorite" size="small" text type="warning" @click="addFavorite(row)">收藏</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
          :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="answerVisible" title="参考答案" width="550px">
      <MarkdownView class="answer-box" :content="currentAnswer" />
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑答案" width="620px">
      <AnswerEditor v-model="editForm" :rows="8" placeholder="请输入答案" />
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUnmastered, submitReview, toggleFavorite, updateAnswer } from '../api'
import AnswerEditor from '../components/AnswerEditor.vue'
import MarkdownView from '../components/MarkdownView.vue'

const loading=ref(false), submitting=ref(false), tableData=ref([]), total=ref(0), currentPage=ref(1), pageSize=ref(20)
const answerVisible=ref(false), currentAnswer=ref('')
const editVisible=ref(false), editForm=ref(''), editingId=ref(null)

const fetchData = async () => {
  loading.value=true
  try { const res=await getUnmastered({page:currentPage.value,size:pageSize.value}); tableData.value=res.data.content; total.value=res.data.totalElements }
  catch(e){} finally { loading.value=false }
}

const showAnswer = (row) => { currentAnswer.value=row.answer||''; answerVisible.value=true }
const markMastered = async (row) => { await submitReview(row.id,{mastered:true}); ElMessage.success('已标记为掌握'); fetchData() }
const editAnswer = (row) => { editingId.value=row.id; editForm.value=row.answer||''; editVisible.value=true }
const submitEdit = async () => { submitting.value=true; try { await updateAnswer(editingId.value,editForm.value); ElMessage.success('答案已更新'); editVisible.value=false; fetchData() } catch(e){} finally { submitting.value=false } }
const addFavorite = async (row) => { await toggleFavorite(row.id); ElMessage.success('已收藏'); fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.page { max-width: 1400px; margin: 0 auto; }

.header-count {
  font-family: var(--font-mono); font-size: 13px; font-weight: 700; color: var(--color-red);
}

.toolbar { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; margin-bottom: 16px; }
.toolbar .el-input { width: 200px; }

.cell-cat { color: var(--color-blue); font-weight: 600; font-size: 13px; }
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; }
.dot.on { background: var(--color-green); box-shadow: 0 0 6px rgba(122,154,126,0.4); }
.dot.off { background: var(--color-ink-faint); }
.wrong-num { color: var(--color-red); font-weight: 700; font-family: var(--font-mono); }

.pagination-row {
  display: flex; justify-content: center; padding-top: 18px; margin-top: 18px;
  border-top: 1px solid var(--color-border);
}

.table-card {
  background: var(--color-bg-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); padding: 22px;
}

@media (max-width: 640px) {
  .toolbar { flex-direction: column; }
  .toolbar .el-input, .toolbar .el-button { width: 100%; }
  .table-card { padding: 14px; }
}
</style>
