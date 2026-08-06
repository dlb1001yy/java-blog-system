# Gitee Wiki 生成 Spec

## Why

项目已上传到 Gitee，需要为项目生成 Wiki 文档。Gitee Wiki 使用独立的 git 仓库（`项目名.wiki.git`），需要先在网页端初始化，再克隆 wiki 仓库、添加 Markdown 文件并推送。当前项目根目录有 4 个文档可作为 Wiki 内容来源，需要整理为 Wiki 格式并提供操作指南。

## What Changes

- **新增** `Gitee-Wiki生成指南.md`，包含完整的 Gitee Wiki 创建与推送步骤
- **新增** `wiki/` 目录，包含预整理的 Wiki 页面文件：
  - `Home.md` — Wiki 首页（基于 README.md 精简，含项目简介与导航）
  - `Docker环境安装.md` — Docker 安装指南（基于 Docker环境安装指南.md）
  - `部署操作手册.md` — 部署操作（基于 部署操作手册.md）
  - `项目开发文档.md` — 开发文档合集（基于 项目开发文档合集.md）

## Impact

- Affected code: `Gitee-Wiki生成指南.md`（新建）、`wiki/` 目录（新建）

## ADDED Requirements

### Requirement: Gitee Wiki 生成指南

系统 SHALL 在根目录提供 `Gitee-Wiki生成指南.md`，涵盖 Gitee Wiki 初始化、克隆 wiki 仓库、复制 Wiki 内容、推送更新的完整步骤，所有命令可直接复制执行。

### Requirement: Wiki 内容文件

系统 SHALL 在 `wiki/` 目录提供 4 个预整理的 Markdown 文件，文件名符合 Gitee Wiki 规范（Home.md 为首页），内容基于项目现有文档整理。

#### Scenario: 用户生成 Gitee Wiki
- **WHEN** 用户按照 `Gitee-Wiki生成指南.md` 逐步执行
- **THEN** 能够在 Gitee 项目页面看到包含 4 个页面的 Wiki
