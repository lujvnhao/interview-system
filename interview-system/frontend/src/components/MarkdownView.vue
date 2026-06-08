<template>
  <div class="markdown-view" v-html="renderedHtml"></div>
</template>

<script setup>
import { computed } from 'vue'
import { renderMarkdown } from '../utils/markdown'

const props = defineProps({
  content: { type: String, default: '' },
  emptyText: { type: String, default: '暂无答案' }
})

const renderedHtml = computed(() => renderMarkdown(props.content, props.emptyText))
</script>

<style scoped>
.markdown-view :deep(.md-code-card) {
  margin: 12px 0;
  background: #111827;
  border: 1px solid #263244;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}
.markdown-view :deep(.md-code-head) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 34px;
  padding: 0 12px;
  font-size: 12px;
  font-weight: 700;
  color: #d1d5db;
  background: #1f2937;
  border-bottom: 1px solid #374151;
}
.markdown-view :deep(.md-code-lang) {
  min-width: 0;
  overflow: hidden;
  color: #f9fafb;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.markdown-view :deep(.md-code-state) {
  flex: none;
  padding: 2px 7px;
  color: #9ca3af;
  font-size: 11px;
  line-height: 1.4;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 999px;
}
.markdown-view :deep(.md-code-block) {
  margin: 0;
  padding: 0;
  background: #111827;
  overflow-x: auto;
}
.markdown-view :deep(.md-code-block code) {
  display: block;
  min-width: max-content;
  padding: 14px 16px;
  color: #e5e7eb;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 13px;
  line-height: 1.7;
  tab-size: 2;
  white-space: pre;
}
</style>
