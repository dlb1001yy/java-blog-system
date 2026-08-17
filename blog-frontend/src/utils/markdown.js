import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const wrapCodeBlock = (preHtml) =>
  `<div class="code-block"><div class="code-block-header"><span class="dot dot-red"></span><span class="dot dot-yellow"></span><span class="dot dot-green"></span><button class="copy-btn" type="button">复制</button></div>${preHtml}</div>`

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight(str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return wrapCodeBlock(`<pre class="hljs"><code>${hljs.highlight(str, { language: lang }).value}</code></pre>`)
      } catch (_) {}
    }
    return wrapCodeBlock(`<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`)
  }
})

// 外链新窗口打开（锚点链接除外）
const defaultLinkOpen = md.renderer.rules.link_open || function (tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}
md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  const href = tokens[idx].attrGet('href') || ''
  if (/^https?:\/\//i.test(href)) {
    tokens[idx].attrSet('target', '_blank')
    tokens[idx].attrSet('rel', 'noopener noreferrer')
  }
  return defaultLinkOpen(tokens, idx, options, env, self)
}
// 图片：防盗链 + 懒加载（与 meta 双保险）
const defaultImage = md.renderer.rules.image || function (tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}
md.renderer.rules.image = function (tokens, idx, options, env, self) {
  tokens[idx].attrSet('referrerpolicy', 'no-referrer')
  tokens[idx].attrSet('loading', 'lazy')
  return defaultImage(tokens, idx, options, env, self)
}

export default md