FROM eclipse-temurin:17-jdk-jammy

# Install ffmpeg (Debian/Ubuntu base). This ensures ffmpeg is available in the container
RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the jar produced by Maven package (use the same artifact name produced by the project's pom)
COPY target/PronounceIT-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
