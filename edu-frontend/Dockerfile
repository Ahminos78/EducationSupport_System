# 多阶段构建
FROM node:20-alpine AS build
WORKDIR /app

# 安装依赖（利用缓存）
COPY package.json package-lock.json ./
RUN npm ci && npm cache clean --force

# 复制源码并构建
COPY . .
RUN npm run build

# 运行阶段 - 用 nginx 托管静态文件
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
