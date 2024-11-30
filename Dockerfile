# Stage 1: Build with Maven
FROM maven:3.8.5-openjdk-11-slim AS builder

# Set Maven options for memory management
ENV MAVEN_OPTS="-Xmx512m"

# Create and cd into app directory
WORKDIR /app

# Copy only pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies separately to take advantage of Docker caching
RUN mvn dependency:go-offline -B || echo "Dependencies may not be fully downloaded"

# Now copy the source code
COPY . .

# Build with specific memory settings and fail-fast options
RUN mvn clean package \
    -B \
    -DskipTests \
    -Dmaven.compiler.maxmem=512m \
    && echo "Build successful" \
    && ls -l target/*.war

# Stage 2: Runtime with Tomcat
FROM tomcat:10-jdk11

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy our application
COPY --from=builder /app/target/cs122b-team-beef.war /usr/local/tomcat/webapps/cs122b-team-beef.war

# Verify the WAR file was copied correctly
RUN ls -l /usr/local/tomcat/webapps/cs122b-team-beef.war || exit 1

EXPOSE 8080

CMD ["catalina.sh", "run"]