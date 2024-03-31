# 使用Java运行环境作为基础镜像
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 将构建好的jar包复制到容器的工作目录中
COPY ./openai-proxy-1.0.0-SNAPSHOT.jar /app/openai-proxy-1.0.0-SNAPSHOT.jar

# 暴露应用所需端口，根据实际情况调整
EXPOSE 9001

# 运行应用
# 使用外部配置文件启动Spring Boot应用，确保config.properties, glm.json, 和 kimi.json在容器启动时被正确挂载
CMD ["java", "-jar", "openai-proxy-1.0.0-SNAPSHOT.jar", "--spring.config.location=file:/app/config.properties"]