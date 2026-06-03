<template>
  <div class="bank-page">
    <div class="page-header">
      <div class="page-title">
        <span class="title-icon"><el-icon><Collection /></el-icon></span>
        <div>
          <h2>题库管理</h2>
          <span class="header-count">共 {{ total }} 题</span>
        </div>
      </div>
    </div>

    <div class="bank-card">
      <!-- 搜索与筛选 -->
      <div class="toolbar">
        <div class="toolbar-filters">
          <el-input v-model="searchKeyword" placeholder="搜索问题或答案..." clearable class="search-input" @clear="applyFilters" @keyup.enter="applyFilters">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterCategory" placeholder="分类" clearable class="filter-select" @change="applyFilters">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
          <el-select v-model="filterTag" placeholder="标签" clearable class="filter-select narrow" @change="applyFilters">
            <el-option v-for="t in tags" :key="t" :label="t" :value="t" />
          </el-select>
          <el-select v-model="filterMastered" placeholder="掌握" clearable class="filter-select mini" @change="applyFilters">
            <el-option label="已掌握" :value="true" />
            <el-option label="未掌握" :value="false" />
          </el-select>
          <el-select v-model="filterFavorite" placeholder="收藏" clearable class="filter-select mini" @change="applyFilters">
            <el-option label="已收藏" :value="true" />
            <el-option label="未收藏" :value="false" />
          </el-select>
          <el-select v-model="sortBy" placeholder="排序" class="filter-select sort" @change="applyFilters">
            <el-option label="默认排序" value="createTime" />
            <el-option label="错题次数" value="wrongCount" />
            <el-option label="复习次数" value="reviewCount" />
            <el-option label="最近复习" value="lastReviewTime" />
            <el-option label="权重" value="weight" />
          </el-select>
          <el-select v-model="sortDir" placeholder="方向" class="filter-select dir" @change="applyFilters">
            <el-option label="降序" value="desc" />
            <el-option label="升序" value="asc" />
          </el-select>
          <el-button type="primary" class="icon-action" @click="applyFilters"><el-icon><Search /></el-icon></el-button>
          <div class="quick-filters">
            <el-button size="small" :type="filterNoAnswer ? 'primary' : 'default'" plain @click="toggleNoAnswerFilter">
              <el-icon><DocumentDelete /></el-icon> 无答案
            </el-button>
            <el-button size="small" :type="filterHotTag ? 'primary' : 'default'" plain @click="toggleHotTagFilter">
              <el-icon><Flag /></el-icon> 高频
            </el-button>
            <el-button size="small" :type="filterLongUnreviewed ? 'primary' : 'default'" plain @click="toggleLongUnreviewedFilter">
              <el-icon><Timer /></el-icon> 久未复习
            </el-button>
          </div>
        </div>

        <div class="toolbar-actions">
          <el-button type="success" @click="showAddDialog"><el-icon><Plus /></el-icon> 新增</el-button>
          <el-button type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDelete">
            <el-icon><Delete /></el-icon> 批量删除
          </el-button>
          <el-button type="warning" plain :disabled="!selectedIds.length" @click="batchTagDialogVisible = true">
            <el-icon><CollectionTag /></el-icon> 批量添加标签
          </el-button>
          <el-button :type="filterTagEmpty ? 'primary' : 'default'" plain @click="toggleEmptyTagFilter">
            <el-icon><Warning /></el-icon> 空白标签
          </el-button>
          <el-dropdown @command="handleExport">
            <el-button><el-icon><Download /></el-icon> 导出 <el-icon><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="json">导出 JSON</el-dropdown-item>
                <el-dropdown-item command="csv">导出 CSV</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-upload :auto-upload="false" :show-file-list="false" accept=".json" :on-change="handleImport" class="upload-inline">
            <el-button><el-icon><Upload /></el-icon> 导入</el-button>
          </el-upload>
          <el-button text type="primary" @click="downloadTemplate"><el-icon><Download /></el-icon> 导入模板</el-button>
          <el-button text @click="showTagDialog"><el-icon><Setting /></el-icon> 管理标签</el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe class="question-table"
        @selection-change="onSelectionChange" max-height="calc(100vh - 300px)">
        <el-table-column type="selection" width="42" />
        <el-table-column prop="id" label="ID" width="55" />
        <el-table-column prop="question" label="问题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="105">
          <template #default="{ row }">
            <span class="cell-tag cat">{{ row.category }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="150">
          <template #default="{ row }">
            <span v-for="t in (row.tags||'').split(',').filter(Boolean)" :key="t"
              class="cell-tag" :class="{ hot: t.trim()==='高频'||t.trim()==='重点' }">{{ t.trim() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="mastered" label="掌握" width="65" align="center">
          <template #default="{ row }">
            <span class="dot-status" :class="row.mastered ? 'on' : 'off'"></span>
          </template>
        </el-table-column>
        <el-table-column prop="favorite" label="收藏" width="60" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.favorite" class="favorite-icon"><StarFilled /></el-icon>
            <el-icon v-else class="muted-icon"><Star /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="权重" width="60" align="center" />
        <el-table-column prop="reviewCount" label="复习" width="55" align="center" />
        <el-table-column prop="wrongCount" label="错次" width="55" align="center">
          <template #default="{ row }">
            <span :class="{ 'err-num': row.wrongCount > 0 }">{{ row.wrongCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastReviewTime" label="最近复习" width="145" show-overflow-tooltip />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="showDetailDialog(row)">详情</el-button>
            <el-button size="small" text type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
          :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId?'编辑题目':'新增题目'" width="650px" :close-on-click-modal="false">
      <el-form :model="form" label-width="70px">
        <el-form-item label="问题" required>
          <el-input v-model="form.question" type="textarea" :rows="3" placeholder="请输入面试题问题" />
        </el-form-item>
        <el-form-item label="答案">
          <AnswerEditor
            v-model="form.answer"
            :rows="8"
            placeholder="请输入参考答案（支持代码字段和 Markdown，可为空）"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" filterable allow-create class="full-field" placeholder="选择或输入分类">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tagsList" multiple filterable allow-create class="full-field" placeholder="选择或输入标签">
            <el-option v-for="t in tags" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="权重">
          <el-input-number v-model="form.weight" :min="1" :max="100" />
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="已掌握"><el-switch v-model="form.mastered" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="已收藏"><el-switch v-model="form.favorite" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ editingId?'保存':'确认新增' }}</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="题目详情" width="650px">
      <div v-if="detailRow" class="detail-content">
        <div class="detail-field"><label>ID</label><span>{{ detailRow.id }}</span></div>
        <div class="detail-field"><label>问题</label><span>{{ detailRow.question }}</span></div>
        <div class="detail-field"><label>答案</label>
          <MarkdownView class="detail-answer" :content="detailRow.answer" />
        </div>
        <div class="detail-field"><label>分类</label><span>{{ detailRow.category }}</span></div>
        <div class="detail-field"><label>标签</label><span>{{ detailRow.tags }}</span></div>
        <div class="detail-row">
          <div class="detail-field"><label>权重</label><span>{{ detailRow.weight }}</span></div>
          <div class="detail-field"><label>掌握</label><span>{{ detailRow.mastered?'是':'否' }}</span></div>
          <div class="detail-field"><label>收藏</label><span>{{ detailRow.favorite?'是':'否' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-field"><label>复习次数</label><span>{{ detailRow.reviewCount }}</span></div>
          <div class="detail-field"><label>掌握次数</label><span>{{ detailRow.correctCount }}</span></div>
          <div class="detail-field"><label>未掌握次数</label><span>{{ detailRow.wrongCount }}</span></div>
        </div>
        <div class="detail-field"><label>最近复习</label><span>{{ detailRow.lastReviewTime || '暂未复习' }}</span></div>
      </div>
    </el-dialog>

    <!-- 批量添加标签弹窗 -->
    <el-dialog v-model="batchTagDialogVisible" title="批量添加标签" width="450px">
      <p class="dialog-tip">已选中 <strong>{{ selectedIds.length }}</strong> 道题目，输入要添加的标签：</p>
      <el-select v-model="batchTagName" filterable allow-create class="full-field" placeholder="选择或输入标签">
        <el-option v-for="t in tags" :key="t" :label="t" :value="t" />
      </el-select>
      <template #footer>
        <el-button @click="batchTagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchAddTag" :loading="batchTagSubmitting">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 标签管理弹窗 -->
    <el-dialog v-model="tagDialogVisible" title="标签管理" width="500px">
      <div class="tag-create-row">
        <el-input v-model="newTagName" placeholder="输入新标签名" @keyup.enter="handleAddTag" />
        <el-button type="primary" @click="handleAddTag">新增</el-button>
      </div>
      <div class="tag-manage-list">
        <div v-for="tag in allTags" :key="tag.id || tag" class="tag-manage-item">
          <span class="tag-manage-name">{{ tag.name || tag }}</span>
          <el-button size="small" text type="danger" @click="handleDeleteTag(tag)">删除</el-button>
        </div>
        <el-empty v-if="!allTags.length" description="暂无标签" :image-size="40" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getQuestionList, createQuestion, updateQuestion, deleteQuestion,
  batchDeleteQuestions, exportJson, importJson, getCategories, getTags,
  createTag, deleteTag, batchAddTag
} from '../api'
import AnswerEditor from '../components/AnswerEditor.vue'
import MarkdownView from '../components/MarkdownView.vue'

const loading = ref(false), submitting = ref(false)
const tableData = ref([]), total = ref(0), currentPage = ref(1), pageSize = ref(20), selectedIds = ref([])
const searchKeyword = ref(''), filterCategory = ref(''), filterTag = ref(''), filterMastered = ref(undefined), filterFavorite = ref(undefined)
const sortBy = ref('createTime'), sortDir = ref('desc')
const filterNoAnswer = ref(false), filterHotTag = ref(false), filterLongUnreviewed = ref(false)
const categories = ref([]), tags = ref([])
const dialogVisible = ref(false), detailVisible = ref(false), editingId = ref(null), detailRow = ref(null)
const form = ref({ question:'', answer:'', category:'Java 基础', tagsList:[], weight:10, mastered:false, favorite:false })

// 标签管理
const tagDialogVisible = ref(false)
const newTagName = ref('')
const allTags = ref([])
const batchTagDialogVisible = ref(false)
const batchTagName = ref('')
const batchTagSubmitting = ref(false)
const filterTagEmpty = ref(false)
const longUnreviewedDays = 14

const showTagDialog = async () => {
  tagDialogVisible.value = true
  await loadAllTags()
}
const loadAllTags = async () => {
  try { const res = await getTags(); allTags.value = (res.data||[]).map(t => ({ id: t, name: t })) } catch {}
}
const handleAddTag = async () => {
  const name = newTagName.value.trim()
  if (!name) { ElMessage.warning('请输入标签名'); return }
  try { await createTag(name); newTagName.value = ''; await loadAllTags(); await fetchMeta(); ElMessage.success('标签已添加') } catch {}
}
const handleDeleteTag = async (tag) => {
  try {
    await deleteTag(tag.id || tag.name || tag)
    await loadAllTags(); await fetchMeta(); fetchData()
    ElMessage.success('标签已删除，相关题目中的该标签已清除')
  } catch {}
}

// 批量添加标签
const handleBatchAddTag = async () => {
  const name = batchTagName.value?.trim()
  if (!name) { ElMessage.warning('请选择或输入标签'); return }
  batchTagSubmitting.value = true
  try {
    await batchAddTag(selectedIds.value, name)
    ElMessage.success(`已为 ${selectedIds.value.length} 道题目添加标签"${name}"`)
    batchTagDialogVisible.value = false
    batchTagName.value = ''
    selectedIds.value = []
    fetchData()
    fetchMeta()
  } catch {} finally { batchTagSubmitting.value = false }
}

// 空白标签筛选切换
const toggleEmptyTagFilter = () => {
  filterTagEmpty.value = !filterTagEmpty.value
  applyFilters()
}

const toggleNoAnswerFilter = () => {
  filterNoAnswer.value = !filterNoAnswer.value
  applyFilters()
}

const toggleHotTagFilter = () => {
  filterHotTag.value = !filterHotTag.value
  applyFilters()
}

const toggleLongUnreviewedFilter = () => {
  filterLongUnreviewed.value = !filterLongUnreviewed.value
  applyFilters()
}

const applyFilters = () => {
  currentPage.value = 1
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      category: filterCategory.value || undefined,
      tag: filterTag.value || undefined,
      mastered: filterMastered.value,
      favorite: filterFavorite.value,
      emptyTag: filterTagEmpty.value || undefined,
      noAnswer: filterNoAnswer.value || undefined,
      hotTag: filterHotTag.value || undefined,
      longUnreviewed: filterLongUnreviewed.value || undefined,
      staleDays: filterLongUnreviewed.value ? longUnreviewedDays : undefined,
      sortBy: sortBy.value,
      sortDir: sortDir.value
    }
    const res = await getQuestionList(params)
    tableData.value = res.data.content
    total.value = res.data.totalElements
  } catch (e) {} finally { loading.value = false }
}

const onSelectionChange = (rows) => { selectedIds.value = rows.map(r=>r.id) }

const showAddDialog = () => {
  editingId.value = null
  form.value = { question:'', answer:'', category:'Java 基础', tagsList:[], weight:10, mastered:false, favorite:false }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  editingId.value = row.id
  form.value = { question: row.question, answer: row.answer||'', category: row.category,
    tagsList: (row.tags||'').split(',').map(t=>t.trim()).filter(Boolean), weight: row.weight,
    mastered: row.mastered, favorite: row.favorite }
  dialogVisible.value = true
}

const showDetailDialog = (row) => { detailRow.value = row; detailVisible.value = true }

const handleSubmit = async () => {
  if (!form.value.question.trim()) { ElMessage.warning('问题不能为空'); return }
  submitting.value = true
  try {
    const data = { question: form.value.question, answer: form.value.answer, category: form.value.category,
      tags: form.value.tagsList.join(','), weight: form.value.weight, mastered: form.value.mastered, favorite: form.value.favorite }
    if (editingId.value) { await updateQuestion(editingId.value, data); ElMessage.success('修改成功') }
    else { await createQuestion(data); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } catch (e) {} finally { submitting.value = false }
}

const handleDelete = async (id) => { await deleteQuestion(id); ElMessage.success('删除成功'); fetchData() }

const handleBatchDelete = async () => {
  if (!selectedIds.value.length) return
  await batchDeleteQuestions(selectedIds.value)
  ElMessage.success(`已删除 ${selectedIds.value.length} 题`); selectedIds.value = []; fetchData()
}

const handleExport = async (cmd) => {
  try {
    const res = await exportJson()
    const blob = new Blob([cmd==='csv'?jsonToCsv(res.data):JSON.stringify(res.data,null,2)], {type:'text/plain;charset=utf-8'})
    const a = document.createElement('a'); a.href = URL.createObjectURL(blob)
    a.download = cmd==='csv'?'questions.csv':'questions.json'; a.click(); URL.revokeObjectURL(a.href)
    ElMessage.success(`导出 ${res.data.length} 题`)
  } catch (e) {}
}

const jsonToCsv = (data) => {
  if (!data.length) return ''
  const keys = ['id','question','answer','category','tags','mastered','favorite','weight','wrongCount','correctCount','reviewCount']
  let csv = keys.join(',')+'\n'
  data.forEach(r=>{ csv += keys.map(k=>{ let v=(r[k]??'').toString(); if(v.includes(',')||v.includes('"')) v='"'+v.replace(/"/g,'""')+'"'; return v }).join(',')+'\n' })
  return csv
}

const handleImport = (file) => {
  const reader = new FileReader()
  reader.onload = async (e) => {
    try { const res = await importJson(JSON.parse(e.target.result)); ElMessage.success(`新增 ${res.data.added} 题，跳过 ${res.data.skipped} 重复`); fetchData() }
    catch { ElMessage.error('JSON 解析失败') }
  }
  reader.readAsText(file.raw)
}

// 下载导入模板
const downloadTemplate = () => {
  const template = [
    {
      "question": "示例：什么是面向对象编程的三大特性？",
      "answer": "封装、继承、多态。封装隐藏内部实现，继承实现代码复用，多态允许不同子类对同一消息做出不同响应。",
      "category": "Java 基础",
      "tags": "高频,重点,八股文",
      "weight": 10,
      "mastered": false,
      "favorite": false
    }
  ]
  const blob = new Blob([JSON.stringify(template, null, 2)], { type: 'application/json' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = 'import_template.json'
  a.click()
  URL.revokeObjectURL(a.href)
  ElMessage.success('模板已下载，请参考格式填写后导入')
}

const fetchMeta = async () => {
  try { const [c,t] = await Promise.all([getCategories(),getTags()]); categories.value = c.data; tags.value = t.data } catch (e) {}
}

onMounted(() => { fetchMeta(); fetchData() })
</script>

<style scoped>
.bank-page { max-width: 1400px; margin: 0 auto; }

.header-count {
  font-family: var(--font-mono);
  font-size: 14px;
  color: var(--color-accent);
  font-weight: 700;
}

.bank-card {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 22px;
  box-shadow: var(--shadow-sm);
}

/* Toolbar */
.toolbar { display: flex; flex-direction: column; gap: 14px; }
.toolbar-filters {
  display: flex; flex-wrap: wrap; gap: 8px; align-items: center;
}
.toolbar-actions {
  display: flex; flex-wrap: wrap; gap: 8px; align-items: center;
  padding-top: 14px; border-top: 1px solid var(--color-border);
}

.search-input { width: 220px; }
.filter-select { width: 120px; }
.filter-select.narrow { width: 110px; }
.filter-select.mini { width: 100px; }
.filter-select.sort { width: 130px; }
.filter-select.dir { width: 82px; }
.icon-action { min-width: 40px; }
.quick-filters { display: flex; gap: 4px; }

/* Table tags */
.cell-tag {
  display: inline-block;
  padding: 1px 8px; margin: 1px 2px;
  font-size: 11px; font-weight: 700; font-family: var(--font-ui);
  color: var(--color-ink-muted); background: var(--color-bg-elevated);
  border: 1px solid var(--color-border); border-radius: var(--radius-sm);
}
.cell-tag.cat { color: var(--color-blue); background: var(--color-blue-soft); border-color: rgba(122,147,168,0.2); }
.cell-tag.hot { color: var(--color-red); background: var(--color-red-soft); border-color: rgba(192,114,99,0.2); }

.dot-status {
  display: inline-block; width: 8px; height: 8px; border-radius: 50%;
}
.dot-status.on { background: var(--color-green); box-shadow: 0 0 6px rgba(122,154,126,0.4); }
.dot-status.off { background: var(--color-ink-faint); }

.favorite-icon { color: var(--color-amber); font-size: 16px; }
.muted-icon { color: var(--color-ink-faint); font-size: 15px; }
.err-num { color: var(--color-red); font-weight: 700; font-family: var(--font-mono); }

.question-table { border: 1px solid var(--color-border); border-radius: var(--radius-md); overflow: hidden; }

/* Upload */
.upload-inline { display: inline-block; }

/* Pagination row */
.pagination-row {
  display: flex; justify-content: center; padding-top: 18px; margin-top: 18px;
  border-top: 1px solid var(--color-border);
}

/* Dialog forms */
.form-two-cols { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-item.col-span { grid-column: 1 / -1; }
.form-label { font-size: 13px; font-weight: 700; color: var(--color-ink-muted); font-family: var(--font-ui); }
.form-label .small-hint { font-size: 11px; font-weight: 500; color: var(--color-ink-faint); }
.detail-block { margin-top: 14px; }
.detail-label { font-size: 12px; font-weight: 700; color: var(--color-ink-muted); margin-bottom: 6px; font-family: var(--font-ui); }
.detail-text { font-size: 14px; color: var(--color-ink); line-height: 1.75; white-space: pre-wrap; }
.empty-answer { color: var(--color-ink-faint); font-style: italic; }

.tag-dialog .el-checkbox { display: flex; margin-bottom: 6px; }

@media (max-width: 860px) {
  .search-input { width: 100%; }
  .filter-select { flex: 1; min-width: 100px; }
  .toolbar-actions { flex-direction: column; }
  .toolbar-actions .el-button { width: 100%; }
  .form-two-cols { grid-template-columns: 1fr; }
  .bank-card { padding: 14px; }
}
</style>
