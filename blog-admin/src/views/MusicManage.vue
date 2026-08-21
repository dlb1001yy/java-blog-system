<template>
  <PageContainer title="音乐管理" description="背景音乐与歌单维护">
    <!-- 法律提示 -->
    <el-alert
      class="legal-alert"
      type="warning"
      show-icon
      :closable="false"
      title="仅限本地演示环境使用，严禁上传未授权版权音乐"
    />

    <el-tabs v-model="activeTab">
      <!-- ================= 歌曲管理 ================= -->
      <el-tab-pane label="歌曲管理" name="songs">
        <div class="table-card">
          <div class="toolbar">
            <el-button type="primary" :icon="UploadIcon" @click="openUploadDialog">上传音乐</el-button>
            <el-input
              v-model="songKeyword"
              placeholder="搜索歌名/歌手/专辑"
              clearable
              class="keyword-input"
              @keyup.enter="handleSongSearch"
              @clear="handleSongSearch"
            >
              <template #append>
                <el-button :icon="Search" @click="handleSongSearch" />
              </template>
            </el-input>
          </div>

          <!-- 统计卡 -->
          <div class="stats-row">
            <div class="stat-card">
              <div class="stat-value">{{ stats.totalSongs }}</div>
              <div class="stat-label">总曲目</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ formatHours(stats.totalDuration) }}</div>
              <div class="stat-label">总时长</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ formatSize(stats.totalSize) }}</div>
              <div class="stat-label">存储大小</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ stats.totalPlayCount }}</div>
              <div class="stat-label">总播放</div>
            </div>
          </div>

          <el-table :data="songList" v-loading="songLoading" :border="false" stripe>
            <el-table-column label="歌曲" min-width="220">
              <template #default="{ row }">
                <div class="song-cell">
                  <el-image
                    v-if="row.cover"
                    :src="row.cover"
                    fit="cover"
                    class="song-cover"
                    :preview-src-list="[row.cover]"
                    preview-teleported
                  />
                  <div v-else class="song-cover song-cover--empty"><el-icon><Headset /></el-icon></div>
                  <div class="song-info">
                    <div class="song-title">{{ row.title }}</div>
                    <div class="song-artist">{{ row.artist || '未知歌手' }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="album" label="专辑" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.album || '-' }}</template>
            </el-table-column>
            <el-table-column label="时长" width="90">
              <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
            </el-table-column>
            <el-table-column label="格式" width="80">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ (row.format || 'mp3').toUpperCase() }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="大小" width="100">
              <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column prop="playCount" label="播放次数" width="100" />
            <el-table-column prop="createTime" label="上传时间" width="170" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handlePreview(row)">预览</el-button>
                <el-button link type="primary" @click="openSongEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="handleSongDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="songPage"
              v-model:page-size="songSize"
              :total="songTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchSongs"
              @current-change="fetchSongs"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- ================= 歌单管理 ================= -->
      <el-tab-pane label="歌单管理" name="playlists">
        <div class="table-card">
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="openPlaylistEdit(null)">新建歌单</el-button>
            <el-input
              v-model="playlistKeyword"
              placeholder="搜索歌单名"
              clearable
              class="keyword-input"
              @keyup.enter="handlePlaylistSearch"
              @clear="handlePlaylistSearch"
            >
              <template #append>
                <el-button :icon="Search" @click="handlePlaylistSearch" />
              </template>
            </el-input>
          </div>

          <el-table :data="playlistList" v-loading="playlistLoading" :border="false" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column label="歌单名" min-width="180">
              <template #default="{ row }">
                <div class="song-cell">
                  <el-image
                    v-if="row.cover"
                    :src="row.cover"
                    fit="cover"
                    class="song-cover"
                    :preview-src-list="[row.cover]"
                    preview-teleported
                  />
                  <div v-else class="song-cover song-cover--empty"><el-icon><List /></el-icon></div>
                  <span class="song-title">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">{{ row.description || '-' }}</template>
            </el-table-column>
            <el-table-column prop="songCount" label="歌曲数" width="90" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status ? 'success' : 'info'" size="small">
                  {{ row.status ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openPlaylistEdit(row)">编辑</el-button>
                <el-button link type="primary" @click="openSongsDrawer(row)">管理歌曲</el-button>
                <el-button link type="danger" @click="handlePlaylistDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="playlistPage"
              v-model:page-size="playlistSize"
              :total="playlistTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchPlaylists"
              @current-change="fetchPlaylists"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传音乐" width="520px" destroy-on-close>
      <el-alert
        class="dialog-legal-alert"
        type="warning"
        show-icon
        :closable="false"
        title="仅限本地演示环境使用，严禁上传未授权版权音乐"
      />
      <el-form :model="uploadForm" label-width="70px">
        <el-form-item label="音频文件" required>
          <el-upload
            drag
            class="audio-uploader"
            accept=".mp3,audio/mpeg"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="() => (uploadForm.file = null)"
            :file-list="uploadFileList"
          >
            <el-icon :size="36" class="upload-icon"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽 mp3 文件到此处，或 <em>点击选择</em></div>
            <template #tip>
              <div class="el-upload__tip">仅支持 mp3 格式，大小不超过 20MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="uploadForm.title" placeholder="请输入歌曲标题" />
        </el-form-item>
        <el-form-item label="歌手">
          <el-input v-model="uploadForm.artist" placeholder="请输入歌手" />
        </el-form-item>
        <el-form-item label="专辑">
          <el-input v-model="uploadForm.album" placeholder="请输入专辑" />
        </el-form-item>
        <el-form-item label="歌词">
          <div class="lyric-toolbar">
            <el-button
              size="small"
              type="primary"
              plain
              :disabled="!uploadForm.file"
              :loading="parsingLyric"
              @click="handleParseLyric"
            >
              解析歌词
            </el-button>
            <span class="lyric-tip">从 mp3 内嵌歌词解析；未解析到可手工填写</span>
          </div>
          <el-input
            v-model="uploadForm.lyric"
            type="textarea"
            :rows="4"
            class="lyric-textarea"
            placeholder="点击「解析歌词」自动回填，或手工输入（LRC 格式），如：[00:12.00]第一句歌词"
          />
        </el-form-item>
        <el-form-item label="封面">
          <div class="cover-field">
            <Upload v-model="uploadForm.cover" placeholder="上传封面" />
            <div class="cover-tip">留空上传时将根据歌名自动生成封面</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">确认上传</el-button>
      </template>
    </el-dialog>

    <!-- 歌曲编辑对话框 -->
    <el-dialog v-model="songEditVisible" title="编辑歌曲" width="460px">
      <el-form :model="songEditForm" label-width="70px">
        <el-form-item label="标题" required>
          <el-input v-model="songEditForm.title" />
        </el-form-item>
        <el-form-item label="歌手">
          <el-input v-model="songEditForm.artist" />
        </el-form-item>
        <el-form-item label="专辑">
          <el-input v-model="songEditForm.album" />
        </el-form-item>
        <el-form-item label="歌词">
          <el-input
            v-model="songEditForm.lyric"
            type="textarea"
            :rows="4"
            class="lyric-textarea"
            placeholder="请输入歌词（LRC 格式）"
          />
        </el-form-item>
        <el-form-item label="封面">
          <Upload v-model="songEditForm.cover" placeholder="上传封面" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="songEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="songSaving" @click="handleSongSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 歌曲预览 -->
    <el-dialog v-model="previewVisible" :title="`预览：${previewSong?.title || ''}`" width="480px">
      <audio v-if="previewVisible && previewSong?.fileUrl" :src="previewSong.fileUrl" controls style="width: 100%" />
      <div v-else>该歌曲无音频文件地址</div>
    </el-dialog>

    <!-- 歌单编辑对话框 -->
    <el-dialog v-model="playlistEditVisible" :title="playlistForm.id ? '编辑歌单' : '新建歌单'" width="500px">
      <el-form :model="playlistForm" label-width="80px">
        <el-form-item label="歌单名" required>
          <el-input v-model="playlistForm.name" placeholder="请输入歌单名" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="playlistForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="playlistForm.cover" placeholder="请输入封面图片地址" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="playlistForm.status">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="playlistEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="playlistSaving" @click="handlePlaylistSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 管理歌曲抽屉 -->
    <el-drawer v-model="songsDrawerVisible" :title="`管理歌曲：${currentPlaylist?.name || ''}`" size="480px">
      <div v-loading="drawerLoading">
        <el-checkbox-group v-model="selectedSongIds">
          <div v-for="song in allSongs" :key="song.id" class="song-check-item">
            <el-checkbox :label="song.id">
              <span class="song-title">{{ song.title }}</span>
              <span class="song-artist"> - {{ song.artist || '未知歌手' }}</span>
            </el-checkbox>
          </div>
          <el-empty v-if="!allSongs.length && !drawerLoading" description="暂无歌曲，请先上传" :image-size="80" />
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="songsDrawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="drawerSaving" @click="handleSavePlaylistSongs">保存</el-button>
      </template>
    </el-drawer>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Upload as UploadIcon, UploadFilled, Headset, List } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import Upload from '@/components/Upload.vue'
import musicApi from '@/api/music'

const activeTab = ref('songs')

// ---------------- 歌曲 ----------------
const songLoading = ref(false)
const songList = ref([])
const songPage = ref(1)
const songSize = ref(10)
const songTotal = ref(0)
const songKeyword = ref('')
const stats = reactive({ totalSongs: 0, totalDuration: 0, totalSize: 0, totalPlayCount: 0 })

const fetchSongs = async () => {
  songLoading.value = true
  try {
    const res = await musicApi.getSongs({
      page: songPage.value,
      size: songSize.value,
      keyword: songKeyword.value || undefined
    })
    songList.value = res.data.records
    songTotal.value = res.data.total
  } finally {
    songLoading.value = false
  }
}

const fetchStats = async () => {
  const res = await musicApi.getSongStats()
  Object.assign(stats, res.data)
}

const handleSongSearch = () => {
  songPage.value = 1
  fetchSongs()
}

// 格式化
const formatDuration = (sec) => {
  if (!sec && sec !== 0) return '-'
  const m = Math.floor(sec / 60)
  const s = String(sec % 60).padStart(2, '0')
  return `${m}:${s}`
}

const formatHours = (sec) => {
  if (!sec) return '0分钟'
  const hours = Math.floor(sec / 3600)
  const minutes = Math.floor((sec % 3600) / 60)
  if (hours > 0) return `${hours}小时${minutes ? minutes + '分' : ''}`
  return `${minutes}分钟`
}

const formatSize = (bytes) => {
  if (!bytes) return '0MB'
  if (bytes >= 1024 * 1024 * 1024) return (bytes / 1024 / 1024 / 1024).toFixed(2) + 'GB'
  return (bytes / 1024 / 1024).toFixed(2) + 'MB'
}

// 上传
const uploadDialogVisible = ref(false)
const uploading = ref(false)
const uploadFileList = ref([])
const uploadForm = reactive({ file: null, title: '', artist: '', album: '', lyric: '', cover: '' })
const parsingLyric = ref(false)

const openUploadDialog = () => {
  uploadForm.file = null
  uploadForm.title = ''
  uploadForm.artist = ''
  uploadForm.album = ''
  uploadForm.lyric = ''
  uploadForm.cover = ''
  uploadFileList.value = []
  uploadDialogVisible.value = true
}

const validateFile = (file) => {
  const isMp3 = file.name.toLowerCase().endsWith('.mp3') || file.type === 'audio/mpeg'
  if (!isMp3) {
    ElMessage.error('仅支持 mp3 格式文件')
    return false
  }
  if (file.size > 20 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 20MB')
    return false
  }
  return true
}

const handleFileChange = (file) => {
  if (!validateFile(file.raw)) {
    uploadFileList.value = []
    uploadForm.file = null
    return
  }
  uploadFileList.value = [file]
  uploadForm.file = file.raw
  if (!uploadForm.title) {
    uploadForm.title = file.name.replace(/\.mp3$/i, '')
  }
}

const handleParseLyric = async () => {
  if (!uploadForm.file) return
  parsingLyric.value = true
  try {
    const res = await musicApi.parseLyric(uploadForm.file, uploadForm.title.trim(), uploadForm.artist.trim())
    const { lyric, cover } = res.data || {}
    if (lyric) {
      uploadForm.lyric = lyric
    }
    // 未手动上传封面时回填自动生成的封面
    if (cover && !uploadForm.cover) {
      uploadForm.cover = cover
    }
    if (lyric) {
      ElMessage.success('歌词解析成功，封面已同步生成')
    } else {
      ElMessage.warning('未匹配到歌词，可手工填写；封面已生成')
    }
  } finally {
    parsingLyric.value = false
  }
}

const handleUpload = async () => {
  if (!uploadForm.file) {
    ElMessage.warning('请先选择音频文件')
    return
  }
  if (!uploadForm.title.trim()) {
    ElMessage.warning('请输入歌曲标题')
    return
  }
  uploading.value = true
  try {
    await musicApi.uploadSong(uploadForm.file, uploadForm.title.trim(), uploadForm.artist.trim(), uploadForm.album.trim(), uploadForm.lyric, uploadForm.cover)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    fetchSongs()
    fetchStats()
  } finally {
    uploading.value = false
  }
}

// 编辑歌曲
const songEditVisible = ref(false)
const songSaving = ref(false)
const songEditForm = reactive({ id: null, title: '', artist: '', album: '', lyric: '', cover: '' })

const openSongEdit = (row) => {
  songEditForm.id = row.id
  songEditForm.title = row.title
  songEditForm.artist = row.artist || ''
  songEditForm.album = row.album || ''
  songEditForm.lyric = row.lyric || ''
  songEditForm.cover = row.cover || ''
  songEditVisible.value = true
}

const handleSongSave = async () => {
  if (!songEditForm.title.trim()) {
    ElMessage.warning('请输入歌曲标题')
    return
  }
  songSaving.value = true
  try {
    await musicApi.updateSong(songEditForm.id, {
      title: songEditForm.title.trim(),
      artist: songEditForm.artist.trim(),
      album: songEditForm.album.trim(),
      lyric: songEditForm.lyric || null,
      cover: songEditForm.cover || null
    })
    ElMessage.success('保存成功')
    songEditVisible.value = false
    fetchSongs()
  } finally {
    songSaving.value = false
  }
}

// 预览
const previewVisible = ref(false)
const previewSong = ref(null)
const handlePreview = (row) => {
  previewSong.value = row
  previewVisible.value = true
}

const handleSongDelete = (row) => {
  ElMessageBox.confirm(`确定要删除歌曲「${row.title}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await musicApi.deleteSong(row.id)
      ElMessage.success('删除成功')
      fetchSongs()
      fetchStats()
    }).catch(() => {})
}

// ---------------- 歌单 ----------------
const playlistLoading = ref(false)
const playlistList = ref([])
const playlistPage = ref(1)
const playlistSize = ref(10)
const playlistTotal = ref(0)
const playlistKeyword = ref('')

const fetchPlaylists = async () => {
  playlistLoading.value = true
  try {
    const res = await musicApi.getPlaylists({
      page: playlistPage.value,
      size: playlistSize.value,
      keyword: playlistKeyword.value || undefined
    })
    playlistList.value = res.data.records
    playlistTotal.value = res.data.total
  } finally {
    playlistLoading.value = false
  }
}

const handlePlaylistSearch = () => {
  playlistPage.value = 1
  fetchPlaylists()
}

const playlistEditVisible = ref(false)
const playlistSaving = ref(false)
const playlistForm = reactive({ id: null, name: '', description: '', cover: '', status: 0 })

const openPlaylistEdit = (row) => {
  playlistForm.id = row?.id || null
  playlistForm.name = row?.name || ''
  playlistForm.description = row?.description || ''
  playlistForm.cover = row?.cover || ''
  playlistForm.status = row?.status ?? 0
  playlistEditVisible.value = true
}

const handlePlaylistSave = async () => {
  if (!playlistForm.name.trim()) {
    ElMessage.warning('请输入歌单名')
    return
  }
  playlistSaving.value = true
  try {
    const data = {
      name: playlistForm.name.trim(),
      description: playlistForm.description.trim(),
      cover: playlistForm.cover.trim(),
      status: playlistForm.status
    }
    if (playlistForm.id) {
      await musicApi.updatePlaylist(playlistForm.id, data)
    } else {
      await musicApi.createPlaylist(data)
    }
    ElMessage.success('保存成功')
    playlistEditVisible.value = false
    fetchPlaylists()
  } finally {
    playlistSaving.value = false
  }
}

const handlePlaylistDelete = (row) => {
  ElMessageBox.confirm(`确定要删除歌单「${row.name}」吗？歌单内歌曲关联将一并删除。`, '提示', { type: 'warning' })
    .then(async () => {
      await musicApi.deletePlaylist(row.id)
      ElMessage.success('删除成功')
      fetchPlaylists()
    }).catch(() => {})
}

// 管理歌曲抽屉
const songsDrawerVisible = ref(false)
const drawerLoading = ref(false)
const drawerSaving = ref(false)
const currentPlaylist = ref(null)
const allSongs = ref([])
const selectedSongIds = ref([])

const openSongsDrawer = async (row) => {
  currentPlaylist.value = row
  songsDrawerVisible.value = true
  drawerLoading.value = true
  try {
    const [songsRes, detailRes] = await Promise.all([
      musicApi.getSongs({ page: 1, size: 1000 }),
      musicApi.getPlaylistDetail(row.id)
    ])
    allSongs.value = songsRes.data.records
    selectedSongIds.value = (detailRes.data.songList || []).map(s => s.id)
  } finally {
    drawerLoading.value = false
  }
}

const handleSavePlaylistSongs = async () => {
  drawerSaving.value = true
  try {
    await musicApi.savePlaylistSongs(currentPlaylist.value.id, selectedSongIds.value)
    ElMessage.success('保存成功')
    songsDrawerVisible.value = false
    fetchPlaylists()
  } finally {
    drawerSaving.value = false
  }
}

onMounted(() => {
  fetchSongs()
  fetchStats()
  fetchPlaylists()
})
</script>

<style scoped>
.legal-alert {
  margin-bottom: var(--space-4);
}

.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.keyword-input {
  width: 280px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.stat-card {
  background: var(--bg-subtle);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  text-align: center;
  border: 1px solid var(--border-color);
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  margin-top: var(--space-1);
  font-size: 13px;
  color: var(--text-secondary);
}

.song-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.song-cover {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}

.song-cover--empty {
  background: var(--bg-subtle);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.song-info {
  min-width: 0;
}

.song-title {
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-artist {
  font-size: 12px;
  color: var(--text-secondary);
}

.dialog-legal-alert {
  margin-bottom: var(--space-4);
}

.audio-uploader :deep(.el-upload-dragger) {
  width: 100%;
}

.upload-icon {
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.lyric-textarea :deep(textarea) {
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.6;
}

.lyric-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  margin-bottom: 6px;
}

.lyric-tip {
  font-size: 12px;
  color: var(--text-secondary);
}

.cover-field {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.cover-tip {
  font-size: 12px;
  color: var(--text-secondary);
  padding-bottom: 4px;
}

.song-check-item {
  padding: 6px 4px;
  border-bottom: 1px solid var(--border-color);
}

.song-check-item:last-child {
  border-bottom: none;
}

:deep(.el-table) {
  border-radius: var(--radius-md);
  --el-table-border-color: var(--border-color);
}

:deep(.el-table th.el-table__cell) {
  background: var(--bg-subtle);
  color: var(--text-regular);
  font-weight: 600;
}

:deep(.el-table tr) {
  transition: background var(--transition-base);
}

:deep(.el-table__row:hover > td.el-table__cell) {
  background: var(--el-color-primary-light-9) !important;
}

:deep(.el-table .el-table__cell) {
  border-bottom: 1px solid var(--border-color);
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: var(--bg-subtle);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-5);
}
</style>
