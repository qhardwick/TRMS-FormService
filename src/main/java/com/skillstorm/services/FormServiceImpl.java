package com.skillstorm.services;

import com.skillstorm.constants.EventType;
import com.skillstorm.constants.Queues;
import com.skillstorm.constants.Status;
import com.skillstorm.dtos.FormDto;
import com.skillstorm.dtos.MessageDto;
import com.skillstorm.exceptions.FormNotFoundException;
import com.skillstorm.exceptions.UnsupportedFileTypeException;
import com.skillstorm.repositories.FormRepository;
import com.skillstorm.utils.DownloadResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FormServiceImpl implements FormService {

    private final FormRepository formRepository;
    private final S3Service s3Service;
    private final RabbitTemplate rabbitTemplate;
    private final Map<String, MonoSink<String>> correlationMap;

    @Autowired
    public FormServiceImpl(FormRepository formRepository, S3Service s3Service, RabbitTemplate rabbitTemplate) {
        this.formRepository = formRepository;
        this.s3Service =s3Service;
        this.rabbitTemplate = rabbitTemplate;
        this.correlationMap = new ConcurrentHashMap<>();
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
        // Pull the Form from the database and update its status to REQUESTED:
        return findById(id).flatMap(formDto -> {
            formDto.setStatus(Status.REQUESTED);

            // If Form contains Supervisor preapproval attachment, move on to Department Head approval:
            if(formDto.getSupervisorAttachment() != null) {
                return formRepository.save(formDto.mapToEntity())
                        .then(submitForDepartmentHeadApproval(id, username));
            }

            // Otherwise, post message to Supervisor Inbox:
            return getSupervisor(username)
                    .map(supervisor -> sendMessageToInbox(id, supervisor))
                    .then(formRepository.save(formDto.mapToEntity())).map(FormDto::new);
        });
    }

    // Submit Form for Department Head approval:
    public Mono<FormDto> submitForDepartmentHeadApproval(UUID id, String username) {
        return findById(id).flatMap(formDto -> {
            if(formDto.getDepartmentHeadAttachment() != null) {
                return sendToBenefitsCoordinator(id, username)
                        .thenReturn(formDto);
            }
            return sendToDepartmentHead(id, username)
                    .thenReturn(formDto);
        });
    }

    // Query for Supervisor from User-Service:
    private Mono<String> getSupervisor(String username) {
        return Mono.create(sink -> {
            String correlationId = UUID.randomUUID().toString();
            correlationMap.put(correlationId, sink);
            rabbitTemplate.convertAndSend(Queues.SUPERVISOR_LOOKUP.getQueue(), username, correlationId);
        });
    }

    // Return Supervisor's username to getSupervisor:
    @RabbitListener(queues = "supervisor-response-queue")
    private Mono<Void> awaitSupervisorResponse(@Payload String supervisor, @Header(AmqpHeaders.CORRELATION_ID) String correlationId) {
        MonoSink<String> sink = correlationMap.remove(correlationId);
        if(sink != null) {
            sink.success(supervisor);
        }
        return Mono.empty();
    }

    // Send a message to a User's Inbox:
    private Mono<Void> sendMessageToInbox(UUID id, String username) {
        MessageDto message = new MessageDto(id, username);
        return Mono.fromRunnable(() -> rabbitTemplate.convertAndSend(Queues.INBOX.getQueue(), message));
    }


    // Send to Department Head's Inbox:
    private Mono<Void> sendToDepartmentHead(UUID id, String username) {
        return Mono.empty();
    }

    // Send to BENCO's Inbox:
    private Mono<Void> sendToBenefitsCoordinator(UUID id, String username) {
        return Mono.empty();
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
