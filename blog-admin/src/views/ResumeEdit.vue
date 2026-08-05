<template>
  <div class="resume-edit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>简历管理</span>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </div>
      </template>
      
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="求职岗位">
              <el-input v-model="form.jobTitle" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电话">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        
        <el-form-item label="照片">
          <Upload v-model="form.avatar" placeholder="上传照片" />
        </el-form-item>
        
        <el-form-item label="个人简介">
          <el-input v-model="form.summary" type="textarea" :rows="4" />
        </el-form-item>
        
        <el-divider>技能特长 (JSON格式)</el-divider>
        <el-form-item label="技能特长">
          <el-input v-model="form.skills" type="textarea" :rows="6" placeholder='[{"name":"Java","level":"熟练","percent":90}]' />
        </el-form-item>
        
        <el-divider>工作经历 (JSON格式)</el-divider>
        <el-form-item label="工作经历">
          <el-input v-model="form.workExperience" type="textarea" :rows="6" placeholder='[{"company":"公司","position":"职位","startDate":"2020-01","endDate":"至今","description":"描述"}]' />
        </el-form-item>
        
        <el-divider>项目经验 (JSON格式)</el-divider>
        <el-form-item label="项目经验">
          <el-input v-model="form.projects" type="textarea" :rows="6" placeholder='[{"name":"项目名","role":"角色","date":"2023-01","description":"描述","technologies":["Java","Vue"]}]' />
        </el-form-item>
        
        <el-divider>教育背景 (JSON格式)</el-divider>
        <el-form-item label="教育背景">
          <el-input v-model="form.education" type="textarea" :rows="6" placeholder='[{"school":"学校","major":"专业","degree":"本科","startDate":"2016-09","endDate":"2020-06","description":"描述"}]' />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import resumeApi from '@/api/resume'
import Upload from '@/components/Upload.vue'

const form = reactive({
  name: '',
  jobTitle: '',
  phone: '',
  email: '',
  address: '',
  avatar: '',
  summary: '',
  skills: '',
  workExperience: '',
  projects: '',
  education: ''
})

const fetchResume = async () => {
  const res = await resumeApi.getResume()
  if (res.data) {
    Object.assign(form, res.data)
  }
}

const handleSave = async () => {
  try {
    await resumeApi.save(form)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => fetchResume())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>