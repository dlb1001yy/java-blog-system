import request from './request'

export default {
  // 分页获取歌曲列表
  getSongs(params) {
    return request.get('/portal/music/songs', { params })
  },

  // 获取歌单列表
  getPlaylists() {
    return request.get('/portal/music/playlists')
  },

  // 获取歌单详情（含歌曲列表）
  getPlaylistDetail(id) {
    return request.get(`/portal/music/playlists/${id}`)
  },

  // 播放计数
  reportPlay(id) {
    return request.post(`/portal/music/songs/${id}/play`)
  }
}
