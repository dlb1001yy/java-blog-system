# Checklist

- [x] 上传接口 `/admin/music/songs/upload` 接收并保存 lyric 参数
- [x] 解析歌词接口 `/admin/music/songs/parse-lyric` 可返回 ID3 内嵌歌词，含时间轴的 LRC 原样保留
- [x] 无内嵌歌词时返回空并提示，表单可手工编辑
- [x] 上传未提供封面时自动生成 500×500 渐变封面并写入 cover，响应返回 cover
- [x] 手动上传封面时使用上传的封面
- [x] MusicManage.vue 上传/编辑对话框有封面上传与预览控件
- [x] 上传对话框有「解析歌词」按钮，成功回填 lyric 字段
- [x] 门户端 Music.vue / PlayerBar.vue 封面空值显示渐变占位不裂图
- [x] 代码自查通过（mvn/npm 构建由用户手动执行）
