<template>
  <div class="upload-container">
    <el-upload
      :show-file-list="false"
      :http-request="doUpload"
      :before-upload="beforeUpload"
      :accept="accept"
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
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import request from '@/api/request'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  action: {
    type: String,
    default: '/v1/storage/upload'
  },
  accept: {
    type: String,
    default: 'image/*'
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

const doUpload = async ({ file, onSuccess, onError }) => {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post(props.action, formData)
    const url = res.data?.url
    if (url) {
      emit('update:modelValue', url)
      emit('success', url)
      ElMessage.success('上传成功')
    } else {
      ElMessage.error('上传失败')
    }
    onSuccess(res)
  } catch (err) {
    onError(err)
  }
}

const beforeUpload = (file) => {
  const isLtMaxSize = file.size / 1024 / 1024 < props.maxSize
  if (!isLtMaxSize) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
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
  border: 1px dashed var(--border-color, #d9d9d9);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--text-secondary, #909399);
  cursor: pointer;
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: var(--primary-color, #409eff);
  color: var(--primary-color, #409eff);
}

.upload-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
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
