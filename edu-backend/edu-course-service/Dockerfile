FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 父 POM
COPY pom.xml ../pom.xml
# 后端聚合 POM
COPY edu-backend/pom.xml ./pom.xml
# 所有子模块 POM（用于依赖解析）
COPY edu-backend/edu-common/pom.xml ./edu-common/pom.xml
COPY edu-backend/edu-gateway/pom.xml ./edu-gateway/pom.xml
COPY edu-backend/edu-user-service/pom.xml ./edu-user-service/pom.xml
COPY edu-backend/edu-course-service/pom.xml ./edu-course-service/pom.xml
COPY edu-backend/edu-enrollment-service/pom.xml ./edu-enrollment-service/pom.xml
COPY edu-backend/edu-interaction-service/pom.xml ./edu-interaction-service/pom.xml
COPY edu-backend/edu-assessment-service/pom.xml ./edu-assessment-service/pom.xml
COPY edu-backend/edu-ai-service/pom.xml ./edu-ai-service/pom.xml

# 下载依赖
RUN mvn dependency:go-offline -pl edu-backend/edu-course-service -am -B -q || true

# 复制所有源码
COPY edu-backend/edu-common/src ./edu-common/src
COPY edu-backend/edu-gateway/src ./edu-gateway/src
COPY edu-backend/edu-user-service/src ./edu-user-service/src
COPY edu-backend/edu-course-service/src ./edu-course-service/src
COPY edu-backend/edu-enrollment-service/src ./edu-enrollment-service/src
COPY edu-backend/edu-interaction-service/src ./edu-interaction-service/src
COPY edu-backend/edu-assessment-service/src ./edu-assessment-service/src
COPY edu-backend/edu-ai-service/src ./edu-ai-service/src

# 编译
RUN mvn clean package -pl edu-backend/edu-course-service -am -DskipTests -B -q

FROM eclipse-temurin:21-jre
WORKDIR /app

# 从根路径取 jar（Maven 聚合工程的路径）
COPY --from=build /build/edu-backend/edu-course-service/target/*.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
