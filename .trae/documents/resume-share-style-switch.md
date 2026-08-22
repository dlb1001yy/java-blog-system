# 计划：分享简历页支持多风格切换查看

## Summary
让分享页 `/resume/share/:token`（Resume.vue）也能像编辑页预览一样，用 4 种流行风格查看简历。复用刚建好的 ResumePreview 组件，改动最小。

## Current State
- [Resume.vue](d:/my-project/java-blog-system/blog-frontend/src/views/Resume.vue) 自绘单一样式（el-avatar、timeline、card 等，约 170 行模板）。
- 已有 computed：`skills`/`workExperience`/`projects`/`education`/`certificates`（解析 JSON 字段），数据来源 `resume` ref。
- [ResumePreview.vue](d:/my-project/java-blog-system/blog-frontend/src/components/ResumePreview.vue) 接收 props：`resume, skills, works, projects, educations, certificates, style`（modern/classic/sidebar/bold）。

## Proposed Changes

文件：`blog-frontend/src/views/Resume.vue`

1. **新增风格状态与切换控件**：`const previewStyle = ref('modern')`；在页面顶部（简历卡片上方）加一行悬浮工具栏：风格切换（el-radio-group + el-radio-button，同 ProfileResume 预览的四个选项）+ 保留已有的「打印/导出 PDF」按钮（如现页面已有则移入工具栏，没有则不新增）。
2. **替换正文渲染**：将现有自绘的简历主体（header/简介/技能/timeline/项目/教育/证书等卡片）替换为：

```html
<div class="resume-paper">
  <ResumePreview
    :resume="resume"
    :skills="skills"
    :works="workExperience"
    :projects="projects"
    :educations="education"
    :certificates="certificates"
    :style="previewStyle"
  />
</div>
```

   现有 computed 数据结构字段与 ResumePreview 的渲染字段一致（name/percent/level、company/position/startDate/endDate/description、school/major/degree、name/issuer/date），无需适配。
3. **删除**：被替换掉的旧模板区块及其不再引用的样式类（avatar/timeline/skills-grid 等）、不再使用的 element-plus 图标 import（保留工具栏实际用到的）。
4. **样式**：`.resume-paper` 加白色纸张卡片样式（阴影、圆角、min-height），风格切换按钮组吸顶或居中于工具栏。
5. 页面仍保持 `hideLayout`（上轮已加），工具栏不打印（`@media print` 中隐藏）。

## Assumptions & Decisions
- 不新增 URL 参数持久化风格（刷新回到默认 modern），保持简单。
- 后端、分享 token 逻辑均不动。

## Verification
1. 打开分享链接：默认「现代极简」渲染，切换四种风格排版随之变化。
2. 打印/导出 PDF 时工具栏不出现。
3. `npm run build` 通过。

## 改动文件
- `blog-frontend/src/views/Resume.vue`
