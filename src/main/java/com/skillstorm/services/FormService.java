package com.skillstorm.services;

import com.skillstorm.constants.EventType;
import com.skillstorm.dtos.FormDto;
import com.skillstorm.utils.DownloadResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FormService {

    // Create new Form:
    Mono<FormDto> createForm(FormDto newForm);

    // Find Form by ID:
    Mono<FormDto> findById(UUID id);

    // Update Form by ID:
    Mono<FormDto> updateById(UUID id, FormDto updatedForm);

    // Delete by ID:
    Mono<Void> deleteById(UUID id);

    // Get all EventTypes:
    Flux<EventType> getEventTypes();

    // Upload Event attachment to S3:
    Mono<FormDto> uploadEventAttachment(UUID id, String contentType, byte[] attachment);

    // Upload Supervisor pre-approval attachment to S3:
    Mono<FormDto> uploadSupervisorAttachment(UUID id, String contentType, byte[] attachment);

    // Upload Department Head pre-approval attachment to S3:
    Mono<FormDto> uploadDepartmentHeadAttachment(UUID id, String contentType, byte[] attachment);

    // Download Event attachment from S3:
    Mono<DownloadResponse> downloadEventAttachment(UUID id);

    // Download Supervisor attachment from S3:
    Mono<DownloadResponse> downloadSupervisorAttachment(UUID id);

    // Download Department Head attachment from S3:
    Mono<DownloadResponse> downloadDepartmentHeadAttachment(UUID id);

    // Submit Form for Supervisor Approval:
    Mono<FormDto> submitForSupervisorApproval(UUID id, String username);
}
