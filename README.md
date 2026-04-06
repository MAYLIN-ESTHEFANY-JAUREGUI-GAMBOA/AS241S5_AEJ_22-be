# SistemaIAs - Integración de APIs de Inteligencia Artificial

**Proyecto de Integración de Servicios de Inteligencia Artificial** con Spring WebFlux y MongoDB. Sistema completo que consume dos APIs especializadas para ofrecer capacidades conversacionales y de generación de imágenes estilo Ghibli.

## 🤖 **Las 2 APIs de IA y Características**

### **ChatBotsIA - ChatGPT-4**
- **Servicio**: Chatbot avanzado con inteligencia artificial conversacional
- **Características**: Respuestas contextuales, conversaciones naturales, persistencia de historial
- **URL**: https://rapidapi.com/rphrp1985/api/chatgpt-42/playground/apiendpoint_670e43e1-f8a9-408c-a9ba-63cf5a3d12a4

### **GhibliAI - Generador de Imágenes**
- **Servicio**: Generación de imágenes estilo Studio Ghibli
- **Características**: Creación artística basada en prompts, múltiples estilos, URLs persistentes
- **URL**: https://rapidapi.com/total-api-devloper-total-api-devloper-default/api/ai-ghibli-image-generator/playground/apiendpoint_18f8be02-c7a8-4f81-a995-11c6c58aa462

## 🛠️ **Herramientas y Versiones Utilizadas**

### **Stack de Desarrollo**
- **Java**: JDK 17
- **Framework**: Spring Boot 3.5.13
- **Programación Reactiva**: Spring WebFlux
- **Base de Datos**: MongoDB (NoSQL)
- **Gestión de Dependencias**: Apache Maven
- **IDE**: IntelliJ IDEA / Visual Studio Code

### **Tecnologías Específicas**
- **WebFlux**: Para programación reactiva y no bloqueante
- **MongoDB Reactive**: Para operaciones de base de datos reactivas
- **Lombok**: Para reducción de código repetitivo
- **SpringDoc OpenAPI**: Para documentación automática de APIs

## 📡 **Consumo de 2 APIs de IA**

Implementamos el consumo de dos APIs de inteligencia artificial utilizando:

### **✅ Spring WebFlux + MongoDB (NoSQL)**
Para realizar consultas, capturar resultados y almacenarlos en una base de datos Cloud:

- **Spring WebFlux**: Manejo de solicitudes HTTP reactivas
- **MongoDB Reactive**: Almacenamiento no relacional de respuestas de IA
- **Project Reactor**: Programación reactiva con Mono y Flux
- **Conexión Cloud**: MongoDB Atlas para persistencia escalable

### **Arquitectura Reactiva**
```
Cliente HTTP → WebClient Reactivo → API IA → Respuesta → MongoDB → Cliente
```

## 🔐 **Gestión de Credenciales y Seguridad**

Todas las credenciales, ya sea de base de datos o de APIs de IA, están configuradas en el archivo `application.yml`:

```yaml
rapidapi:
  chatgpt:
    url: https://chatgpt42.p.rapidapi.com
    key: ${CHATGPT_API_KEY}
    host: chatgpt42.p.rapidapi.com
  ghibli:
    url: https://ai-ghibli-image-generator.p.rapidapi.com/texttoimageghibli
    key: ${GHIBLI_API_KEY}
    host: ai-ghibli-image-generator.p.rapidapi.com

spring:
  data:
    mongodb:
      uri: mongodb+srv://username:password@cluster.mongodb.net/database
```

### **Seguridad Implementada**
- **Variables de Entorno**: Credenciales inyectadas desde variables de entorno
- **@Value Annotation**: Inyección segura de configuración
- **Headers Específicos**: Autenticación para cada API externa
- **Content-Type**: Configurado como application/json

## 🏗️ **Arquitectura del Proyecto**

### **Estructura de Paquetes**
```
pe.edu.vallegrande.SistemaIAs/
├── model/          # Modelos de datos (Chat, GhibliAI)
├── repository/      # Repositorios reactivos MongoDB
├── service/         # Lógica de negocio y consumo de APIs
├── rest/           # Controladores REST
└── SistemaIAsApplication.java
```

### **Flujo de Datos**
1. **Solicitud del Cliente** → Controller REST
2. **Controller** → Service Layer
3. **Service** → WebClient Reactivo
4. **WebClient** → API Externa (ChatGPT-4 / GhibliAI)
5. **Respuesta API** → Procesamiento y Almacenamiento
6. **MongoDB** → Persistencia de Resultados
7. **Respuesta Final** → Cliente

## 📚 **Dependencias Maven**

### **Dependencias Principales**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.0.2</version>
</dependency>
```

## 🚀 **Endpoints REST Disponibles**

### **ChatBotsIA API**
- `GET /api/chat/messages` - Listar todos los mensajes
- `GET /api/chat/messages/{id}` - Obtener mensaje específico
- `POST /api/chat/send` - Enviar mensaje al chatbot
- `DELETE /api/chat/messages/{id}` - Eliminar mensaje

### **GhibliAI API**
- `GET /api/ghibli` - Listar todas las imágenes generadas
- `GET /api/ghibli/{id}` - Obtener imagen específica
- `POST /api/ghibli/generate` - Generar nueva imagen
- `PUT /api/ghibli/{id}` - Actualizar imagen existente
- `DELETE /api/ghibli/{id}` - Eliminar imagen

## 📖 **Documentación Automática**

La aplicación incluye Swagger UI accesible en:
- **URL**: `http://localhost:8080/swagger-ui.html`
- **Documentación API**: `http://localhost:8080/v3/api-docs`

## 🎯 **Características Técnicas**

### **Programación Reactiva**
- **WebClient**: Cliente HTTP no bloqueante
- **Mono/Flux**: Streams reactivos para manejo de datos
- **Backpressure**: Control de flujo de datos
- **Concurrencia**: Múltiples solicitudes simultáneas

### **Manejo de Errores**
- **Reintentos Automáticos**: Para fallas temporales
- **Circuit Breaker**: Protección contra cascadas de errores
- **Logging Detallado**: Trazas completas para debugging
- **Respuestas Consistentes**: Formato unificado de errores

### **Persistencia**
- **MongoDB Atlas**: Base de datos en la nube
- **Reactive Repositories**: Operaciones asíncronas
- **Document Models**: Estructura flexible para diferentes tipos de datos

## 🔄 **Proceso de Desarrollo**

1. **Configuración del Ambiente**: Setup de MongoDB Atlas y credenciales
2. **Implementación de Services**: Lógica de consumo de APIs
3. **Desarrollo de Controllers**: Endpoints REST reactivos
4. **Modelado de Datos**: Entidades con Lombok
5. **Testing**: Validación de integraciones
6. **Documentación**: Swagger y README

## 📊 **Resultados Obtenidos**

- **✅ ChatBot Funcional**: Conversaciones con IA estilo Ghibli
- **✅ Generador de Imágenes**: Creación visual estilo Studio Ghibli
- **✅ API REST Completa**: CRUD para ambos servicios
- **✅ Persistencia Robusta**: MongoDB con datos estructurados
- **✅ Documentación Interactiva**: Swagger UI para testing
- **✅ Manejo de Errores**: Sistema robusto y tolerante a fallos

## 🚀 **Ejecución del Proyecto**

```bash
# Clonar repositorio
git clone [URL-del-repositorio]

# Navegar al proyecto
cd SistemaIAs

# Configurar variables de entorno
export CHATGPT_API_KEY=tu-api-key
export GHIBLI_API_KEY=tu-api-key

# Ejecutar aplicación
mvn spring-boot:run
```

**Acceso**: http://localhost:8080

---

**Desarrollado con Spring Boot, WebFlux, MongoDB y las mejores prácticas de integración de APIs de IA.**
