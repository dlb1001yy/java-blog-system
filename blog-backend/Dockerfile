# ====== 第一阶段：JDK 编译（使用较小的基础镜像 + 手动安装 Maven） ======
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build

# 下载并安装 Maven 3.9.6（约 10MB，远小于完整 Maven 镜像的 800MB+）
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/* \
    && curl -fsSL https://mirrors.tuna.tsinghua.edu.cn/apache/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o /tmp/maven.tar.gz \
    && tar xzf /tmp/maven.tar.gz -C /opt/ \
    && rm /tmp/maven.tar.gz \
    && ln -s /opt/apache-maven-3.9.6/bin/mvn /usr/local/bin/mvn

# 配置阿里云 Maven 镜像加速依赖下载
RUN mkdir -p /root/.m2 && cat > /root/.m2/settings.xml <<'XMLEOF'
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
XMLEOF

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
