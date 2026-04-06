package pe.edu.vallegrande.SistemaIAs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Value("${rapidapi.chat.url}")
    private String rapidApiUrl;
    
    @Value("${rapidapi.chat.key}")
    private String rapidApiKey;
    
    @Value("${rapidapi.chat.host}")
    private String rapidApiHost;
    
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .baseUrl(rapidApiUrl)
            .defaultHeader("X-RapidAPI-Key", rapidApiKey)
            .defaultHeader("X-RapidAPI-Host", rapidApiHost)
            .defaultHeader("Content-Type", "application/json");
    }
}
