package com.skillstorm.services;

import com.skillstorm.constants.EventType;
import com.skillstorm.dtos.FormDto;
import com.skillstorm.exceptions.FormNotFoundException;
import com.skillstorm.exceptions.UnsupportedFileTypeException;
import com.skillstorm.repositories.FormRepository;
import com.skillstorm.utils.DownloadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class FormServiceImpl implements FormService {

    private final FormRepository formRepository;
    private final S3Service s3Service;

    @Autowired
    public FormServiceImpl(FormRepository formRepository, S3Service s3Service) {
        this.formRepository = formRepository;
        this.s3Service =s3Service;
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

    // Find all Forms:
    @Override
    public Flux<FormDto> findAll() {
        return formRepository.findAll()
                .map(FormDto::new);
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

    // Get all Event Types:
    @Override
    public Flux<EventType> getEventTypes() {
        return EventType.getEventTypes();
    }

    // Upload Event attachment to S3:
    @Override
    public Mono<FormDto> uploadEventAttachment(UUID id, String contentType, byte[] attachment) {

        // Verify attachment it of type pdf, png, jpeg, txt, or doc:
        return switch (contentType) {
            case "application/pdf", "image/png", "image/jpg", "image/jpeg", "text/plain",
                 "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    findById(id).flatMap(formDto -> uploadToS3(contentType, attachment).flatMap(key -> {
                        formDto.setAttachment(key);
                        return formRepository.save(formDto.mapToEntity()).map(FormDto::new);
                    }));

            // Handle unsupported file format:
            default ->
                Mono.error(new UnsupportedFileTypeException("attachment.format.must"));
        };
    }

    // Upload Supervisor pre-approval attachment to S3:
    @Override
    public Mono<FormDto> uploadSupervisorAttachment(UUID id, String contentType, byte[] attachment) {

        // Verify attachment is of type .msg:
        if(!"application/vnd.ms-outlook".equalsIgnoreCase(contentType)) {
            return Mono.error(new UnsupportedFileTypeException("file.msg.must"));
        }

        // Upload the attachment to S3 and set the key:
        return findById(id).flatMap(formDto -> uploadToS3(contentType, attachment).flatMap(key -> {
            formDto.setSupervisorAttachment(key);
            return formRepository.save(formDto.mapToEntity()).map(FormDto::new);
        }));
    }

    // Upload Department Head pre-approval attachment to S3:
    @Override
    public Mono<FormDto> uploadDepartmentHeadAttachment(UUID id, String contentType, byte[] attachment) {

        // Verify attachment is of type .msg:
        if(!"application/vnd.ms-outlook".equalsIgnoreCase(contentType)) {
            return Mono.error(new UnsupportedFileTypeException("file.msg.must"));
        }

        // Upload the attachment to S3 and set the key:
        return findById(id).flatMap(formDto -> uploadToS3(contentType, attachment).flatMap(key -> {
            formDto.setDepartmentHeadAttachment(key);
            return formRepository.save(formDto.mapToEntity()).map(FormDto::new);
        }));
    }

    // Download Event attachment from S3:
    @Override
    public Mono<DownloadResponse> downloadEventAttachment(UUID id) {
        return findById(id).flatMap(formDto -> s3Service.getObject(formDto.getAttachment()));
    }

    // Download Supervisor attachment from S3:
    @Override
    public Mono<DownloadResponse> downloadSupervisorAttachment(UUID id) {
        return findById(id).flatMap(formDto -> s3Service.getObject(formDto.getSupervisorAttachment()));
    }

    // Download Department Head attachment from S3:
    @Override
    public Mono<DownloadResponse> downloadDepartmentHeadAttachment(UUID id) {
        return findById(id).flatMap(formDto -> s3Service.getObject(formDto.getDepartmentHeadAttachment()));
    }

    // Submit Form for Supervisor Approval:
    @Override
    public Mono<FormDto> submitForSupervisorApproval(UUID id, String username) {
        return null;
    }

    // Method to perform the actual S3 upload:
    private Mono<String> uploadToS3(String contentType, byte[] attachment) {
        return Mono.defer(() -> {
            String key = UUID.randomUUID().toString();
            return s3Service.uploadFile(key, contentType, attachment)
                    .thenReturn(key);
        });
    }
}
