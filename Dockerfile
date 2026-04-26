# Stage 1: Build with Maven
FROM maven:3.9.0-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run with Java
FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]


# docker build -t maylinjg/kubernetes-b:1.0 .

# docker run -d --name kubernetes-b -p 8085:8085 maylinjg/kubernetes-b:1.0

# docker push maylinjg/kubernetes-b:1.0