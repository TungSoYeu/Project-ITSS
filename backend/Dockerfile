# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
# Dependency resolution (caching)
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src src
RUN mvn package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/ooas-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 3000

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
