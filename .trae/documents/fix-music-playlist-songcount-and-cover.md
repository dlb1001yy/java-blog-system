# 计划：删除歌曲时同步清理歌单关联 + 新建歌单自动生成封面

## Summary
两个问题：
1. 删除歌曲后，歌单的"歌曲数"（songCount）不减少——原因：删除歌曲时没有删除 `music_playlist_song` 关联表记录，而 songCount 是按关联表实时 count 的（`fillSongCount`），残留的关联记录仍被计入。
2. 新建歌单时，封面需要手动填 URL——改为留空时根据歌单名自动生成渐变封面图（复用已有的 `CoverImageGenerator`）。

## Current State Analysis
- 删除歌曲：`MusicSongServiceImpl.adminDelete` 只 `removeById(id)` + 删除存储文件，未清理 `music_playlist_song` 关联。
- 歌单歌曲数：`MusicPlaylistServiceImpl.fillSongCount` 按关联表 count，残留关联导致数量不减。
- 歌单封面：`MusicPlaylistServiceImpl.adminSave` 直接保存实体；新建歌单前端弹窗中封面是文本输入框（`MusicManage.vue` 第 301-303 行）。
- 已有封面生成器：`CoverImageGenerator.generateSquare(title, artist, size)` 生成正方形渐变封面（歌曲在用）；`generate(title)` 生成 1200×630 横版。
- `MusicSongServiceImpl` 中已有依赖注入 `MusicPlaylistSongMapper` 的先例可参考（`MusicPlaylistServiceImpl` 用法）。

## Proposed Changes

### 1. 后端：删除歌曲时清理歌单关联
文件：`blog-backend/src/main/java/com/dlbyy/blog/service/impl/MusicSongServiceImpl.java`

- 注入 `MusicPlaylistSongMapper`（构造器注入，`@RequiredArgsConstructor` 加 final 字段）。
- `adminDelete` 中，删除歌曲记录后执行：
  ```java
  musicPlaylistSongMapper.delete(new LambdaQueryWrapper<MusicPlaylistSong>()
          .eq(MusicPlaylistSong::getSongId, id));
  ```
- 位置放在 `removeById(id)` 之后、删除存储文件之前。
- 新增 import：`LambdaQueryWrapper`、`MusicPlaylistSong`、`MusicPlaylistSongMapper`。

### 2. 后端：新建歌单封面自动生成
文件：`blog-backend/src/main/java/com/dlbyy/blog/service/impl/MusicPlaylistServiceImpl.java`

- 注入 `CoverImageGenerator`。
- `adminSave` 中，仅当 `playlist.getId() == null`（新建）且 `cover` 为空/blank 时：
  ```java
  try {
      playlist.setCover(coverImageGenerator.generateSquare(playlist.getName(), null, 500));
  } catch (Exception e) {
      // 生成失败不阻断保存，仅记日志
  }
  ```
  加 `@Slf4j`（当前类没有，需补）。

### 3. 前端：新建歌单弹窗封面改为上传组件
文件：`blog-admin/src/views/MusicManage.vue`

- 歌单编辑对话框中"封面URL"文本输入框（第 301-303 行）改为 `<Upload v-model="playlistForm.cover" placeholder="上传封面" />`（组件已在本文件导入并在用）。
- 下方加提示文字："留空保存时将根据歌单名自动生成封面"。
- `handlePlaylistSave` 中 `cover: playlistForm.cover.trim()` 保持不变（空字符串后端判 blank 走自动生成）。

### 不改动
- 编辑已有歌单时不自动生成封面（只补空值也可以：按方案 2 仅新建时生成；编辑时用户已有封面或明确留空则不覆盖）。
- 存量脏数据（历史残留关联）不在本次修复范围；如需清理可后续手动 SQL。

## Assumptions & Decisions
- songCount 由关联表实时统计，无需冗余字段维护。
- 封面生成失败静默降级（歌单无封面），不阻断保存。
- 封面尺寸 500×500 正方形，与歌曲封面一致。

## Verification
1. 后端编译通过（用户此前要求不跑 mvn compile，此处跳过，按用户习惯）。
2. 功能验证：
   - 歌单 A 含歌曲 X → 删除 X → 歌单列表中 A 的歌曲数减 1。
   - 新建歌单不上传封面 → 列表中歌单显示自动生成的渐变封面。
