package pe.edu.vallegrande.SistemaIAs.service;

import pe.edu.vallegrande.SistemaIAs.model.ChatBotsIA;
import pe.edu.vallegrande.SistemaIAs.repository.ChatBotsIARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Service
public class ChatBotsIAService {
    
    @Autowired
    private ChatBotsIARepository chatBotsIARepository;
    
    private final WebClient webClient;
    
    public ChatBotsIAService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<ChatBotsIA.ChatResponse> sendMessageToChatGPT(String message, String userId, String sessionId) {
        ChatBotsIA.Message userMessage = ChatBotsIA.Message.builder()
            .role("user")
            .content(message)
            .timestamp(LocalDateTime.now())
            .build();
        
        ChatRequest request = new ChatRequest(
            List.of(userMessage),
            "gpt-4o-mini"
        );
        
        return webClient.post()
            .bodyValue(request)
            .retrieve()
            .onStatus(
                status -> status.value() == 429,
                response -> Mono.error(new RuntimeException("Demasiadas solicitudes a RapidAPI. Por favor espera unos minutos."))
            )
            .bodyToMono(ChatBotsIA.ChatResponse.class)
            .flatMap(response -> saveChatSession(userId, sessionId, userMessage, response));
    }
    
    private Mono<ChatBotsIA.ChatResponse> saveChatSession(String userId, String sessionId, 
                                                        ChatBotsIA.Message userMessage, 
                                                        ChatBotsIA.ChatResponse response) {
        return findByUserIdAndSessionId(userId, sessionId)
            .switchIfEmpty(createNewChatSession(userId, sessionId))
            .flatMap(chatSession -> {
                // Agregar mensaje de usuario
                chatSession.getMessages().add(userMessage);
                
                // Agregar respuesta del asistente
                if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                    ChatBotsIA.Message assistantMessage = ChatBotsIA.Message.builder()
                        .role("assistant")
                        .content(response.getChoices().get(0).getMessage().getContent())
                        .timestamp(LocalDateTime.now())
                        .build();
                    chatSession.getMessages().add(assistantMessage);
                }
                
                chatSession.setUpdatedAt(LocalDateTime.now());
                return chatBotsIARepository.save(chatSession);
            })
            .thenReturn(response);
    }
    
    public Mono<ChatBotsIA> createNewChatSession(String userId, String sessionId) {
        ChatBotsIA newSession = ChatBotsIA.builder()
            .userId(userId)
            .sessionId(sessionId != null ? sessionId : UUID.randomUUID().toString())
            .messages(new java.util.ArrayList<>())
            .model("gpt-4o-mini")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .status("Active")
            .build();
        
        return chatBotsIARepository.save(newSession);
    }
    
    public Mono<ChatBotsIA> findByUserIdAndSessionId(String userId, String sessionId) {
        return chatBotsIARepository.findByUserIdAndSessionId(userId, sessionId);
    }
    
    public Flux<ChatBotsIA> findAllByUserId(String userId) {
        return chatBotsIARepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Flux<ChatBotsIA> findAllBySessionId(String sessionId) {
        return chatBotsIARepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
    }
    
    public Mono<Void> deleteChatSession(String userId, String sessionId) {
        return chatBotsIARepository.findByUserIdAndSessionId(userId, sessionId)
            .flatMap(chatBotsIARepository::delete);
    }
    
    public Flux<ChatBotsIA> findAll() {
        return chatBotsIARepository.findAll();
    }
    
    public Mono<ChatBotsIA> findById(String id) {
        return chatBotsIARepository.findById(id);
    }
    
        
    public Mono<ChatBotsIA.ChatResponse> updateMessageAndGetResponse(String id, String newMessage) {
        return chatBotsIARepository.findById(id)
            .flatMap(existingChat -> {
                // Actualizar o agregar el nuevo mensaje del usuario
                ChatBotsIA.Message userMessage = ChatBotsIA.Message.builder()
                    .role("user")
                    .content(newMessage)
                    .timestamp(LocalDateTime.now())
                    .build();
                
                // Si hay mensajes, reemplazar el último mensaje de usuario
                if (existingChat.getMessages() != null && !existingChat.getMessages().isEmpty()) {
                    // Buscar el último mensaje de usuario y reemplazarlo
                    for (int i = existingChat.getMessages().size() - 1; i >= 0; i--) {
                        if ("user".equals(existingChat.getMessages().get(i).getRole())) {
                            existingChat.getMessages().set(i, userMessage);
                            break;
                        }
                    }
                } else {
                    // Si no hay mensajes, agregar el nuevo
                    existingChat.setMessages(new java.util.ArrayList<>());
                    existingChat.getMessages().add(userMessage);
                }
                
                // Llamar a ChatGPT con el nuevo mensaje
                ChatRequest request = new ChatRequest(
                    List.of(userMessage),
                    "gpt-4o-mini"
                );
                
                return webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                        status -> status.value() == 429,
                        response -> Mono.error(new RuntimeException("Demasiadas solicitudes a RapidAPI. Por favor espera unos minutos."))
                    )
                    .bodyToMono(ChatBotsIA.ChatResponse.class)
                    .flatMap(response -> {
                        // Reemplazar la última respuesta del asistente
                        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                            ChatBotsIA.Message newAssistantMessage = ChatBotsIA.Message.builder()
                                .role("assistant")
                                .content(response.getChoices().get(0).getMessage().getContent())
                                .timestamp(LocalDateTime.now())
                                .build();
                            
                            // Crear nueva lista de mensajes limpia
                            List<ChatBotsIA.Message> newMessages = new ArrayList<>();
                            
                            // Buscar el último mensaje de usuario y mantenerlo
                            ChatBotsIA.Message lastUserMessage = null;
                            for (int i = existingChat.getMessages().size() - 1; i >= 0; i--) {
                                if ("user".equals(existingChat.getMessages().get(i).getRole())) {
                                    lastUserMessage = existingChat.getMessages().get(i);
                                    break;
                                }
                            }
                            
                            // Construir la nueva lista: solo el último user + el nuevo assistant
                            if (lastUserMessage != null) {
                                newMessages.add(lastUserMessage);
                            }
                            newMessages.add(newAssistantMessage);
                            
                            // Reemplazar completamente la lista de mensajes
                            existingChat.setMessages(newMessages);
                        }
                        
                        existingChat.setUpdatedAt(LocalDateTime.now());
                        return chatBotsIARepository.save(existingChat).thenReturn(response);
                    });
            });
    }
    
    public Mono<Void> deleteById(String id) {
        return chatBotsIARepository.deleteById(id);
    }
    
    public Mono<ChatBotsIA> updateChatStatus(String id, String status) {
        return chatBotsIARepository.findById(id)
            .flatMap(chat -> {
                chat.setStatus(status);
                chat.setUpdatedAt(LocalDateTime.now());
                return chatBotsIARepository.save(chat);
            });
    }
    
    public Mono<ChatBotsIA> activateChat(String id) {
        return chatBotsIARepository.findById(id)
            .flatMap(chat -> {
                chat.setStatus("activo");
                chat.setUpdatedAt(LocalDateTime.now());
                return chatBotsIARepository.save(chat);
            });
    }
    
    public Mono<ChatBotsIA> deactivateChat(String id) {
        return chatBotsIARepository.findById(id)
            .flatMap(chat -> {
                chat.setStatus("inactivo");
                chat.setUpdatedAt(LocalDateTime.now());
                return chatBotsIARepository.save(chat);
            });
    }
    
    public Flux<ChatBotsIA> findActiveChats() {
        return chatBotsIARepository.findByStatusOrderByCreatedAtDesc("activo");
    }
    
    public Flux<ChatBotsIA> findInactiveChats() {
        return chatBotsIARepository.findByStatusOrderByCreatedAtDesc("inactivo");
    }
    
    public Flux<ChatBotsIA> findByStatus(String status) {
        return chatBotsIARepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    public Flux<ChatBotsIA> findByMessageRoleAndContent(String role, String content) {
        return chatBotsIARepository.findByMessageRoleAndContent(role, content);
    }
    
    // DTO para la solicitud a la API
    public static class ChatRequest {
        private List<ChatBotsIA.Message> messages;
        private String model;
        
        public ChatRequest() {}
        
        public ChatRequest(List<ChatBotsIA.Message> messages, String model) {
            this.messages = messages;
            this.model = model;
        }
        
        public List<ChatBotsIA.Message> getMessages() { return messages; }
        public void setMessages(List<ChatBotsIA.Message> messages) { this.messages = messages; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }
}
