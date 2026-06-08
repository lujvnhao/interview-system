const LANGUAGE_ALIASES = {
  js: 'javascript',
  jsx: 'javascript',
  ts: 'typescript',
  tsx: 'typescript',
  sh: 'bash',
  shell: 'bash',
  zsh: 'bash',
  mysql: 'sql',
  postgresql: 'sql',
  postgres: 'sql'
}

const LANGUAGE_LABELS = {
  bash: 'Shell',
  c: 'C',
  cpp: 'C++',
  css: 'CSS',
  go: 'Go',
  html: 'HTML',
  java: 'Java',
  javascript: 'JavaScript',
  json: 'JSON',
  kotlin: 'Kotlin',
  python: 'Python',
  sql: 'SQL',
  typescript: 'TypeScript',
  xml: 'XML'
}

const BRACE_LANGUAGES = new Set([
  'c',
  'cpp',
  'css',
  'go',
  'java',
  'javascript',
  'kotlin',
  'typescript'
])

const normalizeNewlines = (code = '') => String(code).replace(/\r\n?/g, '\n').replace(/\u200b/g, '')

const trimCode = (code = '') => {
  const lines = normalizeNewlines(code).split('\n').map(line => line.replace(/[ \t]+$/g, ''))
  while (lines.length && !lines[0].trim()) lines.shift()
  while (lines.length && !lines[lines.length - 1].trim()) lines.pop()
  return lines.join('\n')
}

const compactBlankLines = (code = '') => trimCode(code).replace(/\n{3,}/g, '\n\n')

export const normalizeCodeLanguage = (language = '') => {
  const normalized = String(language).trim().toLowerCase()
  return LANGUAGE_ALIASES[normalized] || normalized
}

export const getCodeLanguageLabel = (language = '') => {
  const normalized = normalizeCodeLanguage(language)
  if (!normalized) return '代码'
  return LANGUAGE_LABELS[normalized] || normalized.toUpperCase()
}

const formatJson = (source) => {
  try {
    return JSON.stringify(JSON.parse(source), null, 2)
  } catch (error) {
    return compactBlankLines(source)
  }
}

const uppercaseSqlKeywords = (sql) => {
  const keywords = [
    'left join', 'right join', 'inner join', 'full join', 'cross join',
    'group by', 'order by', 'insert into', 'delete from', 'create table',
    'alter table', 'select', 'from', 'where', 'join', 'having', 'limit',
    'offset', 'values', 'update', 'set', 'and', 'or', 'on', 'as', 'asc', 'desc'
  ]

  return keywords.reduce((result, keyword) => {
    const pattern = new RegExp(`\\b${keyword.replace(/\s+/g, '\\s+')}\\b`, 'gi')
    return result.replace(pattern, keyword.toUpperCase())
  }, sql)
}

const formatSql = (source) => {
  const squashed = compactBlankLines(source).replace(/\s+/g, ' ')
  const keyworded = uppercaseSqlKeywords(squashed)
    .replace(/\s*,\s*/g, ',\n  ')
    .replace(/\b(SELECT|FROM|WHERE|GROUP\s+BY|ORDER\s+BY|HAVING|LIMIT|OFFSET|VALUES|SET)\b/g, '\n$1')
    .replace(/\b((?:LEFT|RIGHT|INNER|FULL|CROSS)\s+JOIN|JOIN)\b/g, '\n$1')
    .replace(/\b(AND|OR)\b/g, '\n  $1')
    .replace(/;\s*/g, ';\n')

  return keyworded
    .split('\n')
    .map(line => line.trim())
    .filter((line, index, lines) => line.trim() || (index > 0 && lines[index - 1].trim()))
    .map((line) => {
      const topLevelKeyword = /^(SELECT|FROM|WHERE|GROUP\s+BY|ORDER\s+BY|HAVING|LIMIT|OFFSET|VALUES|SET|(?:LEFT|RIGHT|INNER|FULL|CROSS)\s+JOIN|JOIN)\b/
      return topLevelKeyword.test(line) ? line : `  ${line}`
    })
    .join('\n')
    .trim()
}

const countStructuralChar = (line, target) => {
  let count = 0
  let quote = ''
  let escaped = false

  for (const char of line) {
    if (quote) {
      if (escaped) {
        escaped = false
      } else if (char === '\\') {
        escaped = true
      } else if (char === quote) {
        quote = ''
      }
      continue
    }

    if (char === '"' || char === "'" || char === '`') {
      quote = char
      continue
    }

    if (char === target) count += 1
  }

  return count
}

const splitBraceCode = (source) => {
  let result = ''
  let quote = ''
  let escaped = false
  let lineComment = false
  let blockComment = false
  let parenDepth = 0

  const trimRight = () => {
    result = result.replace(/[ \t]+$/g, '')
  }

  for (let index = 0; index < source.length; index += 1) {
    const char = source[index]
    const next = source[index + 1]

    if (lineComment) {
      result += char
      if (char === '\n') lineComment = false
      continue
    }

    if (blockComment) {
      result += char
      if (char === '*' && next === '/') {
        result += next
        index += 1
        blockComment = false
      }
      continue
    }

    if (quote) {
      result += char
      if (escaped) {
        escaped = false
      } else if (char === '\\') {
        escaped = true
      } else if (char === quote) {
        quote = ''
      }
      continue
    }

    if (char === '/' && next === '/') {
      lineComment = true
      result += char + next
      index += 1
      continue
    }

    if (char === '/' && next === '*') {
      blockComment = true
      result += char + next
      index += 1
      continue
    }

    if (char === '"' || char === "'" || char === '`') {
      quote = char
      result += char
      continue
    }

    if (char === '(' || char === '[') parenDepth += 1
    if ((char === ')' || char === ']') && parenDepth > 0) parenDepth -= 1

    if (char === '{') {
      trimRight()
      if (result && !result.endsWith('\n') && !result.endsWith(' ')) result += ' '
      result += '{\n'
      continue
    }

    if (char === '}') {
      trimRight()
      if (result && !result.endsWith('\n')) result += '\n'
      result += '}\n'
      continue
    }

    if (char === ';' && parenDepth === 0) {
      result += ';\n'
      continue
    }

    result += char
  }

  return result
}

const mergeBraceContinuations = (lines) => {
  const merged = []

  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index]
    const next = lines[index + 1]
    if (/^}$/.test(line) && /^(else|catch|finally)\b/.test(next || '')) {
      merged.push(`${line} ${next}`)
      index += 1
    } else {
      merged.push(line)
    }
  }

  return merged
}

const formatBraceCode = (source) => {
  const lines = mergeBraceContinuations(
    splitBraceCode(compactBlankLines(source))
      .replace(/\n{2,}/g, '\n')
      .split('\n')
      .map(line => line.trim())
  )

  const formatted = []
  let indent = 0
  let previousBlank = false

  lines.forEach((line) => {
    if (!line) {
      if (!previousBlank && formatted.length) formatted.push('')
      previousBlank = true
      return
    }

    previousBlank = false

    if (/^}/.test(line) || /^(case\b.+:|default:)/.test(line)) {
      indent = Math.max(0, indent - 1)
    }

    formatted.push(`${'  '.repeat(indent)}${line}`)

    const openCount = countStructuralChar(line, '{')
    const closeCount = countStructuralChar(line, '}')
    const consumedClose = /^}/.test(line) ? 1 : 0
    indent = Math.max(0, indent + openCount - Math.max(0, closeCount - consumedClose))

    if (/^(case\b.+:|default:)/.test(line)) indent += 1
  })

  return formatted.join('\n').trim()
}

const formatShell = (source) => {
  const lines = compactBlankLines(source).split('\n')
  const formatted = []
  let indent = 0

  lines.forEach((rawLine) => {
    const line = rawLine.trim()
    if (!line) {
      if (formatted[formatted.length - 1] !== '') formatted.push('')
      return
    }

    if (/^(done|fi|esac|else|elif)\b/.test(line)) indent = Math.max(0, indent - 1)

    formatted.push(`${'  '.repeat(indent)}${line}`)

    if (/^(if|for|while|until|case)\b/.test(line) || /\b(do|then)$/.test(line)) indent += 1
    if (/^(else|elif)\b/.test(line)) indent += 1
  })

  return formatted.join('\n').trim()
}

export const formatCodeBlock = (code = '', language = '') => {
  const source = compactBlankLines(code)
  if (!source) return ''

  const normalizedLanguage = normalizeCodeLanguage(language)
  if (normalizedLanguage === 'json') return formatJson(source)
  if (normalizedLanguage === 'sql') return formatSql(source)
  if (normalizedLanguage === 'bash') return formatShell(source)
  if (BRACE_LANGUAGES.has(normalizedLanguage)) return formatBraceCode(source)

  return source
}

export const formatCodeFences = (text = '') => {
  const lines = String(text || '').replace(/\r\n?/g, '\n').split('\n')
  const formattedLines = []

  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index]
    const openMatch = line.match(/^```([^\n`]*)$/)
    if (!openMatch) {
      formattedLines.push(line)
      continue
    }

    const info = openMatch[1].trim()
    const codeLines = []
    let closeIndex = -1

    for (let cursor = index + 1; cursor < lines.length; cursor += 1) {
      if (/^```\s*$/.test(lines[cursor])) {
        closeIndex = cursor
        break
      }
      codeLines.push(lines[cursor])
    }

    if (closeIndex < 0) {
      formattedLines.push(line, ...codeLines)
      index += codeLines.length
      continue
    }

    const language = info.split(/\s+/)[0] || ''
    formattedLines.push(`\`\`\`${info}`)
    formattedLines.push(formatCodeBlock(codeLines.join('\n'), language))
    formattedLines.push('```')
    index = closeIndex
  }

  return formattedLines.join('\n')
}
