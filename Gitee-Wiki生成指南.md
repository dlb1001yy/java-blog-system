# Gitee Wiki 生成指南

> 本文档介绍如何为 Gitee 上的项目创建 Wiki，并将 `wiki/` 目录中的预整理文档推送到 Gitee Wiki。

---

## 一、Gitee Wiki 简介

Gitee Wiki 是项目文档的独立知识库，与代码仓库分离。每个 Gitee 项目都有一个独立的 Wiki 仓库（`项目名.wiki.git`），通过 git 推送 Markdown 文件来更新 Wiki 内容。

**Wiki 与仓库文档的区别：**

| 特性 | 仓库文档 | Gitee Wiki |
|------|----------|------------|
| 存储位置 | 项目代码仓库内 | 独立的 wiki 仓库 |
| 访问方式 | 浏览代码文件 | 项目页面「Wiki」标签 |
| 文件格式 | 任意 | Markdown（.md） |
| 首页 | 无固定首页 | `Home.md` 为首页 |
| 版本管理 | 跟随代码提交 | 独立提交历史 |

---

## 二、前置条件

1. 项目已上传到 Gitee（已有 Gitee 仓库）
2. 本地已安装 Git
3. 已准备好 `wiki/` 目录中的 4 个 Markdown 文件：
   - `Home.md` — Wiki 首页
   - `Docker环境安装.md` — Docker 安装指南
   - `部署操作手册.md` — 部署操作手册
   - `项目开发文档.md` — 项目开发文档合集

---

## 三、初始化 Gitee Wiki

### 步骤 1：在 Gitee 网页端初始化 Wiki

1. 打开浏览器，访问你的 Gitee 项目页面
2. 点击顶部导航栏的 **「Wiki」** 标签
3. 如果是第一次使用，会看到「初始化 Wiki」按钮，点击它
4. 系统会自动创建一个默认的 `Home.md` 页面
5. Wiki 初始化完成

> **注意**：必须先在网页端初始化 Wiki，否则 wiki 仓库不存在，无法克隆。

### 步骤 2：获取 Wiki 仓库地址

1. 在 Gitee 项目页面，点击 **「Wiki」** 标签
2. 点击页面右侧的 **「克隆/下载」** 按钮（或「管理」→「Wiki 仓库地址」）
3. 复制 Wiki 仓库的 HTTPS 地址，格式类似：
   ```
   https://gitee.com/<你的用户名>/<项目名>.wiki.git
   ```

> **示例**：如果你的项目地址是 `https://gitee.com/zhangsan/java-blog`，则 Wiki 仓库地址是 `https://gitee.com/zhangsan/java-blog.wiki.git`

---

## 四、克隆 Wiki 仓库并推送内容

### 步骤 1：克隆 Wiki 仓库

在本地选择一个目录（不要在项目目录内），执行：

```bash
# 替换为你的 Wiki 仓库地址
git clone https://gitee.com/<你的用户名>/<项目名>.wiki.git
```

### 步骤 2：复制 Wiki 内容

将项目 `wiki/` 目录中的 4 个文件复制到克隆的 wiki 仓库中：

```bash
# 进入 wiki 仓库目录
cd <项目名>.wiki

# 复制文件（路径替换为你的实际项目路径）
cp /d/my-project/java-blog-system/wiki/Home.md .
cp /d/my-project/java-blog-system/wiki/Docker环境安装.md .
cp /d/my-project/java-blog-system/wiki/部署操作手册.md .
cp /d/my-project/java-blog-system/wiki/项目开发文档.md .

# 查看文件
ls -la
# 预期看到：Home.md  Docker环境安装.md  部署操作手册.md  项目开发文档.md
```

> **Windows 用户**：使用资源管理器或以下命令复制：
> ```cmd
> copy "D:\my-project\java-blog-system\wiki\Home.md" .
> copy "D:\my-project\java-blog-system\wiki\Docker环境安装.md" .
> copy "D:\my-project\java-blog-system\wiki\部署操作手册.md" .
> copy "D:\my-project\java-blog-system\wiki\项目开发文档.md" .
> ```

### 步骤 3：推送 Wiki 内容

```bash
# 添加所有文件
git add .

# 提交
git commit -m "添加项目 Wiki 文档：首页、Docker安装、部署手册、开发文档"

# 推送到 Gitee
git push origin master
```

> 如果提示输入账号密码，输入你的 Gitee 用户名和密码（或访问令牌）。

### 步骤 4：验证 Wiki

1. 回到 Gitee 项目页面
2. 点击 **「Wiki」** 标签
3. 应该能看到首页（Home.md 内容）和左侧的页面列表
4. 点击左侧页面名称可切换查看不同文档

---

## 五、后续更新 Wiki

当项目文档有更新时，按以下步骤同步到 Gitee Wiki：

### 方法一：修改 wiki 仓库后推送

```bash
# 进入 wiki 仓库目录
cd <项目名>.wiki

# 编辑文件（例如更新部署手册）
# 可以用编辑器直接修改 部署操作手册.md

# 提交并推送
git add .
git commit -m "更新部署操作手册"
git push origin master
```

### 方法二：从项目目录同步更新

```bash
# 1. 先在项目目录中更新 wiki/ 下的文件
# 2. 复制到 wiki 仓库
cp /d/my-project/java-blog-system/wiki/部署操作手册.md <项目名>.wiki/

# 3. 提交并推送
cd <项目名>.wiki
git add .
git commit -m "同步更新部署操作手册"
git push origin master
```

### 方法三：在 Gitee 网页端直接编辑

1. 打开 Gitee 项目 → **「Wiki」** 标签
2. 点击要编辑的页面
3. 点击右上角 **「编辑」** 按钮
4. 在线编辑 Markdown 内容
5. 点击 **「保存」**

---

## 六、添加新 Wiki 页面

### 方法一：通过文件推送

1. 在 `wiki/` 目录创建新的 `.md` 文件
2. 复制到 wiki 仓库并推送：

```bash
cp /d/my-project/java-blog-system/wiki/新页面.md <项目名>.wiki/
cd <项目名>.wiki
git add .
git commit -m "添加新 Wiki 页面：新页面"
git push origin master
```

### 方法二：在网页端创建

1. 打开 Gitee 项目 → **「Wiki」** 标签
2. 点击 **「新建页面」** 按钮
3. 输入页面标题和内容
4. 点击 **「保存」**

---

## 七、常见问题

### Q1：找不到「Wiki」标签

**原因**：Gitee 免费版可能默认不显示 Wiki。

**解决**：
1. 进入项目 → **「管理」** → **「功能设置」**
2. 确认「Wiki」功能已开启
3. 如果没有此选项，检查你的 Gitee 账户类型

### Q2：克隆 Wiki 仓库提示 403 或需要密码

**原因**：Wiki 仓库需要 Gitee 账号认证。

**解决**：
- 使用 Gitee 用户名和密码登录
- 如果开启了二次验证，需要使用**访问令牌**代替密码
  1. 访问 https://gitee.com/profile/personal_access_tokens
  2. 生成新令牌，勾选 `projects` 权限
  3. 克隆时用令牌代替密码

### Q3：推送大文件失败

**原因**：`项目开发文档.md` 文件较大（~315KB），可能超出 Gitee 单文件限制。

**解决**：
- Gitee Wiki 单文件限制通常为 1MB，315KB 在限制内
- 如果仍然失败，尝试拆分大文件为多个小页面
- 或者将大文档放在项目仓库中，Wiki 中只放链接

### Q4：Wiki 页面间如何链接

Gitee Wiki 支持页面间链接，使用文件名（不含 `.md`）作为链接：

```markdown
[部署操作手册](部署操作手册)
[Docker环境安装](Docker环境安装)
```

### Q5：Wiki 页面不显示目录

Gitee Wiki 会自动根据 Markdown 标题生成页面目录。确保：
- 使用标准的 `#`、`##`、`###` 标题层级
- 标题前后有空行
- 不要在代码块内使用 `#` 开头的行

### Q6：如何删除 Wiki 页面

```bash
# 在 wiki 仓库中删除文件
cd <项目名>.wiki
git rm 要删除的页面.md
git commit -m "删除 Wiki 页面"
git push origin master
```

---

## 八、Wiki 文件清单

本项目 `wiki/` 目录包含以下 4 个文件：

| 文件名 | 内容 | 源文件 |
|--------|------|--------|
| `Home.md` | Wiki 首页：项目简介、架构图、技术栈、快速开始、文档导航 | README.md（精简） |
| `Docker环境安装.md` | Ubuntu 22.04 安装 Docker 的完整步骤 | Docker环境安装指南.md |
| `部署操作手册.md` | 从镜像拉取到访问测试的完整部署流程 | 部署操作手册.md |
| `项目开发文档.md` | 架构设计、后端/前端/移动端完整代码与问题修复 | 项目开发文档合集.md |

---

## 九、快速操作汇总

```bash
# 1. 克隆 wiki 仓库
git clone https://gitee.com/<用户名>/<项目名>.wiki.git

# 2. 进入 wiki 仓库
cd <项目名>.wiki

# 3. 复制 wiki 文件
cp /d/my-project/java-blog-system/wiki/*.md .

# 4. 提交并推送
git add .
git commit -m "初始化项目 Wiki"
git push origin master

# 5. 打开 Gitee 项目 Wiki 页面查看
# https://gitee.com/<用户名>/<项目名>/wiki
```
