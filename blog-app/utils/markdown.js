// 轻量级 Markdown 解析器（无外部依赖）
// 支持：标题、粗体、斜体、行内代码、代码块、链接、图片、引用、
// 有序/无序列表、分割线、GFM 换行、段落。输出 HTML 字符串。
// 注：替换原 marked 依赖，避免 HBuilderX uni-app 项目无 node_modules 时 500 报错。

// HTML 转义，防止注入
const escapeHtml = (str) => {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

// 行内格式解析：粗体、斜体、行内代码、链接、图片
const parseInline = (text) => {
  // 先用占位符保护行内代码和图片/链接，避免被后续规则二次处理
  const placeholders = []
  const put = (html) => {
    placeholders.push(html)
    return `\u0000${placeholders.length - 1}\u0000`
  }

  // 行内代码 `code`
  text = text.replace(/`([^`]+)`/g, (_, code) => put(`<code>${escapeHtml(code)}</code>`))

  // 图片 ![alt](url)
  text = text.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, (_, alt, url) =>
    put(`<img src="${escapeHtml(url)}" alt="${escapeHtml(alt)}" referrerpolicy="no-referrer" loading="lazy" />`)
  )

  // 链接 [text](url)
  text = text.replace(/\[([^\]]+)\]\(([^)]+)\)/g, (_, label, url) =>
    put(`<a href="${escapeHtml(url)}" target="_blank" rel="noopener noreferrer">${escapeHtml(label)}</a>`)
  )

  // 转义剩余的 HTML 特殊字符
  text = escapeHtml(text)

  // 粗体 **text** 或 __text__
  text = text.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  text = text.replace(/__([^_]+)__/g, '<strong>$1</strong>')

  // 斜体 *text* 或 _text_
  text = text.replace(/(^|[^*])\*([^*]+)\*/g, '$1<em>$2</em>')
  text = text.replace(/(^|[^_])_([^_]+)_/g, '$1<em>$2</em>')

  // 还原占位符
  text = text.replace(/\u0000(\d+)\u0000/g, (_, i) => placeholders[Number(i)])

  return text
}

/**
 * 将 Markdown 文本解析为 HTML 字符串
 * @param {string} markdownText
 * @returns {string}
 */
export const parseMarkdown = (markdownText) => {
  if (!markdownText) return ''
  try {
    const src = String(markdownText)
    const lines = src.replace(/\r\n/g, '\n').split('\n')
    const html = []
    let i = 0

    while (i < lines.length) {
      const line = lines[i]

      // 空行
      if (/^\s*$/.test(line)) {
        i++
        continue
      }

      // 代码块 ```
      if (/^```/.test(line.trim())) {
        const lang = line.trim().slice(3).trim()
        const codeLines = []
        i++
        while (i < lines.length && !/^```/.test(lines[i].trim())) {
          codeLines.push(lines[i])
          i++
        }
        i++ // 跳过结束 ```
        const langAttr = lang ? ` class="language-${escapeHtml(lang)}"` : ''
        html.push(`<pre><code${langAttr}>${escapeHtml(codeLines.join('\n'))}</code></pre>`)
        continue
      }

      // 标题 # ~ ######
      const headMatch = /^(#{1,6})\s+(.*)$/.exec(line)
      if (headMatch) {
        const level = headMatch[1].length
        html.push(`<h${level}>${parseInline(headMatch[2])}</h${level}>`)
        i++
        continue
      }

      // 分割线 --- / *** / ___
      if (/^\s*([-*_])\1{2,}\s*$/.test(line)) {
        html.push('<hr />')
        i++
        continue
      }

      // 引用 >（连续行合并）
      if (/^>\s?/.test(line)) {
        const quoteLines = []
        while (i < lines.length && /^>\s?/.test(lines[i])) {
          quoteLines.push(lines[i].replace(/^>\s?/, ''))
          i++
        }
        html.push(`<blockquote>${parseInline(quoteLines.join('<br>'))}</blockquote>`)
        continue
      }

      // 无序列表 - / * / +
      if (/^\s*[-*+]\s+/.test(line)) {
        const items = []
        while (i < lines.length && /^\s*[-*+]\s+/.test(lines[i])) {
          items.push(`<li>${parseInline(lines[i].replace(/^\s*[-*+]\s+/, ''))}</li>`)
          i++
        }
        html.push(`<ul>${items.join('')}</ul>`)
        continue
      }

      // 有序列表 1. / 2.
      if (/^\s*\d+\.\s+/.test(line)) {
        const items = []
        while (i < lines.length && /^\s*\d+\.\s+/.test(lines[i])) {
          items.push(`<li>${parseInline(lines[i].replace(/^\s*\d+\.\s+/, ''))}</li>`)
          i++
        }
        html.push(`<ol>${items.join('')}</ol>`)
        continue
      }

      // 普通段落：连续非空、非块级标记的行合并，GFM 单换行转 <br>
      const paraLines = []
      while (
        i < lines.length &&
        !/^\s*$/.test(lines[i]) &&
        !/^```/.test(lines[i].trim()) &&
        !/^(#{1,6})\s+/.test(lines[i]) &&
        !/^\s*([-*_])\1{2,}\s*$/.test(lines[i]) &&
        !/^>\s?/.test(lines[i]) &&
        !/^\s*[-*+]\s+/.test(lines[i]) &&
        !/^\s*\d+\.\s+/.test(lines[i])
      ) {
        paraLines.push(lines[i])
        i++
      }
      html.push(`<p>${paraLines.map(parseInline).join('<br>')}</p>`)
    }

    return html.join('\n')
  } catch (e) {
    console.error('Markdown解析失败', e)
    return markdownText
  }
}

export default { parseMarkdown }
