# Usar una imagen base con Java 17 y curl
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Copiar el .jar (asegúrate de que el nombre es correcto)
COPY target/bets-1.0-SNAPSHOT.jar app.jar

# Exponer el puerto 8082 para bets-service
EXPOSE 8082

# Health Check apuntando al puerto 8082
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# Comando de arranque
CMD ["java", "-jar", "app.jar"]