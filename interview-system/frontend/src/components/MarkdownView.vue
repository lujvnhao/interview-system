<template>
  <div class="markdown-view" v-html="renderedHtml"></div>
</template>

<script setup>
import { computed } from 'vue'
import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  emptyText: {
    type: String,
    default: '暂无答案'
  }
})

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  typographer: false
})

const renderCodeCard = (code, language = '') => {
  const escapedLanguage = markdown.utils.escapeHtml(language)
  const langClass = escapedLanguage ? ` language-${escapedLanguage}` : ''
  const label = escapedLanguage || '代码'

  return [
    '<div class="md-code-card">',
    `<div class="md-code-head"><span>${label}</span></div>`,
    `<pre class="md-code-block"><code class="${langClass}">${markdown.utils.escapeHtml(code)}</code></pre>`,
    '</div>'
  ].join('')
}

markdown.renderer.rules.fence = (tokens, idx) => {
  const token = tokens[idx]
  const language = token.info ? token.info.trim().split(/\s+/)[0] : ''
  return renderCodeCard(token.content, language)
}

markdown.renderer.rules.code_block = (tokens, idx) => {
  return renderCodeCard(tokens[idx].content)
}

const defaultLinkOpen = markdown.renderer.rules.link_open || ((tokens, idx, options, env, self) => {
  return self.renderToken(tokens, idx, options)
})

markdown.renderer.rules.link_open = (tokens, idx, options, env, self) => {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, idx, options, env, self)
}

const renderedHtml = computed(() => {
  const source = props.content?.trim()
  const html = source
    ? markdown.render(source)
    : `<p class="markdown-empty">${markdown.utils.escapeHtml(props.emptyText)}</p>`

  return DOMPurify.sanitize(html, {
    ADD_ATTR: ['target', 'rel']
  })
})
</script>

<style scoped>
.markdown-view {
  padding: 18px;
  color: #344054;
  font-size: 15px;
  line-height: 1.85;
  word-break: break-word;
  background: #f8faf9;
  border: 1px solid #dfe5e8;
  border-radius: 8px;
}

.markdown-view :deep(*) {
  max-width: 100%;
}

.markdown-view :deep(*:first-child) {
  margin-top: 0;
}

.markdown-view :deep(*:last-child) {
  margin-bottom: 0;
}

.markdown-view :deep(h1),
.markdown-view :deep(h2),
.markdown-view :deep(h3),
.markdown-view :deep(h4) {
  margin: 18px 0 10px;
  color: #1f2933;
  font-weight: 800;
  line-height: 1.35;
}

.markdown-view :deep(h1) {
  font-size: 22px;
}

.markdown-view :deep(h2) {
  font-size: 19px;
}

.markdown-view :deep(h3) {
  font-size: 17px;
}

.markdown-view :deep(h4) {
  font-size: 15px;
}

.markdown-view :deep(p),
.markdown-view :deep(ul),
.markdown-view :deep(ol),
.markdown-view :deep(blockquote),
.markdown-view :deep(pre),
.markdown-view :deep(table),
.markdown-view :deep(.md-code-card) {
  margin: 0 0 12px;
}

.markdown-view :deep(ul),
.markdown-view :deep(ol) {
  padding-left: 22px;
}

.markdown-view :deep(li + li) {
  margin-top: 4px;
}

.markdown-view :deep(a) {
  color: #0f766e;
  font-weight: 700;
  text-decoration: none;
}

.markdown-view :deep(a:hover) {
  text-decoration: underline;
}

.markdown-view :deep(blockquote) {
  padding: 10px 14px;
  color: #5f6f7d;
  background: #eef5f3;
  border-left: 3px solid #0f766e;
  border-radius: 0 8px 8px 0;
}

.markdown-view :deep(code) {
  padding: 2px 5px;
  color: #0f766e;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 0.92em;
  background: #e6f4f1;
  border-radius: 5px;
}

.markdown-view :deep(.md-code-card) {
  overflow: hidden;
  background: #f6f8fa;
  border: 1px solid #dfe5e8;
  border-radius: 8px;
}

.markdown-view :deep(.md-code-head) {
  display: flex;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  color: #5f6f7d;
  font-size: 12px;
  font-weight: 800;
  line-height: 1;
  background: #eef1f4;
  border-bottom: 1px solid #dfe5e8;
}

.markdown-view :deep(pre.md-code-block) {
  margin: 0;
  overflow-x: auto;
  background: #f8fafc;
  border: 0;
  border-radius: 0;
}

.markdown-view :deep(pre.md-code-block code) {
  display: block;
  min-width: max-content;
  padding: 14px;
  color: #1f2933;
  line-height: 1.7;
  white-space: pre;
  background: transparent;
  border-radius: 0;
}

.markdown-view :deep(table) {
  display: block;
  overflow-x: auto;
  border-collapse: collapse;
}

.markdown-view :deep(th),
.markdown-view :deep(td) {
  padding: 8px 10px;
  border: 1px solid #dfe5e8;
}

.markdown-view :deep(th) {
  color: #1f2933;
  font-weight: 800;
  background: #eef5f3;
}

.markdown-view :deep(.markdown-empty) {
  color: #667085;
}
</style>
