# This Dockerfile builds a Java application using Maven and then runs it with a JRE.
# Use a multi-stage build to keep the final image small
# Use the official Maven image to build the application 
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Use a smaller JRE image to run the application
FROM openjdk:21-jdk AS runner
WORKDIR /app
COPY --from=builder /app/target/analytics-service-*.jar app.jar
EXPOSE 4002
ENTRYPOINT ["java", "-jar", "app.jar"]
