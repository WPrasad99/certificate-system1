# Use Maven image to build the project
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use OpenJDK image for runtime
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/certificate-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
