# 修复 blog-app 首页博客列表封面图无法显示

## Summary
blog-app 首页文章列表的封面图无法显示。根因：后端 `cover_image` 字段存的是相对路径（如 `/api/uploads/xxx.jpg`，与头像字段一致），而 [ArticleItem.vue](file:///d:/my-project/java-blog-system/blog-app/components/ArticleItem.vue#L4-L9) 直接 `:src="article.coverImage"` 绑定，未拼接服务器地址。在 uni-app H5（5173）下，相对路径会解析为 `http://localhost:5173/api/uploads/...` → 404，图片裂图。同一问题也存在于 [detail.vue](file:///d:/my-project/java-blog-system/blog-app/pages/article/detail.vue#L82-L88) 的"相关文章"封面。

修复：新增 `resolveFileUrl` 工具函数统一处理相对/绝对路径，在 ArticleItem 与 detail.vue 的相关文章封面应用；并加图片加载失败回退（隐藏裂图、回退为纯文本布局），保证整体布局在任何情况下都不破。

## Current State Analysis（基于 Phase 1 探查）
- [common/config.js](file:///d:/my-project/java-blog-system/blog-app/common/config.js#L1)：`BASE_URL = 'http://localhost:8080/api'`，服务器 origin 为 `http://localhost:8080`。API 调用走 `request.js` 自动拼 BASE_URL，但 `<image :src>` 是浏览器原生请求，不走 request.js。
- [components/ArticleItem.vue](file:///d:/my-project/java-blog-system/blog-app/components/ArticleItem.vue#L4-L9)：第 4-9 行 `<image v-if="article.coverImage" :src="article.coverImage" class="cover" mode="aspectFill" />`，无 URL 处理、无加载失败处理。第 11 行内容区根据 `article.coverImage` 切换 `full` 布局。`.cover` 100x80 圆角 8px。
- [pages/article/detail.vue](file:///d:/my-project/java-blog-system/blog-app/pages/article/detail.vue#L82-L88)：第 82-88 行"相关文章"封面同样直接绑定 `:src="item.coverImage"`，同样问题。
- 后端 [blog-backend/.../entity/Article.java:18](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/entity/Article.java#L18)：`private String coverImage;`，[sql/create_sql.sql:49](file:///d:/my-project/java-blog-system/blog-backend/sql/create_sql.sql#L49) `cover_image varchar(255)`。结合记忆中头像字段值为 `/api/uploads/1785749796261_5534.jpg`，确认 coverImage 同为相对路径。
- [index.html](file:///d:/my-project/java-blog-system/blog-app/index.html) 已加 `<meta name="referrer" content="no-referrer">`（上一轮），外链图床防盗链已解决；故若封面是外链（gitee 等）也能显示，仅需处理相对路径拼接。

## Proposed Changes

### 改动 1：新增 `resolveFileUrl` 工具函数
在 [common/config.js](file:///d:/my-project/java-blog-system/blog-app/common/config.js) 末尾追加：
```js
// 服务器 origin（去掉 /api 后缀），用于拼接相对路径的文件 URL
const SERVER_ORIGIN = BASE_URL.replace(/\/api$/, '')

/**
 * 解析文件 URL：相对路径拼接服务器 origin，已是完整 http(s) URL 的原样返回。
 * coverImage / avatar 等后端返回的路径统一走此函数。
 * @param {string} path
 * @returns {string}
 */
export function resolveFileUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//i.test(path)) return path
  if (path.startsWith('/')) return SERVER_ORIGIN + path
  return SERVER_ORIGIN + '/' + path
}
```
**Why**：集中处理"相对路径 vs 完整 URL"两种情况，避免每个组件重复判断；服务器 origin 从 BASE_URL 派生，环境切换时只改一处。

### 改动 2：ArticleItem.vue 应用 URL 解析 + 加载失败回退
[components/ArticleItem.vue](file:///d:/my-project/java-blog-system/blog-app/components/ArticleItem.vue) 改动：
- script：`import { resolveFileUrl } from '@/common/config.js'`；新增 `coverError` ref；新增 computed `coverUrl`：
  ```js
  import { computed, ref } from 'vue'
  import { resolveFileUrl } from '@/common/config.js'
  const props = defineProps({ article: Object })
  const emit = defineEmits(['click'])
  const coverError = ref(false)
  const coverUrl = computed(() => resolveFileUrl(props.article?.coverImage))
  // article 变化时重置错误态
  // （watch article.id 重置 coverError）
  ```
  注意：`coverError` 需在文章切换时重置，加 `watch(() => props.article?.id, () => { coverError.value = false })`。
- template 第 4-9 行改为：
  ```html
  <image
    v-if="coverUrl && !coverError"
    :src="coverUrl"
    class="cover"
    mode="aspectFill"
    @error="coverError = true"
  />
  ```
- template 第 11 行内容区 class 判断改为：`:class="['content', (coverUrl && !coverError) ? '' : 'full']"`（无封面或加载失败时回退为纯文本满宽布局，避免左侧留空）。
- style `.cover` 增加柔和占位背景（图片加载中显示淡灰底，避免白块）：
  ```scss
  .cover {
    width: 100px;
    height: 80px;
    border-radius: $radius-md;
    flex-shrink: 0;
    background: $color-bg;
  }
  ```
  （保持现有尺寸不变，仅确认占位底色存在。）

**Why**：URL 拼接解决显示；`@error` 回退保证单条记录图片损坏时不留裂图空位，整体列表布局保持整齐。

### 改动 3：detail.vue 相关文章封面同样修复
[pages/article/detail.vue](file:///d:/my-project/java-blog-system/blog-app/pages/article/detail.vue#L82-L88) 改动：
- script：`import { resolveFileUrl } from '@/common/config.js'`。
- 因 relatedArticles 是多条，用计算属性数组不如直接在模板内调用。将第 82-88 行改为：
  ```html
  <image
    v-if="resolveFileUrl(item.coverImage)"
    :src="resolveFileUrl(item.coverImage)"
    class="related-cover"
    mode="aspectFill"
  />
  ```
  （相关文章封面尺寸 60x60，失败时 v-if 隐藏即可，相关文章列表本身是文字主导，无需复杂回退。）
- 注意：模板内直接调用导入的函数在 Vue3 `<script setup>` 中可用（函数在 setup 作用域内）。

**Why**：同一根因的连带修复，保证详情页相关文章封面也正常显示。

## Assumptions & Decisions
- **假设**：`coverImage` 存储格式为相对路径 `/api/uploads/xxx.jpg`（与头像字段一致，已由记忆与 entity 字段类型佐证）。`resolveFileUrl` 同时兼容完整 URL（外链 gitee 等）与相对路径，故即使个别记录为完整 URL 也能正确处理。
- **决定**：不改后端、不改上传逻辑，仅前端解析。服务器 origin 从 `BASE_URL` 派生（`replace(/\/api$/, '')`），环境切换只改 `config.js` 一处。
- **决定**：不做"无封面时生成默认渐变封面"等额外功能（用户未要求，保持最小改动）。仅做"有封面则正确显示，无封面或加载失败则回退纯文本布局"。
- **决定**：相关文章封面不加 `@error` 复杂回退（列表文字主导，v-if 隐藏足够）。
- **不处理**：blog-frontend / blog-admin 不在本次范围（blog-frontend 有 Vite 代理 `/api`，封面本就能显示；blog-admin 是后台编辑预览，非本次诉求）。

## Verification
1. blog-app H5（HBuilderX 跑 5173，后端 8080 已启动）：进首页，带封面的文章卡片封面正常显示（不再是裂图）。
2. 浏览器 Network 检查封面图片请求 URL 应为 `http://localhost:8080/api/uploads/xxx.jpg`（200），而非 `localhost:5173`（404）。
3. 若存在外链封面（gitee 等），配合已有 `no-referrer` meta 也能显示。
4. 进文章详情页，底部"相关文章"封面正常显示。
5. 手动构造一条 coverImage 为空的文章 → 卡片回退为纯文本满宽布局，无空位；手动把某条 coverImage 改为不存在的 URL → `@error` 触发后回退纯文本布局，列表整体不破。
6. 列表整体视觉：有封面卡片（左图右文）与无封面卡片（纯文本满宽）混排时，间距、对齐一致。
