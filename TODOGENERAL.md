
# THE ROADMAP 

NEXT STEPS:

- Trabajar en el Double y mover el evento a la carpeta de eventos comunes
- Rehacer tests de integración y del servicio para GAME CORE
- Test de integración para testear el correcto funcionamiento de los appends de APISIX (userId)

---
### Deuda Técnica y Mejoras Pendientes

* **Refactorizar Mappers con MapStruct:**
    * **Tarea:** Sustituir las implementaciones manuales por interfaces de MapStruct.
    * **Beneficio:** Reducir código repetitivo y aumentar la seguridad en tiempo de compilación.

* **Implementar Perfiles de Spring para Configuración:** **COMPLETAO**
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
  
* **Refactorizar clases immutables a records**

* **Refactorizar user id's de Long a UUID 💀**


* **Sonar Qube de todo**

** Para el "test" emular el Docker compose y la definción de los testcontainers