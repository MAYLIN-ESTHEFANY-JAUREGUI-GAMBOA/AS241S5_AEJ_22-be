package pe.edu.vallegrande.SistemaIAs.rest;

import pe.edu.vallegrande.SistemaIAs.model.ChatBotsIA;
import pe.edu.vallegrande.SistemaIAs.service.ChatBotsIAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chatbots")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "ChatBots IA", description = "API para gestionar conversaciones con ChatGPT")
public class ChatBotsIARestController {
    
    @Autowired
    private ChatBotsIAService chatBotsIAService;
    
    @PostMapping("/chat")
    @Operation(summary = "Enviar mensaje al chatbot", description = "Envía un mensaje a ChatGPT y guarda la conversación")
    public Mono<ResponseEntity<ChatBotsIA.ChatResponse>> sendMessage(
            @Valid @RequestBody ChatRequest chatRequest) {
        
        return chatBotsIAService.sendMessageToChatGPT(
                chatRequest.getMessage(), 
                chatRequest.getUserId(), 
                chatRequest.getSessionId())
            .map(ResponseEntity::ok);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener conversaciones por usuario", description = "Retorna todas las conversaciones de un usuario")
    public Flux<ChatBotsIA> getChatsByUserId(
            @Parameter(description = "ID del usuario") @PathVariable String userId) {
        return chatBotsIAService.findAllByUserId(userId);
    }
    
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Obtener conversación por sesión", description = "Retorna una conversación específica por su ID de sesión")
    public Mono<ResponseEntity<ChatBotsIA>> getChatBySessionId(
            @Parameter(description = "ID de la sesión") @PathVariable String sessionId) {
        return chatBotsIAService.findAllBySessionId(sessionId)
            .next()
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}/session/{sessionId}")
    @Operation(summary = "Obtener conversación específica", description = "Retorna una conversación específica de un usuario")
    public Mono<ResponseEntity<ChatBotsIA>> getChatByUserIdAndSessionId(
            @Parameter(description = "ID del usuario") @PathVariable String userId,
            @Parameter(description = "ID de la sesión") @PathVariable String sessionId) {
        return chatBotsIAService.findByUserIdAndSessionId(userId, sessionId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/session")
    @Operation(summary = "Crear nueva sesión", description = "Crea una nueva sesión de chat")
    public Mono<ResponseEntity<ChatBotsIA>> createNewSession(
            @Valid @RequestBody CreateSessionRequest request) {
        return chatBotsIAService.createNewChatSession(request.getUserId(), request.getSessionId())
            .map(ResponseEntity::ok);
    }
    
    @DeleteMapping("/user/{userId}/session/{sessionId}")
    @Operation(summary = "Eliminar sesión", description = "Elimina una sesión de chat específica")
    public Mono<ResponseEntity<Void>> deleteChatSession(
            @Parameter(description = "ID del usuario") @PathVariable String userId,
            @Parameter(description = "ID de la sesión") @PathVariable String sessionId) {
        return chatBotsIAService.deleteChatSession(userId, sessionId)
            .thenReturn(ResponseEntity.ok().build());
    }
    
    @GetMapping
    @Operation(summary = "Obtener todas las conversaciones", description = "Retorna todas las conversaciones guardadas")
    public Flux<ChatBotsIA> getAllChats() {
        return chatBotsIAService.findAll();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener conversación por ID", description = "Retorna una conversación específica por su ID")
    public Mono<ResponseEntity<ChatBotsIA>> getChatById(
            @Parameter(description = "ID de la conversación") @PathVariable String id) {
        return chatBotsIAService.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
        
    @PutMapping("/{id}/message")
    @Operation(summary = "Actualizar mensaje y obtener respuesta", description = "Actualiza el último mensaje de usuario y obtiene nueva respuesta de ChatGPT")
    public Mono<ResponseEntity<ChatBotsIA.ChatResponse>> updateMessageAndGetResponse(
            @Parameter(description = "ID de la conversación") @PathVariable String id,
            @Valid @RequestBody UpdateMessageRequest request) {
        return chatBotsIAService.updateMessageAndGetResponse(id, request.getMessage())
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar conversación por ID", description = "Elimina una conversación específica por su ID")
    public Mono<ResponseEntity<Void>> deleteChatById(
            @Parameter(description = "ID de la conversación") @PathVariable String id) {
        return chatBotsIAService.deleteById(id)
            .thenReturn(ResponseEntity.ok().build());
    }
    
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activar conversación", description = "Activa una conversación específica por su ID")
    public Mono<ResponseEntity<ChatBotsIA>> activateChat(
            @Parameter(description = "ID de la conversación") @PathVariable String id) {
        return chatBotsIAService.activateChat(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar conversación", description = "Desactiva una conversación específica por su ID")
    public Mono<ResponseEntity<ChatBotsIA>> deactivateChat(
            @Parameter(description = "ID de la conversación") @PathVariable String id) {
        return chatBotsIAService.deactivateChat(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/active")
    @Operation(summary = "Obtener conversaciones activas", description = "Retorna todas las conversaciones con estado activo")
    public Flux<ChatBotsIA> getActiveChats() {
        return chatBotsIAService.findActiveChats();
    }
    
    @GetMapping("/inactive")
    @Operation(summary = "Obtener conversaciones inactivas", description = "Retorna todas las conversaciones con estado inactivo")
    public Flux<ChatBotsIA> getInactiveChats() {
        return chatBotsIAService.findInactiveChats();
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Obtener conversaciones por estado", description = "Retorna todas las conversaciones con un estado específico")
    public Flux<ChatBotsIA> getChatsByStatus(
            @Parameter(description = "Estado de la conversación (activo/inactivo)") @PathVariable String status) {
        return chatBotsIAService.findByStatus(status);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Buscar conversaciones por rol y contenido", description = "Retorna conversaciones que contengan mensajes con un rol y contenido específicos")
    public Flux<ChatBotsIA> searchChatsByRoleAndContent(
            @Parameter(description = "Rol del mensaje (user/assistant)") @RequestParam String role,
            @Parameter(description = "Contenido del mensaje a buscar") @RequestParam String content) {
        return chatBotsIAService.findByMessageRoleAndContent(role, content);
    }
    
    // DTOs para las solicitudes
    public static class ChatRequest {
        private String message;
        private String userId;
        private String sessionId;
        
        // Getters y Setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
    
    public static class CreateSessionRequest {
        private String userId;
        private String sessionId;
        
        // Getters y Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
    
    public static class UpdateMessageRequest {
        private String message;
        
        // Getters y Setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
