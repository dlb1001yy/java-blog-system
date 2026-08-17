# Tasks

- [x] Task 1: 清理 blog-admin 死文件与依赖
  - [x] SubTask 1.1: 删除 src/components/HelloWorld.vue、Editor.vue、SvgIcon.vue
  - [x] SubTask 1.2: 删除 src/assets/vite.svg、vue.svg、hero.png
  - [x] SubTask 1.3: 删除 src/style.css
  - [x] SubTask 1.4: 从 package.json 移除 devDependencies.sass 与 dependencies.highlight.js
  - [x] SubTask 1.5: 更新 blog-admin/README.md 中对已删文件的描述
  - [x] SubTask 1.6: 运行 npm install 刷新 lock 文件并执行 npm run build 验证构建成功（2400 modules，exit 0）

- [x] Task 2: 清理 blog-frontend 死文件与依赖
  - [x] SubTask 2.1: 删除 src/api/auth.js
  - [x] SubTask 2.2: 删除 src/assets/vite.svg、vue.svg、hero.png
  - [x] SubTask 2.3: 删除 src/style.css
  - [x] SubTask 2.4: 从 package.json 移除 devDependencies.sass
  - [x] SubTask 2.5: 运行 npm install 刷新 lock 文件并执行 npm run build 验证构建成功（2025 modules，exit 0）

- [x] Task 3: 清理 blog-app 死文件
  - [x] SubTask 3.1: 删除根目录空文件 config.js（零引用，生效配置在 common/config.js）
  - [x] SubTask 3.2: 删除 components/Loading.vue（零引用）

- [x] Task 4: 清理 blog-backend 死代码与历史文档
  - [x] SubTask 4.1: 删除 src/test/java/com/dlbyy/blog/PasswordGenerator.java
  - [x] SubTask 4.2: 删除 blog-backend/.trae/documents/ 目录（9 个历史规划文档）
  - [x] SubTask 4.3: 运行 mvn compile（含 test-compile）验证编译通过（exit 0）；顺带修复 README.md:401 对已删类的悬空引用

- [x] Task 5: 清理根目录一次性产物（用户确认范围：仅 _merge_docs.py）
  - [x] SubTask 5.1: 删除 _merge_docs.py
  - [ ] ~~SubTask 5.2: 删除 .codebuddy/plans/ 目录~~（用户确认保留，不执行）

# Task Dependencies
- Task 1、2、3、4、5 相互独立，已并行执行完成

# 用户决策结果（2026-08-17）
- 执行范围：Task 1 ~ Task 4 全部 + 仅删除 _merge_docs.py
- 明确保留（不清理）：.codebuddy/plans/、wiki/、blog-backend mapper 空 XML、根目录 .trae/documents/、.gitignore 规则
