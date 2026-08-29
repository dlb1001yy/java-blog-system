import { BASE_URL, TOKEN_KEY } from './config.js'
import { signRequest } from './signing.js'

const UPLOAD_URL = '/v1/storage/upload'
const MAX_SIZE = 10 * 1024 * 1024 // 10MB

/**
 * 上传图片文件，成功 resolve 文件 URL
 * @param {string} filePath 本地文件路径（uni.chooseImage 返回的 tempFilePaths 项）
 * @param {object} options { loading: true } 是否显示加载中
 * @returns {Promise<string>} 上传后的文件 URL
 */
export function uploadImage(filePath, options = {}) {
  return new Promise((resolve, reject) => {
    // 前置校验文件大小
    uni.getFileInfo({
      filePath,
      success: (info) => {
        if (info.size > MAX_SIZE) {
          uni.showToast({ title: '图片不能超过 10MB', icon: 'none' })
          reject(new Error('文件超过 10MB'))
          return
        }
        doUpload(filePath, options, resolve, reject)
      },
      fail: () => {
        // 部分平台 getFileInfo 不可用，跳过大小校验直接上传
        doUpload(filePath, options, resolve, reject)
      }
    })
  })
}

function doUpload(filePath, options, resolve, reject) {
  if (options.loading !== false) {
    uni.showLoading({ title: '上传中...', mask: true })
  }

  const token = uni.getStorageSync(TOKEN_KEY)
  const sign = signRequest('POST', UPLOAD_URL)

  uni.uploadFile({
    url: BASE_URL + UPLOAD_URL,
    filePath,
    name: 'file',
    header: {
      'Authorization': token ? `Bearer ${token}` : '',
      'X-Timestamp': sign.timestamp,
      'X-Nonce': sign.nonce,
      'X-Signature': sign.signature
    },
    success: (res) => {
      if (res.statusCode !== 200) {
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(res)
        return
      }
      let body
      try {
        body = JSON.parse(res.data)
      } catch (e) {
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(e)
        return
      }
      if (body.code === 200 && body.data && body.data.url) {
        resolve(body.data.url)
      } else {
        uni.showToast({ title: body.message || '上传失败', icon: 'none' })
        reject(body)
      }
    },
    fail: (err) => {
      uni.showToast({ title: '网络连接失败', icon: 'none' })
      reject(err)
    },
    complete: () => {
      if (options.loading !== false) {
        uni.hideLoading()
      }
    }
  })
}
