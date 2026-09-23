# 修复 App 端音乐播放条显示不正确（图标缺失 + 封面 404）

## Summary

截图标红区域为全局音乐播放条（PlayerBar），其"显示不正确"实为**两个独立根因**叠加：

1. **图标全部缺失（代码问题，本次修复）**：App 端（APP-PLUS）不渲染模板中的 `<svg>` 标签，也不支持 `v-html`。编译产物证实 [Icon.vue](file:///d:/my-project/java-blog-system/blog-app/components/Icon.vue) 被编译为 `createElementBlock("svg", {..., innerHTML: path})`，在 App 端原生子类框架下两者均失效。这是**全局问题**：底部 TabBar 四个图标、NavBar 返回箭头、约 13 个文件中的静态内联 SVG 同样不显示（截图中 TabBar 仅剩纯文字可证）。播放条"右侧大片空白"= 上一首/下一首/收起按钮是透明 view + 丢失的图标；"空心绿圆" = 播放按钮只剩背景色。用户已确认**全局修复所有图标**。
2. **封面灰块 + 进度条不动（服务器运维，本次给出操作步骤）**：歌曲"四季予你"的封面与 MP3 均 404。实测 `http://gz.aeert.com:19612/uploads/2026/08/31/xxx.png` 与 `/uploads/music/xxx.mp3` 返回 404，响应头 `Server: nginx/1.24.0 (Ubuntu)`（宿主机 Nginx 抛出）。[部署操作手册.md L161-176](file:///d:/my-project/java-blog-system/部署操作手册.md) 已记载：宿主机 Nginx（19612 统一入口）缺少 `/uploads/` → MinIO 反代。MP3 404 导致音频无法播放 → `state.duration=0` → 进度条恒为 0%。

## Current State Analysis

- **技术栈**：blog-app 为 uni-app + Vue3（HBuilderX 管理，无 package.json），App 端 vue 页面视图层由 uni-app 框架接管，仅识别内置组件（view/text/image 等）；`<svg>` 非内置组件不渲染；`v-html` 仅 H5 端可用。
- **Icon 组件现状**（[Icon.vue](file:///d:/my-project/java-blog-system/blog-app/components/Icon.vue)）：根 view 内放 `<svg ... v-html="path">`，icons 字典含 play/pause/next/prev/music/home/book/user/chevron-left 等 30+ 图标（24x24 viewBox，线描 + 填充两种风格混合）。props：`name/size/color/stroke`。
- **静态 SVG 分布**（Grep `<svg` 共 13 个文件）：components/TabBar.vue（home/book/music/mine 四图标）、components/NavBar.vue（chevron-left 返回箭头）、ArticleItem.vue、pages/index/index.vue、pages/article/detail.vue、subpkg/pages/readlater/index.vue、subpkg/pages/mine/register.vue、login.vue、subpkg-article/pages/tags.vue、category.vue、list.vue、archives.vue。
- **PlayerBar 封面无兜底**（[PlayerBar.vue L8-L16](file:///d:/my-project/java-blog-system/blog-app/components/PlayerBar.vue#L8-L16)）：`v-if="song.cover"` 直接绑 src，404 时显示灰块，未回退到渐变占位图标（fab 悬浮球同理）。
- **主题色**：亮色 `--app-primary: #059669` / 暗色 `#34D399`，由页面根节点 `.theme-dark` 类级联（[App.vue](file:///d:/my-project/java-blog-system/blog-app/App.vue)）；JS 侧色值在 [common/theme.js](file:///d:/my-project/java-blog-system/blog-app/common/theme.js) 的 `colors/darkColors`。TabBar 激活态颜色依赖 `color: var(--app-primary)` 继承——图标方案需保留"跟随 CSS color"能力。
- **H5 端**：SVG 原生支持，一切正常（历史文档均在 H5 验证）；修复方案不得引入 H5 回归。

## Proposed Changes

### 1. 重写 `blog-app/components/Icon.vue` 渲染方式（核心，接口不变）

用 **CSS mask + SVG data URI** 替代 `<svg>` 标签 + `v-html`：

- **原理**：view 元素 + `mask-image`（含 `-webkit-` 前缀，Android WebView 必需）以 SVG 形状作遮罩，`background-color` 上色。mask 只取 SVG 的 alpha 通道，形状内颜色无关紧要，线描/填充两种风格均兼容；颜色完全由 CSS 控制，保留主题跟随能力。
- **模板**：根节点改为单个 view：
  ```html
  <view class="icon" :style="iconStyle"></view>
  ```
- **script**：保留现有 `icons` 字典与 props（name/size/color/stroke）不变；新增 computed：
  ```js
  // 拼 SVG data URI：外层补 xmlns 与描边属性（与原 <svg> 标签属性一致），fill 型图标 path 自带覆盖属性
  const iconDataUri = computed(() => {
    const content = icons[props.name] || ''
    if (!content) return ''
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#000" stroke-width="${props.stroke}" stroke-linecap="round" stroke-linejoin="round">${content}</svg>`
    return `url("data:image/svg+xml,${encodeURIComponent(svg)}")`
  })
  // 同时输出尺寸、color（currentColor 依赖自身 color 计算值）、background-color、mask 五件套
  const iconStyle = computed(() => ({
    width: sizeUnit.value,
    height: sizeUnit.value,
    color: props.color,
    backgroundColor: props.color,          // currentColor 合法，解析自身 color
    webkitMaskImage: `url("data:image/svg+xml,...")`,
    maskImage: iconDataUri.value,
    webkitMaskImage: iconDataUri.value
  }))
  ```
- **style**：`.icon` 类中固定 `mask-size: 100% 100%; mask-repeat: no-repeat; mask-position: center;`（连同 `-webkit-` 前缀版本）。
- **Why**：调用方（PlayerBar、各页面）**零改动**即恢复所有 `<Icon>` 图标；H5 端 mask 同样生效，无回归。

### 2. 替换静态内联 SVG 为 `<Icon>`（13 个文件）

- [TabBar.vue](file:///d:/my-project/java-blog-system/blog-app/components/TabBar.vue)：四个 `v-if/v-else-if` SVG 块替换为 `<Icon :name="item.icon" :size="22" />`（字典已有 home/book/music；mine 人形对应字典 `user`——把 list 项 `icon: 'mine'` 改为 `'user'`）。图标颜色默认 currentColor，自动跟随 tab-item 的激活态 color。
- [NavBar.vue L9-L11](file:///d:/my-project/java-blog-system/blog-app/components/NavBar.vue#L9-L11)：返回箭头替换为 `<Icon name="chevron-left" :size="24" />`。
- 其余 11 个文件（ArticleItem、index、detail、readlater、register、login、tags、category、list、archives）：逐文件将 `<svg>` 块映射到 Icon 字典同名图标（chevron-right/down、search、clock、eye、close、user 等）；字典缺失的形状在 icons 字典中按原 path 补充条目。**只替换渲染方式，不改动交互与样式尺寸**。

### 3. PlayerBar 封面加载失败兜底

[PlayerBar.vue](file:///d:/my-project/java-blog-system/blog-app/components/PlayerBar.vue) 展开态与悬浮球两处 `<image>` 增加 `@error` 状态（`coverError` ref，`watch(() => song.value?.id)` 重置）：失败时回退到既有 `cover-placeholder` 渐变底 + music 图标分支（把 `v-if="song.cover"` 改为 `v-if="song.cover && !coverError"`）。服务器 404 未修复期间界面不再显示灰块。

### 4. 服务器 Nginx 补 `/uploads/` 反代（运维操作，用户在服务器执行）

按 [部署操作手册.md L161-176](file:///d:/my-project/java-blog-system/部署操作手册.md) 在宿主机 Nginx 监听 19612 的 server 块（与 `/api` location 同级）追加：

```nginx
location /uploads/ {
    proxy_pass http://127.0.0.1:9000/blog/;
    proxy_set_header Host $host;
    client_max_body_size 50m;
}
```

```bash
nginx -t && nginx -s reload
curl -I http://gz.aeert.com:19612/uploads/music/1788173387288_4812.mp3   # 预期 200 + audio/mpeg
curl -I http://gz.aeert.com:19612/uploads/2026/08/31/1788173311349_1370.png  # 预期 200 + image/png
```

## Assumptions & Decisions

- **方案选型**：CSS mask + data URI（而非 iconfont / PNG 切图）。理由：接口零改动、保留 currentColor 主题跟随（暗色模式下 TabBar/按钮变色不破）、无新增构建依赖；Android 4.4+ WebView 均支持带前缀 mask。
- **不引入** uni-icons / uview 等图标库：与"最小改动、不新增依赖"原则冲突。
- **`encodeURIComponent` 编码 data URI**：保证 `#`（颜色值中常见）等字符不破坏 URL。
- **服务器 404 属运维配置**，不在代码仓内修改；PlayerBar 兜底（改动 3）保证配置修复前 UI 不破。
- **进度条不动**由 MP3 404 引起（duration=0），Nginx 修复后自动恢复，代码无需改动。
- **H5 端回归风险**：mask 方案在 H5 生效且视觉一致（同形状同颜色），逐页冒烟验证。

## Verification Steps

1. **HBuilderX 运行到 Android 真机/模拟器（App 端）**：
   - 底部 TabBar 四个图标正常显示，激活项图标随主题变绿（暗色模式变浅绿）；
   - 文章详情页等 NavBar 返回箭头正常；
   - 音乐页播放歌曲后返回首页：播放条显示封面、歌名歌手、上一首/播放(三角图标)/下一首/收起四个图标齐全，右侧无异常空白；
   - 播放中进度条推进、暂停/播放图标切换、收起后悬浮球正常。
2. **H5 端回归**：上述页面图标全部正常（与改造前视觉一致），无控制台报错。
3. **封面链路**（服务器 Nginx 修复后）：App 端播放条封面显示真实图（非渐变占位）；修复前验证兜底——封面显示渐变占位 + 音符图标而非灰块。
4. **curl 验证**：两条 URL 返回 200 且 Content-Type 为 audio/mpeg / image/png。
5. **边界**：`<Icon name="不存在的名字">` 渲染为空 view 不报错；无封面歌曲（cover 为空）显示渐变占位。
