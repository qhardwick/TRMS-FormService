package com.skillstorm.repositories;

import com.skillstorm.entities.Form;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface FormRepository extends ReactiveCassandraRepository<Form, UUID> {

    // Find Form by UUID:
    @Query("SELECT * FROM form WHERE id = ?0")
    Mono<Form> findById(UUID id);

    // Find all active Forms by Username:
    @Query("SELECT * FROM form WHERE username = ?0 ALLOW FILTERING")
    Flux<Form> findAllFormsByUsername(String username);
}
