# 1. Imagen base
# Partimos de una imagen oficial que ya incluye Maven y una versión de Java compatible (17).
FROM maven:3.9-eclipse-temurin-17

# 2. Directorio de trabajo
# Establecemos el directorio donde se ejecutarán todos los comandos dentro del contenedor.
WORKDIR /app

# 3. Optimización de caché de Docker
# Copiamos primero solo el pom.xml y descargamos las dependencias.
# Docker guardará esta capa en caché. Si en el futuro solo cambias el código de los tests
# (y no el pom.xml), este paso no se volverá a ejecutar, haciendo los builds mucho más rápidos.
COPY pom.xml .
RUN mvn dependency:go-offline

# 4. Copia del código fuente
# Copiamos el resto del código de nuestros tests.
COPY src ./src

# 5. Comando de ejecución
# Este es el comando que se ejecutará cuando el contenedor arranque.
# Llama a la fase 'verify' de Maven, que activará el plugin Failsafe
# que configuramos en el pom.xml para lanzar nuestros tests E2E.
CMD ["mvn", "verify"]