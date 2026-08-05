# Checklist

## 后端实体与数据库
- [x] `sql/create_sql.sql` 末尾有 ALTER TABLE 语句新增 9 个字段
- [x] `marital_status tinyint`（婚姻状况）
- [x] `work_years int`（工作年限）
- [x] `expected_salary varchar(50)`（期望薪资）
- [x] `highest_education varchar(20)`（最高学历）
- [x] `job_search_status tinyint`（求职状态）
- [x] `hukou varchar(100)`（户籍）
- [x] `self_evaluation text`（自我评价）
- [x] `certificates text`（证书荣誉 JSON）
- [x] `interests varchar(500)`（兴趣爱好）
- [x] `ResumeInfo.java` 新增 9 个对应字段（类型正确）
- [x] `mvn compile` 编译通过

## 后台 ResumeEdit.vue 重构
- [x] `form` reactive 包含 9 个新字段
- [x] 有 `skillList`/`workList`/`projectList`/`educationList`/`certificateList` 5 个数组 ref
- [x] 有 `parseJsonArray` 工具函数安全解析 JSON
- [x] `fetchResume` 把 JSON 字符串解析到数组 ref
- [x] `handleSave` 把数组 ref 序列化回 form 字段
- [x] 基本信息区有：姓名、性别(radio)、出生日期(date-picker)、婚姻状况(select)、户籍、电话、邮箱、地址、照片
- [x] 求职意向区有：求职岗位、求职状态(select)、期望薪资、工作年限、最高学历(select)
- [x] 个人简介区有 summary textarea
- [x] 自我评价区有 selfEvaluation textarea
- [x] 技能特长区为动态列表（名称/熟练度/百分比），支持新增删除
- [x] 工作经历区为动态列表（公司/职位/起止月份/描述），支持新增删除
- [x] 项目经验区为动态列表（名称/角色/时间/描述/技术栈 tag），支持新增删除
- [x] 教育背景区为动态列表（学校/专业/学历/起止月份/描述），支持新增删除
- [x] 证书荣誉区为动态列表（证书名/颁发机构/日期），支持新增删除
- [x] 兴趣爱好区有 interests 输入
- [x] 无 VSCode 诊断错误

## 前台 blog-frontend Resume.vue
- [x] 已移除 `defaultResume` 示例数据
- [x] 无数据时显示 el-empty 空状态
- [x] 头部展示性别、年龄、婚姻、工作年限、学历、求职状态、期望薪资、户籍
- [x] 有「自我评价」卡片（selfEvaluation 非空时显示）
- [x] 有「证书荣誉」卡片（certificates 非空时显示，时间线布局）
- [x] 有「兴趣爱好」展示
- [x] 有 genderText/maritalText/jobSearchText/age 等 computed
- [x] 无 VSCode 诊断错误

## 移动端 blog-app resume/index.vue
- [x] Hero 下方有「基本信息」section（性别/年龄/婚姻/工作年限/学历/求职状态/期望薪资/户籍）
- [x] 有「自我评价」section
- [x] 有「证书荣誉」section（时间线卡片）
- [x] 有「兴趣爱好」section
- [x] 有 genderText/maritalText/jobSearchText/age 等 computed
- [x] 无数据时显示空状态
- [x] 无诊断错误

## 端到端验证
- [x] 后端编译通过
- [x] 后台 ResumeEdit.vue 无诊断错误
- [x] 前台 Resume.vue 无诊断错误
- [x] 移动端 resume/index.vue 无诊断错误
- [ ] 后台填写全部分区数据保存后，刷新页面数据正确回显（需用户重启后端 + 执行 ALTER TABLE SQL 后验证）
- [ ] JSON 字段（skills/workExperience/projects/education/certificates）在后台表单与数据库间正确序列化/反序列化（需用户重启后端后验证）
- [ ] 前台简历页展示新增字段与新分区（需用户重启后端后验证）
- [ ] 移动端简历页展示新增字段与新分区（需用户重启后端后验证）
- [ ] 旧数据（新字段为 NULL）加载时不报错（需用户重启后端后验证）
