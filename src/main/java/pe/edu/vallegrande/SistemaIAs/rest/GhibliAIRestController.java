package pe.edu.vallegrande.SistemaIAs.rest;

import pe.edu.vallegrande.SistemaIAs.model.GhibliAI;
import pe.edu.vallegrande.SistemaIAs.service.GhibliAIService;
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
@RequestMapping("/api/ghibli")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Ghibli AI", description = "API para generar imágenes estilo Ghibli")
public class GhibliAIRestController {
    
    @Autowired
    private GhibliAIService ghibliAIService;
    
    @PostMapping("/generate")
    @Operation(summary = "Generar imagen Ghibli", description = "Genera una imagen estilo Ghibli basada en un prompt")
    public Mono<ResponseEntity<GhibliAI.GhibliResponse>> generateImage(
            @Valid @RequestBody GenerateImageRequest request) {
        
        return ghibliAIService.generateGhibliImage(
                request.getPrompt(), 
                request.getStyle(), 
                request.getAspectRatio(),
                request.getUserId())
            .map(ResponseEntity::ok)
            .onErrorResume(RuntimeException.class, e -> {
                if (e.getMessage().contains("Error: No se pudo generar la imagen")) {
                    return Mono.just(ResponseEntity.badRequest().build());
                } else if (e.getMessage().contains("Error procesando la respuesta")) {
                    return Mono.just(ResponseEntity.status(500).build());
                } else {
                    return Mono.just(ResponseEntity.status(500).build());
                }
            });
    }
    
    @GetMapping
    @Operation(summary = "Obtener todas las imágenes", description = "Retorna todas las imágenes generadas")
    public Flux<GhibliAI> getAllImages() {
        return ghibliAIService.findAll();
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar imagen Ghibli", description = "Actualiza una imagen existente generando una nueva con la IA")
    public Mono<ResponseEntity<GhibliAI.GhibliResponse>> updateImage(
            @PathVariable String id,
            @Valid @RequestBody GenerateImageRequest request) {
        
        return ghibliAIService.updateGhibliImage(id, request.getPrompt(), request.getStyle(), request.getAspectRatio(), request.getUserId())
            .map(ResponseEntity::ok)
            .onErrorResume(RuntimeException.class, e -> {
                if (e.getMessage().contains("Error: No se pudo generar la imagen")) {
                    return Mono.just(ResponseEntity.badRequest().build());
                } else if (e.getMessage().contains("Error procesando la respuesta")) {
                    return Mono.just(ResponseEntity.status(500).build());
                } else if (e.getMessage().contains("Imagen no encontrada")) {
                    return Mono.just(ResponseEntity.notFound().build());
                } else {
                    return Mono.just(ResponseEntity.status(500).build());
                }
            });
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener imagen por ID", description = "Retorna una imagen específica por su ID")
    public Mono<ResponseEntity<GhibliAI>> getImageById(
            @Parameter(description = "ID de la imagen") @PathVariable String id) {
        return ghibliAIService.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener imágenes por usuario", description = "Retorna todas las imágenes de un usuario")
    public Flux<GhibliAI> getImagesByUserId(
            @Parameter(description = "ID del usuario") @PathVariable String userId) {
        return ghibliAIService.findByUserId(userId);
    }
    
    @GetMapping("/style/{style}")
    @Operation(summary = "Obtener imágenes por estilo", description = "Retorna todas las imágenes por estilo")
    public Flux<GhibliAI> getImagesByStyle(
            @Parameter(description = "Estilo de imagen") @PathVariable String style) {
        return ghibliAIService.findByStyle(style);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar imagen por ID", description = "Elimina una imagen específica por su ID")
    public Mono<ResponseEntity<Void>> deleteImageById(
            @Parameter(description = "ID de la imagen") @PathVariable String id) {
        return ghibliAIService.deleteById(id)
            .thenReturn(ResponseEntity.ok().build());
    }
    
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activar imagen", description = "Activa una imagen específica por su ID")
    public Mono<ResponseEntity<GhibliAI>> activateImage(
            @Parameter(description = "ID de la imagen") @PathVariable String id) {
        return ghibliAIService.activateGhibli(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar imagen", description = "Desactiva una imagen específica por su ID")
    public Mono<ResponseEntity<GhibliAI>> deactivateImage(
            @Parameter(description = "ID de la imagen") @PathVariable String id) {
        return ghibliAIService.deactivateGhibli(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/active")
    @Operation(summary = "Obtener imágenes activas", description = "Retorna todas las imágenes con estado activo")
    public Flux<GhibliAI> getActiveImages() {
        return ghibliAIService.findActiveGhibli();
    }
    
    @GetMapping("/inactive")
    @Operation(summary = "Obtener imágenes inactivas", description = "Retorna todas las imágenes con estado inactivo")
    public Flux<GhibliAI> getInactiveImages() {
        return ghibliAIService.findInactiveGhibli();
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Obtener imágenes por estado", description = "Retorna todas las imágenes con un estado específico")
    public Flux<GhibliAI> getImagesByStatus(
            @Parameter(description = "Estado de la imagen (activo/inactivo/procesando/error)") @PathVariable String status) {
        return ghibliAIService.findByStatus(status);
    }
    
    // DTOs para las solicitudes
    public static class GenerateImageRequest {
        private String prompt;
        private String style;
        private String aspectRatio;
        private String userId;
        
        // Getters y Setters
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        
        public String getStyle() { return style; }
        public void setStyle(String style) { this.style = style; }
        
        public String getAspectRatio() { return aspectRatio; }
        public void setAspectRatio(String aspectRatio) { this.aspectRatio = aspectRatio; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
}
