# Step 1: Build stage
FROM maven:3.8.6-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -Dcheckstyle.skip=true

# Step 2: Runtime stage (using maintained Eclipse Temurin image)
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/target/fundoo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar", \
            "--server.port=8081", \
            "--spring.datasource.url=jdbc:h2:mem:testdb", \
            "--spring.datasource.driver-class-name=org.h2.Driver", \
            "--spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"]
