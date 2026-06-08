<template>
  <div class="page">
    <div class="page-header">
      <div class="page-title">
        <span class="title-icon saved"><el-icon><StarFilled /></el-icon></span>
        <div>
          <h2>收藏题目</h2>
          <span class="header-count">共 {{ total }} 题</span>
        </div>
      </div>
      <div class="page-actions">
        <el-button type="primary" size="large" @click="$router.push('/?mode=favorites')">
          <el-icon><VideoPlay /></el-icon> 收藏题专项抽背
        </el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe class="data-table" max-height="calc(100vh - 260px)">
        <el-table-column prop="id" label="ID" width="55" />
        <el-table-column prop="question" label="问题" min-width="280">
          <template #default="{row}">
            <span class="table-ellipsis-cell" :title="row.question || ''">{{ row.question }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="105">
          <template #default="{row}"><span class="cell-cat">{{ row.category }}</span></template>
        </el-table-column>
        <el-table-column prop="mastered" label="掌握" width="65" align="center">
          <template #default="{row}">
            <span class="dot" :class="row.mastered?'on':'off'"></span>
          </template>
        </el-table-column>
        <el-table-column prop="wrongCount" label="错次" width="55" align="center" />
        <el-table-column prop="lastReviewTime" label="最近复习" width="145">
          <template #default="{row}">
            <span class="table-ellipsis-cell" :title="row.lastReviewTime || ''">
              {{ row.lastReviewTime || '暂未复习' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button size="small" text @click="showAnswer(row)">查看答案</el-button>
            <el-button size="small" text type="primary" @click="editAnswer(row)">编辑答案</el-button>
            <el-button size="small" text type="warning" @click="unfavorite(row)">取消收藏</el-button>
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
import { getFavorites, toggleFavorite, updateAnswer } from '../api'
import AnswerEditor from '../components/AnswerEditor.vue'
import MarkdownView from '../components/MarkdownView.vue'

const loading=ref(false), submitting=ref(false), tableData=ref([]), total=ref(0), currentPage=ref(1), pageSize=ref(20)
const answerVisible=ref(false), currentAnswer=ref('')
const editVisible=ref(false), editForm=ref(''), editingId=ref(null)

const fetchData = async () => {
  loading.value=true
  try { const res=await getFavorites({page:currentPage.value,size:pageSize.value}); tableData.value=res.data.content; total.value=res.data.totalElements }
  catch(e){} finally { loading.value=false }
}

const showAnswer = (row) => { currentAnswer.value=row.answer||''; answerVisible.value=true }
const editAnswer = (row) => { editingId.value=row.id; editForm.value=row.answer||''; editVisible.value=true }
const submitEdit = async () => { submitting.value=true; try { await updateAnswer(editingId.value,editForm.value); ElMessage.success('答案已更新'); editVisible.value=false; fetchData() } catch(e){} finally { submitting.value=false } }
const unfavorite = async (row) => { await toggleFavorite(row.id); ElMessage.success('已取消收藏'); fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.page { max-width: 1400px; margin: 0 auto; }

.header-count {
  font-family: var(--font-mono); font-size: 13px; font-weight: 700; color: var(--color-amber);
}

.toolbar { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; margin-bottom: 16px; }
.toolbar .el-input { width: 200px; }

.empty-fav { padding: 60px 20px; text-align: center; color: var(--color-ink-muted); }

@media (max-width: 640px) { .toolbar { flex-direction: column; } .toolbar .el-input, .toolbar .el-button { width: 100%; } }
</style>
