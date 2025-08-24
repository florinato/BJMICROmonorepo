# Usamos una imagen que ya tenga Maven y Java para poder ejecutar el comando de test.
FROM maven:3.9-eclipse-temurin-17

# Directorio de trabajo
WORKDIR /app

# Copiamos TODO el contenido del módulo e2e-tests (pom.xml, src, y la carpeta target generada por Maven).
# Esto asume que has ejecutado 'mvn clean install' en tu máquina primero.
COPY . .

# El comando que se ejecutará al arrancar el contenedor:
# Ejecuta los tests usando el código y las dependencias ya compiladas.
# Añadimos de nuevo el -DskipTests=false para asegurar que se ejecuten.
CMD ["mvn", "verify", "-DskipTests=false"]