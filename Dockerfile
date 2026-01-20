############################
# 1) Build/Test stage
############################
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Gradle 캐시 최적화: 의존성 파일 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle* settings.gradle* gradle.properties* ./

# 의존성 다운로드(캐시 레이어)
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null || true

# 소스 복사
COPY . .

# (선택) 테스트 stage 분리: 여기서는 빌드만
RUN ./gradlew --no-daemon clean bootJar

############################
# 2) Test stage
############################
FROM build AS test
# 테스트는 CI에서 이 stage만 실행하게 할 거야
ARG SPRING_PROFILES_ACTIVE=test
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

# 필요하면 여기서 특정 테스트만/옵션 추가 가능
CMD ["./gradlew", "--no-daemon", "test"]

############################
# 3) Runtime stage
############################
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# 보안: non-root 사용자
RUN useradd -u 10001 -m appuser
USER appuser

# 빌드 산출물만 복사
COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
