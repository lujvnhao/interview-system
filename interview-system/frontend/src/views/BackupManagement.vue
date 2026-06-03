<template>
  <div class="backup-page">
    <div class="page-header">
      <div class="page-title">
        <span class="title-icon"><el-icon><FolderOpened /></el-icon></span>
        <div>
          <h2>备份管理</h2>
          <span class="header-desc">查看备份、手动创建备份或恢复题库和标签</span>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="fetchBackups" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="handleCreateBackup" :loading="creating">
          <el-icon><Plus /></el-icon>
          手动备份
        </el-button>
      </div>
    </div>

    <div class="summary-grid">
      <div class="summary-card">
        <span class="summary-value">{{ backups.length }}</span>
        <span class="summary-label">备份数量</span>
      </div>
      <div class="summary-card">
        <span class="summary-value">{{ latestBackupTime }}</span>
        <span class="summary-label">最近备份</span>
      </div>
      <div class="summary-card">
        <span class="summary-value">{{ latestBackup?.questionCount || 0 }}</span>
        <span class="summary-label">题目数</span>
      </div>
      <div class="summary-card">
        <span class="summary-value">{{ latestBackup?.tagCount || 0 }}</span>
        <span class="summary-label">标签数</span>
      </div>
    </div>

    <div class="table-card">
      <div class="table-head">
        <div>
          <h3>备份列表</h3>
          <p>当前自动备份和手动快照均可用于恢复</p>
        </div>
      </div>

      <el-table v-if="backups.length" :data="backups" stripe v-loading="loading">
        <el-table-column label="备份" min-width="220">
          <template #default="{ row }">
            <div class="backup-name">
              <el-tag :type="row.type === 'current' ? 'info' : 'success'" size="small">
                {{ row.type === 'current' ? '自动' : '手动' }}
              </el-tag>
              <div class="backup-name-main">
                <strong>{{ row.label }}</strong>
                <span>{{ row.path }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="备份时间" min-width="170">
          <template #default="{ row }">
            <div class="time-cell">
              <strong>{{ formatDate(row.exportedAt) }}</strong>
              <span>修改 {{ formatDate(row.modifiedAt) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="数据量" width="130" align="center">
          <template #default="{ row }">
            <div class="count-cell">
              <span>{{ row.questionCount }} 题</span>
              <span>{{ row.tagCount }} 标签</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="文件信息" min-width="210">
          <template #default="{ row }">
            <div class="file-cell">
              <span>{{ row.questionsFile }} · {{ formatSize(row.questionsSize) }}</span>
              <span>{{ row.tagsFile }} · {{ formatSize(row.tagsSize) }}</span>
              <strong>合计 {{ formatSize(row.totalSize) }}</strong>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="128" align="right" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="danger"
              plain
              :loading="restoringId === row.id"
              @click="handleRestore(row)"
            >
              恢复
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else description="暂无可用备份" :image-size="72">
        <el-button type="primary" @click="handleCreateBackup" :loading="creating">立即创建备份</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getBackups,
  createBackup,
  restoreBackup,
  getQuestionList,
  getStatistics,
  getTags
} from '../api'

const backups = ref([])
const loading = ref(false)
const creating = ref(false)
const restoringId = ref('')

const latestBackup = computed(() => backups.value[0] || null)
const latestBackupTime = computed(() => latestBackup.value ? formatDate(latestBackup.value.exportedAt) : '-')

const fetchBackups = async () => {
  loading.value = true
  try {
    const res = await getBackups()
    backups.value = res.data || []
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const handleCreateBackup = async () => {
  creating.value = true
  try {
    await createBackup()
    ElMessage.success('备份已创建')
    await fetchBackups()
  } catch (e) {
  } finally {
    creating.value = false
  }
}

const handleRestore = async (backup) => {
  try {
    await ElMessageBox.prompt(
      `将使用「${backup.label}」恢复题库和标签，当前数据会被该备份替换。请输入“恢复”完成二次确认。`,
      '危险操作',
      {
        type: 'warning',
        confirmButtonText: '确认恢复',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
        inputPlaceholder: '请输入：恢复',
        inputPattern: /^恢复$/,
        inputErrorMessage: '请输入“恢复”后才能继续'
      }
    )

    restoringId.value = backup.id
    await restoreBackup(backup.id)
    ElMessage.success('恢复完成，数据已刷新')
    await refreshDataAfterRestore()
  } catch (e) {
  } finally {
    restoringId.value = ''
  }
}

const refreshDataAfterRestore = async () => {
  await Promise.allSettled([
    fetchBackups(),
    getQuestionList({ page: 1, size: 1 }),
    getStatistics(),
    getTags()
  ])
}

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

const formatSize = (value) => {
  const size = Number(value || 0)
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(2)} MB`
}

onMounted(fetchBackups)
</script>

<style scoped>
.backup-page {
  max-width: 1360px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 18px;
  margin-bottom: 18px;
  border-bottom: 1px solid #dfe5e8;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #0f766e;
  font-size: 20px;
  background: #e6f4f1;
  border-radius: 8px;
}

.page-title h2 {
  color: #1f2933;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.2;
}

.header-desc {
  display: inline-block;
  margin-top: 5px;
  color: #667085;
  font-size: 13px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(140px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-card {
  min-width: 0;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #dfe5e8;
  border-top: 3px solid #0f766e;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
}

.summary-value {
  display: block;
  overflow: hidden;
  color: #1f2933;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-label {
  display: block;
  margin-top: 8px;
  color: #667085;
  font-size: 13px;
  font-weight: 700;
}

.table-card {
  padding: 18px;
  background: #ffffff;
  border: 1px solid #dfe5e8;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04), 0 12px 28px rgba(16, 24, 40, 0.04);
}

.table-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.table-head h3 {
  color: #1f2933;
  font-size: 16px;
  font-weight: 800;
}

.table-head p {
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
  font-weight: 600;
}

.backup-name {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  min-width: 0;
}

.backup-name-main {
  min-width: 0;
}

.backup-name-main strong,
.time-cell strong {
  display: block;
  color: #1f2933;
  font-size: 13px;
  font-weight: 800;
}

.backup-name-main span,
.time-cell span,
.count-cell span,
.file-cell span {
  display: block;
  margin-top: 3px;
  overflow: hidden;
  color: #667085;
  font-size: 12px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.count-cell,
.file-cell {
  line-height: 1.5;
}

.file-cell strong {
  display: block;
  margin-top: 4px;
  color: #0f766e;
  font-size: 12px;
  font-weight: 800;
}

.table-card :deep(.el-table) {
  overflow: hidden;
  border: 1px solid #edf1f3;
  border-radius: 8px;
}

@media (max-width: 920px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .table-card,
  .summary-card {
    padding: 14px;
  }

  .header-actions .el-button {
    flex: 1;
  }
}
</style>
