<template>
  <transition name="player-slide">
    <div v-if="player.currentSong" class="player-bar">
      <div class="player-inner">
        <!-- 歌词弹出面板 -->
        <transition name="lyric-fade">
          <div v-if="showLyric" ref="lyricPanelRef" class="lyric-panel">
            <div class="lyric-header">
              <span class="lyric-title">歌词</span>
              <span class="lyric-song">{{ player.currentSong.title }} - {{ player.currentSong.artist }}</span>
              <el-button circle text size="small" class="lyric-close" @click="showLyric = false">
                <el-icon :size="14"><Close /></el-icon>
              </el-button>
            </div>
            <div v-if="lyricLines.length" ref="lyricScrollRef" class="lyric-scroll">
              <div
                v-for="(line, i) in lyricLines"
                :key="i"
                :ref="el => setLineRef(el, i)"
                class="lyric-line"
                :class="{ active: i === activeLyricIndex }"
                @click="player.seek(line.time)"
              >{{ line.text }}</div>
            </div>
            <div v-else class="lyric-empty">暂无歌词</div>
          </div>
        </transition>

        <!-- 左：封面 + 信息 -->
        <div ref="songInfoRef" class="song-info" @click="showLyric = !showLyric">
          <el-tooltip content="歌词" placement="top">
            <img v-if="player.currentSong.cover" :src="player.currentSong.cover" alt="cover" class="cover" @error="onCoverError" />
          </el-tooltip>
          <div v-if="!player.currentSong.cover" class="cover cover-placeholder">
            <el-icon :size="20"><Headset /></el-icon>
          </div>
          <div class="meta">
            <div class="title">{{ player.currentSong.title }}</div>
            <div class="artist">{{ player.currentSong.artist }}</div>
          </div>
        </div>

        <!-- 中：控制 + 进度条 -->
        <div class="player-center">
          <div class="controls">
            <el-button :icon="ArrowLeft" circle text class="ctrl-btn" @click="player.prev()" />
            <el-button circle type="primary" class="play-btn" @click="player.toggle()">
              <el-icon :size="20">
                <component :is="player.isPlaying ? 'VideoPause' : 'VideoPlay'" />
              </el-icon>
            </el-button>
            <el-button :icon="ArrowRight" circle text class="ctrl-btn" @click="player.next()" />
          </div>
          <div class="progress">
            <span class="time">{{ formatTime(player.currentTime) }}</span>
            <div
              ref="progressRef"
              class="progress-track"
              @mousedown="onProgressMouseDown"
            >
              <div class="progress-filled" :style="{ width: progressPercent + '%' }"></div>
            </div>
            <span class="time">{{ formatTime(player.duration) }}</span>
          </div>
        </div>

        <!-- 右：循环/随机/音量 -->
        <div class="player-right">
          <el-tooltip :content="repeatLabel" placement="top">
            <el-button circle text class="ctrl-btn" :class="{ active: player.repeat !== 'none' }" @click="player.toggleRepeat()">
              <el-icon :size="16">
                <component :is="player.repeat === 'one' ? 'RefreshRight' : 'Refresh'" />
              </el-icon>
              <span v-if="player.repeat === 'one'" class="one-badge">1</span>
            </el-button>
          </el-tooltip>
          <el-tooltip content="随机播放" placement="top">
            <el-button circle text class="ctrl-btn" :class="{ active: player.shuffle }" @click="player.toggleShuffle()">
              <el-icon :size="16"><Sort /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip :content="player.volume === 0 ? '取消静音' : '静音'" placement="top">
            <el-button circle text class="ctrl-btn" @click="toggleMute">
              <el-icon :size="16">
                <component :is="player.volume === 0 ? 'Mute' : 'Headset'" />
              </el-icon>
            </el-button>
          </el-tooltip>
          <input
            type="range"
            min="0"
            max="1"
            step="0.01"
            :value="player.volume"
            class="volume-slider"
            @input="onVolumeInput"
          />
          <!-- 播放列表 -->
          <el-popover placement="top-end" :width="320" trigger="click" popper-class="playlist-popover">
            <template #reference>
              <el-tooltip content="播放列表" placement="top">
                <el-button circle text class="ctrl-btn">
                  <el-icon :size="16"><List /></el-icon>
                </el-button>
              </el-tooltip>
            </template>
            <div class="playlist">
              <div class="playlist-scroll">
                <div
                  v-for="(song, index) in player.playlist"
                  :key="song.id || index"
                  class="playlist-item"
                  :class="{ current: index === player.currentIndex }"
                  @click="player.playAt(index)"
                >
                  <span class="pl-index">
                    <el-icon v-if="index === player.currentIndex" :size="12"><Headset /></el-icon>
                    <template v-else>{{ index + 1 }}</template>
                  </span>
                  <span class="pl-title">{{ song.title }}</span>
                  <span class="pl-artist">{{ song.artist }}</span>
                </div>
              </div>
              <div class="playlist-footer">共 {{ player.playlist.length }} 首</div>
            </div>
          </el-popover>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { ArrowLeft, ArrowRight, List, Headset, Close } from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores/player'

const player = usePlayerStore()
const progressRef = ref(null)
const dragging = ref(false)
const lastVolume = ref(0.8)

/* ---- 歌词弹出面板 ---- */
const showLyric = ref(false)
const lyricPanelRef = ref(null)
const songInfoRef = ref(null)
const lyricScrollRef = ref(null)
const lineEls = []

const parseLrc = (lrc) => {
  if (!lrc) return []
  const lines = []
  for (const raw of String(lrc).split(/\r?\n/)) {
    const text = raw.replace(/\[[^\]]*\]/g, '').trim()
    if (!text) continue
    const tags = raw.match(/\[(\d+):(\d+(?:\.\d+)?)\]/g) || []
    for (const tag of tags) {
      const m = tag.match(/\[(\d+):(\d+(?:\.\d+)?)\]/)
      const time = parseInt(m[1], 10) * 60 + parseFloat(m[2])
      lines.push({ time, text })
    }
  }
  return lines.sort((a, b) => a.time - b.time)
}

const lyricLines = computed(() => parseLrc(player.currentSong?.lyric))

const activeLyricIndex = computed(() => {
  const t = player.currentTime
  let idx = -1
  for (let i = 0; i < lyricLines.value.length; i++) {
    if (lyricLines.value[i].time <= t + 0.3) idx = i
    else break
  }
  return idx
})

const setLineRef = (el, i) => {
  if (el) lineEls[i] = el
}

watch(activeLyricIndex, (idx) => {
  if (idx < 0) return
  const el = lineEls[idx]
  const box = lyricScrollRef.value
  if (el && box) {
    box.scrollTo({
      top: el.offsetTop - box.clientHeight / 2 + el.offsetHeight / 2,
      behavior: 'smooth'
    })
  }
})

watch(() => player.currentSong?.id, () => {
  lineEls.length = 0
  if (lyricScrollRef.value) lyricScrollRef.value.scrollTop = 0
})

const onDocClick = (e) => {
  if (!showLyric.value) return
  const target = e.target
  if (
    (lyricPanelRef.value && lyricPanelRef.value.contains(target)) ||
    (songInfoRef.value && songInfoRef.value.contains(target))
  ) return
  showLyric.value = false
}

const onDocKeydown = (e) => {
  if (e.key === 'Escape') showLyric.value = false
}

onMounted(() => {
  document.addEventListener('click', onDocClick)
  document.addEventListener('keydown', onDocKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('keydown', onDocKeydown)
})

const onVolumeInput = (e) => {
  const v = Number(e.target.value)
  if (v > 0) lastVolume.value = v
  player.setVolume(v)
}

const toggleMute = () => {
  if (player.volume === 0) {
    player.setVolume(lastVolume.value || 0.8)
  } else {
    lastVolume.value = player.volume
    player.setVolume(0)
  }
}

const progressPercent = computed(() => {
  if (!player.duration) return 0
  return Math.min((player.currentTime / player.duration) * 100, 100)
})

const repeatLabel = computed(() => {
  if (player.repeat === 'one') return '单曲循环'
  if (player.repeat === 'all') return '列表循环'
  return '顺序播放'
})

const formatTime = (s) => {
  if (!s || !isFinite(s)) return '00:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
}

const seekFromEvent = (e) => {
  const track = progressRef.value
  if (!track || !player.duration) return
  const rect = track.getBoundingClientRect()
  const ratio = Math.min(Math.max((e.clientX - rect.left) / rect.width, 0), 1)
  player.seek(ratio * player.duration)
}

const onProgressMouseDown = (e) => {
  dragging.value = true
  seekFromEvent(e)
  const onMove = (ev) => seekFromEvent(ev)
  const onUp = () => {
    dragging.value = false
    window.removeEventListener('mousemove', onMove)
    window.removeEventListener('mouseup', onUp)
  }
  window.addEventListener('mousemove', onMove)
  window.addEventListener('mouseup', onUp)
}

const onCoverError = (e) => {
  e.target.style.display = 'none'
}
</script>

<style scoped>
.player-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 72px;
  background: var(--card-bg);
  border-top: 1px solid var(--border-color);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  z-index: 900; /* 低于 BackToTop / Header，避免遮挡 */
  display: flex;
  align-items: center;
}

.player-inner {
  position: relative;
  width: 1400px;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 24px;
}

/* ---- 歌词弹出面板 ---- */
.lyric-panel {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 20px;
  width: 420px;
  max-width: calc(100vw - 32px);
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md, 8px);
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  z-index: 10;
}

.lyric-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border-color);
}

.lyric-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  flex-shrink: 0;
}

.lyric-song {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.lyric-scroll {
  max-height: 340px;
  overflow-y: auto;
  padding: 8px 4px;
  scroll-behavior: smooth;
}

.lyric-line {
  padding: 6px 12px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: color 0.3s, transform 0.3s, font-size 0.3s;
}

.lyric-line.active {
  color: var(--primary-color);
  font-weight: 600;
  font-size: 16px;
  transform: scale(1.05);
  transform-origin: left center;
  background: rgba(64, 158, 255, 0.06);
}

.lyric-empty {
  padding: 32px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}

.lyric-fade-enter-active,
.lyric-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.lyric-fade-enter-from,
.lyric-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.song-info {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 240px;
  flex-shrink: 0;
  cursor: pointer;
}

.cover {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  background: var(--bg-color);
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, var(--primary-color), #9b59b6);
}

.meta {
  min-width: 0;
}

.title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.artist {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.player-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.controls {
  display: flex;
  align-items: center;
  gap: 4px;
}

.ctrl-btn {
  position: relative;
  color: var(--text-regular);
}

.ctrl-btn.active {
  color: var(--primary-color);
}

.one-badge {
  position: absolute;
  top: 2px;
  right: 4px;
  font-size: 9px;
  font-weight: 700;
  color: var(--primary-color);
}

.play-btn {
  width: 40px;
  height: 40px;
}

.progress {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  max-width: 600px;
}

.time {
  font-size: 12px;
  color: var(--text-secondary);
  width: 40px;
  text-align: center;
  flex-shrink: 0;
}

.progress-track {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: var(--border-color);
  cursor: pointer;
  position: relative;
}

.progress-track:hover {
  height: 6px;
}

.progress-filled {
  height: 100%;
  border-radius: 2px;
  background: var(--primary-color);
  position: relative;
}

.progress-filled::after {
  content: '';
  position: absolute;
  right: -5px;
  top: 50%;
  transform: translateY(-50%);
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--primary-color);
  opacity: 0;
  transition: opacity 0.2s;
}

.progress-track:hover .progress-filled::after {
  opacity: 1;
}

.player-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.volume-icon {
  color: var(--text-regular);
}

.volume-slider {
  width: 90px;
  accent-color: var(--primary-color);
  cursor: pointer;
}

.playlist {
  display: flex;
  flex-direction: column;
}

.playlist-scroll {
  max-height: 300px;
  overflow-y: auto;
}

.playlist-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: var(--radius-sm, 6px);
  cursor: pointer;
  font-size: 13px;
}

.playlist-item:hover {
  background: var(--bg-color, #f5f7fa);
}

.playlist-item.current .pl-title {
  color: var(--primary-color);
  font-weight: 600;
}

.pl-index {
  width: 20px;
  color: var(--text-secondary);
  flex-shrink: 0;
  text-align: center;
  font-size: 12px;
}

.playlist-item.current .pl-index {
  color: var(--primary-color);
}

.pl-title {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--text-primary);
}

.pl-artist {
  color: var(--text-secondary);
  font-size: 12px;
  flex-shrink: 0;
}

.playlist-footer {
  padding-top: 8px;
  margin-top: 4px;
  border-top: 1px solid var(--border-color, #ebeef5);
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
}

.player-slide-enter-active,
.player-slide-leave-active {
  transition: transform 0.3s ease;
}

.player-slide-enter-from,
.player-slide-leave-to {
  transform: translateY(100%);
}

@media (max-width: 768px) {
  .song-info {
    width: 140px;
  }

  .player-right .volume-icon,
  .player-right .volume-slider {
    display: none;
  }
}
</style>
