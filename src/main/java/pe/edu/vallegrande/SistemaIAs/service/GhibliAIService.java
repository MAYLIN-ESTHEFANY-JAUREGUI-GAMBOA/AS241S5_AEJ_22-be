package pe.edu.vallegrande.SistemaIAs.service;

import pe.edu.vallegrande.SistemaIAs.model.GhibliAI;
import pe.edu.vallegrande.SistemaIAs.repository.GhibliAIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GhibliAIService {
    
    @Autowired
    private GhibliAIRepository ghibliAIRepository;
    
    private final WebClient webClient;
    
    @Value("${rapidapi.ghibli.url}")
    private String ghibliApiUrl;
    
    @Value("${rapidapi.ghibli.key}")
    private String ghibliApiKey;
    
    @Value("${rapidapi.ghibli.host}")
    private String ghibliApiHost;
    
    @Autowired
    public GhibliAIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    private WebClient getWebClient() {
        return webClient.mutate()
            .defaultHeader("x-rapidapi-key", ghibliApiKey)
            .defaultHeader("x-rapidapi-host", ghibliApiHost)
            .defaultHeader("Content-Type", "application/json")
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB
            .build();
    }
    
    public Mono<GhibliAI.GhibliResponse> generateGhibliImage(String prompt, String style, String aspectRatio, String userId) {
        long startTime = System.currentTimeMillis();
        
        GhibliAI ghibliAI = GhibliAI.builder()
            .userId(userId)
            .prompt(prompt)
            .style(style)
            .aspectRatio(aspectRatio)
            .status("procesando") // Procesando
            .createdAt(LocalDateTime.now())
            .build();
        
        // Guardar registro inicial
        return ghibliAIRepository.save(ghibliAI)
            .flatMap(savedGhibli -> {
                // Llamar a la API de Ghibli
                GhibliRequest request = new GhibliRequest(prompt, style, aspectRatio);
                
                return getWebClient().post()
                    .uri(ghibliApiUrl)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                        status -> status.value() == 429,
                        response -> Mono.error(new RuntimeException("Demasiadas solicitudes a RapidAPI. Por favor espera unos minutos."))
                    )
                    .bodyToMono(String.class) // First get the raw response
                    .timeout(java.time.Duration.ofSeconds(30))
                    .retry(1)
                    .flatMap(rawResponse -> {
                        // Log the raw response for debugging
                        System.out.println("Raw API Response: " + rawResponse);
                        
                        // Try to parse as GhibliResponse
                        try {
                            // Simple parsing to extract image URL
                            String imageUrl = extractImageUrlFromResponse(rawResponse);
                            
                            // Validar si la imagen se generó correctamente
                            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                                // Si no hay URL, eliminar el registro y devolver error
                                return ghibliAIRepository.delete(savedGhibli)
                                    .then(Mono.error(new RuntimeException("Error: No se pudo generar la imagen")));
                            }
                            
                            GhibliAI.GhibliResponse response = GhibliAI.GhibliResponse.builder()
                                .imageUrl(imageUrl)
                                .status("activo")
                                .build();
                            return Mono.just(response);
                        } catch (Exception e) {
                            System.err.println("Error parsing response: " + e.getMessage());
                            // Eliminar el registro y devolver error
                            return ghibliAIRepository.delete(savedGhibli)
                                .then(Mono.error(new RuntimeException("Error procesando la respuesta de la API")));
                        }
                    })
                    .doOnError(error -> {
                        // Eliminar el registro en caso de error
                        System.err.println("Error durante la generación: " + error.getMessage());
                        ghibliAIRepository.delete(savedGhibli).subscribe();
                    })
                    .onErrorResume(error -> {
                        // Propagar el error para que el controlador lo maneje
                        return Mono.error(error);
                    })
                    .flatMap(response -> {
                        // Solo guardar si la respuesta es exitosa
                        if (response.getImageUrl() == null || "error".equals(response.getStatus())) {
                            return Mono.error(new RuntimeException("Error: La respuesta no contiene una imagen válida"));
                        }
                        
                        // Descargar y guardar la imagen localmente
                        return downloadAndSaveImage(response.getImageUrl())
                            .flatMap(localImageUrl -> {
                                // Actualizar con la URL local
                                savedGhibli.setImageUrl(localImageUrl);
                                savedGhibli.setStatus(response.getStatus());
                                savedGhibli.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                                savedGhibli.setUpdatedAt(LocalDateTime.now());
                                return ghibliAIRepository.save(savedGhibli).thenReturn(response);
                            })
                            .onErrorResume(e -> {
                                // Si falla la descarga, usar la URL original (fallback)
                                System.err.println("Error descargando imagen, usando URL original: " + e.getMessage());
                                savedGhibli.setImageUrl(response.getImageUrl());
                                savedGhibli.setStatus(response.getStatus());
                                savedGhibli.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                                savedGhibli.setUpdatedAt(LocalDateTime.now());
                                return ghibliAIRepository.save(savedGhibli).thenReturn(response);
                            });
                    });
            });
    }
    
    // Métodos CRUD
    public Flux<GhibliAI> findAll() {
        return ghibliAIRepository.findAll();
    }
    
    public Mono<GhibliAI> findById(String id) {
        return ghibliAIRepository.findById(id);
    }
    
    public Flux<GhibliAI> findByUserId(String userId) {
        return ghibliAIRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Flux<GhibliAI> findByStyle(String style) {
        return ghibliAIRepository.findByStyleOrderByCreatedAtDesc(style);
    }
    
    public Mono<Void> deleteById(String id) {
        return ghibliAIRepository.deleteById(id);
    }
    
    public Mono<GhibliAI.GhibliResponse> updateGhibliImage(String id, String prompt, String style, String aspectRatio, String userId) {
        // Primero verificar si el documento existe
        return ghibliAIRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Imagen no encontrada con ID: " + id)))
            .flatMap(existingImage -> {
                // Actualizar los campos del documento existente
                existingImage.setPrompt(prompt);
                existingImage.setStyle(style != null ? style : "ghibli");
                existingImage.setAspectRatio(aspectRatio != null ? aspectRatio : "1:1");
                existingImage.setUserId(userId != null ? userId : existingImage.getUserId());
                existingImage.setStatus("procesando"); // En procesamiento
                existingImage.setImageUrl(null); // Limpiar URL anterior
                existingImage.setUpdatedAt(LocalDateTime.now());
                
                // Generar nueva imagen
                return generateNewImageForUpdate(existingImage);
            });
    }
    
    private Mono<GhibliAI.GhibliResponse> generateNewImageForUpdate(GhibliAI ghibliAI) {
        long startTime = System.currentTimeMillis();
        
        // Guardar el registro actualizado con estado de procesamiento
        return ghibliAIRepository.save(ghibliAI)
            .flatMap(savedGhibli -> {
                // Crear solicitud para la API
                GhibliRequest request = new GhibliRequest(savedGhibli.getPrompt(), savedGhibli.getStyle(), savedGhibli.getAspectRatio());
                
                return getWebClient().post()
                    .uri(ghibliApiUrl)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                        status -> status.value() == 429,
                        response -> Mono.error(new RuntimeException("Demasiadas solicitudes a RapidAPI. Por favor espera unos minutos."))
                    )
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofSeconds(30))
                    .retry(1)
                    .flatMap(rawResponse -> {
                        System.out.println("Raw API Response for Update: " + rawResponse);
                        
                        try {
                            String imageUrl = extractImageUrlFromResponse(rawResponse);
                            
                            // Validar si la imagen se generó correctamente
                            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                                // Si no hay URL, revertir al estado anterior y devolver error
                                return ghibliAIRepository.save(savedGhibli)
                                    .then(Mono.error(new RuntimeException("Error: No se pudo generar la imagen")));
                            }
                            
                            GhibliAI.GhibliResponse response = GhibliAI.GhibliResponse.builder()
                                .imageUrl(imageUrl)
                                .status("activo")
                                .build();
                            return Mono.just(response);
                        } catch (Exception e) {
                            System.err.println("Error parsing response: " + e.getMessage());
                            // Revertir al estado anterior y devolver error
                            return ghibliAIRepository.save(savedGhibli)
                                .then(Mono.error(new RuntimeException("Error procesando la respuesta de la API")));
                        }
                    })
                    .doOnError(error -> {
                        // Revertir al estado anterior en caso de error
                        System.err.println("Error durante la actualización: " + error.getMessage());
                        ghibliAIRepository.save(savedGhibli).subscribe();
                    })
                    .onErrorResume(error -> {
                        // Propagar el error para que el controlador lo maneje
                        return Mono.error(error);
                    })
                    .flatMap(apiResponse -> {
                        // Solo guardar si la respuesta es exitosa
                        if (apiResponse.getImageUrl() == null || "error".equals(apiResponse.getStatus())) {
                            return Mono.error(new RuntimeException("Error: La respuesta no contiene una imagen válida"));
                        }
                        
                        // Descargar y guardar la imagen localmente
                        return downloadAndSaveImage(apiResponse.getImageUrl())
                            .flatMap(localImageUrl -> {
                                // Actualizar el registro con la URL local
                                long endTime = System.currentTimeMillis();
                                savedGhibli.setImageUrl(localImageUrl);
                                savedGhibli.setStatus(apiResponse.getStatus());
                                savedGhibli.setProcessingTimeMs(endTime - startTime);
                                savedGhibli.setUpdatedAt(LocalDateTime.now());
                                
                                return ghibliAIRepository.save(savedGhibli)
                                    .thenReturn(apiResponse);
                            })
                            .onErrorResume(e -> {
                                // Si falla la descarga, usar la URL original (fallback)
                                System.err.println("Error descargando imagen, usando URL original: " + e.getMessage());
                                long endTime = System.currentTimeMillis();
                                savedGhibli.setImageUrl(apiResponse.getImageUrl());
                                savedGhibli.setStatus(apiResponse.getStatus());
                                savedGhibli.setProcessingTimeMs(endTime - startTime);
                                savedGhibli.setUpdatedAt(LocalDateTime.now());
                                
                                return ghibliAIRepository.save(savedGhibli)
                                    .thenReturn(apiResponse);
                            });
                    });
            });
    }
    
    // Helper method to extract image URL from various response formats
    private String extractImageUrlFromResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return null;
        }
        
        System.out.println("Attempting to extract URL from: " + rawResponse);
        
        // Try to parse as JSON and extract common image URL fields
        try {
            // Simple string-based extraction - solo buscar imageUrl
            if (rawResponse.contains("\"imageUrl\":")) {
                int start = rawResponse.indexOf("\"imageUrl\":\"") + 12;
                int end = rawResponse.indexOf("\"", start);
                if (end > start) {
                    String url = rawResponse.substring(start, end);
                    System.out.println("Found imageUrl: " + url);
                    return url;
                }
            }
            
            // Si no encuentra imageUrl, buscar otros formatos pero asignarlos a imageUrl
            if (rawResponse.contains("\"image_url\":")) {
                int start = rawResponse.indexOf("\"image_url\":\"") + 13;
                int end = rawResponse.indexOf("\"", start);
                if (end > start) {
                    String url = rawResponse.substring(start, end);
                    System.out.println("Found image_url: " + url);
                    return url;
                }
            }
            
            if (rawResponse.contains("\"url\":")) {
                int start = rawResponse.indexOf("\"url\":\"") + 7;
                int end = rawResponse.indexOf("\"", start);
                if (end > start) {
                    String url = rawResponse.substring(start, end);
                    System.out.println("Found url: " + url);
                    return url;
                }
            }
            
            // If it's a plain URL string
            if (rawResponse.startsWith("http")) {
                String url = rawResponse.trim();
                System.out.println("Found plain URL: " + url);
                return url;
            }
        } catch (Exception e) {
            System.err.println("Error extracting image URL: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("No URL found in response");
        return null;
    }
    
    // DTO para la solicitud a la API
    public static class GhibliRequest {
        private String prompt;
        private String prompt_enhanced;
        private String negative_prompt;
        private Integer width;
        private Integer height;
        private String style;
        private String aspect_ratio;
        
        public GhibliRequest() {}
        
        public GhibliRequest(String prompt, String style, String aspectRatio) {
            this.prompt = prompt;
            this.prompt_enhanced = prompt + " in Studio Ghibli style, beautiful anime art, soft colors, magical atmosphere";
            this.negative_prompt = "blurry, low quality, distorted, ugly";
            this.width = 512;
            this.height = 512;
            this.style = style != null ? style : "ghibli";
            this.aspect_ratio = aspectRatio != null ? aspectRatio : "1:1";
        }
        
        // Getters y Setters
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        
        public String getPrompt_enhanced() { return prompt_enhanced; }
        public void setPrompt_enhanced(String prompt_enhanced) { this.prompt_enhanced = prompt_enhanced; }
        
        public String getNegative_prompt() { return negative_prompt; }
        public void setNegative_prompt(String negative_prompt) { this.negative_prompt = negative_prompt; }
        
        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }
        
        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }
        
        public String getStyle() { return style; }
        public void setStyle(String style) { this.style = style; }
        
        public String getAspect_ratio() { return aspect_ratio; }
        public void setAspect_ratio(String aspect_ratio) { this.aspect_ratio = aspect_ratio; }
    }
    
    // Métodos para manejar status
    public Mono<GhibliAI> activateGhibli(String id) {
        return ghibliAIRepository.findById(id)
            .flatMap(ghibli -> {
                ghibli.setStatus("activo");
                ghibli.setUpdatedAt(LocalDateTime.now());
                return ghibliAIRepository.save(ghibli);
            });
    }
    
    public Mono<GhibliAI> deactivateGhibli(String id) {
        return ghibliAIRepository.findById(id)
            .flatMap(ghibli -> {
                ghibli.setStatus("inactivo");
                ghibli.setUpdatedAt(LocalDateTime.now());
                return ghibliAIRepository.save(ghibli);
            });
    }
    
    public Flux<GhibliAI> findActiveGhibli() {
        return ghibliAIRepository.findByStatusOrderByCreatedAtDesc("activo");
    }
    
    public Flux<GhibliAI> findInactiveGhibli() {
        return ghibliAIRepository.findByStatusOrderByCreatedAtDesc("inactivo");
    }
    
    public Flux<GhibliAI> findByStatus(String status) {
        return ghibliAIRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    // Método para descargar y guardar imagen localmente
    private Mono<String> downloadAndSaveImage(String imageUrl) {
        return Mono.fromCallable(() -> {
            try {
                // Crear directorio si no existe
                Path uploadDir = Paths.get("uploads/ghibli");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                
                // Generar nombre único para el archivo
                String fileExtension = imageUrl.contains(".png") ? ".png" : 
                                     imageUrl.contains(".jpg") ? ".jpg" : 
                                     imageUrl.contains(".jpeg") ? ".jpeg" : ".png";
                String fileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = uploadDir.resolve(fileName);
                
                // Descargar imagen
                URL url = new URL(imageUrl);
                InputStream inputStream = url.openStream();
                FileOutputStream outputStream = new FileOutputStream(filePath.toFile());
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                inputStream.close();
                outputStream.close();
                
                System.out.println("Imagen descargada y guardada: " + filePath);
                
                // Devolver URL local
                return "http://localhost:8080/uploads/ghibli/" + fileName;
            } catch (Exception e) {
                System.err.println("Error descargando imagen: " + e.getMessage());
                throw new RuntimeException("Error descargando imagen: " + e.getMessage());
            }
        });
    }
}
