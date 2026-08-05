# 简历管理功能重构 Spec

## Why
当前 `blog-admin` 的简历管理页（`ResumeEdit.vue`）使用体验差：① 缺失性别、出生日期（实体已有但表单未呈现）、婚姻状况等招聘网站标配字段；② 技能、工作经历、项目经验、教育背景四个核心模块是原始 JSON 文本框，用户需手写 `[{"company":"...","position":"..."}]`，极易出错；③ 前台 `blog-frontend` 和 `blog-app` 的简历展示页也未展示新增字段。需参考 BOSS 直聘 / 智联招聘等成熟招聘网站的简历结构，对四个项目（admin / backend / frontend / app）的简历功能进行统一重构。

## What Changes

### 后端 `blog-backend`
- **数据库 `resume_info` 表新增字段**（提供 ALTER TABLE SQL，不重建表）：
  - `marital_status` tinyint — 婚姻状况（0未婚 1已婚 2离异）
  - `work_years` int — 工作年限
  - `expected_salary` varchar(50) — 期望薪资
  - `highest_education` varchar(20) — 最高学历（高中/大专/本科/硕士/博士）
  - `job_search_status` tinyint — 求职状态（0离职-随时到岗 1在职-暂不流动 2在职-考虑机会）
  - `hukou` varchar(100) — 户籍所在地
  - `self_evaluation` text — 自我评价（与个人简介 summary 区分）
  - `certificates` text — 证书荣誉（JSON 数组）
  - `interests` varchar(500) — 兴趣爱好
- **`ResumeInfo` 实体类**同步新增上述字段（类型与数据库对应）。
- Controller 层 `AdminResumeController` / `PortalResumeController` 无需改动（直接透传实体）。

### 后台 `blog-admin`（`ResumeEdit.vue` 重点重构）
- **基本信息区**：姓名、性别（el-radio 0男/1女）、出生日期（el-date-picker）、婚姻状况（el-select）、户籍、电话、邮箱、现居地址、照片上传
- **求职意向区**：求职岗位、求职状态（el-select）、期望薪资、工作年限、最高学历（el-select）
- **个人简介区**：summary textarea
- **自我评价区**：selfEvaluation textarea（新增）
- **技能特长区**：结构化动态列表，每行 [技能名、熟练度(select 精通/熟练/掌握/了解)、百分比(slider)]，支持新增/删除行，保存时序列化为 JSON
- **工作经历区**：结构化动态列表，每行 [公司、职位、开始月份、结束月份、工作描述]，支持新增/删除
- **项目经验区**：结构化动态列表，每行 [项目名、担任角色、项目时间、项目描述、技术栈(input+回车 tag)]，支持新增/删除
- **教育背景区**：结构化动态列表，每行 [学校、专业、学历(select)、开始月份、结束月份、描述]，支持新增/删除
- **证书荣誉区**（新增）：结构化动态列表，每行 [证书名称、颁发机构、获得日期]，支持新增/删除
- **兴趣爱好区**（新增）：interests 文本输入
- 加载时把后端返回的 JSON 字符串解析为数组绑定到表单；保存时把数组序列化为 JSON 字符串发回后端。

### 前台 `blog-frontend`（`Resume.vue`）
- 头部信息区补充展示：性别、出生日期（算年龄）、婚姻状况、工作年限、最高学历、求职状态、期望薪资、户籍
- 新增「自我评价」卡片（与个人简介分开展示）
- 新增「证书荣誉」卡片（时间线展示）
- 新增「兴趣爱好」展示
- 移除 `defaultResume` 示例数据回退逻辑（真实博客不应展示假数据，无数据时显示空状态提示）

### 移动端 `blog-app`（`pages/resume/index.vue`）
- Hero 卡片下方补充基本信息行：性别/年龄/婚姻/工作年限/学历/求职状态
- 新增「自我评价」「证书荣誉」「兴趣爱好」section
- 移除无数据时的静默空页面，改为友好空状态提示

## Impact
- Affected specs: 无（首次重构简历模块）
- Affected code:
  - 后端：`ResumeInfo.java`、`sql/create_sql.sql`（追加 ALTER TABLE 段）
  - 后台：`src/views/ResumeEdit.vue`（大幅重写）、`src/api/resume.js`（无改动，沿用 save 接口）
  - 前台：`src/views/Resume.vue`（补充字段展示 + 新 section）
  - 移动端：`pages/resume/index.vue`（补充字段展示 + 新 section）
- 兼容性：数据库用 ALTER TABLE 增量加列，旧数据不受影响；后端接口签名不变（实体扩展字段，旧前端忽略新字段）；JSON 字段格式与现有保持一致（skills/workExperience/projects/education 数组结构不变），仅 certificates 为新增 JSON 字段。

## ADDED Requirements

### Requirement: 简历扩展字段
系统 SHALL 在 `resume_info` 表中支持婚姻状况、工作年限、期望薪资、最高学历、求职状态、户籍、自我评价、证书荣誉、兴趣爱好等招聘网站标配字段。

#### Scenario: 保存含新字段的简历
- **WHEN** 用户在后台填写婚姻状况=已婚、工作年限=5、期望薪资=20-30K、最高学历=本科并保存
- **THEN** 数据库对应字段写入正确值，重新加载时表单回显这些字段

#### Scenario: 旧数据兼容
- **WHEN** 数据库已有旧简历记录（新字段为 NULL）
- **THEN** 后台加载时新字段表单项为空/默认值，不影响保存

### Requirement: 后台结构化简历编辑
`blog-admin` 的 `ResumeEdit.vue` SHALL 用结构化动态表单替代原始 JSON 文本框，用户无需手写 JSON。

#### Scenario: 添加工作经历
- **WHEN** 用户点击「工作经历」区的「新增」按钮
- **THEN** 出现一行包含公司、职位、开始月份、结束月份、描述的输入项

#### Scenario: 删除技能
- **WHEN** 用户点击某技能行的「删除」按钮
- **THEN** 该行从列表移除，保存时不包含该项

#### Scenario: 保存时自动序列化
- **WHEN** 用户填写完结构化表单点击保存
- **THEN** 前端将 skills/workExperience/projects/education/certificates 数组序列化为 JSON 字符串提交，后端原样存入对应 text 字段

#### Scenario: 加载时自动反序列化
- **WHEN** 后台打开简历编辑页，后端返回的 skills 字段为 `[{"name":"Java","level":"精通","percent":90}]`
- **THEN** 表单渲染一行技能：名称=Java、熟练度=精通、百分比=90

### Requirement: 前台简历展示新字段
`blog-frontend` 和 `blog-app` SHALL 展示新增的简历字段。

#### Scenario: 展示基本信息扩展
- **WHEN** 简历数据含 gender=0、birthDate=1995-06、maritalStatus=0、workYears=5
- **THEN** 前台展示「男 / 30岁 / 未婚 / 5年经验」等摘要信息

#### Scenario: 展示自我评价
- **WHEN** 简历 self_evaluation 字段非空
- **THEN** 前台显示「自我评价」卡片，内容为该字段文本

#### Scenario: 无数据空状态
- **WHEN** 后端返回的简历数据为 null
- **THEN** 前台显示「暂无简历信息」空状态，不展示假数据

## MODIFIED Requirements

### Requirement: 简历实体字段
`ResumeInfo` 实体在原有 name/jobTitle/gender/birthDate/phone/email/address/avatar/summary/skills/workExperience/education/projects 基础上，新增 maritalStatus/workYears/expectedSalary/highestEducation/jobSearchStatus/hukou/selfEvaluation/certificates/interests 字段，类型与数据库列对应。

## REMOVED Requirements
无（不删除现有功能，仅扩展和重构 UI）
