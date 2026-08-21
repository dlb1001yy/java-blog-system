<template>
  <div class="container music-page">
    <!-- Now Playing Banner -->
    <div class="card now-playing">
      <template v-if="player.currentSong">
        <img v-if="player.currentSong.cover" :src="player.currentSong.cover" alt="cover" class="np-cover" />
        <div v-else class="np-cover np-cover-placeholder">
          <el-icon :size="40"><Headset /></el-icon>
        </div>
        <div class="np-info">
          <div class="np-label">正在播放</div>
          <div class="np-title">{{ player.currentSong.title }}</div>
          <div class="np-artist">
            {{ player.currentSong.artist }}
            <span v-if="player.currentSong.album" class="np-album">· {{ player.currentSong.album }}</span>
          </div>
          <div class="eq" :class="{ paused: !player.isPlaying }">
            <span v-for="i in 7" :key="i" :style="{ animationDelay: (i * 0.12) + 's' }"></span>
          </div>
          <div class="np-progress">
            <span class="np-time">{{ formatTime(player.currentTime) }}</span>
            <el-slider
              :model-value="player.currentTime"
              :max="player.duration || 1"
              :show-tooltip="false"
              class="np-slider"
              @input="onSeek"
            />
            <span class="np-time">{{ formatTime(player.duration) }}</span>
          </div>
        </div>
        <el-button circle type="primary" size="large" class="np-play" @click="player.toggle()">
          <el-icon :size="24">
            <component :is="player.isPlaying ? 'VideoPause' : 'VideoPlay'" />
          </el-icon>
        </el-button>
      </template>
      <template v-else>
        <div class="np-empty">
          <el-icon :size="40" color="var(--text-secondary)"><Headset /></el-icon>
          <p>还没有播放的音乐，从下方列表选一首吧</p>
        </div>
      </template>
    </div>

    <!-- 歌词面板 -->
    <div class="card lyric-card" v-if="player.currentSong && lyricLines.length">
      <div class="section-header">
        <h2 class="section-title">歌词</h2>
      </div>
      <div class="lyric-scroll" ref="lyricScrollRef">
        <div
          v-for="(line, i) in lyricLines"
          :key="i"
          :ref="el => setLineRef(el, i)"
          class="lyric-line"
          :class="{ active: i === activeLyricIndex }"
          @click="player.seek(line.time)"
        >{{ line.text }}</div>
      </div>
    </div>

    <!-- 歌曲列表 -->
    <div class="card song-list-card">
      <div class="section-header">
        <h2 class="section-title">{{ playlistTitle }}</h2>
        <el-tag size="small" type="info">{{ songs.length }} 首</el-tag>
      </div>
      <el-table
        :data="songs"
        v-loading="loading"
        empty-text="暂无歌曲"
        @row-click="onRowClick"
        class="song-table"
        :row-class-name="rowClassName"
      >
        <el-table-column width="56" align="center">
          <template #default="{ $index }">
            <span v-if="isCurrentRow($index) && player.isPlaying" class="row-eq">
              <i></i><i></i><i></i>
            </span>
            <span v-else class="row-index">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="歌曲">
          <template #default="{ row }">
            <div class="song-cell">
              <img v-if="row.cover" :src="row.cover" alt="" class="song-cover" />
              <div v-else class="song-cover song-cover-placeholder">
                <el-icon :size="14"><Headset /></el-icon>
              </div>
              <span class="song-title" :class="{ active: isCurrentSong(row) }">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="artist" label="歌手" min-width="120" />
        <el-table-column prop="album" label="专辑" min-width="140" show-overflow-tooltip />
        <el-table-column label="时长" width="80" align="center">
          <template #default="{ row }">{{ formatTime(row.duration) }}</template>
        </el-table-column>
        <el-table-column label="" width="70" align="center">
          <template #default="{ row }">
            <el-button circle text type="primary" @click.stop="playSong(row)">
              <el-icon>
                <component :is="isCurrentSong(row) && player.isPlaying ? 'VideoPause' : 'VideoPlay'" />
              </el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          @current-change="onPageChange"
        />
      </div>
    </div>

    <!-- 推荐歌单（无数据不展示） -->
    <div v-if="playlists.length > 0" class="card playlists-card">
      <div class="section-header">
        <h2 class="section-title">推荐歌单</h2>
      </div>
      <div class="playlists">
        <div v-for="pl in playlists" :key="pl.id" class="playlist-item" @click="loadPlaylist(pl)">
          <div class="playlist-cover">
            <img v-if="pl.cover" :src="pl.cover" alt="" />
            <div v-else class="playlist-cover playlist-cover-placeholder">
              <el-icon :size="24"><Headset /></el-icon>
            </div>
            <span class="playlist-count">{{ pl.songCount }} 首</span>
          </div>
          <div class="playlist-name">{{ pl.name }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import musicApi from '@/api/music'
import { usePlayerStore } from '@/stores/player'

const player = usePlayerStore()

const songs = ref([])
const playlists = ref([])
const loading = ref(false)
const loadingPlaylists = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const playlistTitle = ref('全部歌曲')
const currentPlaylistId = ref(null)

const formatTime = (s) => {
  if (!s || !isFinite(s)) return '00:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
}

const fetchSongs = async () => {
  loading.value = true
  try {
    const res = await musicApi.getSongs({ page: page.value, size: pageSize.value })
    const d = res.data || {}
    songs.value = d.records || d.list || []
    total.value = d.total || songs.value.length
  } catch {
    /* 错误已由拦截器提示 */
  } finally {
    loading.value = false
  }
}

const fetchPlaylists = async () => {
  loadingPlaylists.value = true
  try {
    const res = await musicApi.getPlaylists()
    playlists.value = res.data?.records || []
  } catch {
    /* ignore */
  } finally {
    loadingPlaylists.value = false
  }
}

const isCurrentSong = (row) => player.currentSong && player.currentSong.id === row.id
const isCurrentRow = (index) => isCurrentSong(songs.value[index])
const rowClassName = ({ row }) => (isCurrentSong(row) ? 'current-row' : '')

const playSong = (row) => {
  if (isCurrentSong(row)) {
    player.toggle()
    return
  }
  player.setPlaylist(songs.value, songs.value.indexOf(row))
}

const onRowClick = (row) => playSong(row)

const onPageChange = (p) => {
  page.value = p
  currentPlaylistId.value = null
  playlistTitle.value = '全部歌曲'
  fetchSongs()
}

const loadPlaylist = async (pl) => {
  try {
    const res = await musicApi.getPlaylistDetail(pl.id)
    const list = (res.data && (res.data.songList || res.data.songs)) || []
    if (list.length === 0) return
    songs.value = list
    playlistTitle.value = (res.data && res.data.name) || pl.name
    currentPlaylistId.value = pl.id
    total.value = list.length
    player.setPlaylist(list, 0)
  } catch {
    /* ignore */
  }
}

const onSeek = (val) => player.seek(val)

/* ---- 歌词 ---- */
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

onMounted(() => {
  fetchSongs()
  fetchPlaylists()
})
</script>

<style scoped>
.music-page {
  padding-bottom: 24px;
}

/* ---- Now Playing ---- */
.now-playing {
  display: flex;
  align-items: center;
  gap: 24px;
  background: linear-gradient(135deg, var(--card-bg) 60%, rgba(64, 158, 255, 0.08));
}

.np-cover {
  width: 120px;
  height: 120px;
  border-radius: var(--radius-md);
  object-fit: cover;
  flex-shrink: 0;
  box-shadow: var(--shadow-light);
}

.np-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, var(--primary-color), #9b59b6);
}

.np-info {
  flex: 1;
  min-width: 0;
}

.np-label {
  font-size: 12px;
  color: var(--primary-color);
  font-weight: 600;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.np-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.np-artist {
  font-size: 14px;
  color: var(--text-regular);
  margin-top: 2px;
}

.np-album {
  color: var(--text-secondary);
}

.np-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  max-width: 480px;
}

.np-slider {
  flex: 1;
}

.np-time {
  font-size: 12px;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.np-play {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
}

.np-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px 0;
  color: var(--text-secondary);
}

/* ---- CSS 均衡器动画 ---- */
.eq {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 24px;
  margin-top: 8px;
}

.eq span {
  width: 4px;
  border-radius: 2px;
  background: var(--primary-color);
  animation: eq-bounce 0.9s ease-in-out infinite alternate;
}

.eq.paused span {
  animation-play-state: paused;
  height: 4px !important;
}

@keyframes eq-bounce {
  from { height: 4px; }
  to { height: 24px; }
}

/* ---- 歌词 ---- */
.lyric-card {
  max-width: 640px;
}

.lyric-scroll {
  max-height: 320px;
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

/* ---- 歌曲列表 ---- */
.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.song-table {
  cursor: pointer;
  width: 100%;
}

.song-table :deep(.el-table__row:hover) {
  background: rgba(64, 158, 255, 0.05);
}

.song-table :deep(.current-row) {
  background: rgba(64, 158, 255, 0.1);
}

.song-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.song-cover {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  background: var(--bg-color);
}

.song-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, var(--primary-color), #9b59b6);
  flex-shrink: 0;
}

.song-title {
  font-weight: 500;
  color: var(--text-primary);
}

.song-title.active {
  color: var(--primary-color);
}

.row-index {
  color: var(--text-secondary);
  font-size: 13px;
}

.row-eq {
  display: inline-flex;
  align-items: flex-end;
  gap: 2px;
  height: 14px;
}

.row-eq i {
  width: 3px;
  background: var(--primary-color);
  animation: eq-bounce-sm 0.8s ease-in-out infinite alternate;
}

.row-eq i:nth-child(1) { height: 4px; animation-delay: 0s; }
.row-eq i:nth-child(2) { height: 10px; animation-delay: 0.2s; }
.row-eq i:nth-child(3) { height: 6px; animation-delay: 0.4s; }

@keyframes eq-bounce-sm {
  from { height: 3px; }
  to { height: 14px; }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* ---- 歌单 ---- */
.playlists {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.playlist-item {
  cursor: pointer;
}

.playlist-cover {
  position: relative;
  border-radius: var(--radius-md);
  overflow: hidden;
  aspect-ratio: 1;
  background: var(--bg-color);
  box-shadow: var(--shadow-light);
  transition: transform 0.25s, box-shadow 0.25s;
}

.playlist-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, var(--primary-color), #9b59b6);
}

.playlist-item:hover .playlist-cover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-base);
}

.playlist-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.playlist-count {
  position: absolute;
  right: 6px;
  bottom: 6px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
  padding: 1px 8px;
  border-radius: 10px;
}

.playlist-name {
  margin-top: 8px;
  font-size: 14px;
  color: var(--text-regular);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 768px) {
  .now-playing {
    flex-direction: column;
    text-align: center;
  }

  .np-progress {
    margin-left: auto;
    margin-right: auto;
  }
}
</style>
