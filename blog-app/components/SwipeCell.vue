<template>
  <!--
    根容器：relative + overflow hidden，裁剪拖出容器的部分。
    touch 事件绑在根上（而非内容层），从露出的操作区往右滑也能收回
  -->
  <view
    class="swipe-cell"
    @touchstart="onTouchStart"
    @touchmove="onTouchMove"
    @touchend="onTouchEnd"
    @touchcancel="onTouchCancel"
  >
    <!-- 右侧操作层：默认被内容层盖住，左滑后露出，宽 = actionWidth -->
    <view class="swipe-actions" :style="{ width: actionWidth + 'px' }">
      <!--
        操作按钮由使用方通过 #actions 插槽传入，例如：
        <SwipeCell ref="cellRef">
          <template #default> ...条目内容... </template>
          <template #actions>
            <view class="del-btn" @click="onDelete(item)">删除</view>
          </template>
        </SwipeCell>
      -->
      <slot name="actions"></slot>
    </view>

    <!-- 内容层：z-index 高于操作层，负向 translateX 露出操作区 -->
    <view
      :class="['swipe-content', dragging ? 'dragging' : '']"
      :style="{ transform: 'translateX(' + offset + 'px)' }"
      @click="onContentClick"
    >
      <slot></slot>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  // 右侧操作区总宽度（单位 px）
  actionWidth: { type: Number, default: 72 }
})

// click：点按内容条目；open/close：吸附展开/收起时触发
const emit = defineEmits(['click', 'open', 'close'])

// 内容层水平位移：0 = 收起，-actionWidth = 完全展开
const offset = ref(0)
// 是否处于水平拖动中：拖动中不加 transition，保证内容跟手
const dragging = ref(false)

// ===== 手势局部状态（每次触摸重置，无需响应式）=====
let startX = 0        // 触摸起点 X
let startY = 0        // 触摸起点 Y
let startTime = 0     // 触摸起始时间戳（预留：如需"快速轻扫直接开合"可按耗时判定）
let startOffset = 0   // 手势开始时的 offset
let moved = false     // 手势方向是否已判定（水平拖动 或 纵向滚动，只判定一次，避免斜滑抖动）
let openedAtStart = false // 手势开始时是否展开态：用于"点空白收起"且不触发 click
let lastTouchEndTime = 0  // 最近一次 touchend 时刻：触屏端已在 touchend 处理点击，
                          // 随后系统补发的合成 click 落在该时间戳 350ms 内则吞掉，避免重复 emit
// ==============================

// 把位移限制在 [-actionWidth, 0]
const clamp = (v) => Math.max(-props.actionWidth, Math.min(0, v))

// 触摸开始：记录起点与初始状态
const onTouchStart = (e) => {
  const touch = e.touches[0]
  startX = touch.clientX
  startY = touch.clientY
  startTime = Date.now()
  startOffset = offset.value
  moved = false
  // 展开态下的触摸先按"点击空白收起"预期处理（见 onTouchEnd）
  openedAtStart = offset.value < 0
}

// 触摸移动：判定方向后，仅水平主导时进入拖动
const onTouchMove = (e) => {
  const touch = e.touches[0]
  const dx = touch.clientX - startX
  const dy = touch.clientY - startY

  // 首次位移超过 8px 才判定方向，且只判定一次
  if (!moved) {
    if (Math.abs(dx) > 8 && Math.abs(dx) > Math.abs(dy)) {
      // 水平位移主导 → 进入拖动
      moved = true
      dragging.value = true
    } else if (Math.abs(dy) > 8 && Math.abs(dy) > Math.abs(dx)) {
      // 纵向位移主导 → 判定为页面滚动，本手势放弃拖动
      moved = true
    }
  }

  if (dragging.value) {
    // 跟随手指，范围 [-actionWidth, 0]
    offset.value = clamp(startOffset + dx)
    // 拖动中阻止页面纵向滚动冲突（小程序端无此 API，靠 dragging 标记即可）
    // #ifdef H5
    e.preventDefault && e.preventDefault()
    // #endif
  }
}

// 触摸结束：按半宽阈值吸附开/合；未拖动且位移很小视为点击
const onTouchEnd = (e) => {
  lastTouchEndTime = Date.now()
  const touch = e.changedTouches[0]
  const dx = touch.clientX - startX
  const dy = touch.clientY - startY

  if (dragging.value) {
    // 松手吸附：越过操作区一半宽度 → 展开，否则收起
    dragging.value = false
    if (offset.value <= -props.actionWidth / 2) open()
    else close()
  } else if (Math.abs(dx) < 10 && Math.abs(dy) < 10) {
    // 本次从未水平拖动且位移小于 10px → 视为点击
    if (openedAtStart) {
      // 展开态下点击内容：仅收起，不向外抛 click（即阻止冒泡到条目点击）
      close()
    } else {
      emit('click')
    }
  }
}

// 触摸被系统打断（来电/弹窗等）：拖动中则复位并收起，避免卡在半开状态
const onTouchCancel = () => {
  lastTouchEndTime = Date.now()
  if (dragging.value) {
    dragging.value = false
    close()
  }
}

// 内容层 click：仅"纯鼠标点击"（无 touch 序列，如 H5 桌面端）走到这里；
// 触屏端的点击已在 touchend 处理过，这里吞掉系统补发的合成 click
const onContentClick = () => {
  if (Date.now() - lastTouchEndTime < 350) return
  emit('click')
}

// 编程展开：内容左移 actionWidth，露出操作区
const open = () => {
  offset.value = -props.actionWidth
  emit('open')
}

// 编程收起：内容归位。
// 页面级"滑开其他条目时自动收起"由父组件维护 ref 列表调用本方法实现，
// 组件自身不做全局通信（示例见文件底部注释）
const close = () => {
  offset.value = 0
  emit('close')
}

defineExpose({ open, close })

/*
  ===== 父组件使用示例（稍后阅读列表：滑开一条自动收起其他条目）=====

  <SwipeCell
    v-for="(item, i) in list"
    :key="item.id"
    :ref="(el) => (cellRefs[i] = el)"
    @open="onCellOpen(i)"
    @click="goDetail(item)"
  >
    <ArticleItem :article="item" />
    <template #actions>
      <view class="del-btn" @click="onDelete(item)">删除</view>
    </template>
  </SwipeCell>

  const cellRefs = ref([])   // 每个 SwipeCell 的组件实例
  const onCellOpen = (i) => {
    // 某条目展开时，通过 ref 收起其他所有条目（组件自身不做全局通信）
    cellRefs.value.forEach((cell, j) => {
      if (j !== i && cell && cell.close) cell.close()
    })
  }
  ==================================================================
*/
</script>

<style lang="scss" scoped>
/* 根容器：relative + overflow hidden 裁剪右侧操作区（操作层定位在容器内） */
.swipe-cell {
  position: relative;
  overflow: hidden;
}

/* 右侧操作层：贴容器右缘、占满高度、宽度由 actionWidth 行内样式控制 */
.swipe-actions {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
}

/* 内容层：盖在操作层之上；松手/编程开合时平滑过渡 */
.swipe-content {
  position: relative;
  z-index: 1;
  transition: transform 0.25s ease;
}

/* 拖动中禁用过渡，保证内容层跟手 */
.swipe-content.dragging {
  transition: none;
}

/* ===== 使用方按钮参考样式（写在页面里，非组件内）=====
   按钮应撑满操作区高度，例如删除按钮：
   .del-btn {
     width: 100%;
     height: 100%;
     display: flex;
     align-items: center;
     justify-content: center;
     background: $color-danger;
     color: #fff;
     font-size: 14px;
   }
   ============================================ */
</style>
