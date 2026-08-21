import request from './request'

export default {
  // ---------------- 歌曲 ----------------

  // 上传音频文件（file + title/artist/album/lyric/cover）
  uploadSong(file, title, artist, album, lyric, cover) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('title', title)
    if (artist) formData.append('artist', artist)
    if (album) formData.append('album', album)
    if (lyric) formData.append('lyric', lyric)
    if (cover) formData.append('cover', cover)
    return request.post('/admin/music/songs/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000
    })
  },

  // 解析歌词：优先 MP3 内嵌（ID3v2 USLT），无内嵌时按歌名+歌手在线匹配 LRC
  parseLyric(file, title, artist) {
    const formData = new FormData()
    formData.append('file', file)
    if (title) formData.append('title', title)
    if (artist) formData.append('artist', artist)
    return request.post('/admin/music/songs/parse-lyric', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    })
  },

  // 歌曲分页
  getSongs(params) {
    return request.get('/admin/music/songs', { params })
  },

  // 存储统计
  getSongStats() {
    return request.get('/admin/music/songs/stats')
  },

  // 新增歌曲（外链元数据）
  createSong(data) {
    return request.post('/admin/music/songs', data)
  },

  // 更新歌曲
  updateSong(id, data) {
    return request.put(`/admin/music/songs/${id}`, data)
  },

  // 删除歌曲
  deleteSong(id) {
    return request.delete(`/admin/music/songs/${id}`)
  },

  // ---------------- 歌单 ----------------

  // 歌单分页
  getPlaylists(params) {
    return request.get('/admin/music/playlists', { params })
  },

  // 歌单详情（含 songList）
  getPlaylistDetail(id) {
    return request.get(`/admin/music/playlists/${id}`)
  },

  // 新增歌单
  createPlaylist(data) {
    return request.post('/admin/music/playlists', data)
  },

  // 更新歌单
  updatePlaylist(id, data) {
    return request.put(`/admin/music/playlists/${id}`, data)
  },

  // 删除歌单
  deletePlaylist(id) {
    return request.delete(`/admin/music/playlists/${id}`)
  },

  // 设置歌单歌曲（songIds 全量替换）
  savePlaylistSongs(id, songIds) {
    return request.post(`/admin/music/playlists/${id}/songs`, { songIds })
  }
}
