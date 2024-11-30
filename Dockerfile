# Stage 1: Build
FROM maven:3.8.5-openjdk-11-slim AS builder

WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies and plugins
RUN mvn dependency:resolve \
    && mvn dependency:resolve-plugins \
    && mvn dependency:go-offline

# Copy source code
COPY . .

# Build (without offline mode)
RUN mvn clean package -B -DskipTests

# Stage 2: Runtime
FROM tomcat:10-jdk11

# Remove default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy our application
COPY --from=builder /app/target/cs122b-team-beef.war /usr/local/tomcat/webapps/cs122b-team-beef.war

EXPOSE 8080

CMD ["catalina.sh", "run"]