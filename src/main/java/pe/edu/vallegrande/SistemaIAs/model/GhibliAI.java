package pe.edu.vallegrande.SistemaIAs.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "GhibliAI")
public class GhibliAI {
    
    @Id
    private String id;
    
    @Field("user_id")
    private String userId;
    
    @Field("prompt")
    private String prompt;
    
    @Field("style")
    private String style;
    
    @Field("aspect_ratio")
    private String aspectRatio;
    
    @Field("image_url")
    private String imageUrl;
    
    @Field("status")
    private String status; // "activo", "inactivo", "procesando", "error"
    
    @Field("processing_time_ms")
    private Long processingTimeMs;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    // DTO para la respuesta de la API
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GhibliResponse {
        private String imageUrl;
        private String status;
        
        public String getImageUrl() {
            return imageUrl;
        }
    }
}
