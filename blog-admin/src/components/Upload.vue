<template>
  <div class="upload-container">
    <el-upload
      :action="action"
      :headers="headers"
      :show-file-list="false"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :accept="accept"
      :limit="limit"
    >
      <div v-if="!modelValue" class="upload-placeholder">
        <el-icon :size="28"><Plus /></el-icon>
        <span>{{ placeholder }}</span>
      </div>
      <div v-else class="upload-preview">
        <img :src="modelValue" class="preview-image" />
        <div class="preview-overlay">
          <el-icon @click.stop="handleRemove"><Delete /></el-icon>
        </div>
      </div>
    </el-upload>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { signRequest } from '@/api/signing'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  action: {
    type: String,
    default: '/api/admin/upload'
  },
  accept: {
    type: String,
    default: 'image/*'
  },
  limit: {
    type: Number,
    default: 1
  },
  placeholder: {
    type: String,
    default: '上传图片'
  },
  maxSize: {
    type: Number,
    default: 10 // MB
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'remove'])

// el-upload 走自身 XHR，不经过 axios 拦截器，需手动附加 token 与请求签名头
const headers = computed(() => {
  const { timestamp, nonce, signature } = signRequest('POST', props.action)
  return {
    Authorization: `Bearer ${localStorage.getItem('admin_token')}`,
    'X-Timestamp': timestamp,
    'X-Nonce': nonce,
    'X-Signature': signature
  }
})

const beforeUpload = (file) => {
  const isLtMaxSize = file.size / 1024 / 1024 < props.maxSize
  if (!isLtMaxSize) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

const handleSuccess = (response) => {
  if (response.code === 200) {
    emit('update:modelValue', response.data)
    emit('success', response.data)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleError = () => {
  ElMessage.error('上传失败，请重试')
}

const handleRemove = () => {
  emit('update:modelValue', '')
  emit('remove')
}
</script>

<style scoped>
.upload-container {
  display: inline-block;
}

.upload-placeholder {
  width: 200px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #909399;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-preview {
  position: relative;
  width: 200px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: #fff;
  font-size: 24px;
  cursor: pointer;
}

.upload-preview:hover .preview-overlay {
  opacity: 1;
}
</style>