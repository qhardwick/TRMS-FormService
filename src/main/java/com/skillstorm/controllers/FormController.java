package com.skillstorm.controllers;

import com.skillstorm.constants.EventType;
import com.skillstorm.dtos.FormDto;
import com.skillstorm.services.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/forms")
public class FormController {

    private final FormService formService;

    @Autowired
    public FormController(FormService formService) {
        this.formService = formService;
    }

    // Test endpoint:
    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Hello Form Service");
    }

    // Create new Form:
    @PostMapping
    public Mono<FormDto> createForm(@RequestBody FormDto newForm) {
        return formService.createForm(newForm);
    }

    // Find Form by ID:
    @GetMapping("/{id}")
    public Mono<FormDto> findById(@PathVariable("id") UUID id) {
        return formService.findById(id);
    }

    // Update Form by ID:
    @PutMapping("/{id}")
    public Mono<FormDto> updateById(@PathVariable("id") UUID id, @RequestBody FormDto updatedForm) {
        return formService.updateById(id, updatedForm);
    }

    // Delete Form by ID:
    @DeleteMapping("/{id}")
    public Mono<Void> deleteById(@PathVariable("id") UUID id) {
        return formService.deleteById(id);
    }

    // Get EventTypes. Used to populate a list for the user to choose from:
    @GetMapping("/events")
    public Flux<EventType> getEventTypes() {
        return formService.getEventTypes();
    }
}
