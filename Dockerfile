# Stage 1: Dependencies
FROM maven:3.8.5-openjdk-11-slim AS deps
WORKDIR /app
COPY pom.xml .
# Download dependencies only
RUN mvn dependency:go-offline -B

# Stage 2: Build
FROM maven:3.8.5-openjdk-11-slim AS builder
WORKDIR /app
# Copy the dependencies from previous stage
COPY --from=deps /root/.m2 /root/.m2
# Copy source code
COPY . .
# Build with offline mode and skip tests
RUN mvn clean package -B -DskipTests -o

# Stage 3: Runtime
FROM tomcat:10-jdk11
# Remove default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/*
# Copy our application
COPY --from=builder /app/target/cs122b-team-beef.war /usr/local/tomcat/webapps/cs122b-team-beef.war

EXPOSE 8080
CMD ["catalina.sh", "run"]