<template>
  <!-- 双形态播放器：仅在有当前曲目时渲染，展开态为迷你播放条，收起态为右侧悬浮球 -->
  <view v-if="song">
    <!-- 展开态：迷你播放条 -->
    <view v-if="!state.collapsed" class="player-bar" :style="{ bottom: barBottom }">
      <!-- 左：封面 + 歌曲信息（点击预留） -->
      <view class="song-info">
        <image
          v-if="song.cover && !coverError"
          class="cover"
          :src="resolveFileUrl(song.cover)"
          mode="aspectFill"
          @error="coverError = true"
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
        <!-- 收起为悬浮球 -->
        <view class="ctrl-btn collapse-btn" @click.stop="onToggleCollapsed">
          <Icon name="chevron-right" :size="18" />
        </view>
      </view>

      <!-- 底部 2px 进度条 -->
      <view class="progress">
        <view class="progress-filled" :style="{ width: progressPercent + '%' }"></view>
      </view>
    </view>

    <!-- 收起态：右侧悬浮球（点击展开），封面播放中旋转、暂停静止；播放中外圈主色呼吸光晕 -->
    <view
      v-else
      :class="['player-fab', state.isPlaying ? 'playing' : '']"
      @click.stop="onToggleCollapsed"
    >
      <image
        v-if="song.cover && !coverError"
        class="fab-cover"
        :class="state.isPlaying ? 'spinning' : 'spinning paused'"
        :src="resolveFileUrl(song.cover)"
        mode="aspectFill"
        @error="coverError = true"
      />
      <view v-else class="fab-cover fab-placeholder" :class="state.isPlaying ? 'spinning' : 'spinning paused'">
        <Icon name="music" :size="22" color="#FFFFFF" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { state, currentSong, toggle, next, prev, toggleCollapsed } from '@/common/player.js'
import { resolveFileUrl } from '@/common/config.js'
import Icon from '@/components/Icon.vue'

const props = defineProps({
  // 当前页面是否有自定义 TabBar（决定展开态底部偏移）
  hasTabBar: { type: Boolean, default: true },
  // 直接覆盖 bottom 定位（如答题页避开固定交卷栏），如 'calc(64px + env(safe-area-inset-bottom))'
  bottom: { type: String, default: '' }
})

const song = currentSong

// 封面加载失败标记：失败后回退渐变占位（避免灰块），切歌时重置重试新封面
const coverError = ref(false)
watch(() => song.value?.id, () => { coverError.value = false })

// 展开态定位：bottom prop 优先；否则按是否有 TabBar 决定（TabBar 56px + 安全区 / 12px + 安全区）
const barBottom = computed(() => {
  if (props.bottom) return props.bottom
  return props.hasTabBar
    ? 'calc(56px + env(safe-area-inset-bottom))'
    : 'calc(12px + env(safe-area-inset-bottom))'
})

const progressPercent = computed(() => {
  if (!state.duration) return 0
  return Math.min((state.currentTime / state.duration) * 100, 100)
})

const onToggle = () => toggle()
const onNext = () => next()
const onPrev = () => prev()
const onToggleCollapsed = () => toggleCollapsed()
</script>

<style lang="scss" scoped>
/* 展开态：全局迷你播放条，bottom 由内联 style 提供（TabBar 之上 / 无 TabBar 页面贴近底部） */
.player-bar {
  position: fixed;
  left: 12px;
  right: 12px;
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
  background: var(--app-bg, #FAFAF9);
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
  color: var(--app-text, #1C1917);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.artist {
  font-size: 11px;
  line-height: 1.3;
  color: var(--app-text-secondary, #57534E);
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
  color: var(--app-text-secondary, #57534E);

  &:active {
    opacity: 0.6;
  }
}

/* 收起按钮：复用 ctrl-btn，紧随 next 之后 */
.collapse-btn {
  margin-left: 2px;
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
  background: var(--app-divider, #F5F5F4);
}

.progress-filled {
  height: 100%;
  border-radius: 0 1px 1px 0;
  background: linear-gradient(90deg, var(--app-primary, $color-primary), var(--app-primary-light, $color-primary-light));
}

/* ===== 收起态：右侧悬浮球 ===== */
/* 白色实描边 + 双层投影：任何背景（含浅色封面/暗色主题）下轮廓一眼可识别 */
.player-fab {
  position: fixed;
  right: 8px;
  top: 45%;
  z-index: 998;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2.5px solid #FFFFFF;
  overflow: hidden;
  box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.08), 0 6px 18px rgba(15, 23, 42, 0.35);

  &:active {
    opacity: 0.7;
  }
}

/* 播放中：主色呼吸光晕（暂停时静止，仅保留白边轮廓） */
.player-fab.playing {
  animation: fab-glow 2s ease-in-out infinite;
}

.fab-cover {
  width: 100%;
  height: 100%;

  /* 播放中旋转，暂停时静止（animation-play-state） */
  &.spinning {
    animation: fab-rotate 12s linear infinite;
  }

  &.spinning.paused {
    animation-play-state: paused;
  }
}

/* 无封面：主色渐变底 + music 图标居中 */
.fab-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $color-primary, $color-accent);
}

@keyframes fab-rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes fab-glow {
  0%, 100% {
    box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.08), 0 6px 18px rgba(15, 23, 42, 0.35),
      0 0 0 6px rgba(5, 150, 105, 0.35);
  }
  50% {
    box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.08), 0 6px 18px rgba(15, 23, 42, 0.35),
      0 0 0 11px rgba(5, 150, 105, 0.12);
  }
}
</style>
