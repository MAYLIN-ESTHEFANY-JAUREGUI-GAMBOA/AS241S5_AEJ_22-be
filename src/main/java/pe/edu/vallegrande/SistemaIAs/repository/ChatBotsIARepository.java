package pe.edu.vallegrande.SistemaIAs.repository;

import pe.edu.vallegrande.SistemaIAs.model.ChatBotsIA;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ChatBotsIARepository extends ReactiveMongoRepository<ChatBotsIA, String> {
    
    Flux<ChatBotsIA> findByUserId(String userId);
    
    Flux<ChatBotsIA> findBySessionId(String sessionId);
    
    Mono<ChatBotsIA> findByUserIdAndSessionId(String userId, String sessionId);
    
    Flux<ChatBotsIA> findByUserIdOrderByCreatedAtDesc(String userId);
    
    Flux<ChatBotsIA> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    Flux<ChatBotsIA> findByStatus(String status);
    
    Flux<ChatBotsIA> findByStatusOrderByCreatedAtDesc(String status);
    
    @Query("{ 'messages': { $elemMatch: { 'role': ?0, 'content': { $regex: ?1, $options: 'i' } } } }")
    Flux<ChatBotsIA> findByMessageRoleAndContent(String role, String content);
}
