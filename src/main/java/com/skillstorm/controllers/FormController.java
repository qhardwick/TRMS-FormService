package com.skillstorm.controllers;

import com.skillstorm.constants.EventType;
import com.skillstorm.constants.GradeFormat;
import com.skillstorm.dtos.DenialDto;
import com.skillstorm.dtos.FormDto;
import com.skillstorm.services.FormService;
import jakarta.validation.Valid;
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

    // Find all Forms. Test utility for now, but may eventually return all Forms created by a specific User:
    @GetMapping
    public Flux<FormDto> findAll() {
        return formService.findAll();
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

    // Get GradeFormats. Used to populate a list for the user to choose from:
    @GetMapping("/grade-formats")
    public Flux<GradeFormat> getGradingFormats() {
        return formService.getGradingFormats();
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

    // Submit Form for Department Head approval (should only be used by Supervisor):
    @PostMapping("/{id}/submit-to-department-head")
    public Mono<FormDto> submitToDepartmentHead(@PathVariable("id") UUID id, @RequestHeader("username") String supervisor) {
        return formService.submitForDepartmentHeadApproval(id, supervisor);
    }

    // Submit Form for Benco approval (should only be used by Department Head):
    @PostMapping("/{id}/submit-to-benco")
    public Mono<FormDto> submitToBenco(@PathVariable("id") UUID id, @RequestHeader("username") String departmentHead) {
        return formService.submitForBencoApproval(id, departmentHead);
    }

    // Deny the reimbursement request:
    // TODO: 'Approver' username in header?
    @PutMapping("/{id}/deny")
    public Mono<FormDto> denyRequest(@PathVariable("id") UUID id, @Valid @RequestBody DenialDto denialDto) {
        return formService.denyRequest(id, denialDto.getReason());
    }
}
