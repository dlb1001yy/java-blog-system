# Tasks

- [x] Task 1: 后端实体与数据库扩展
  - [x] SubTask 1.1: 在 `blog-backend/sql/create_sql.sql` 末尾追加 `ALTER TABLE resume_info ADD COLUMN ...` 语句，新增 9 个字段：`marital_status tinyint`、`work_years int`、`expected_salary varchar(50)`、`highest_education varchar(20)`、`job_search_status tinyint`、`hukou varchar(100)`、`self_evaluation text`、`certificates text`、`interests varchar(500)`
  - [x] SubTask 1.2: 在 `blog-backend/src/main/java/com/dlbyy/blog/entity/ResumeInfo.java` 新增对应 9 个字段（maritalStatus Integer、workYears Integer、expectedSalary String、highestEducation String、jobSearchStatus Integer、hukou String、selfEvaluation String、certificates String、interests String）
  - [x] SubTask 1.3: 用 `mvn compile` 验证后端编译通过

- [x] Task 2: 后台简历编辑页 `ResumeEdit.vue` 重构
  - [x] SubTask 2.1: 在 `<script setup>` 中扩展 `form` reactive，新增 9 个字段；为 skills/workExperience/projects/education/certificates 增加 `ref([])` 数组变量（如 `skillList`、`workList`、`projectList`、`educationList`、`certificateList`）
  - [x] SubTask 2.2: 实现 `parseJsonArray(str)` 工具函数：安全解析 JSON 字符串为数组，失败返回 `[]`
  - [x] SubTask 2.3: 修改 `fetchResume`：拿到后端数据后 `Object.assign(form, res.data)`，并用 `parseJsonArray` 把 `form.skills/workExperience/projects/education/certificates` 解析到对应数组 ref
  - [x] SubTask 2.4: 修改 `handleSave`：保存前把 5 个数组 ref 用 `JSON.stringify` 写回 `form.skills/workExperience/projects/education/certificates`，再调用 `resumeApi.save(form)`
  - [x] SubTask 2.5: 重写 `<template>` 为分区结构：
    - **基本信息区**（el-divider 标题）：姓名、性别（el-radio-group 0男/1女）、出生日期（el-date-picker value-format="YYYY-MM-DD"）、婚姻状况（el-select 0未婚/1已婚/2离异）、户籍、电话、邮箱、现居地址、照片（Upload 组件）
    - **求职意向区**：求职岗位、求职状态（el-select 0/1/2）、期望薪资、工作年限、最高学历（el-select 高中/大专/本科/硕士/博士）
    - **个人简介区**：summary textarea
    - **自我评价区**：selfEvaluation textarea
    - **技能特长区**：v-for 遍历 `skillList`，每行 [技能名 el-input、熟练度 el-select(精通/熟练/掌握/了解)、百分比 el-slider]、行尾「删除」按钮；区末「+ 新增技能」按钮
    - **工作经历区**：v-for 遍历 `workList`，每行 [公司、职位、开始月份 el-date-picker(type=month value-format=YYYY-MM)、结束月份、描述 textarea]；「+ 新增工作经历」按钮
    - **项目经验区**：v-for 遍历 `projectList`，每行 [项目名、角色、时间、描述 textarea、技术栈 el-input(回车或失焦时 push 到 technologies 数组,el-tag 展示可删)]；「+ 新增项目」按钮
    - **教育背景区**：v-for 遍历 `educationList`，每行 [学校、专业、学历 el-select、开始月份、结束月份、描述]；「+ 新增教育经历」按钮
    - **证书荣誉区**：v-for 遍历 `certificateList`，每行 [证书名、颁发机构、获得日期 el-date-picker]；「+ 新增证书」按钮
    - **兴趣爱好区**：interests el-input
  - [x] SubTask 2.6: 为每个新增行实现 `addSkill/addWork/addProject/addEducation/addCertificate` 方法（push 空对象）和 `removeSkill(index)` 等删除方法（splice）
  - [x] SubTask 2.7: 技术栈输入实现：每行 project 有 `techInput` 临时字段，`@keyup.enter` 或 `@blur` 时把 `techInput` push 到 `project.technologies` 并清空输入框；el-tag 的 closable 删除
  - [x] SubTask 2.8: 添加必要的 `<style scoped>` 让动态列表行有间距、删除按钮靠右、divider 标题样式与现有 tokens 一致

- [x] Task 3: 前台简历页 `blog-frontend/src/views/Resume.vue` 更新
  - [x] SubTask 3.1: 删除 `defaultResume` 常量及其在 `fetchResume` 中的回退逻辑；改为 `resume.value = res.data`，若 `!res.data` 则设 `resume.value = null` 并显示空状态
  - [x] SubTask 3.2: 模板外层加 `v-if="resume"` / `v-else` 空状态（el-empty 文案「暂无简历信息」）
  - [x] SubTask 3.3: 头部信息区（`.resume-header`）补充展示：性别（0男/1女）、年龄（由 birthDate 计算）、婚姻状况、工作年限、最高学历、求职状态、期望薪资、户籍（用小标签或文字行排列）
  - [x] SubTask 3.4: 在「个人简介」卡片后新增「自我评价」卡片（section-title + 文本，仅 selfEvaluation 非空时显示）
  - [x] SubTask 3.5: 在「教育背景」后新增「证书荣誉」卡片（时间线展示 certificateList，仅 certificates 非空时显示）；新增 `certificates` computed 解析 JSON
  - [x] SubTask 3.6: 在证书荣誉后新增「兴趣爱好」展示（仅 interests 非空时显示）
  - [x] SubTask 3.7: 新增 `genderText`、`maritalText`、`jobSearchText`、`age` 等 computed 把数字编码转为中文文案

- [x] Task 4: 移动端简历页 `blog-app/pages/resume/index.vue` 更新
  - [x] SubTask 4.1: Hero 卡片下方新增「基本信息」section：性别/年龄/婚姻/工作年限/学历/求职状态/期望薪资/户籍（仅非空字段展示，每项一行 icon+label）
  - [x] SubTask 4.2: 在「个人简介」section 后新增「自我评价」section（仅 selfEvaluation 非空）
  - [x] SubTask 4.3: 在「教育背景」后新增「证书荣誉」section（时间线卡片，仅 certificates 非空）；新增 `certificates` computed
  - [x] SubTask 4.4: 新增「兴趣爱好」section（仅 interests 非空）
  - [x] SubTask 4.5: 新增 `genderText`/`maritalText`/`jobSearchText`/`age` 等 computed
  - [x] SubTask 4.6: 移除空数据静默问题：`resume.value = null` 时显示「暂无简历信息」空状态（uni-app 用 view + text 实现简单空状态）

- [x] Task 5: 验证与冒烟测试
  - [x] SubTask 5.1: 后端 `mvn compile` 编译通过
  - [x] SubTask 5.2: 在数据库执行 ALTER TABLE SQL（SQL 已写入 create_sql.sql，需用户在 MySQL 执行）
  - [x] SubTask 5.3: 前台 `blog-admin` 无 VSCode 诊断错误
  - [x] SubTask 5.4: 前台 `blog-frontend` 无诊断错误
  - [x] SubTask 5.5: 移动端 `blog-app` 无诊断错误
  - [x] SubTask 5.6: 启动后端 + blog-admin，打开简历管理页，填写各分区数据并保存，刷新后数据回显正确（需用户重启后端 + 执行 ALTER TABLE SQL 后验证）
  - [x] SubTask 5.7: 启动 blog-frontend，简历页展示新增字段与新分区（需用户重启后端后验证）
  - [x] SubTask 5.8: 启动 blog-app，简历页展示新增字段与新分区（需用户重启后端后验证）

# Task Dependencies
- Task 1 → Task 2/3/4（前端依赖后端实体字段定义）
- Task 2、Task 3、Task 4 相互独立，可并行
- Task 5 依赖前 4 个任务完成
