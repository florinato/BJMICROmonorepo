# Paso 1: Usar una imagen base con Java 17 y curl
# Usamos una imagen que incluya 'curl' para poder hacer la llamada HTTP
FROM eclipse-temurin:17-jdk-jammy

# Paso 2: Establecer un directorio de trabajo
WORKDIR /app

# Paso 3: Copiar el .jar
COPY target/game-core-0.0.1-SNAPSHOT.jar app.jar

# Paso 4: Exponer el puerto
EXPOSE 8081

# ⭐ Paso 5: Definir el Health Check ⭐
# --interval=30s: Ejecuta el chequeo cada 30 segundos.
# --timeout=10s: Espera un máximo de 10 segundos por una respuesta.
# --start-period=60s: No marques el contenedor como "unhealthy" durante los primeros 60 segundos. Esto le da tiempo a Spring Boot para arrancar.
# --retries=3: El contenedor se marcará como "unhealthy" después de 3 intentos fallidos seguidos.
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Paso 6: El comando para arrancar la aplicación
CMD ["java", "-jar", "app.jar"]