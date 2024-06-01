package com.skillstorm.services;

import com.skillstorm.dtos.FormDto;
import com.skillstorm.exceptions.FormNotFoundException;
import com.skillstorm.repositories.FormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class FormServiceImpl implements FormService {

    private final FormRepository formRepository;

    @Autowired
    public FormServiceImpl(FormRepository formRepository) {
        this.formRepository = formRepository;
    }

    // Create new Form:
    @Override
    public Mono<FormDto> createForm(FormDto newForm) {
        return formRepository.save(newForm.mapToEntity())
                .map(FormDto::new);
    }

    // Find Form by ID:
    @Override
    public Mono<FormDto> findById(UUID id) {
        return formRepository.findById(id)
                .map(FormDto::new)
                .switchIfEmpty(Mono.error(new FormNotFoundException("form.not.found", id)));
    }

    // Update Form by ID:
    // TODO: Check for existence prior to saving
    @Override
    public Mono<FormDto> updateById(UUID id, FormDto updatedForm) {
        updatedForm.setId(id);
        return formRepository.save(updatedForm.mapToEntity())
                .map(FormDto::new);
    }

    // Delete Form by ID:
    // TODO: Check for existence prior to deleting
    @Override
    public Mono<Void> deleteById(UUID id) {
        return formRepository.deleteById(id);
    }
}
