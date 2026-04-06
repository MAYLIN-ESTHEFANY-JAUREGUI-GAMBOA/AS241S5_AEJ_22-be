package pe.edu.vallegrande.SistemaIAs.repository;

import pe.edu.vallegrande.SistemaIAs.model.GhibliAI;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GhibliAIRepository extends ReactiveMongoRepository<GhibliAI, String> {
    
    // Métodos personalizados
    Flux<GhibliAI> findByUserIdOrderByCreatedAtDesc(String userId);
    
    Flux<GhibliAI> findByStyleOrderByCreatedAtDesc(String style);
    
    Mono<GhibliAI> findByUserIdAndId(String userId, String id);
    
    Flux<GhibliAI> findByStatusOrderByCreatedAtDesc(String status);
}
