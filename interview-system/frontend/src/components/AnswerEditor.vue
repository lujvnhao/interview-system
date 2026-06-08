<template>
  <div class="answer-editor">
    <div class="editor-toolbar" @mousedown="saveRichSelection(); saveSourceSelection()">
      <el-button-group>
        <el-tooltip content="一级标题" placement="top">
          <el-button size="small" aria-label="一级标题" @click="insertHeading(1)">H1</el-button>
        </el-tooltip>
        <el-tooltip content="二级标题" placement="top">
          <el-button size="small" aria-label="二级标题" @click="insertHeading(2)">H2</el-button>
        </el-tooltip>
        <el-tooltip content="三级标题" placement="top">
          <el-button size="small" aria-label="三级标题" @click="insertHeading(3)">H3</el-button>
        </el-tooltip>
      </el-button-group>

      <el-button-group>
        <el-tooltip content="加粗" placement="top">
          <el-button size="small" class="symbol-button" aria-label="加粗" @click="toggleBold">B</el-button>
        </el-tooltip>
        <el-tooltip content="无序列表" placement="top">
          <el-button size="small" aria-label="无序列表" @click="insertList('- ')">
            <el-icon><List /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="有序列表" placement="top">
          <el-button size="small" aria-label="有序列表" @click="insertOrderedList">
            <el-icon><Sort /></el-icon>
          </el-button>
        </el-tooltip>
      </el-button-group>

      <el-button-group>
        <el-tooltip content="行内代码" placement="top">
          <el-button size="small" aria-label="行内代码" @click="insertInlineCode">
            <el-icon><Tickets /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="添加超链接" placement="top">
          <el-button size="small" aria-label="添加超链接" @click="insertLink">链接</el-button>
        </el-tooltip>
        <el-dropdown trigger="click" @command="insertCodeBlock">
          <el-button size="small" aria-label="插入代码字段">
            <el-icon><Document /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="auto">智能识别</el-dropdown-item>
              <el-dropdown-item command="">代码字段</el-dropdown-item>
              <el-dropdown-item command="java">Java</el-dropdown-item>
              <el-dropdown-item command="javascript">JavaScript</el-dropdown-item>
              <el-dropdown-item command="sql">SQL</el-dropdown-item>
              <el-dropdown-item command="bash">Shell</el-dropdown-item>
              <el-dropdown-item command="json">JSON</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-button-group>

      <el-button-group>
        <el-dropdown trigger="click" @command="insertTemplate">
          <el-button size="small" aria-label="插入答题模板">模板</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="concept">概念题</el-dropdown-item>
              <el-dropdown-item command="compare">对比题</el-dropdown-item>
              <el-dropdown-item command="scenario">场景题</el-dropdown-item>
              <el-dropdown-item command="code">代码题</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-tooltip content="整理格式" placement="top">
          <el-button size="small" aria-label="整理格式" @click="formatMarkdown">整理</el-button>
        </el-tooltip>
        <el-tooltip content="格式化所有代码字段" placement="top">
          <el-button size="small" aria-label="格式化代码字段" @click="formatCodeBlocks">格式化代码</el-button>
        </el-tooltip>
      </el-button-group>

      <el-radio-group v-model="viewMode" size="small" class="view-switch" aria-label="编辑视图">
        <el-radio-button label="rich">写作</el-radio-button>
        <el-radio-button label="source">源码</el-radio-button>
        <el-radio-button label="split">分屏</el-radio-button>
        <el-radio-button label="preview">预览</el-radio-button>
      </el-radio-group>
    </div>

    <div class="editor-workspace" :class="`mode-${viewMode}`">
      <div
        v-if="viewMode === 'rich'"
        ref="richEditorRef"
        class="rich-editor"
        contenteditable="true"
        :data-placeholder="placeholder"
        :style="{ minHeight: editorMinHeight }"
        @input="handleRichInput"
        @paste="handleRichPaste"
        @click="handleRichClick"
        @keydown="handleRichKeydown"
        @compositionstart="handleRichCompositionStart"
        @compositionend="handleRichCompositionEnd"
        @focus="saveRichSelection"
        @keyup="saveRichSelection"
        @mouseup="saveRichSelection"
      ></div>

      <el-input
        v-if="viewMode === 'source' || viewMode === 'split'"
        ref="editorRef"
        :model-value="modelValue"
        type="textarea"
        :rows="rows"
        :placeholder="placeholder"
        @update:model-value="updateValue"
        @keydown="handleEditorKeydown"
        @blur="saveSourceSelection"
      />

      <div v-if="viewMode === 'split' || viewMode === 'preview'" class="preview-pane">
        <MarkdownView :content="modelValue" empty-text="输入内容后在这里预览" />
      </div>
    </div>

    <div class="editor-footer">
      <span>{{ contentStats.chars }} 字</span>
      <span>{{ contentStats.lines }} 行</span>
      <span>{{ contentStats.headings }} 级标题</span>
      <span>{{ contentStats.codeBlocks }} 个代码字段</span>
      <span class="smart-state" :class="smartState.type">{{ smartState.text }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'
import { formatCodeBlock, formatCodeFences, getCodeLanguageLabel, normalizeCodeLanguage } from '../utils/codeFormatter'

// Editor 使用独立 MarkdownIt 实例，避免污染共享渲染器
const markdown = new MarkdownIt({
  html: false, linkify: true, breaks: true, typographer: false
})
import MarkdownView from './MarkdownView.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  rows: {
    type: Number,
    default: 8
  },
  placeholder: {
    type: String,
    default: '请输入答案'
  }
})

const emit = defineEmits(['update:modelValue', 'escape'])
const editorRef = ref(null)
const richEditorRef = ref(null)
const savedRichRange = ref(null)
const richInputActive = ref(false)
const richComposing = ref(false)
const viewMode = ref('rich')
const undoStack = ref([])
const redoStack = ref([])
const lastHistoryValue = ref(props.modelValue || '')
const applyingHistory = ref(false)
const locallyEmittedValue = ref(null)
const maxHistorySize = 80

const editorMinHeight = computed(() => `${Math.max(props.rows * 25, 180)}px`)

const escapeHtml = (value = '') => markdown.utils.escapeHtml(value)
const codePlaceholder = '在这里输入代码'
const zeroWidthSpace = '\u200b'

const renderCodeCardHtml = (code, language = '') => {
  const normalizedLanguage = normalizeCodeLanguage(language)
  const escapedLanguage = escapeHtml(normalizedLanguage)
  const langClass = escapedLanguage ? ` language-${escapedLanguage}` : ''
  const label = escapeHtml(getCodeLanguageLabel(normalizedLanguage))
  const placeholder = escapeHtml(codePlaceholder)
  const hasCodeContent = String(code || '').replace(/\u200b/g, '').trim()
  const codeContent = hasCodeContent ? formatCodeBlock(code, normalizedLanguage) : ''

  return [
    `<div class="md-code-card" data-code-card="true" data-code-language="${escapedLanguage}">`,
    `<div class="md-code-head" contenteditable="false"><span class="md-code-lang">${label}</span><span class="md-code-state">格式化展示</span></div>`,
    `<pre class="md-code-block"><code class="${langClass}" data-placeholder="${placeholder}">${escapeHtml(codeContent)}</code></pre>`,
    '</div>'
  ].join('')
}

markdown.renderer.rules.fence = (tokens, idx) => {
  const token = tokens[idx]
  const language = token.info ? token.info.trim().split(/\s+/)[0] : ''
  return renderCodeCardHtml(token.content, language)
}

markdown.renderer.rules.code_block = (tokens, idx) => renderCodeCardHtml(tokens[idx].content)

const defaultLinkOpen = markdown.renderer.rules.link_open || ((tokens, idx, options, env, self) => {
  return self.renderToken(tokens, idx, options)
})

markdown.renderer.rules.link_open = (tokens, idx, options, env, self) => {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, idx, options, env, self)
}

const renderMarkdownHtml = (content) => {
  const html = content?.trim() ? markdown.render(content) : ''
  return DOMPurify.sanitize(html, {
    ADD_ATTR: ['target', 'rel', 'class', 'contenteditable', 'data-code-card', 'data-code-language', 'data-placeholder']
  })
}

const templates = {
  concept: '## 核心概念\n\n- \n\n## 关键点\n\n- \n\n## 常见追问\n\n- \n',
  compare: '## 相同点\n\n- \n\n## 不同点\n\n- \n\n## 使用场景\n\n- \n',
  scenario: '## 思路\n\n1. \n2. \n\n## 风险点\n\n- \n\n## 结论\n\n',
  code: '## 实现思路\n\n\n```java\n\n```\n\n## 复杂度 / 注意点\n\n- \n'
}

const contentStats = computed(() => {
  const text = props.modelValue || ''
  const trimmed = text.trim()
  return {
    chars: trimmed.replace(/\s/g, '').length,
    lines: trimmed ? trimmed.split('\n').length : 0,
    headings: (text.match(/^#{1,6}\s+/gm) || []).length,
    codeBlocks: Math.floor((text.match(/```/g) || []).length / 2)
  }
})

const codeLikePattern = /(public\s+class|class\s+\w+|function\s+\w*|=>|SELECT\s+.+\s+FROM|INSERT\s+INTO|npm\s+|mvn\s+|curl\s+|-{2}\w+|;\s*$)/im

const smartState = computed(() => {
  const text = props.modelValue || ''
  if (!text.trim()) return { type: 'muted', text: '空答案' }
  if (codeLikePattern.test(text) && contentStats.value.codeBlocks === 0) {
    return { type: 'warn', text: '建议使用代码字段' }
  }
  if (contentStats.value.headings === 0 && contentStats.value.lines >= 6) {
    return { type: 'warn', text: '建议分级' }
  }
  return { type: 'ok', text: '结构正常' }
})

const normalizeMarkdown = (text) => {
  return (text || '')
    .replace(/\r\n/g, '\n')
    .split('\n')
    .map(line => line.replace(/[ \t]+$/g, ''))
    .join('\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

const updateValue = (value) => {
  emit('update:modelValue', value)
}

const commitValue = (value, { history = false } = {}) => {
  const nextValue = value || ''
  const currentValue = lastHistoryValue.value || ''
  if (nextValue === currentValue) return

  if (history) {
    undoStack.value.push(currentValue)
    if (undoStack.value.length > maxHistorySize) undoStack.value.shift()
    redoStack.value = []
  }

  lastHistoryValue.value = nextValue
  locallyEmittedValue.value = nextValue
  emit('update:modelValue', nextValue)
}

const getTextarea = () => editorRef.value?.textarea

const updateText = async (value, selectionStart, selectionEnd = selectionStart) => {
  emit('update:modelValue', value)
  await nextTick()
  const textarea = getTextarea()
  if (!textarea) return
  textarea.focus()
  textarea.selectionStart = selectionStart
  textarea.selectionEnd = selectionEnd
}

// 保存 textarea 选中状态，防止点击工具栏按钮时焦点丢失导致选中清空
let savedSourceSelection = null

const saveSourceSelection = () => {
  if (viewMode.value === 'rich') return
  const textarea = getTextarea()
  if (textarea) {
    savedSourceSelection = {
      start: textarea.selectionStart,
      end: textarea.selectionEnd,
      direction: textarea.selectionDirection
    }
  }
}

const getSelection = () => {
  const value = props.modelValue || ''
  const textarea = getTextarea()
  let start = textarea?.selectionStart
  let end = textarea?.selectionEnd
  // fallback: 点击工具栏按钮后 textarea 失焦导致 selection 被清空，使用失焦前保存的值
  if ((start == null || start === end) && savedSourceSelection) {
    start = savedSourceSelection.start
    end = savedSourceSelection.end
    // 用后即清，避免下次操作读到过期选中
    savedSourceSelection = null
  }
  start = start ?? value.length
  end = end ?? value.length
  return { value, start, end, selected: value.slice(start, end) }
}

const replaceSelection = (replacement, cursorOffset = replacement.length, selectLength = 0) => {
  const { value, start, end } = getSelection()
  const nextValue = value.slice(0, start) + replacement + value.slice(end)
  updateText(nextValue, start + cursorOffset, start + cursorOffset + selectLength)
}

const replaceTextRange = (selection, replacement, cursorOffset = replacement.length, selectLength = 0) => {
  const { value, start, end } = selection
  const nextValue = value.slice(0, start) + replacement + value.slice(end)
  updateText(nextValue, start + cursorOffset, start + cursorOffset + selectLength)
}

const ensureBlockSpacing = (text) => {
  const { value, start } = getSelection()
  const before = value.slice(0, start)
  const after = value.slice(start)
  const prefix = before && !before.endsWith('\n\n') ? before.endsWith('\n') ? '\n' : '\n\n' : ''
  const suffix = after && !after.startsWith('\n\n') ? after.startsWith('\n') ? '\n' : '\n\n' : ''
  return `${prefix}${text}${suffix}`
}

const isRichRange = (range) => {
  const editor = richEditorRef.value
  if (!editor || !range) return false
  return editor.contains(range.commonAncestorContainer)
}

const saveRichSelection = () => {
  if (typeof window === 'undefined' || viewMode.value !== 'rich') return
  const selection = window.getSelection()
  if (!selection?.rangeCount) return
  const range = selection.getRangeAt(0)
  if (isRichRange(range)) savedRichRange.value = range.cloneRange()
  updateActiveCodeCard()
}

const restoreRichSelection = () => {
  const editor = richEditorRef.value
  if (!editor || typeof window === 'undefined') return
  editor.focus()
  if (!savedRichRange.value) return
  const selection = window.getSelection()
  selection.removeAllRanges()
  selection.addRange(savedRichRange.value)
}

const getRichSelectedText = () => {
  restoreRichSelection()
  return typeof window === 'undefined' ? '' : window.getSelection()?.toString() || ''
}

const syncRichEditor = async (force = false) => {
  await nextTick()
  const editor = richEditorRef.value
  if (!editor || viewMode.value !== 'rich') return
  if (!force && (richInputActive.value || richComposing.value)) return
  const html = renderMarkdownHtml(props.modelValue || '')
  if (editor.innerHTML !== html) editor.innerHTML = html
}

watch(
  () => viewMode.value,
  (value) => {
    if (value === 'rich') syncRichEditor(true)
  },
  { immediate: true }
)

watch(
  () => props.modelValue,
  (value) => {
    const nextValue = value || ''
    if (locallyEmittedValue.value === nextValue) {
      locallyEmittedValue.value = null
      return
    }

    if (richInputActive.value || applyingHistory.value) return
    lastHistoryValue.value = nextValue
    undoStack.value = []
    redoStack.value = []
    if (viewMode.value === 'rich') syncRichEditor(true)
  }
)

const cleanText = (text = '') => text.replace(/\u00a0/g, ' ').replace(/\u200b/g, '')

const codeTextFromNode = (node) => {
  if (!node) return ''
  if (node.nodeType === Node.TEXT_NODE) return node.textContent || ''
  if (node.nodeType !== Node.ELEMENT_NODE) return ''

  const element = node
  const tag = element.tagName.toLowerCase()
  if (tag === 'br') return '\n'

  const text = Array.from(element.childNodes).map(codeTextFromNode).join('')
  if (['div', 'p', 'li'].includes(tag) && text && !text.endsWith('\n')) return `${text}\n`
  return text
}

const readPreCodeText = (preElement) => {
  if (!preElement) return ''
  const nodes = Array.from(preElement.childNodes)
  return cleanText(nodes.map((node, index) => {
    let text = codeTextFromNode(node)
    const hasNextTextNode = nodes.slice(index + 1).some(nextNode => cleanText(codeTextFromNode(nextNode)).trim())
    if (hasNextTextNode && text && !text.endsWith('\n')) text += '\n'
    return text
  }).join('')).replace(/\n+$/g, '')
}

const normalizePreCodeElement = (preElement, language = '') => {
  const codeElement = preElement.querySelector('code') || document.createElement('code')
  const normalizedLanguage = normalizeCodeLanguage(language)
  codeElement.className = normalizedLanguage ? `language-${normalizedLanguage}` : ''
  codeElement.setAttribute('data-placeholder', codePlaceholder)

  while (preElement.firstChild) preElement.removeChild(preElement.firstChild)
  preElement.appendChild(codeElement)
  return codeElement
}

const inlineMarkdown = (node) => {
  if (node.nodeType === Node.TEXT_NODE) return cleanText(node.textContent)
  if (node.nodeType !== Node.ELEMENT_NODE) return ''

  const element = node
  const tag = element.tagName.toLowerCase()
  const children = () => Array.from(element.childNodes).map(inlineMarkdown).join('')

  if (tag === 'br') return '\n'
  if (tag === 'strong' || tag === 'b') {
    const text = children().trim()
    return text ? `**${text}**` : ''
  }
  if (tag === 'em' || tag === 'i') {
    const text = children().trim()
    return text ? `*${text}*` : ''
  }
  if (tag === 'code' && !element.closest('pre')) {
    return `\`${cleanText(element.textContent).replace(/`/g, '\\`')}\``
  }
  if (tag === 'a') {
    const text = children().trim()
    const href = element.getAttribute('href')
    return text && href ? `[${text}](${href})` : text
  }

  return children()
}

const languageFromCode = (codeElement) => {
  const className = codeElement?.getAttribute('class') || ''
  const match = className.match(/language-([^\s]+)/)
  return match ? match[1] : ''
}

const serializePre = (preElement) => {
  const codeElement = preElement.querySelector('code') || preElement
  const language = languageFromCode(codeElement)
  const code = readPreCodeText(preElement)
  return `\`\`\`${language}\n${code}\n\`\`\`\n\n`
}

const blockSelector = 'h1,h2,h3,h4,h5,h6,p,div,ul,ol,li,pre,blockquote,.md-code-card'

const hasBlockChild = (element) => {
  return Array.from(element.children || []).some(child => child.matches?.(blockSelector))
}

const serializeListItem = (itemElement) => {
  const inlineParts = []
  const nestedParts = []

  Array.from(itemElement.childNodes).forEach(child => {
    if (child.nodeType === Node.ELEMENT_NODE && ['ul', 'ol'].includes(child.tagName.toLowerCase())) {
      const nested = serializeBlock(child).trim()
      if (nested) nestedParts.push(nested.split('\n').map(line => `  ${line}`).join('\n'))
      return
    }
    inlineParts.push(inlineMarkdown(child))
  })

  return [inlineParts.join('').trim(), ...nestedParts].filter(Boolean).join('\n')
}

const serializeList = (listElement, ordered) => {
  const items = Array.from(listElement.children)
    .filter(child => child.tagName?.toLowerCase() === 'li')
    .map((item, index) => `${ordered ? `${index + 1}.` : '-'} ${serializeListItem(item)}`)
  return items.length ? `${items.join('\n')}\n\n` : ''
}

const serializeChildrenAsBlocks = (element) => {
  return Array.from(element.childNodes).map(serializeBlock).join('')
}

const serializeBlock = (node) => {
  if (node.nodeType === Node.TEXT_NODE) {
    const text = cleanText(node.textContent).trim()
    return text ? `${text}\n\n` : ''
  }
  if (node.nodeType !== Node.ELEMENT_NODE) return ''

  const element = node
  const tag = element.tagName.toLowerCase()

  if (element.classList.contains('md-code-card')) {
    const pre = element.querySelector('pre')
    return pre ? serializePre(pre) : ''
  }
  if (tag === 'pre') return serializePre(element)
  if (/^h[1-6]$/.test(tag)) {
    return `${'#'.repeat(Number(tag.slice(1)))} ${inlineMarkdown(element).trim()}\n\n`
  }
  if (tag === 'ul' || tag === 'ol') return serializeList(element, tag === 'ol')
  if (tag === 'blockquote') {
    const quote = serializeChildrenAsBlocks(element).trim()
    return quote ? `${quote.split('\n').map(line => `> ${line}`).join('\n')}\n\n` : ''
  }
  if (tag === 'p' || tag === 'div') {
    if (hasBlockChild(element)) return serializeChildrenAsBlocks(element)
    const text = inlineMarkdown(element).trim()
    return text ? `${text}\n\n` : ''
  }
  if (tag === 'br') return '\n'

  const text = inlineMarkdown(element).trim()
  return text ? `${text}\n\n` : ''
}

const serializeRichContent = () => {
  const editor = richEditorRef.value
  if (!editor) return ''
  return normalizeMarkdown(Array.from(editor.childNodes).map(serializeBlock).join(''))
}

const syncRichValueFromDom = async () => {
  richInputActive.value = true
  saveRichSelection()
  commitValue(serializeRichContent(), { history: true })
  await nextTick()
  richInputActive.value = false
  saveRichSelection()
}

const handleRichInput = (event) => {
  saveRichSelection()
  if (richComposing.value || event?.isComposing) return
  syncRichValueFromDom()
}

const handleRichCompositionStart = () => {
  richComposing.value = true
}

const handleRichCompositionEnd = () => {
  richComposing.value = false
  nextTick(() => syncRichValueFromDom())
}

const handleRichPaste = (event) => {
  event.preventDefault()
  const text = event.clipboardData?.getData('text/plain') || ''
  restoreRichSelection()
  document.execCommand('insertText', false, text)
  handleRichInput()
}

const handleRichClick = (event) => {
  const codeCard = event.target?.closest?.('.md-code-card')
  if (!codeCard) {
    updateActiveCodeCard()
    return
  }

  const pre = codeCard.querySelector('.md-code-block')
  const code = codeCard.querySelector('.md-code-block code')
  if (!code || readPreCodeText(pre).trim()) {
    updateActiveCodeCard()
    return
  }

  event.preventDefault()
  code.textContent = zeroWidthSpace
  placeCaret(code, false)
  setActiveCodeCard(codeCard)
}

const runRichCommand = (command, value = null) => {
  restoreRichSelection()
  document.execCommand(command, false, value)
  handleRichInput()
}

const insertRichHtml = (html) => {
  restoreRichSelection()
  document.execCommand('insertHTML', false, html)
  handleRichInput()
}

const getRichSelection = () => {
  if (typeof window === 'undefined') return null
  const selection = window.getSelection()
  if (!selection?.rangeCount) return null
  return selection
}

const getSelectionElement = (selection) => {
  const node = selection?.anchorNode
  if (!node) return null
  return node.nodeType === Node.ELEMENT_NODE ? node : node.parentElement
}

const setActiveCodeCard = (activeCard) => {
  const editor = richEditorRef.value
  if (!editor) return

  editor.querySelectorAll('.md-code-card').forEach(card => {
    const code = card.querySelector('.md-code-block code')
    if (card !== activeCard && code?.textContent === zeroWidthSpace) code.textContent = ''
    card.classList.toggle('is-editing', card === activeCard)
  })
}

const updateActiveCodeCard = () => {
  const editor = richEditorRef.value
  if (!editor) return

  const selection = getRichSelection()
  if (!selection) {
    setActiveCodeCard(null)
    return
  }
  const currentElement = getSelectionElement(selection)
  const codeCard = currentElement?.closest?.('.md-code-card')
  setActiveCodeCard(codeCard && editor.contains(codeCard) ? codeCard : null)
}

const placeCaret = (node, atStart = false) => {
  if (!node || typeof window === 'undefined') return
  const range = document.createRange()
  range.selectNodeContents(node)
  range.collapse(atStart)
  const selection = window.getSelection()
  selection.removeAllRanges()
  selection.addRange(range)
  saveRichSelection()
}

const focusLatestCodeBlock = async () => {
  await nextTick()
  const editor = richEditorRef.value
  const codeBlocks = editor?.querySelectorAll?.('.md-code-block code')
  const latestCode = codeBlocks?.[codeBlocks.length - 1]
  if (!latestCode) return
  if (!latestCode.textContent) latestCode.textContent = zeroWidthSpace
  placeCaret(latestCode, false)
  setActiveCodeCard(latestCode.closest('.md-code-card'))
}

const syncRichHistoryCommand = async (command) => {
  if (command === 'undo' && !undoStack.value.length) return
  if (command === 'redo' && !redoStack.value.length) return

  applyingHistory.value = true
  const currentValue = lastHistoryValue.value || ''
  const nextValue = command === 'undo' ? undoStack.value.pop() : redoStack.value.pop()
  if (command === 'undo') {
    redoStack.value.push(currentValue)
  } else {
    undoStack.value.push(currentValue)
  }

  lastHistoryValue.value = nextValue || ''
  locallyEmittedValue.value = lastHistoryValue.value
  emit('update:modelValue', lastHistoryValue.value)
  await nextTick()
  applyingHistory.value = false
  await syncRichEditor(true)
  await nextTick()
  placeCaret(richEditorRef.value, false)
}

const removeEmptyCodeCardOnBackspace = () => {
  const editor = richEditorRef.value
  const selection = getRichSelection()
  if (!editor || !selection?.isCollapsed) return false

  const currentElement = getSelectionElement(selection)
  const codeCard = currentElement?.closest?.('.md-code-card')
  const preElement = codeCard?.querySelector?.('pre')
  const codeElement = preElement?.querySelector?.('code') || preElement
  if (!codeCard || !preElement || !codeElement || !preElement.contains(selection.anchorNode)) return false

  const codeText = readPreCodeText(preElement).replace(/\u200b/g, '').trim()
  if (codeText) return false

  const nextTarget = codeCard.nextSibling
  const previousTarget = codeCard.previousSibling
  codeCard.remove()

  if (!editor.childNodes.length) {
    editor.innerHTML = ''
    placeCaret(editor)
  } else if (nextTarget) {
    placeCaret(nextTarget, true)
  } else {
    placeCaret(previousTarget, false)
  }

  syncRichValueFromDom()
  return true
}

const wrapSelection = (left, right, fallback) => {
  const { selected } = getSelection()
  const content = selected || fallback
  replaceSelection(`${left}${content}${right}`, left.length, content.length)
}

const toggleBold = () => {
  if (viewMode.value === 'rich') {
    runRichCommand('bold')
    return
  }
  wrapSelection('**', '**', '重点内容')
}

const insertInlineCode = () => {
  if (viewMode.value === 'rich') {
    const content = getRichSelectedText() || 'code'
    insertRichHtml(`<code>${escapeHtml(content)}</code>`)
    return
  }
  wrapSelection('`', '`', 'code')
}

const normalizeLinkUrl = (value = '') => {
  const trimmed = value.trim()
  if (!trimmed) return ''
  if (/^https?:\/\//i.test(trimmed)) return trimmed
  if (/^[\w.-]+\.[a-z]{2,}(:\d+)?([/?#].*)?$/i.test(trimmed)) return `https://${trimmed}`
  return ''
}

const isValidLinkUrl = (url) => {
  try {
    const parsed = new URL(url)
    return ['http:', 'https:'].includes(parsed.protocol)
  } catch (e) {
    return false
  }
}

const escapeMarkdownLinkText = (text) => text.replace(/\\/g, '\\\\').replace(/\]/g, '\\]')
const escapeMarkdownLinkUrl = (url) => url.replace(/\s/g, '%20').replace(/\)/g, '%29')

const requestLinkUrl = async () => {
  try {
    const { value } = await ElMessageBox.prompt('输入要跳转的网页地址', '添加超链接', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPlaceholder: 'https://example.com',
      inputValidator: (input) => {
        const normalized = normalizeLinkUrl(input || '')
        return normalized && isValidLinkUrl(normalized) ? true : '请输入有效的 http/https 网页地址'
      }
    })
    return normalizeLinkUrl(value)
  } catch (e) {
    return ''
  }
}

const insertLink = async () => {
  if (viewMode.value === 'rich') {
    const selectedText = getRichSelectedText().trim()
    const url = await requestLinkUrl()
    if (!url) return
    const text = selectedText || url
    insertRichHtml(
      `<a href="${escapeHtml(url)}" target="_blank" rel="noopener noreferrer">${escapeHtml(text)}</a>`
    )
    return
  }

  const selection = getSelection()
  const selectedText = selection.selected.trim()
  const url = await requestLinkUrl()
  if (!url) return

  const text = selectedText || '链接文字'
  const markdownLink = `[${escapeMarkdownLinkText(text)}](${escapeMarkdownLinkUrl(url)})`
  replaceTextRange(selection, markdownLink, 1, text.length)
}

const insertHeading = (level) => {
  if (viewMode.value === 'rich') {
    runRichCommand('formatBlock', `h${level}`)
    return
  }
  const prefix = `${'#'.repeat(level)} `
  const { selected } = getSelection()
  const content = selected || `标题 ${level}`
  const lines = content.split('\n').map(line => `${prefix}${line || `标题 ${level}`}`).join('\n')
  replaceSelection(lines, prefix.length, selected ? lines.length - prefix.length : content.length)
}

const insertList = (prefix) => {
  if (viewMode.value === 'rich') {
    runRichCommand('insertUnorderedList')
    return
  }
  const { selected } = getSelection()
  const content = selected || '要点一\n要点二'
  const lines = content.split('\n').map(line => `${prefix}${line || '要点'}`).join('\n')
  replaceSelection(lines, prefix.length, selected ? lines.length - prefix.length : 3)
}

const insertOrderedList = () => {
  if (viewMode.value === 'rich') {
    runRichCommand('insertOrderedList')
    return
  }
  const { selected } = getSelection()
  const content = selected || '第一步\n第二步'
  const lines = content.split('\n').map((line, index) => `${index + 1}. ${line || '步骤'}`).join('\n')
  replaceSelection(lines, 3, selected ? lines.length - 3 : 3)
}

const detectCodeLanguage = (code) => {
  const source = code.trim()
  if (!source) return ''
  if (/^\s*[{[]/.test(source)) return 'json'
  if (/\b(public|private|protected|class|interface|SpringBootApplication|System\.out)\b/.test(source)) return 'java'
  if (/\b(function|const|let|var|=>|console\.log|import\s+.+\s+from)\b/.test(source)) return 'javascript'
  if (/\b(SELECT|INSERT|UPDATE|DELETE|CREATE|ALTER|FROM|WHERE|JOIN)\b/i.test(source)) return 'sql'
  if (/^(npm|pnpm|yarn|mvn|gradle|curl|cd|ls|grep|docker)\b/m.test(source)) return 'bash'
  return ''
}

const formatCodeBlocksInMarkdown = (text = '') => {
  return formatCodeFences(text)
}

const formatRichCodeBlocksInDom = () => {
  const editor = richEditorRef.value
  if (!editor) return false

  let changed = false
  editor.querySelectorAll('pre.md-code-block').forEach((preElement) => {
    const codeElement = preElement.querySelector('code') || preElement
    const language = languageFromCode(codeElement)
    const sourceCode = readPreCodeText(preElement)

    if (!sourceCode.trim()) {
      if (preElement.textContent) {
        normalizePreCodeElement(preElement, language).textContent = ''
        changed = true
      }
      return
    }

    const formattedCode = formatCodeBlock(sourceCode, language)
    const isPlainCodeElement = codeElement !== preElement &&
      preElement.childNodes.length === 1 &&
      preElement.firstChild === codeElement &&
      Array.from(codeElement.childNodes).every(child => child.nodeType === Node.TEXT_NODE)

    if (formattedCode !== sourceCode || !isPlainCodeElement) {
      normalizePreCodeElement(preElement, language).textContent = formattedCode
      changed = true
    }
  })

  return changed
}

const insertCodeBlock = (language) => {
  if (viewMode.value === 'rich') {
    const selectedCode = getRichSelectedText()
    const resolvedLanguage = language === 'auto' ? detectCodeLanguage(selectedCode) : language
    insertRichHtml(renderCodeCardHtml(selectedCode, resolvedLanguage || ''))
    focusLatestCodeBlock()
    return
  }

  const { selected } = getSelection()
  const sample = selected || ''
  const resolvedLanguage = language === 'auto' ? detectCodeLanguage(sample) : language
  const prefix = `\n\`\`\`${resolvedLanguage || ''}\n`
  const suffix = '\n```\n'
  replaceSelection(`${prefix}${sample}${suffix}`, prefix.length, sample.length)
}

const insertTemplate = (type) => {
  const template = templates[type]
  if (!template) return

  if (viewMode.value === 'rich') {
    insertRichHtml(renderMarkdownHtml(template))
    return
  }

  const block = ensureBlockSpacing(template)
  const firstListItem = block.indexOf('- ')
  const firstOrderedItem = block.indexOf('1. ')
  const firstEditableIndex = [firstListItem, firstOrderedItem]
    .filter(index => index >= 0)
    .sort((a, b) => a - b)[0]
  const cursorOffset = firstEditableIndex >= 0 ? firstEditableIndex + 2 : block.length
  replaceSelection(block, cursorOffset, 0)
}

const formatMarkdown = async () => {
  if (viewMode.value === 'rich') {
    formatRichCodeBlocksInDom()
    const normalized = normalizeMarkdown(serializeRichContent())
    commitValue(normalized, { history: true })
    await nextTick()
    richInputActive.value = false
    syncRichEditor(true)
    return
  }

  const normalized = normalizeMarkdown(formatCodeBlocksInMarkdown(props.modelValue || ''))
  updateText(normalized, normalized.length)
}

const formatCodeBlocks = async () => {
  if (viewMode.value === 'rich') {
    formatRichCodeBlocksInDom()
    const normalized = normalizeMarkdown(serializeRichContent())
    commitValue(normalized, { history: true })
    await nextTick()
    richInputActive.value = false
    await syncRichEditor(true)
    return
  }

  const formatted = formatCodeBlocksInMarkdown(props.modelValue || '')
  updateText(formatted, formatted.length)
}

const handleEditorKeydown = (event) => {
  if (event.key === 'Escape') {
    event.stopPropagation()
    event.preventDefault()
    emit('escape')
    return
  }

  const modifierPressed = event.metaKey || event.ctrlKey
  if (!modifierPressed) return

  const key = event.key.toLowerCase()
  if (key === 'b') {
    event.preventDefault()
    toggleBold()
  } else if (key === 'k') {
    event.preventDefault()
    insertLink()
  } else if (key === '`') {
    event.preventDefault()
    insertCodeBlock('auto')
  } else if (event.shiftKey && key === 'f') {
    event.preventDefault()
    formatMarkdown()
  }
}

const handleRichKeydown = (event) => {
  if (event.key === 'Escape') {
    event.stopPropagation()
    event.preventDefault()
    emit('escape')
    return
  }

  if (!richComposing.value && event.key === 'Backspace' && removeEmptyCodeCardOnBackspace()) {
    event.preventDefault()
    return
  }

  const modifierPressed = event.metaKey || event.ctrlKey
  if (!modifierPressed) return

  const key = event.key.toLowerCase()
  if (key === 'z') {
    event.preventDefault()
    syncRichHistoryCommand(event.shiftKey ? 'redo' : 'undo')
  } else if (key === 'y') {
    event.preventDefault()
    syncRichHistoryCommand('redo')
  } else if (key === 'b') {
    event.preventDefault()
    toggleBold()
  } else if (key === 'k') {
    event.preventDefault()
    insertLink()
  } else if (key === '`') {
    event.preventDefault()
    insertCodeBlock('auto')
  } else if (event.shiftKey && key === 'f') {
    event.preventDefault()
    formatMarkdown()
  }
}
</script>

<style scoped>
.answer-editor {
  min-width: 0;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  margin-bottom: 8px;
  background: #f8faf9;
  border: 1px solid #dfe5e8;
  border-radius: 8px;
}

.editor-toolbar :deep(.el-button) {
  min-width: 34px;
  padding: 0 9px !important;
  color: #344054;
  border-radius: 6px !important;
}

.editor-toolbar :deep(.el-button + .el-button) {
  margin-left: 0;
}

.symbol-button {
  font-weight: 900 !important;
}

.view-switch {
  margin-left: auto;
}

.editor-workspace {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.editor-workspace.mode-split {
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
}

.rich-editor {
  padding: 18px;
  overflow: auto;
  color: #344054;
  font-size: 15px;
  line-height: 1.85;
  word-break: break-word;
  background: #f8faf9;
  border: 1px solid #dfe5e8;
  border-radius: 8px;
  outline: none;
}

.rich-editor:focus {
  border-color: #0f766e;
  box-shadow: 0 0 0 2px rgba(15, 118, 110, 0.12);
}

.rich-editor:empty::before {
  color: #98a2b3;
  content: attr(data-placeholder);
}

.rich-editor :deep(*) {
  max-width: 100%;
}

.rich-editor :deep(*:first-child) {
  margin-top: 0;
}

.rich-editor :deep(*:last-child) {
  margin-bottom: 0;
}

.rich-editor :deep(h1),
.rich-editor :deep(h2),
.rich-editor :deep(h3),
.rich-editor :deep(h4) {
  margin: 18px 0 10px;
  color: #1f2933;
  font-weight: 800;
  line-height: 1.35;
}

.rich-editor :deep(h1) {
  font-size: 22px;
}

.rich-editor :deep(h2) {
  font-size: 19px;
}

.rich-editor :deep(h3) {
  font-size: 17px;
}

.rich-editor :deep(h4) {
  font-size: 15px;
}

.rich-editor :deep(p),
.rich-editor :deep(ul),
.rich-editor :deep(ol),
.rich-editor :deep(blockquote),
.rich-editor :deep(pre),
.rich-editor :deep(table),
.rich-editor :deep(.md-code-card) {
  margin: 0 0 12px;
}

.rich-editor :deep(ul),
.rich-editor :deep(ol) {
  padding-left: 22px;
}

.rich-editor :deep(li + li) {
  margin-top: 4px;
}

.rich-editor :deep(a) {
  color: #0f766e;
  font-weight: 700;
  text-decoration: none;
}

.rich-editor :deep(blockquote) {
  padding: 10px 14px;
  color: #5f6f7d;
  background: #eef5f3;
  border-left: 3px solid #0f766e;
  border-radius: 0 8px 8px 0;
}

.rich-editor :deep(code) {
  padding: 2px 5px;
  color: #0f766e;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 0.92em;
  background: #e6f4f1;
  border-radius: 5px;
}

.rich-editor :deep(.md-code-card) {
  overflow: hidden;
  background: #111827;
  border: 1px solid #263244;
  border-radius: 8px;
}

.rich-editor :deep(.md-code-head) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 34px;
  padding: 0 12px;
  color: #d1d5db;
  font-size: 12px;
  font-weight: 800;
  line-height: 1;
  background: #1f2937;
  border-bottom: 1px solid #374151;
  user-select: none;
}

.rich-editor :deep(.md-code-lang) {
  min-width: 0;
  overflow: hidden;
  color: #f9fafb;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rich-editor :deep(.md-code-state) {
  flex: none;
  padding: 2px 7px;
  color: #9ca3af;
  font-size: 11px;
  line-height: 1.4;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 999px;
}

.rich-editor :deep(pre.md-code-block) {
  margin: 0;
  overflow-x: auto;
  background: #111827;
  border: 0;
  border-radius: 0;
}

.rich-editor :deep(pre.md-code-block code) {
  display: block;
  min-width: max-content;
  min-height: 24px;
  padding: 14px;
  color: #e5e7eb;
  line-height: 1.7;
  tab-size: 2;
  white-space: pre;
  background: transparent;
  border-radius: 0;
}

.rich-editor :deep(pre.md-code-block code[data-placeholder]:empty::before) {
  color: #98a2b3;
  font-style: italic;
  content: attr(data-placeholder);
  pointer-events: none;
}

.rich-editor :deep(.md-code-card.is-editing pre.md-code-block code[data-placeholder]:empty::before) {
  content: "";
}

.preview-pane {
  min-width: 0;
  max-height: 420px;
  overflow: auto;
}

.preview-pane :deep(.markdown-view) {
  min-height: 100%;
}

.answer-editor :deep(.el-textarea__inner) {
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, "PingFang SC", monospace;
  line-height: 1.75;
}

.editor-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  color: #667085;
  font-size: 12px;
  font-weight: 700;
}

.editor-footer span {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 0 7px;
  background: #f0f3f5;
  border-radius: 6px;
}

.smart-state.ok {
  color: #0f766e;
  background: #e6f4f1;
}

.smart-state.warn {
  color: #b45309;
  background: #fff4df;
}

.smart-state.muted {
  color: #667085;
}

@media (max-width: 760px) {
  .view-switch {
    width: 100%;
    margin-left: 0;
  }

  .view-switch :deep(.el-radio-button) {
    flex: 1;
  }

  .view-switch :deep(.el-radio-button__inner) {
    width: 100%;
  }

  .editor-workspace.mode-split {
    grid-template-columns: 1fr;
  }
}
</style>
