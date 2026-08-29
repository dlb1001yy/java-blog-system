<template>
  <!-- 迷你播放条：仅在有当前曲目时渲染，浮于底部 TabBar 之上 -->
  <view v-if="song" class="player-bar">
    <!-- 左：封面 + 歌曲信息（点击预留） -->
    <view class="song-info">
      <image
        v-if="song.cover"
        class="cover"
        :src="resolveFileUrl(song.cover)"
        mode="aspectFill"
      />
      <view v-else class="cover cover-placeholder">
        <Icon name="music" :size="18" color="#FFFFFF" />
      </view>
      <view class="meta">
        <text class="title">{{ song.title }}</text>
        <text class="artist">{{ song.artist }}</text>
      </view>
    </view>

    <!-- 右：控制按钮组 -->
    <view class="controls">
      <view class="ctrl-btn" @click.stop="onPrev">
        <Icon name="prev" :size="20" />
      </view>
      <view class="play-btn" @click.stop="onToggle">
        <Icon :name="state.isPlaying ? 'pause' : 'play'" :size="16" color="#FFFFFF" />
      </view>
      <view class="ctrl-btn" @click.stop="onNext">
        <Icon name="next" :size="20" />
      </view>
    </view>

    <!-- 底部 2px 进度条 -->
    <view class="progress">
      <view class="progress-filled" :style="{ width: progressPercent + '%' }"></view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { state, currentSong, toggle, next, prev } from '@/common/player.js'
import { resolveFileUrl } from '@/common/config.js'
import Icon from '@/components/Icon.vue'

const song = currentSong

const progressPercent = computed(() => {
  if (!state.duration) return 0
  return Math.min((state.currentTime / state.duration) * 100, 100)
})

const onToggle = () => toggle()
const onNext = () => next()
const onPrev = () => prev()
</script>

<style lang="scss" scoped>
/* 全局迷你播放条：fixed 于底部 TabBar（56px + 安全区）之上 */
.player-bar {
  position: fixed;
  left: 12px;
  right: 12px;
  bottom: calc(56px + env(safe-area-inset-bottom));
  z-index: 998;
  box-sizing: border-box;
  height: 56px;
  padding: 0 10px;
  border-radius: 14px;
  background: var(--app-bg-card, #FFFFFF);
  box-shadow: $shadow-floating;
  display: flex;
  align-items: center;
  overflow: hidden;
  transition: transform 0.15s ease;

  /* 整条卡片按压轻缩放反馈 */
  &:active {
    transform: scale(0.97);
  }
}

/* ===== 左：封面 + 信息 ===== */
.song-info {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.cover {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: var(--app-bg, #F1F5F9);
}

/* 无封面：主色渐变底 + music 图标 */
.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $color-primary, $color-accent);
}

.meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.3;
  color: var(--app-text, #0F172A);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.artist {
  font-size: 11px;
  line-height: 1.3;
  color: var(--app-text-secondary, #64748B);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 右：按钮组 ===== */
.controls {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
}

.ctrl-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: var(--app-text-secondary, #64748B);

  &:active {
    opacity: 0.6;
  }
}

/* 播放/暂停：主色圆形底（跟随主题变量） */
.play-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: #FFFFFF;
  background: var(--app-primary, $color-primary);

  &:active {
    opacity: 0.8;
  }
}

/* ===== 底部进度条 ===== */
.progress {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background: var(--app-divider, #F1F5F9);
}

.progress-filled {
  height: 100%;
  border-radius: 0 1px 1px 0;
  background: linear-gradient(90deg, var(--app-primary, $color-primary), var(--app-primary-light, $color-primary-light));
}
</style>
