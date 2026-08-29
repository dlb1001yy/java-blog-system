<template>
  <!-- 外层 view 作为页面根节点，主题类挂此处向内级联 CSS 变量 -->
  <view :class="['page-root', isDark ? 'theme-dark' : '']">
    <!-- ===== 正在播放大卡：固定顶部，不随下方列表滚动 ===== -->
    <view class="now-playing">
      <template v-if="song">
        <view class="np-top">
          <image
            v-if="song.cover"
            class="np-cover"
            :src="npCover"
            mode="aspectFill"
          />
          <view v-else class="np-cover np-cover-placeholder">
            <Icon name="music" :size="32" color="#FFFFFF" />
          </view>
          <view class="np-info">
            <text class="np-label">正在播放</text>
            <text class="np-title">{{ song.title }}</text>
            <text class="np-artist">{{ song.artist }}{{ song.album ? ' · ' + song.album : '' }}</text>
          </view>
        </view>

        <!-- 进度：mm:ss + slider（拖动结束才 seek） -->
        <view class="np-progress">
          <text class="np-time">{{ formatTime(sliderValue) }}</text>
          <slider
            class="np-slider"
            :value="sliderValue"
            :min="0"
            :max="sliderMax"
            :step="1"
            :activeColor="sliderActiveColor"
            :backgroundColor="sliderBgColor"
            :block-size="14"
            @changing="onChanging"
            @change="onSeek"
          />
          <text class="np-time">{{ formatTime(state.duration) }}</text>
        </view>

        <!-- 控制行：上一首 / 播放暂停 / 下一首 / 循环模式 / 随机 -->
        <view class="np-controls">
          <view class="np-btn" @click="onPrev">
            <Icon name="prev" :size="20" />
          </view>
          <view class="np-play" @click="onToggle">
            <Icon :name="state.isPlaying ? 'pause' : 'play'" :size="24" color="#FFFFFF" />
          </view>
          <view class="np-btn" @click="onNext">
            <Icon name="next" :size="20" />
          </view>
          <view
            :class="['np-btn', 'np-mode', state.repeat !== 'none' ? 'active' : '']"
            @click="onToggleRepeat"
          >
            <view v-if="state.repeat === 'one'" class="np-badge">1</view>
            <Icon :name="state.repeat === 'one' ? 'repeat-one' : 'repeat'" :size="18" />
          </view>
          <view
            :class="['np-btn', 'np-mode', state.shuffle ? 'active' : '']"
            @click="onToggleShuffle"
          >
            <Icon name="shuffle" :size="18" />
          </view>
        </view>
      </template>

      <!-- 空态：无当前曲目 -->
      <view v-else class="np-empty">
        <view class="np-cover np-cover-placeholder">
          <Icon name="music" :size="32" color="#FFFFFF" />
        </view>
        <text class="np-empty-text">点一首歌开始 listening</text>
      </view>
    </view>

    <!-- ===== 下方滚动区：歌曲列表 + 推荐歌单 ===== -->
    <scroll-view class="scroll-area" scroll-y @scrolltolower="onLoadMore">
      <!-- 歌曲列表区 -->
      <view class="section">
        <view class="section-head">
          <text class="section-title">全部歌曲</text>
          <text class="section-hint">分页加载更多</text>
        </view>

        <!-- 首屏骨架 -->
        <Skeleton v-if="songsLoading && songs.length === 0" type="article" :count="3" />
        <template v-else>
          <!-- 空状态 -->
          <view v-if="songs.length === 0" class="empty">
            <view class="empty-icon"><Icon name="music" :size="40" /></view>
            <text class="empty-text">暂无歌曲</text>
          </view>

          <view
            v-for="(item, index) in songs"
            :key="item.id"
            :class="['song-row', isCurrentSong(item) ? 'current' : '']"
            @click="onRowClick(item)"
          >
            <!-- 序号：当前曲显示均衡器动画（播放中才动） -->
            <view class="song-index">
              <view v-if="isCurrentSong(item)" :class="['row-eq', state.isPlaying ? '' : 'paused']">
                <view class="bar"></view>
                <view class="bar"></view>
                <view class="bar"></view>
              </view>
              <text v-else class="index-num">{{ index + 1 }}</text>
            </view>

            <view class="song-info">
              <text :class="['song-title', isCurrentSong(item) ? 'active' : '']">{{ item.title }}</text>
              <text class="song-meta">{{ item.artist }}{{ item.album ? ' · ' + item.album : '' }}</text>
            </view>

            <text class="song-duration">{{ formatTime(item.duration) }}</text>
            <view class="song-play" @click.stop="onRowClick(item)">
              <Icon :name="isCurrentSong(item) && state.isPlaying ? 'pause' : 'play'" :size="16" />
            </view>
          </view>

          <!-- 加载更多（三点跳动）/ 没有更多 -->
          <view v-if="songs.length > 0 && songsLoading" class="status">
            <LoadingDots :size="6" />
          </view>
          <view v-if="songs.length > 0 && !songsLoading && !hasMore" class="status">没有更多了</view>
        </template>
      </view>

      <!-- 推荐歌单区（加载中或有数据时显示，无数据不占位） -->
      <view v-if="playlistsLoading || playlists.length > 0" class="section">
        <view class="section-head">
          <text class="section-title">推荐歌单</text>
        </view>

        <Skeleton v-if="playlistsLoading" type="article" :count="2" />
        <view v-else class="playlists">
          <view
            v-for="pl in playlists"
            :key="pl.id"
            class="playlist-item"
            @click="onPlaylistClick(pl)"
          >
            <view class="playlist-cover-wrap">
              <image
                v-if="pl.cover"
                class="playlist-cover"
                :src="playlistCover(pl)"
                mode="aspectFill"
                lazy-load
              />
              <view v-else class="playlist-cover playlist-cover-placeholder">
                <Icon name="music" :size="24" color="#FFFFFF" />
              </view>
              <text class="playlist-count">{{ pl.songCount || 0 }} 首</text>
            </view>
            <text class="playlist-name">{{ pl.name }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部 TabBar：本页自带大播放卡，不重复挂 PlayerBar -->
    <TabBar current="/subpkg-music/pages/index" />
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { colors, darkColors, isDark, applyNavBarTheme } from '@/common/theme.js'
import { state, currentSong, setPlaylist, toggle, next, prev, seek, toggleRepeat, toggleShuffle } from '@/common/player.js'
import { resolveFileUrl } from '@/common/config.js'
import { optimizeImageUrl } from '@/common/imageUrl.js'
import Icon from '@/components/Icon.vue'
import Skeleton from '@/components/Skeleton.vue'
import LoadingDots from '@/components/LoadingDots.vue'
import TabBar from '@/components/TabBar.vue'

const song = currentSong

// ===== 歌曲列表分页状态 =====
const songs = ref([])
const page = ref(1)
const pageSize = 20
const songsLoading = ref(false)
const hasMore = ref(true)

// ===== 推荐歌单状态 =====
const playlists = ref([])
const playlistsLoading = ref(true)

// ===== 进度 slider：拖动中脱离播放器实际进度，结束才 seek =====
const draggingValue = ref(-1)
// slider 值：拖动中用拖动值，否则跟随播放器当前进度
const sliderValue = computed(() => {
  const d = state.duration
  if (draggingValue.value >= 0) return draggingValue.value
  if (!d || d <= 0) return 0
  return Math.min(Math.floor(state.currentTime), Math.floor(d))
})
// max 至少为 1，避免 duration 为 0 时拖不动
const sliderMax = computed(() => Math.max(Math.floor(state.duration) || 1, 1))

// slider 配色需跟随主题（属性不支持 CSS 变量，用内联值注入）
const sliderActiveColor = computed(() => (isDark.value ? darkColors.primary : colors.primary))
const sliderBgColor = computed(() => (isDark.value ? darkColors.border : colors.border))

// mm:ss 时间格式化
const formatTime = (s) => {
  if (!s || !isFinite(s)) return '00:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
}

// 正在播放卡封面：相对路径拼 origin + 命中 CDN 时压缩到 240px
const npCover = computed(() => {
  if (!song.value || !song.value.cover) return ''
  return optimizeImageUrl(resolveFileUrl(song.value.cover), 240)
})

// 歌单封面：压缩到 200px
const playlistCover = (pl) => optimizeImageUrl(resolveFileUrl(pl && pl.cover), 200)

// ===== 拉取歌曲分页列表 =====
const fetchSongs = async () => {
  if (!hasMore.value || songsLoading.value) return
  songsLoading.value = true
  try {
    const res = await api.getSongs({ page: page.value, size: pageSize })
    const d = res.data || {}
    const records = d.records || d.list || []
    if (page.value === 1) {
      songs.value = records
    } else {
      songs.value = songs.value.concat(records)
    }
    // 不足一页或已达 total 时判定无更多
    if (records.length < pageSize) {
      hasMore.value = false
    } else if (d.total) {
      hasMore.value = songs.value.length < d.total
    } else {
      hasMore.value = true
    }
  } catch (e) {
    // 失败已由拦截器 toast
  } finally {
    songsLoading.value = false
  }
}

// ===== 拉取推荐歌单 =====
const fetchPlaylists = async () => {
  playlistsLoading.value = true
  try {
    const res = await api.getPlaylists()
    playlists.value = (res.data && res.data.records) || []
  } catch (e) {
    playlists.value = []
  } finally {
    playlistsLoading.value = false
  }
}

// 当前曲判定
const isCurrentSong = (item) => !!song.value && !!item && song.value.id === item.id

// 行点击：非当前曲 setPlaylist(当前列表, index) 播放，当前曲 toggle
const onRowClick = (item) => {
  if (!item) return
  if (isCurrentSong(item)) {
    toggle()
    return
  }
  setPlaylist(songs.value, songs.value.indexOf(item))
}

// 歌单点击：取歌单曲目并从第一首开始播放
const onPlaylistClick = async (pl) => {
  if (!pl) return
  try {
    const res = await api.getPlaylist(pl.id)
    const d = res.data || {}
    const list = d.songList || d.songs || []
    if (list.length === 0) {
      uni.showToast({ title: '歌单暂无歌曲', icon: 'none' })
      return
    }
    setPlaylist(list, 0)
    uni.showToast({ title: `已切换歌单：${pl.name || '推荐歌单'}`, icon: 'none' })
  } catch (e) {
    // 失败已由拦截器 toast
  }
}

// ===== 播放控制（薄封装，保持模板语义清晰） =====
const onToggle = () => toggle()
const onNext = () => next()
const onPrev = () => prev()
const onToggleRepeat = () => toggleRepeat()
const onToggleShuffle = () => toggleShuffle()

// ===== 进度条 =====
// 拖动中：记录拖动值让时间标签即时跟随
const onChanging = (e) => {
  draggingValue.value = e.detail.value
}
// 拖动结束：seek 到目标秒并恢复跟随
const onSeek = (e) => {
  seek(e.detail.value)
  draggingValue.value = -1
}

// scroll-view 触底加载更多
const onLoadMore = () => {
  if (songsLoading.value || !hasMore.value) return
  page.value++
  fetchSongs()
}

// 页面显示时同步原生导航栏配色；主题切换时实时刷新
onShow(() => applyNavBarTheme())
watch(isDark, () => applyNavBarTheme())

// 页面加载：并行拉取歌曲与歌单
onLoad(() => {
  fetchSongs()
  fetchPlaylists()
})
</script>

<style lang="scss" scoped>
/* 页面根节点：flex 列布局，播放卡固定、下方 scroll-view 滚动 */
.page-root {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--app-bg, #F1F5F9);
}

/* ===== 正在播放大卡：白底卡片 + 主色轻渐变 ===== */
.now-playing {
  flex-shrink: 0;
  margin: $spacing-md $spacing-lg 0;
  padding: $spacing-lg;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: 14px;
  box-shadow: $shadow-card;
}

.np-top {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.np-cover {
  flex-shrink: 0;
  width: 88px;
  height: 88px;
  border-radius: $radius-lg;
  background: var(--app-bg, #F1F5F9);
}

/* 无封面：主色渐变底 + music 图标 */
.np-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $color-primary, $color-accent);
}

.np-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.np-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1px;
  color: var(--app-primary, $color-primary);
}

.np-title {
  font-size: 17px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--app-text, #0F172A);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.np-artist {
  font-size: 12px;
  line-height: 1.3;
  color: var(--app-text-secondary, #64748B);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 进度行 ===== */
.np-progress {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-top: $spacing-md;
}

.np-time {
  flex-shrink: 0;
  width: 38px;
  font-size: 11px;
  color: var(--app-text-tertiary, #94A3B8);
  text-align: center;
}

/* 收窄 uni slider 两侧默认 margin，贴合时间标签 */
.np-slider {
  flex: 1;
  margin: 0;
}

/* ===== 控制行 ===== */
.np-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-lg;
  margin-top: $spacing-sm;
}

.np-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  color: var(--app-text-secondary, #64748B);
  transition: opacity 0.15s ease;

  &:active {
    opacity: 0.6;
  }
}

/* 循环 / 随机激活态：主色 */
.np-mode.active {
  color: var(--app-primary, $color-primary);
}

/* 单曲循环角标 "1" */
.np-badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 12px;
  height: 12px;
  line-height: 12px;
  border-radius: 6px;
  font-size: 9px;
  font-weight: 700;
  text-align: center;
  color: #FFFFFF;
  background: var(--app-primary, $color-primary);
}

/* 大播放/暂停圆钮：56px 主色渐变白字 */
.np-play {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  color: #FFFFFF;
  background: linear-gradient(135deg, $color-primary, $color-primary-light);
  box-shadow: 0 4px 12px rgba($color-primary, 0.35);
  transition: transform 0.15s ease;

  &:active {
    transform: scale(0.92);
  }
}

/* ===== 空态 ===== */
.np-empty {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.np-empty-text {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* ===== 下方滚动区 ===== */
.scroll-area {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  /* 底部留白避开 TabBar（56px + 安全区），本页不挂 PlayerBar */
  padding-bottom: calc(64px + env(safe-area-inset-bottom));
}

.section {
  margin-top: $spacing-lg;
  padding: 0 $spacing-lg;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-sm;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--app-text, #0F172A);
}

.section-hint {
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* ===== 歌曲行 ===== */
.song-row {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: 10px;
  box-shadow: $shadow-card;
  transition: transform 0.15s ease;

  /* 按压轻缩放反馈 */
  &:active {
    transform: scale(0.98);
  }

  /* 当前曲行：浅主色底高亮 */
  &.current {
    background: rgba($color-primary, 0.08);
  }
}

.song-index {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.index-num {
  font-size: 13px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* 跳动均衡器：三竖条，暂停时停住 */
.row-eq {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 14px;

  .bar {
    width: 3px;
    border-radius: 1px;
    background: var(--app-primary, $color-primary);
    animation: eq-bounce 0.8s ease-in-out infinite alternate;
  }

  .bar:nth-child(1) { animation-delay: 0s; }
  .bar:nth-child(2) { animation-delay: 0.2s; }
  .bar:nth-child(3) { animation-delay: 0.4s; }

  &.paused .bar {
    animation-play-state: paused;
  }
}

@keyframes eq-bounce {
  from { height: 3px; }
  to { height: 14px; }
}

.song-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.song-title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.3;
  color: var(--app-text, #0F172A);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  /* 当前曲：主色加粗 */
  &.active {
    font-weight: 700;
    color: var(--app-primary, $color-primary);
  }
}

.song-meta {
  font-size: 12px;
  line-height: 1.3;
  color: var(--app-text-secondary, #64748B);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-duration {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* 行尾播放按钮 */
.song-play {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  color: var(--app-primary, $color-primary);
  background: rgba($color-primary, 0.1);
  transition: opacity 0.15s ease;

  &:active {
    opacity: 0.6;
  }
}

/* ===== 推荐歌单：2 列网格 ===== */
.playlists {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
}

.playlist-item {
  /* 2 列：减去列间距后均分 */
  width: calc((100% - #{$spacing-md}) / 2);
  transition: transform 0.15s ease;

  &:active {
    transform: scale(0.97);
  }
}

.playlist-cover-wrap {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 100%; /* 1:1 正方形 */
  border-radius: $radius-lg;
  overflow: hidden;
  background: var(--app-bg, #F1F5F9);
  box-shadow: $shadow-card;
}

.playlist-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.playlist-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $color-primary, $color-accent);
}

/* 右下角歌曲数角标：黑半透明圆角胶囊 */
.playlist-count {
  position: absolute;
  right: 6px;
  bottom: 6px;
  padding: 1px 8px;
  border-radius: $radius-full;
  font-size: 11px;
  color: #FFFFFF;
  background: rgba(0, 0, 0, 0.55);
}

.playlist-name {
  display: block;
  margin-top: $spacing-sm;
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 空态 / 加载态 ===== */
.empty {
  padding: 48px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: var(--app-text-tertiary, #94A3B8);
}

.empty-icon {
  margin-bottom: $spacing-sm;
  opacity: 0.5;
}

.empty-text {
  font-size: 14px;
  color: var(--app-text-tertiary, #94A3B8);
}

.status {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-md;
  color: var(--app-text-tertiary, #94A3B8);
  font-size: 12px;
}
</style>
