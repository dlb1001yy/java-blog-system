# Tasks

- [x] Task 1: 后端修复上传接口接收 lyric 参数
  - [x] AdminMusicController `/admin/music/songs/upload` 新增 `lyric` 请求参数
  - [x] MusicSongService.uploadAndSave 签名与 Impl 增加 lyric，保存到实体
- [x] Task 2: 后端新增 Mp3LyricParser 工具类
  - [x] 解析 ID3v2 头与 USLT（含 v2.3/v2.4）歌词帧，返回文本（含时间轴则保留）
  - [x] 新增接口 `POST /admin/music/songs/parse-lyric`（multipart file），返回解析歌词或空+提示
- [x] Task 3: 后端封面生成集成
  - [x] CoverImageGenerator 新增 500×500 正方形方法（渐变背景+歌名/歌手文字，从歌词提取若干关键词可选）
  - [x] uploadAndSave：cover 为空时生成封面，saveBytes 存储 URL 写入 cover；上传响应返回歌曲完整信息（含 cover）
- [x] Task 4: blog-admin 前端表单改造
  - [x] api/music.js 新增 parseLyric(file)
  - [x] MusicManage.vue 上传对话框：新增「解析歌词」按钮（选文件后可用，成功回填表单 lyric）；新增封面上传/预览组件（复用 Upload.vue，调 /v1/storage/upload）
  - [x] 编辑对话框新增封面字段
- [x] Task 5: blog-frontend 封面兜底
  - [x] Music.vue：currentSong.cover / row.cover / pl.cover 为空时显示渐变占位 div
  - [x] PlayerBar.vue cover 为空时显示占位
- [x] Task 6: 验证（构建由用户手动执行，不代跑 mvn/npm）
  - [x] 代码自查通过（无明显编译/语法问题）
  - [ ] 手动流程：上传含/不含内嵌歌词的 MP3 验证解析回填、封面自动生成、门户端展示

# Task Dependencies
- Task 4 依赖 Task 1/2/3（接口就绪）
- Task 5 独立可并行
- Task 6 依赖全部
