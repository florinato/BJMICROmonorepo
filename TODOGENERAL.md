
### **Prioridades Inmediatas 🔥**

Este es el plan de acción a corto plazo para avanzar con las funcionalidades críticas.

* **Desarrollar `auth-service`:** Implementar la lógica de registro, login y emisión de tokens JWT.
* **Implementar API Gateway (APISIX):** Configurar una instancia standalone de APISIX como puerta de entrada al sistema.
* **Tests End-to-End (E2E):** Crear pruebas que verifiquen el flujo completo entre los servicios (Crear paquete externo)
* **Documentación de API:** Integrar Swagger/OpenAPI en los controladores para generar la documentación.
* **Mantenimiento de Configuración:** Unificar y mejorar la configuración de Kafka en los servicios `user` y `game-core`.
    * Utilizar un `YAML` como fuente única de verdad.
    * Definir los topics de Kafka usando `records`.

---

### **Tareas de Desarrollo Pendientes 💻**

Funcionalidades del roadmap que aún no se han completado.

* **Integración Completa de APISIX:** Una vez standalone, configurarlo para validar tokens y enriquecer las peticiones hacia los microservicios.

---

### **Deuda Técnica y Mejoras 🛠️**

Tareas importantes para mejorar la calidad, seguridad y mantenibilidad del código a largo plazo.

* **Refactorizar User IDs (Crítico):** Cambiar los `id` de usuario de tipo `Long` a `UUID`.
* **Implementar Patrón Transactional Outbox:** Asegurar la entrega garantizada de eventos de Kafka para aumentar la resiliencia del sistema.
* **Análisis con SonarQube:** Integrar SonarQube para analizar la calidad y seguridad de todo el código.
* **Refactorizar a MapStruct:** Sustituir los mappers manuales por interfaces de MapStruct para reducir código y mejorar la seguridad en compilación.
* **Refactorizar a Records:** Convertir las clases inmutables a `records` de Java.
* **Gestión de Vulnerabilidades:** Utilizar Snyk o el plugin de OWASP para detectar y gestionar dependencias vulnerables.

---

### **Completado ✅**

Hitos que ya han sido finalizados.

* **Finalizar Integración `user-service`:** Completar los tests para el productor de eventos de `bets-service` que actualiza el saldo del usuario al finalizar una partida.
* **Fase 1: Lógica de Negocio Central:** Finalizada la lógica de `game-core`, `bets-service` y la comunicación por Kafka.
* **Fase 2: Tests de Integración:** Configurados los tests con Testcontainers para levantar un broker de Kafka real.
* **Fase 3: Containerización:** Creados los `Dockerfile` para cada servicio y el `docker-compose.yml` para orquestar la aplicación.
* **Desarrollo de `user-service`:** Implementado el servicio para gestionar usuarios y su saldo.
* **Perfiles de Spring:** Creados los ficheros de configuración para entornos de desarrollo (`dev`) y producción (`prod`).
```