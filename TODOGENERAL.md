
# THE ROADMAP (FINAL PHASE)

### Fase 1: Lógica de Negocio Central ✅
* **Estado:** **COMPLETADA**
* **Logros:**
    * Se ha finalizado la lógica principal de `game-core`, incluyendo las acciones de `startGame`, `playerHit`, `playerStand` y `playerDoubleDown`.
    * Se ha implementado la lógica en `bets-service` para procesar los resultados del juego (`PLAYER_WINS`, `DEALER_WINS`, `PUSH`) y calcular los pagos correspondientes, incluyendo el pago especial por Blackjack.
    * Se ha establecido la comunicación asíncrona vía Kafka entre ambos servicios.

### Fase 2: Tests de Integración Automatizados (Testcontainers) ✅
* **Estado:** **COMPLETADA**
* **Logros:**
    * Se ha configurado Testcontainers para levantar un broker de Kafka real durante la ejecución de los tests.
    * Se ha creado un test de integración (`GameCoreServiceTCIntegrationTest`) que verifica la publicación de eventos a un broker de Kafka real.

### Fase 3: Containerización (Docker)
* **Estado:** PENDIENTE
* **Descripción:** Crear un `Dockerfile` para cada microservicio y un fichero `docker-compose.yml` para orquestar toda la pila de la aplicación (MySQL, Kafka, y los servicios).

### Fase 4: Implementar la Capa de Usuario y Seguridad
* **Estado:** PENDIENTE
* **Descripción:**
    * **`user-service`:** Desarrollar el servicio para gestionar los datos de los usuarios y su saldo (`balance`).
    * **`auth-service`:** Implementar la lógica para el registro, login y emisión de tokens (JWT).

### Fase 5: Integrar el API Gateway (APISIX)
* **Estado:** PENDIENTE
* **Descripción:** Configurar APISIX como la única puerta de entrada al sistema, validando tokens y enriqueciendo las peticiones.

---
### Deuda Técnica y Mejoras Pendientes

* **Refactorizar Mappers con MapStruct:**
    * **Tarea:** Sustituir las implementaciones manuales por interfaces de MapStruct.
    * **Beneficio:** Reducir código repetitivo y aumentar la seguridad en tiempo de compilación.

* **Implementar Perfiles de Spring para Configuración:**
    * **Tarea:** Crear ficheros `application-dev.yml` y `application-prod.yml`.
    * **Beneficio:** Separación limpia de la configuración por entorno y despliegues robustos.

* **Implementar Patrón Transactional Outbox:**
    * **Tarea:** Asegurar la entrega de eventos de Kafka.
    * **Beneficio:** Aumenta la resiliencia y la fiabilidad del sistema.

* **Añadir Documentación de API (Swagger/OpenAPI):**
    * **Tarea:** Integrar Swagger en los controladores.
    * **Beneficio:** Facilita el uso y la prueba de la API.

* **Gestionar Vulnerabilidades de Dependencias:**
    * **Tarea:** Utilizar herramientas como Snyk o el plugin de OWASP.
    * **Beneficio:** Mejora la seguridad general de la aplicación.