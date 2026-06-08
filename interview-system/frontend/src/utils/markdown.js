import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import { formatCodeBlock, getCodeLanguageLabel, normalizeCodeLanguage } from './codeFormatter'

/**
 * 共享 Markdown 渲染器 — MarkdownView 和 AnswerEditor 共用
 */
const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  typographer: false
})

/**
 * 渲染代码块为卡片样式 HTML
 */
function renderCodeCard(code, language = '') {
  const normalizedLanguage = normalizeCodeLanguage(language)
  const escapedLang = md.utils.escapeHtml(normalizedLanguage)
  const langClass = escapedLang ? ` language-${escapedLang}` : ''
  const label = md.utils.escapeHtml(getCodeLanguageLabel(normalizedLanguage))
  const formattedCode = formatCodeBlock(code, normalizedLanguage)

  return [
    `<div class="md-code-card" data-code-language="${escapedLang}">`,
    `<div class="md-code-head"><span class="md-code-lang">${label}</span><span class="md-code-state">格式化展示</span></div>`,
    `<pre class="md-code-block"><code class="${langClass}">${md.utils.escapeHtml(formattedCode)}</code></pre>`,
    '</div>'
  ].join('')
}

// 覆盖 fenced code 渲染
md.renderer.rules.fence = (tokens, idx) => {
  const token = tokens[idx]
  const language = token.info ? token.info.trim().split(/\s+/)[0] : ''
  return renderCodeCard(token.content, language)
}

md.renderer.rules.code_block = (tokens, idx) => {
  return renderCodeCard(tokens[idx].content)
}

// 链接强制新标签打开
const defaultLinkOpen = md.renderer.rules.link_open || ((tokens, idx, options, env, self) => {
  return self.renderToken(tokens, idx, options)
})
md.renderer.rules.link_open = (tokens, idx, options, env, self) => {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, idx, options, env, self)
}

/** 共享 DOMPurify 配置 */
const PURIFY_CONFIG = { ADD_ATTR: ['target', 'rel', 'class', 'data-code-language'] }

/**
 * 渲染 Markdown 为安全的 HTML
 * @param {string} content - Markdown 文本
 * @param {string} emptyText - 空内容时的提示文本
 * @returns {string} 安全的 HTML
 */
export function renderMarkdown(content, emptyText = '暂无答案') {
  if (!content) return DOMPurify.sanitize(emptyText, PURIFY_CONFIG)
  return DOMPurify.sanitize(md.render(content), PURIFY_CONFIG)
}

export { md, renderCodeCard, PURIFY_CONFIG }
