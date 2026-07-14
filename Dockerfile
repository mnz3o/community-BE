# 빌드
# Gradle과 JDK 21이 포함된 이미지를 사용하여 프로젝트를 빌드
FROM gradle:8.14.3-jdk21 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 관련 파일 먼저 복사하여 Layer Cache 활용
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# BuildKit Cache를 활용하여 의존성 캐시 재사용
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew dependencies --no-daemon

# 프로젝트 소스 코드 복사
COPY src ./src

# Spring Boot JAR 파일 생성
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew bootJar --no-daemon

# 실행
# 실행만 하므로 JRE 이미지만 사용
FROM eclipse-temurin:21-jre

# 작업 디렉토리 설정
WORKDIR /app

# Health Check에 사용할 wget 설치
RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*

# Non-root 사용자 생성
RUN addgroup --system spring \
    && adduser --system spring --ingroup spring

# Build Stage에서 생성된 JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 작업 디렉토리 권한 변경
RUN chown -R spring:spring /app

# Root 대신 spring 사용자로 실행
USER spring

# Spring Boot 실행 포트
EXPOSE 8080

# 컨테이너 상태 확인
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# 환경 변수 기반으로 JVM 옵션을 주입하여 Spring Boot 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]