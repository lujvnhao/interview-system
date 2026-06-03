<template>
  <div class="page">
    <div class="page-header">
      <h2>⭐ 收藏题目</h2>
      <span class="header-count" v-if="total">共 {{ total }} 题</span>
      <div style="flex:1"></div>
      <el-button type="primary" size="large" @click="$router.push('/?mode=favorites')">
        <el-icon><VideoPlay /></el-icon> 收藏题专项抽背
      </el-button>
    </div>

    <div class="card">
      <el-table :data="tableData" v-loading="loading" stripe max-height="calc(100vh - 260px)">
        <el-table-column prop="id" label="ID" width="55" />
        <el-table-column prop="question" label="问题" min-width="280" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="105">
          <template #default="{row}"><span class="cell-cat">{{ row.category }}</span></template>
        </el-table-column>
        <el-table-column prop="mastered" label="掌握" width="65" align="center">
          <template #default="{row}">
            <span class="dot" :class="row.mastered?'on':'off'"></span>
          </template>
        </el-table-column>
        <el-table-column prop="wrongCount" label="错次" width="55" align="center" />
        <el-table-column prop="lastReviewTime" label="最近复习" width="145" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button size="small" text @click="showAnswer(row)">查看答案</el-button>
            <el-button size="small" text type="primary" @click="editAnswer(row)">编辑答案</el-button>
            <el-button size="small" text type="warning" @click="unfavorite(row)">取消收藏</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:16px;text-align:right">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
          :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="answerVisible" title="参考答案" width="550px">
      <div class="answer-box">{{ currentAnswer || '暂无答案' }}</div>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑答案" width="550px">
      <el-input v-model="editForm" type="textarea" :rows="8" placeholder="请输入答案" />
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

const loading=ref(false), submitting=ref(false), tableData=ref([]), total=ref(0), currentPage=ref(1), pageSize=ref(20)
const answerVisible=ref(false), currentAnswer=ref('')
const editVisible=ref(false), editForm=ref(''), editingId=ref(null)

const fetchData = async () => {
  loading.value=true
  try { const res=await getFavorites({page:currentPage.value,size:pageSize.value}); tableData.value=res.data.content; total.value=res.data.totalElements }
  catch(e){} finally { loading.value=false }
}

const showAnswer = (row) => { currentAnswer.value=row.answer||'暂无答案'; answerVisible.value=true }
const editAnswer = (row) => { editingId.value=row.id; editForm.value=row.answer||''; editVisible.value=true }
const submitEdit = async () => { submitting.value=true; try { await updateAnswer(editingId.value,editForm.value); ElMessage.success('答案已更新'); editVisible.value=false; fetchData() } catch(e){} finally { submitting.value=false } }
const unfavorite = async (row) => { await toggleFavorite(row.id); ElMessage.success('已取消收藏'); fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.page { max-width: 1400px; margin: 0 auto; }
.page-header { display:flex; align-items:baseline; gap:12px; margin-bottom:20px; }
.page-header h2 { font-size:22px; font-weight:700; color:#1e293b; }
.header-count { font-size:14px; color:#94a3b8; font-weight:500; }
.card { background:#fff; border-radius:20px; padding:24px; box-shadow: 0 1px 3px rgba(0,0,0,.04); }
.cell-cat { font-size:11px; font-weight:600; padding:2px 8px; border-radius:5px; background:#eef2ff; color:#6366f1; }
.dot { display:inline-block; width:10px; height:10px; border-radius:50%; }
.dot.on { background:#10b981; box-shadow:0 0 0 3px rgba(16,185,129,.15); }
.dot.off { background:#cbd5e1; }
.answer-box { background:#f8fafc; padding:20px; border-radius:12px; line-height:1.9; white-space:pre-wrap; font-size:15px; color:#334155; }
</style>
