<template>
  <!-- 分享海报弹层：全屏半透明遮罩 + 居中卡片 -->
  <view v-if="show" class="poster-mask" @click="close">
    <view class="poster-card" @click.stop>
      <text class="poster-title">分享文章</text>

      <!-- 海报生成成功：展示导出的临时图片（widthFix 等比缩放） -->
      <image v-if="posterImg" :src="posterImg" mode="widthFix" style="width: 100%" class="poster-img" />
      <!-- 生成中占位 -->
      <view v-else class="poster-loading">
        <text class="poster-loading-text">海报生成中...</text>
      </view>

      <!-- 底部按钮行 -->
      <view class="poster-actions">
        <view class="poster-btn primary" @click="saveToAlbum">保存到相册</view>
        <view class="poster-btn" @click="copyLink">复制链接</view>
        <view class="poster-btn" @click="close">关闭</view>
      </view>
    </view>

    <!-- 隐藏绘制区：屏幕外 canvas（旧版 Canvas API，仅用于生成海报） -->
    <canvas
      canvas-id="sharePoster"
      :style="{ width: canvasW + 'px', height: canvasH + 'px' }"
      style="position: fixed; left: -9999px; top: 0;"
    />
  </view>
</template>

<script setup>
import { ref, computed, watch, nextTick, getCurrentInstance } from 'vue'
import { BASE_URL, SITE_URL } from '@/common/config.js'
import { createQrMatrix, drawQrToCanvas } from '@/utils/qrcode.js'

const props = defineProps({
  // 文章对象（必传：取 id / title / summary / type / createTime）
  article: { type: Object, required: true },
  // 弹层显隐，支持 v-model:show
  show: { type: Boolean, default: false }
})
const emit = defineEmits(['update:show'])

// 当前组件实例（旧版 Canvas API 需传组件实例以定位组件内 canvas）
const instance = getCurrentInstance()

// 画布尺寸（px）
const canvasW = 340
const canvasH = 560
// 海报临时文件路径（生成成功前为空，弹层显示“生成中”占位）
const posterImg = ref('')

// 文章类型映射（摘要兜底文案用）
const typeMap = { 0: '原创', 1: '转载', 2: '翻译' }

// 日期字符串（yyyy-MM-dd）
const dateStr = computed(() => {
  const t = props.article && props.article.createTime
  return t ? String(t).slice(0, 10) : ''
})

// 文章链接：SITE_URL 非空优先；否则 H5 用当前站点路由，App/小程序回退服务器站点地址
const articleLink = computed(() => {
  const id = (props.article && props.article.id) || ''
  if (SITE_URL) return SITE_URL + id
  let link = ''
  // #ifdef H5
  link = location.origin + '/#/pages/article/detail?id=' + id
  // #endif
  // #ifndef H5
  link = BASE_URL.replace(/\/api$/, '')
  // #endif
  return link
})

// 关闭弹层
const close = () => emit('update:show', false)

// 文本自动换行：逐字累加测宽断行；最多 maxLines 行，超出部分在末行截断加“…”
const wrapText = (ctx, text, maxWidth, maxLines) => {
  const chars = Array.from(String(text || ''))
  const lines = []
  let line = ''
  for (let i = 0; i < chars.length; i++) {
    const next = line + chars[i]
    if (line && ctx.measureText(next).width > maxWidth) {
      lines.push(line)
      if (lines.length === maxLines) {
        // 已到行数上限：在本行内截断加省略号
        let cut = line
        while (cut && ctx.measureText(cut + '…').width > maxWidth) cut = cut.slice(0, -1)
        lines[lines.length - 1] = cut + '…'
        return lines
      }
      line = chars[i]
    } else {
      line = next
    }
  }
  if (line) lines.push(line)
  return lines
}

// 绘制海报（旧版 Canvas API：背景 → 品牌渐变条 → 标题 → 摘要 → 分割线 → 二维码）
const drawPoster = () => {
  const ctx = uni.createCanvasContext('sharePoster', instance.proxy)

  // ① 背景：白色
  ctx.fillStyle = '#FFFFFF'
  ctx.fillRect(0, 0, canvasW, canvasH)

  // ② 顶部 340x120 品牌渐变条 + 站点名 + 日期
  const grad = ctx.createLinearGradient(0, 0, canvasW, 120)
  grad.addColorStop(0, '#4F46E5')
  grad.addColorStop(1, '#06B6D4')
  ctx.fillStyle = grad
  ctx.fillRect(0, 0, canvasW, 120)
  ctx.fillStyle = '#FFFFFF'
  ctx.font = 'bold 18px sans-serif'
  ctx.fillText('Java码农笔记', 24, 52)
  ctx.fillStyle = 'rgba(255,255,255,0.8)'
  ctx.font = '11px sans-serif'
  ctx.fillText(dateStr.value, 24, 80)

  // ③ 标题：16px 加粗，最多 3 行
  ctx.fillStyle = '#0F172A'
  ctx.font = 'bold 16px sans-serif'
  let y = 158
  wrapText(ctx, props.article && props.article.title, canvasW - 48, 3)
    .forEach((line) => { ctx.fillText(line, 24, y); y += 24 })

  // ④ 摘要：12px 最多 4 行（无摘要用类型文案兜底）
  const summary = (props.article && props.article.summary) ||
    `一篇${typeMap[props.article && props.article.type] || '原创'}技术文章，扫码即可阅读全文。`
  ctx.fillStyle = '#64748B'
  ctx.font = '12px sans-serif'
  y += 10
  wrapText(ctx, summary, canvasW - 48, 4)
    .forEach((line) => { ctx.fillText(line, 24, y); y += 19 })

  // ⑤ 分割线
  ctx.fillStyle = '#E2E8F0'
  ctx.fillRect(24, canvasH - 154, canvasW - 48, 1)

  // ⑥ 底部：左侧二维码 + 右侧文案
  const matrix = createQrMatrix(articleLink.value)
  drawQrToCanvas(ctx, matrix, 24, canvasH - 134, 110)
  ctx.fillStyle = '#0F172A'
  ctx.font = '13px sans-serif'
  ctx.fillText('扫码阅读全文', 150, canvasH - 134 + 48)
  ctx.fillStyle = '#94A3B8'
  ctx.font = '10px sans-serif'
  ctx.fillText('Java码农笔记 · 分享技术，记录成长', 150, canvasH - 134 + 70)

  // 绘制完成后延时导出临时文件（等待各端渲染就绪）
  ctx.draw(false, () => {
    setTimeout(() => {
      uni.canvasToTempFilePath({
        canvasId: 'sharePoster',
        success: (res) => { posterImg.value = res.tempFilePath },
        fail: () => uni.showToast({ title: '海报生成失败', icon: 'none' })
      }, instance.proxy)
    }, 300)
  })
}

// 弹层打开时重置并重新生成海报（nextTick + 延时等待 canvas 挂载完成）
watch(() => props.show, async (val) => {
  if (!val) return
  posterImg.value = ''
  await nextTick()
  setTimeout(drawPoster, 100)
})

// 保存到相册：App/小程序走相册 API，H5 提示长按保存
const saveToAlbum = () => {
  if (!posterImg.value) return
  // #ifndef H5
  uni.saveImageToPhotosAlbum({
    filePath: posterImg.value,
    success: () => uni.showToast({ title: '已保存到相册', icon: 'none' }),
    fail: (err) => {
      const msg = (err && err.errMsg) || ''
      if (/auth|deny/i.test(msg)) {
        // 权限被拒：引导去系统设置开启
        uni.showModal({
          title: '需要相册权限',
          content: '请在设置中开启',
          success: (r) => { if (r.confirm) uni.openSetting() }
        })
      } else {
        uni.showToast({ title: '保存失败', icon: 'none' })
      }
    }
  })
  // #endif
  // #ifdef H5
  uni.showToast({ title: '长按海报图片保存', icon: 'none' })
  // #endif
}

// 复制链接（setClipboardData 自带“内容已复制”toast）
const copyLink = () => {
  uni.setClipboardData({ data: articleLink.value })
}
</script>

<style lang="scss" scoped>
/* 遮罩：fixed 全屏半透明黑 */
.poster-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 居中卡片：宽约 300px，超高可滚动 */
.poster-card {
  width: 300px;
  max-height: 80vh;
  overflow-y: auto;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  padding: $spacing-lg;
  box-sizing: border-box;
}

.poster-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  text-align: center;
  margin-bottom: $spacing-md;
}

/* 海报图片：宽度撑满卡片 */
.poster-img {
  display: block;
  width: 100%;
  border-radius: $radius-md;
}

/* 生成中占位 */
.poster-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 240px;
  background: var(--app-bg, #F1F5F9);
  border-radius: $radius-md;
}
.poster-loading-text {
  font-size: 13px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* 底部按钮行 */
.poster-actions {
  display: flex;
  gap: $spacing-sm;
  margin-top: $spacing-lg;
}
/* 胶囊按钮：与详情页操作条风格一致，按下缩放反馈 */
.poster-btn {
  flex: 1;
  height: 36px;
  line-height: 36px;
  text-align: center;
  font-size: 13px;
  border-radius: $radius-full;
  border: 1px solid var(--app-border, #E2E8F0);
  color: var(--app-text-secondary, #64748B);
  transition: transform 0.15s ease;
}
.poster-btn:active {
  transform: scale(0.95);
}
/* 主按钮：主色实底 */
.poster-btn.primary {
  background: $color-primary;
  border-color: $color-primary;
  color: #fff;
}
</style>
