# ====== 第一阶段：Maven 编译 ======
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 先复制 pom.xml 预下载依赖（利用 Docker 缓存层加速构建）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并编译打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# ====== 第二阶段：JRE 运行 ======
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 创建上传目录
RUN mkdir -p /app/uploads

# 从构建阶段复制 JAR
COPY --from=builder /build/target/blog-backend-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令，激活 docker profile
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
