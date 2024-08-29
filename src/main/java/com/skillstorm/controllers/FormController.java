package com.skillstorm.controllers;

import com.skillstorm.constants.EventType;
import com.skillstorm.constants.GradeFormat;
import com.skillstorm.constants.Status;
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

    // View all of a User's Forms. Optionally filter by status:
    @GetMapping("/active")
    public Flux<FormDto> findAllFormsByUsernameAndStatus(@RequestParam(value = "status", required = false) String status, @RequestHeader("username") String username) {
        return formService.findAllFormsByUsernameAndStatus(username, status);
    }

    // Update Form by ID:
    @PutMapping("/{id}")
    public Mono<FormDto> updateById(@PathVariable("id") UUID id, @Valid @RequestBody FormDto updatedForm) {
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

    // Get Status types. Used to populate a list for the User to use as a filter:
    @GetMapping("statuses")
    public Flux<Status> getAllStatuses() {
        return formService.getAllStatuses();
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

    // Upload completion attachment after event, proving satisfactory performance:
    @PostMapping("{id}/completion-attachment")
    public Mono<FormDto> submitCompletionAttachment(@PathVariable("id") UUID id, @RequestHeader("Content-Type") String contentType, @RequestBody byte[] completionAttachment) {
        return formService.submitCompletionAttachment(id, contentType, completionAttachment);
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

    // Submit Form for Approval:
    @PutMapping("/{id}/submit")
    public Mono<FormDto> submit(@PathVariable("id") UUID id, @RequestHeader("username") String username) {
        return formService.submitForApproval(id, username);
    }

    // Supervisor approve request:
    @PutMapping("/{id}/supervisor-approve")
    public Mono<FormDto> supervisorApproval(@PathVariable("id") UUID id, @RequestHeader("username") String supervisor) {
        return formService.supervisorApprove(id, supervisor);
    }

    // Department Head approve request:
    @PutMapping("/{id}/department-head-approve")
    public Mono<FormDto> departmentHeadApproval(@PathVariable("id") UUID id, @RequestHeader("username") String departmentHead) {
        return formService.departmentHeadApprove(id, departmentHead);
    }

    // Deny the reimbursement request:
    // TODO: 'Approver' username in header?
    @PutMapping("/{id}/deny")
    public Mono<FormDto> denyRequest(@PathVariable("id") UUID id, @Valid @RequestBody DenialDto denialDto) {
        return formService.denyRequest(id, denialDto.getReason());
    }

    // Benco approve request. Still pending and requires passing grade / presentation to be granted:
    // TODO: Verify that approver is a Benco either here or in service method:
    @PutMapping("/{id}/benco-approve")
    public Mono<FormDto> bencoApproval(@PathVariable("id") UUID id, @RequestHeader("username") String benco) {
        return formService.bencoApprove(id);
    }

    // Awards the reimbursement to the User after satisfactory completion of the event:
    // TODO: Verify that the approver is either a Benco or a Department Head
    // TODO: Consider recalculating reimbursement amount prior to finishing as a simpler means of handling the canceled Pending form scenario
    @PutMapping("/{id}/award-reimbursement")
    public Mono<FormDto> awardReimbursement(@PathVariable("id") UUID id, @RequestHeader("username") String approver) {
        return formService.awardReimbursement(id);
    }

    // Cancel a Reimbursement request. Only if has not yet been awarded:
    //TODO: If Form was Pending, Recalculate other Pending forms to utilize any freed Reimbursement allowance?
    @DeleteMapping("/{id}/cancel")
    public Mono<Void> cancelRequest(@PathVariable("id") UUID id) {
        return formService.cancelRequest(id);
    }
}
