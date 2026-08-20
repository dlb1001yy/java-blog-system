<template>
  <transition name="player-slide">
    <div v-if="player.currentSong" class="player-bar">
      <div class="player-inner">
        <!-- 左：封面 + 信息 -->
        <div class="song-info">
          <img :src="player.currentSong.cover" alt="cover" class="cover" @error="onCoverError" />
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
          <el-icon class="volume-icon" :size="16"><Headset /></el-icon>
          <input
            type="range"
            min="0"
            max="1"
            step="0.01"
            :value="player.volume"
            class="volume-slider"
            @input="player.setVolume(Number($event.target.value))"
          />
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores/player'

const player = usePlayerStore()
const progressRef = ref(null)
const dragging = ref(false)

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
  width: 1400px;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.song-info {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 240px;
  flex-shrink: 0;
}

.cover {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  background: var(--bg-color);
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
