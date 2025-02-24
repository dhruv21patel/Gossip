# ---------- Build Stage ----------
    FROM maven:3.8.7-openjdk-18-slim AS build
    WORKDIR /app
    
    # Copy the Maven configuration files first to leverage Docker caching
    COPY pom.xml .
    # If you have a pom.xml in the root and a 'src' folder, copy both:
    COPY src ./src
    
    # Build the Spring Boot application (skip tests for faster builds)
    RUN mvn clean package -DskipTests
    
    # ---------- Runtime Stage ----------
    FROM openjdk:17-jdk-slim
    WORKDIR /app
    
    # Copy the fat jar (Spring Boot executable jar) from the build stage
    # This uses a wildcard to copy your jar; ensure your artifact builds a jar in /app/target.
    COPY --from=build /app/target/*.jar app.jar
    
    # Expose the port your Spring Boot application runs on
    EXPOSE 8080
    
    # Set the entrypoint to run the Spring Boot application
    ENTRYPOINT ["java", "-jar", "app.jar"]
    