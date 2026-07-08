# 빌드
# Gradle과 JDK 21이 포함된 이미지를 사용하여 프로젝트를 빌드
FROM gradle:8.14.3-jdk21 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 관련 파일 먼저 복사하여 Layer Cache 활용
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# 의존성을 먼저 다운로드하여 Layer Cache 활용
RUN ./gradlew dependencies

# 프로젝트 소스 코드 복사
COPY src ./src

# Spring Boot JAR 파일 생성
RUN ./gradlew bootJar

# 실행
# 실행만 하므로 JRE 이미지만 사용
FROM eclipse-temurin:21-jre

# 작업 디렉토리 설정
WORKDIR /app

# Build Stage에서 생성된 JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# Spring Boot 실행 포트
EXPOSE 8080

# Spring Boot 애플리케이션 실행
CMD ["java", "-jar", "app.jar"]