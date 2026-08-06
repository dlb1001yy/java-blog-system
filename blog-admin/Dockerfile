# ====== 第一阶段：Node 编译 ======
FROM node:18-alpine AS builder

WORKDIR /build

# 先复制 package.json 预安装依赖（利用 Docker 缓存层加速构建）
COPY package.json package-lock.json* ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源码并构建
COPY . .
RUN npm run build

# ====== 第二阶段：nginx 运行 ======
FROM nginx:1.25-alpine

# 清空默认配置
RUN rm -rf /usr/share/nginx/html/*

# 复制构建产物（vite base 为 /admin/，产物在 dist/admin/ 下）
COPY --from=builder /build/dist /usr/share/nginx/html

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
