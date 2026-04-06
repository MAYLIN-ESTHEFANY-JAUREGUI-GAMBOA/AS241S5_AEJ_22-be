package pe.edu.vallegrande.SistemaIAs.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "ChatBotsIA")
public class ChatBotsIA {
    
    @Id
    private String id;
    
    @Field("user_id")
    private String userId;
    
    @Field("session_id")
    private String sessionId;
    
    @Field("messages")
    private List<Message> messages;
    
    @Field("model")
    private String model;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("status")
    private String status; // "activo" o "inactivo"
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        @Field("role")
        private String role; // "user" o "assistant"
        
        @Field("content")
        private String content;
        
        @Field("timestamp")
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatResponse {
        private String id;
        private String object;
        private Long created;
        private String model;
        private List<Choice> choices;
        private Usage usage;
        private String systemFingerprint;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Choice {
            private String finishReason;
            private Integer index;
            private Message message;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Usage {
            private Integer promptTokens;
            private Integer completionTokens;
            private Integer totalTokens;
        }
    }
}
