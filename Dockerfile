# Stage 1: Build with Maven + JDK 21
FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /app
ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

# Set up Maven settings for GitHub authentication taking arguments for username and token
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    echo "Cache directory mounted"
    
RUN mkdir -p /root/.m2 && \ 
    echo "<settings><servers><server><id>github</id><username>${GITHUB_USERNAME}</username><password>${GITHUB_TOKEN}</password></server></servers></settings>" > /root/.m2/settings.xml && \
    cat /root/.m2/settings.xml && \
    mvn -s /root/.m2/settings.xml dependency:go-offline

RUN mvn clean package -DskipTests && cp target/*.jar target/app.jar

# Stage 2: Create the final image with JDK 21 that just runs the application
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /app/target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--debug"]