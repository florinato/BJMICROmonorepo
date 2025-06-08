# Cosas importantes con Kafka en Windows

- No usar rutas tipo /c/... en server.properties → usar C:/...
- Si kafka-storage.sh format no genera meta.properties, revisar rutas.
- Kafka con KRaft necesita:
    - log.dirs con ruta real
    - kafka-storage.sh format con UUID
    - controller.quorum.voters correctamente definido
