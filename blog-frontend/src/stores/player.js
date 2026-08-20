import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import musicApi from '@/api/music'

const PREFS_KEY = 'player-prefs'

function loadPrefs() {
  try {
    return JSON.parse(localStorage.getItem(PREFS_KEY)) || {}
  } catch {
    return {}
  }
}

export const usePlayerStore = defineStore('player', () => {
  const prefs = loadPrefs()

  const playlist = ref([])
  const currentIndex = ref(-1)
  const isPlaying = ref(false)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(prefs.volume ?? 0.8)
  const repeat = ref(prefs.repeat ?? 'none') // none | all | one
  const shuffle = ref(prefs.shuffle ?? false)

  const currentSong = computed(() =>
    currentIndex.value >= 0 && currentIndex.value < playlist.value.length
      ? playlist.value[currentIndex.value]
      : null
  )

  // 全局唯一 Audio 对象
  const audio = new Audio()
  audio.volume = volume.value

  const savePrefs = () => {
    localStorage.setItem(
      PREFS_KEY,
      JSON.stringify({ volume: volume.value, repeat: repeat.value, shuffle: shuffle.value })
    )
  }

  const reportPlay = (id) => {
    musicApi.reportPlay(id).catch(() => {})
  }

  const play = () => {
    audio.play().catch(() => {})
  }

  const loadCurrent = () => {
    const song = currentSong.value
    if (!song) return
    audio.src = song.fileUrl
    currentTime.value = 0
    duration.value = song.duration || 0
    isPlaying.value = true
    play()
    reportPlay(song.id)
  }

  audio.addEventListener('timeupdate', () => {
    currentTime.value = audio.currentTime
  })
  audio.addEventListener('loadedmetadata', () => {
    if (audio.duration && isFinite(audio.duration)) duration.value = audio.duration
  })
  audio.addEventListener('ended', () => {
    handleEnded()
  })

  const nextIndex = (step) => {
    const len = playlist.value.length
    if (len === 0) return -1
    if (shuffle.value && len > 1) {
      let i
      do {
        i = Math.floor(Math.random() * len)
      } while (i === currentIndex.value)
      return i
    }
    return (currentIndex.value + step + len) % len
  }

  const handleEnded = () => {
    if (repeat.value === 'one') {
      audio.currentTime = 0
      play()
      return
    }
    const next = nextIndex(1)
    if (repeat.value === 'none' && currentIndex.value === playlist.value.length - 1 && !shuffle.value) {
      isPlaying.value = false
      return
    }
    playAt(next)
  }

  const setPlaylist = (list, index = 0) => {
    playlist.value = list || []
    if (playlist.value.length === 0) {
      currentIndex.value = -1
      isPlaying.value = false
      audio.pause()
      return
    }
    playAt(Math.min(Math.max(index, 0), playlist.value.length - 1))
  }

  const playAt = (index) => {
    if (index < 0 || index >= playlist.value.length) return
    currentIndex.value = index
    loadCurrent()
  }

  const toggle = () => {
    if (!currentSong.value) {
      if (playlist.value.length > 0) playAt(0)
      return
    }
    if (isPlaying.value) {
      audio.pause()
      isPlaying.value = false
    } else {
      play()
      isPlaying.value = true
    }
  }

  const next = () => {
    if (playlist.value.length === 0) return
    playAt(nextIndex(1))
  }

  const prev = () => {
    if (playlist.value.length === 0) return
    // 播放超过 3 秒时回到开头
    if (audio.currentTime > 3) {
      audio.currentTime = 0
      return
    }
    playAt(nextIndex(-1))
  }

  const seek = (time) => {
    if (isFinite(time)) {
      audio.currentTime = time
      currentTime.value = time
    }
  }

  const setVolume = (v) => {
    volume.value = Math.min(Math.max(v, 0), 1)
    audio.volume = volume.value
    savePrefs()
  }

  const toggleRepeat = () => {
    repeat.value = repeat.value === 'none' ? 'all' : repeat.value === 'all' ? 'one' : 'none'
    savePrefs()
  }

  const toggleShuffle = () => {
    shuffle.value = !shuffle.value
    savePrefs()
  }

  return {
    playlist,
    currentIndex,
    isPlaying,
    currentTime,
    duration,
    volume,
    repeat,
    shuffle,
    currentSong,
    setPlaylist,
    playAt,
    play,
    toggle,
    next,
    prev,
    seek,
    setVolume,
    toggleRepeat,
    toggleShuffle
  }
})
