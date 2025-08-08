# STAGE 1: Build Stage (La Cocina)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copiamos pom.xml y descargamos dependencias para el cacheo de capas
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente y compilamos
COPY src ./src
RUN mvn package -DskipTests


# STAGE 2: Runtime Stage (El Plato Servido)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# --- Instalamos curl para el Healthcheck ---
# Actualizamos los repositorios e instalamos curl.
# El --no-install-recommends evita instalar paquetes innecesarios.
# Luego limpiamos el cache de apt para mantener la imagen ligera.
RUN apt-get update && \
    apt-get install -y curl --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

# Copiamos ÚNICAMENTE el .jar compilado desde la etapa 'build'.
COPY --from=build /app/target/auth-1.0-SNAPSHOT.jar app.jar

# --- NUEVO: Añadimos el Healthcheck de vuelta ---
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

# Exponemos el puerto
EXPOSE 8084

# Comando de arranque
CMD ["java", "-jar", "app.jar"]







#FROM eclipse-temurin:17-jdk-jammy
#
## Directorio de trabajo
#WORKDIR /app
#
## Copiar el .jar (asumiendo el nombre estándar de Maven)
#COPY target/auth-1.0-SNAPSHOT.jar app.jar
#
## Exponer el puerto 8084 para auth-service
#EXPOSE 8084
#
## Health Check apuntando al puerto 8084
#HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
#  CMD curl -f http://localhost:8084/actuator/health || exit 1
#
## Comando de arranque
#CMD ["java", "-jar", "app.jar"]