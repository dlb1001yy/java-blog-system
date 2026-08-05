# 简历页面空数据时展示示例数据

## 问题分析

**当前问题**:
1. 模板使用 `v-if="resume"` 控制整体显示，当后端返回 `null` 或空数据时，页面空白
2. 各模块（技能、工作经历、项目经验、教育背景）使用 `v-if` 判断，空数据时模块不显示
3. 用户体验差，页面看起来像是加载失败

## 修复方案

**修改文件**: `blog-admin/src/views/Resume.vue`

**修改内容**:

### 1. 添加默认示例数据常量（在 `<script setup>` 中）

```js
// 默认示例数据（后端无数据时展示）
const defaultResume = {
  name: '张三',
  jobTitle: 'Java开发工程师',
  phone: '138-0000-0000',
  email: 'zhangsan@example.com',
  address: '北京市朝阳区',
  avatar: '',
  summary: '5年Java开发经验，熟练掌握Spring Boot、微服务架构、MySQL、Redis等技术栈。具备良好的编码习惯和团队协作能力，善于解决技术难题。',
  skills: JSON.stringify([
    { name: 'Java', level: '精通', percent: 90, color: '#67C23A' },
    { name: 'Spring Boot', level: '精通', percent: 85, color: '#409EFF' },
    { name: 'MySQL', level: '熟练', percent: 80, color: '#E6A23C' },
    { name: 'Redis', level: '熟练', percent: 75, color: '#F56C6C' },
    { name: 'Vue.js', level: '熟悉', percent: 70, color: '#909399' }
  ]),
  workExperience: JSON.stringify([
    {
      company: 'ABC科技有限公司',
      position: '高级Java开发工程师',
      startDate: '2021-03',
      endDate: '至今',
      description: '负责核心业务系统的架构设计和开发，优化系统性能，带领小组完成多个重要项目。'
    },
    {
      company: 'XYZ互联网公司',
      position: 'Java开发工程师',
      startDate: '2019-07',
      endDate: '2021-02',
      description: '参与电商平台的开发与维护，负责订单模块、支付模块的功能迭代。'
    }
  ]),
  projects: JSON.stringify([
    {
      name: '企业级博客系统',
      role: '核心开发',
      date: '2023-01',
      description: '基于Spring Boot + Vue.js开发的全栈博客系统，支持文章管理、评论、标签分类等功能。',
      technologies: ['Spring Boot', 'Vue.js', 'MySQL', 'Redis', 'MyBatis-Plus']
    }
  ]),
  education: JSON.stringify([
    {
      school: '某某大学',
      major: '计算机科学与技术',
      degree: '本科',
      startDate: '2015-09',
      endDate: '2019-06',
      description: ''
    }
  ])
}
```

### 2. 修改 `fetchResume` 方法，空数据时使用默认数据

```js
const fetchResume = async () => {
  try {
    const res = await articleApi.getResume()
    // 如果后端返回空数据，使用示例数据
    resume.value = res.data || defaultResume
  } catch {
    // 请求失败也使用示例数据
    resume.value = defaultResume
  }
}
```

### 3. 移除模板中的 `v-if` 条件（或保留但对默认数据不再生效）

由于默认数据包含所有字段内容，移除 `v-if` 后也能正常展示示例数据。

## 验证步骤

1. 后端无数据时（返回null），访问简历页面应展示示例数据
2. 后端有正常数据时，展示真实数据
3. 后端接口异常时，展示示例数据
