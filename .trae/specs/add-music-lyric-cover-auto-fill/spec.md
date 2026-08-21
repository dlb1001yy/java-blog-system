# 上传歌曲自动解析歌词与生成封面 Spec

## Why
管理端上传歌曲时：后端上传接口不接收 lyric（前端传了但丢失），上传/编辑表单均无歌曲封面 cover 字段，导致门户端音乐页和底部播放器封面无法展示。用户希望上传时自动从音频文件解析歌词（LRC 格式）回填歌词字段，并自动生成歌曲封面图回填封面字段。

## What Changes
- 后端 `POST /admin/music/songs/upload` 接口接收 `lyric` 参数并保存（修复丢失 bug）
- 新增后端歌词解析：解析 MP3 ID3v2 内嵌歌词（USLT 帧），原样保留（若源数据本身带 LRC 时间轴则保留时间轴，无时间轴则为纯文本歌词）
- 新增后端封面生成：上传时若未提供 cover，从歌名/歌词关键词生成本地渐变封面（复用/扩展 CoverImageGenerator），经 fileStorageService.saveBytes 存储返回 URL 写入 cover 字段
- 管理端 MusicManage.vue 上传/编辑表单新增「封面」字段（可上传图片，也可留空自动生成）；上传对话框：选好音频文件后可点「解析歌词」按钮，后端解析返回歌词并回填表单 lyric 字段；封面预览
- 门户端 Music.vue / PlayerBar.vue 封面空值兜底（cover 为空显示渐变占位，不显示裂图）

## Impact
- Affected specs: 无
- Affected code:
  - blog-backend: AdminMusicController.java、MusicSongService(+Impl)、MusicSongServiceImpl.uploadAndSave、新增 Mp3LyricParser 工具类、CoverImageGenerator.java（新增正方形 500×500 方法）
  - blog-admin: src/views/MusicManage.vue、src/api/music.js
  - blog-frontend: src/views/Music.vue、src/components/PlayerBar.vue

## ADDED Requirements

### Requirement: 上传歌曲自动解析歌词
系统 SHALL 在管理端上传歌曲时，支持从 MP3 文件解析 ID3v2 USLT 内嵌歌词，以 LRC 兼容文本回填表单歌词字段。

#### Scenario: 音频含内嵌歌词
- **WHEN** 管理员选择 MP3 文件并点击「解析歌词」（或直接上传）
- **THEN** 后端解析 ID3v2 USLT/歌词帧，返回歌词文本；若含 `[mm:ss.xx]` 时间轴则原样保留，前端回填 lyric 字段

#### Scenario: 音频无内嵌歌词
- **WHEN** MP3 无 USLT 帧
- **THEN** 接口返回空歌词并提示「未解析到内嵌歌词」，表单 lyric 保持可手工编辑

### Requirement: 上传歌曲自动生成封面
系统 SHALL 在歌曲未提供封面时，基于歌名（及可选歌词关键词）本地生成 500×500 渐变封面图并保存到文件存储，URL 写入 cover 字段。

#### Scenario: 未提供封面
- **WHEN** 上传/保存歌曲且 cover 为空
- **THEN** 后端生成渐变封面（歌名+歌手文字），存储后 URL 写入 cover；上传接口响应中返回 cover 供前端展示

#### Scenario: 手动上传封面
- **WHEN** 管理员在表单中通过上传组件提供了封面
- **THEN** 使用上传的封面，不生成

### Requirement: 管理端歌曲表单封面字段
MusicManage.vue 上传与编辑对话框 SHALL 提供封面上传/预览控件（走 /v1/storage/upload），字段绑定 song.cover。

### Requirement: 门户端封面兜底
门户端歌曲/歌单封面为空时 SHALL 显示 CSS 渐变占位而非裂图。

## MODIFIED Requirements

### Requirement: 上传接口保存歌词
`POST /admin/music/songs/upload` SHALL 接收并保存 lyric 参数（此前前端传参后端忽略导致丢失）。

## REMOVED Requirements
无
