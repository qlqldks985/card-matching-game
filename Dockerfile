# 1단계: 빌드 (의존성 레이어를 먼저 받아 두면 소스만 바뀌었을 때 다시 받지 않는다)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q package -DskipTests && cp target/cardgame-*.jar app.jar

# 2단계: 실행 (JRE 만, root 가 아닌 사용자로)
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 app
COPY --from=build /workspace/app.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
