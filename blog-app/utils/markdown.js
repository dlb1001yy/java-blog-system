// 假设项目中已引入 marked 库 (npm install marked)
import marked from 'marked'

// 配置 marked 选项
marked.setOptions({
  breaks: true, // 支持 Github 换行
  gfm: true     // 支持 Github 风格 Markdown
})

export const parseMarkdown = (markdownText) => {
  if (!markdownText) return ''
  try {
    return marked.parse(markdownText)
  } catch (e) {
    console.error('Markdown解析失败', e)
    return markdownText
  }
}
