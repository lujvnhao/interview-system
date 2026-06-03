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
  border: 1px solid #dfe5e8;
  border-radius: 8px;
  overflow: hidden;
}
.markdown-view :deep(.md-code-head) {
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 700;
  color: #667085;
  background: #f6f8f9;
  border-bottom: 1px solid #dfe5e8;
}
.markdown-view :deep(.md-code-block) {
  margin: 0;
  padding: 14px;
  background: #1f2933;
  color: #e5e7eb;
  font-size: 13px;
  line-height: 1.7;
  overflow-x: auto;
}
</style>
