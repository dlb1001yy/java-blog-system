<template>
  <PageContainer title="系统设置" description="管理网站配置、密码与上传">
    <div class="content-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="网站配置" name="site">
          <el-form :model="siteConfig" label-width="120px" style="max-width: 600px;">
            <el-form-item label="网站名称">
              <el-input v-model="siteConfig.blogName" />
            </el-form-item>
            <el-form-item label="网站描述">
              <el-input v-model="siteConfig.blogDescription" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="网站域名">
              <el-input v-model="siteConfig.blogDomain" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveSite">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="120px" style="max-width: 600px;">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="文件上传" name="upload">
          <el-form :model="uploadConfig" label-width="120px" style="max-width: 600px;">
            <el-form-item label="上传路径">
              <el-input v-model="uploadConfig.uploadPath" />
            </el-form-item>
            <el-form-item label="允许类型">
              <el-input v-model="uploadConfig.allowedTypes" />
            </el-form-item>
            <el-form-item label="最大大小(MB)">
              <el-input-number v-model="uploadConfig.maxSize" :min="1" :max="100" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveUpload">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import authApi from '@/api/auth'
import request from '@/api/request'
import PageContainer from '@/components/PageContainer.vue'

const activeTab = ref('site')
const pwdFormRef = ref()

const siteConfig = reactive({
  blogName: 'Java码农笔记',
  blogDescription: '记录Java学习之路，分享技术心得',
  blogDomain: 'http://localhost:8080'
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const uploadConfig = reactive({
  uploadPath: './uploads/',
  allowedTypes: 'jpg,jpeg,png,gif,webp',
  maxSize: 10
})

const handleSaveSite = async () => {
  try {
    await request.put('/admin/config/site', siteConfig)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleChangePassword = async () => {
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await authApi.changePassword({
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码修改成功')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } catch (error) {
      ElMessage.error('密码修改失败')
    }
  })
}

const handleSaveUpload = async () => {
  try {
    await request.put('/admin/config/upload', uploadConfig)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped>
.content-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  transition: box-shadow var(--transition-base);
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 4px rgba(99, 102, 241, 0.1);
}
:deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  transition: box-shadow var(--transition-base);
}
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 4px rgba(99, 102, 241, 0.1);
}

:deep(.el-tabs__item) {
  font-size: var(--font-base);
  font-weight: 500;
}
:deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
  height: 3px;
  border-radius: var(--radius-full);
}
:deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}
:deep(.el-tabs__item:hover) {
  color: var(--color-primary-light);
}
</style>