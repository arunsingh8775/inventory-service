# Dockerfile
FROM eclipse-temurin:17-jre-jammy

# Run as a non-root user for safety
RUN useradd -ms /bin/bash appuser
WORKDIR /app

# Your GitHub Action/VM step renames the built artifact to app.jar
# so we just copy that one file.
COPY app.jar /app/app.jar

# Default port (can be overridden via env SERVER_PORT used by Spring Boot)
ENV SERVER_PORT=8082

EXPOSE 8082

# Respect JAVA_TOOL_OPTIONS passed from docker-compose (GC, memory, etc.)
# Don't hardcode --server.port here; Spring Boot will read SERVER_PORT env.
ENTRYPOINT ["java","-jar","/app/app.jar"]

USER appuser
