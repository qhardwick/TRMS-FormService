package com.skillstorm.controllers;

import com.skillstorm.constants.EventType;
import com.skillstorm.dtos.FormDto;
import com.skillstorm.services.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
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

    // Upload Event attachment to S3:
    @PostMapping("/{id}/attachment")
    public Mono<FormDto> uploadEventAttachment(@PathVariable("id") UUID id, @RequestHeader("Content-Type") String contentType, @RequestBody byte[] attachment) {
        return formService.uploadEventAttachment(id, contentType, attachment);
    }

    // Upload Supervisor preapproval to S3:
    @PostMapping("/{id}/supervisor-attachment")
    public Mono<FormDto> uploadSupervisorAttachment(@PathVariable("id") UUID id, @RequestHeader("Content-Type") String contentType, @RequestBody byte[] attachment) {
        return formService.uploadSupervisorAttachment(id, contentType, attachment);
    }

    // Upload Department Head preapproval to S3:
    @PostMapping("/{id}/department-head-attachment")
    public Mono<FormDto> uploadDepartmentHeadAttachment(@PathVariable("id") UUID id, @RequestHeader("Content-Type") String contentType, @RequestBody byte[] attachment) {
        return formService.uploadDepartmentHeadAttachment(id, contentType, attachment);
    }

    // Download Event attachment from S3:
    @GetMapping("/{id}/attachment")
    public Mono<ResponseEntity<Resource>> downloadEventAttachment(@PathVariable("id") UUID id) {
        return formService.downloadEventAttachment(id).map(downloadResponse -> {
            InputStream inputStream = downloadResponse.getInputStream();
            String contentType = downloadResponse.getContentType();
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        });
    }

    // Download Supervisor preapproval from S3:
    @GetMapping("/{id}/supervisor-attachment")
    public Mono<ResponseEntity<Resource>> downloadSupervisorAttachment(@PathVariable("id") UUID id) {
        return formService.downloadSupervisorAttachment(id).map(downloadResponse -> {
            InputStream inputStream = downloadResponse.getInputStream();
            String contentType = downloadResponse.getContentType();
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        });
    }

    // Download Department Head preapproval from S3:
    @GetMapping("/{id}/department-head-attachment")
    public Mono<ResponseEntity<Resource>> downloadDepartmentHeadAttachment(@PathVariable("id") UUID id) {
        return formService.downloadDepartmentHeadAttachment(id).map(downloadResponse -> {
            InputStream inputStream = downloadResponse.getInputStream();
            String contentType = downloadResponse.getContentType();
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        });
    }

    // Submit Form for Supervisor Approval:
    @PostMapping("/{id}/submit")
    public Mono<FormDto> submit(@PathVariable("id") UUID id, @RequestHeader("username") String username) {
        return formService.submitForSupervisorApproval(id, username);
    }
}
