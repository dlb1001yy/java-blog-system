# 上传歌曲歌词自动补齐（联网兜底）计划

## Summary
MP3 无内嵌歌词时（parse-lyric 返回 null），自动联网按「歌名+歌手」从网易云音乐公开接口搜索并下载 LRC 歌词，使上传歌曲全流程零手工：选文件 → 自动回填歌名 → 点解析歌词自动补齐（内嵌优先、联网兜底）→ 确认上传自动生成封面。封面已实现自动生成，本次不动。

## Current State Analysis
- `parse-lyric` 返回 null 是因为该 MP3 的 ID3v2 无 USLT 内嵌歌词（`Mp3LyricParser.parse` 返回 null），本地无法凭空生成歌词
- 封面自动生成已实现（MusicSongServiceImpl.uploadAndSave L70-76，cover 为空时 generateSquare），用户被「未解析到内嵌歌词」警告误导以为封面也要手工
- 后端无任何 HTTP 客户端依赖调用外部接口（项目有 RestTemplate 可用，Spring Web 自带）
- 前端 MusicManage.vue：handleParseLyric L448-462（data 为空提示手工填写）；handleUpload L473-483 传 lyric/cover

## Proposed Changes

### 1. 后端新增 `OnlineLyricService`（blog-backend/src/main/java/com/dlbyy/blog/service/OnlineLyricService.java + impl）
- 用 Spring 自带 `RestTemplate`（新建 Bean 或 new，避免引入新依赖）调用网易云公开接口：
  1. `GET https://music.163.com/api/search/get/web?s={keyword}&type=1&limit=5` 搜索歌曲（keyword = "title artist"），取第一个歌手匹配的 songId
  2. `GET https://music.163.com/api/song/lyric?id={songId}&lv=1&kv=1&tv=-1` 获取 `lrc.lyric`（带时间轴的 LRC）
- 请求带 UA（网易云接口要求，否则可能 403），超时 5s，任何异常吞掉返回 null（不影响主流程）
- 输出统一为 LRC 文本；无匹配返回 null

### 2. 后端 `parse-lyric` 接口增强（AdminMusicController）
- 签名改为 `parseLyric(file, title, artist)`（title/artist 可选RequestParam）
- 逻辑：先 `Mp3LyricParser.parse(file)`；为 null 且 title 不为空时调 `onlineLyricService.fetchLrc(title, artist)`
- 返回 Result<String>，同时可通过 message 区分来源（"内嵌歌词"/"在线匹配"/"未找到"），data 仍为歌词文本或 null

### 3. 后端 `uploadAndSave` 兜底（MusicSongServiceImpl）
- 上传时若 lyric 为空：先尝试内嵌解析（复用 Mp3LyricParser.parse(file)），再调 onlineLyricService.fetchLrc(title, artist)，取到则保存——保证直接点「确认上传」不经解析按钮也能零手工

### 4. 前端 MusicManage.vue
- `handleParseLyric`：调用 `musicApi.parseLyric(file, title, artist)`；res.data 有值回填并提示来源（读 message）；为 null 提示「未解析到内嵌歌词，已尝试在线匹配失败，可手工填写」
- `api/music.js parseLyric` 增加可选 title/artist 参数
- 封面字段提示文案改为「留空将在上传时根据歌名自动生成封面」（已有，无需改）

## Assumptions & Decisions
- 网易云接口为非官方公开接口，可能限流/变更；失败仅降级为手工填写，不阻塞上传
- **封面生成永不依赖歌词**：无论在线歌词是否获取到，封面始终用「歌曲名 + 歌手」生成 500×500 渐变封面（现状逻辑 MusicSongServiceImpl L70-76 已如此，保持不变）
- 在线歌词获取不到时：lyric 留空可手工填写，上传照常成功，封面照常自动生成
- 服务器需能访问 music.163.com（若 docker 内无法出网则永远走手工，属环境问题）
- 不做歌词缓存/重试，保持最简

## Verification（构建由用户手动执行）
1. 上传含内嵌歌词 MP3 → 解析回填内嵌歌词
2. 上传无内嵌歌词但网易云能搜到的歌 → 解析/上传自动回填在线 LRC
3. 都没有 → 提示手工，上传仍成功且封面自动生成
4. 后端 mvn compile、前端 npm run build 由用户手动执行
