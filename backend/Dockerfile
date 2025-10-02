# Use Maven image to build the application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Install ffmpeg for Linux
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/PronounceIT-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Create a startup script that writes credentials from env var
RUN echo '#!/bin/sh\n\
if [ -n "$GOOGLE_CREDENTIALS_JSON" ]; then\n\
  echo "$GOOGLE_CREDENTIALS_JSON" > /app/credentials.json\n\
  echo "Google credentials written to /app/credentials.json"\n\
fi\n\
exec java -jar /app/app.jar' > /app/startup.sh && chmod +x /app/startup.sh

ENTRYPOINT ["/app/startup.sh"]