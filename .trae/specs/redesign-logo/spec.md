# Logo 重新设计 Spec

## Why
当前项目使用的 Coffee 图标作为 logo 缺乏辨识度，与"Java码农笔记"项目特色关联不强。需要重新设计一个符合 Java 技术博客特色的 logo，提升品牌识别度。

## What Changes
- 设计新的 SVG logo，融合 Java 元素（如咖啡杯、代码符号、字母 J 等）
- 更新 AppHeader 组件中的 logo 图标
- 更新 favicon.ico 图标
- 保持与项目整体配色协调（主色调 #409eff）

## Impact
- Affected specs: 无
- Affected code: 
  - `blog-admin/src/components/AppHeader.vue` - logo 图标区域
  - `blog-admin/public/favicon.svg` - 网站图标

## ADDED Requirements
### Requirement: 新 Logo 设计
The system SHALL provide a new logo design that reflects the Java technology blog identity.

#### Scenario: Logo 展示
- **WHEN** 用户访问网站首页
- **THEN** 头部导航栏展示新 logo，包含图标和"Java码农笔记"文字
- **AND** 图标应体现 Java/技术/笔记的特色元素

#### Scenario: Favicon 展示
- **WHEN** 用户打开网站标签页
- **THEN** 标签页图标展示与新 logo 一致的简化图标

## MODIFIED Requirements
### Requirement: AppHeader Logo 组件
修改 AppHeader 中的 logo 区域，使用新的 SVG 图标替换 Coffee 图标。

## REMOVED Requirements
### Requirement: Coffee 图标 Logo
**Reason**: 当前 Coffee 图标与项目特色关联不强
**Migration**: 替换为新的 Java/技术主题图标
