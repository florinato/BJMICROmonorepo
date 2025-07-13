# Usar una imagen base con Java 17 y curl
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Copiar el .jar (asumiendo el nombre estándar de Maven)
COPY target/user-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto 8083 para user-service
EXPOSE 8083

# Health Check apuntando al puerto 8083
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# Comando de arranque
CMD ["java", "-jar", "app.jar"]