# Usar una imagen base con Java 17 y curl
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Copiar el .jar (asumiendo el nombre estándar de Maven)
COPY target/auth-1.0-SNAPSHOT.jar app.jar

# Exponer el puerto 8084 para auth-service
EXPOSE 8084

# Health Check apuntando al puerto 8084
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

# Comando de arranque
CMD ["java", "-jar", "app.jar"]