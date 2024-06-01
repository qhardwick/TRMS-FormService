package com.skillstorm.repositories;

import com.skillstorm.entities.Form;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FormRepository extends ReactiveCassandraRepository<Form, UUID> {
}
