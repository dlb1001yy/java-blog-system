/**
 * 全局音乐播放器（模块级单例，Vue3 reactive）
 * 对标 blog-frontend/src/stores/player.js 的能力：
 * 播放列表、播放/暂停、上一首/下一首、进度、音量、循环/随机、偏好持久化
 * 音频实例为模块级唯一 uni.createInnerAudioContext()，页面切换不销毁
 */
import { reactive, computed } from 'vue'
import request from './request.js'
import { resolveFileUrl } from './config.js'

// 偏好持久化 key：{ volume, repeat, shuffle }
const PREFS_KEY = 'app_player_prefs'

function loadPrefs() {
  try {
    const raw = uni.getStorageSync(PREFS_KEY)
    if (!raw) return {}
    const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch (e) {
    return {}
  }
}

const prefs = loadPrefs()

const state = reactive({
  playlist: [],
  currentIndex: -1,
  isPlaying: false,
  currentTime: 0, // 秒
  duration: 0,    // 秒
  volume: prefs.volume != null ? Number(prefs.volume) : 0.8,
  repeat: ['none', 'all', 'one'].indexOf(prefs.repeat) > -1 ? prefs.repeat : 'none',
  shuffle: !!prefs.shuffle
})

// 当前歌曲：索引越界时返回 null
const currentSong = computed(() => {
  const i = state.currentIndex
  return i >= 0 && i < state.playlist.length ? state.playlist[i] : null
})

const savePrefs = () => {
  try {
    uni.setStorageSync(PREFS_KEY, {
      volume: state.volume,
      repeat: state.repeat,
      shuffle: state.shuffle
    })
  } catch (e) {
    // 写入失败不影响本次会话生效
  }
}

// 上报播放量：静默失败，不弹 loading
const reportPlay = (id) => {
  if (!id) return
  request({
    url: `/portal/music/songs/${id}/play`,
    method: 'POST',
    loading: false
  }).catch(() => {})
}

// ===== 全局唯一音频实例（模块顶层创建） =====
const audio = uni.createInnerAudioContext()
// 音乐场景下不跟随系统静音开关
audio.obeyMuteSwitch = false
audio.volume = state.volume

audio.onPlay(() => {
  state.isPlaying = true
})
audio.onPause(() => {
  state.isPlaying = false
})
audio.onStop(() => {
  state.isPlaying = false
})
audio.onTimeUpdate(() => {
  state.currentTime = audio.currentTime || 0
  const d = audio.duration
  if (d && isFinite(d)) state.duration = d
})
audio.onCanplay(() => {
  const d = audio.duration
  if (d && isFinite(d)) state.duration = d
})
audio.onEnded(() => {
  handleEnded()
})
audio.onError(() => {
  state.isPlaying = false
  uni.showToast({ title: '播放失败', icon: 'none' })
  audio.stop()
})

// ===== 播放控制 =====
const play = () => {
  audio.play()
  state.isPlaying = true
}

const pause = () => {
  audio.pause()
  state.isPlaying = false
}

// 有当前曲才生效
const toggle = () => {
  if (!currentSong.value) return
  if (state.isPlaying) {
    pause()
  } else {
    play()
  }
}

// 计算切歌目标索引：shuffle 随机且避开当前曲；否则按 step 顺序（支持循环）
const nextIndex = (step) => {
  const len = state.playlist.length
  if (len === 0) return -1
  if (state.shuffle && len > 1) {
    let i
    do {
      i = Math.floor(Math.random() * len)
    } while (i === state.currentIndex)
    return i
  }
  return (state.currentIndex + step + len) % len
}

const playAt = (index) => {
  const len = state.playlist.length
  if (index < 0 || index >= len) return
  state.currentIndex = index
  const song = state.playlist[index]
  state.currentTime = 0
  state.duration = Number(song.duration) || 0
  state.isPlaying = true
  // 相对路径由 resolveFileUrl 拼接服务器 origin，完整 URL 原样返回
  audio.src = resolveFileUrl(song.fileUrl)
  audio.play()
  reportPlay(song.id)
}

// 替换播放列表并从 index 开始播放
const setPlaylist = (list, index = 0) => {
  state.playlist = Array.isArray(list) ? list : []
  if (state.playlist.length === 0) {
    state.currentIndex = -1
    state.isPlaying = false
    audio.pause()
    return
  }
  playAt(Math.min(Math.max(index, 0), state.playlist.length - 1))
}

const next = () => {
  const len = state.playlist.length
  if (len === 0) return
  const atEnd = state.currentIndex >= len - 1
  if (atEnd && !state.shuffle) {
    if (state.repeat === 'none') {
      // 顺序播完：停止并复位播放态
      audio.stop()
      state.isPlaying = false
      state.currentTime = 0
      return
    }
    if (state.repeat === 'one') {
      // 单曲循环到末尾：重播当前曲
      seek(0)
      play()
      return
    }
  }
  playAt(nextIndex(1))
}

const prev = () => {
  const len = state.playlist.length
  if (len === 0) return
  // 播放超过 3 秒：先回到开头
  if (state.currentTime > 3) {
    seek(0)
    return
  }
  playAt(nextIndex(-1))
}

// 进度跳转：clamp 0 ~ duration
const seek = (t) => {
  if (typeof t !== 'number' || !isFinite(t)) return
  let time = Math.max(t, 0)
  if (state.duration > 0) time = Math.min(time, state.duration)
  audio.seek(time)
  state.currentTime = time
}

const setVolume = (v) => {
  state.volume = Math.min(Math.max(Number(v) || 0, 0), 1)
  audio.volume = state.volume
  savePrefs()
}

// none → all → one 循环
const toggleRepeat = () => {
  state.repeat = state.repeat === 'none' ? 'all' : state.repeat === 'all' ? 'one' : 'none'
  savePrefs()
}

const toggleShuffle = () => {
  state.shuffle = !state.shuffle
  savePrefs()
}

// 播完回调：单曲循环回到开头重播，否则交给 next() 处理边界
const handleEnded = () => {
  if (state.repeat === 'one') {
    seek(0)
    play()
    return
  }
  next()
}

export {
  state,
  currentSong,
  setPlaylist,
  playAt,
  play,
  pause,
  toggle,
  next,
  prev,
  seek,
  setVolume,
  toggleRepeat,
  toggleShuffle
}
