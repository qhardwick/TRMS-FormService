package com.skillstorm.services;

import com.skillstorm.dtos.DownloadResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
public class S3ServiceImpl implements S3Service {

    private final S3AsyncClient s3Client;
    private final S3Presigner presigner;
    private final String bucket;

    @Autowired
    public S3ServiceImpl(S3AsyncClient s3Client, S3Presigner presigner, @Value("${BUCKET}") String bucket) {
        this.s3Client = s3Client;
        this.presigner = presigner;
        this.bucket = bucket;
    }

    // Upload file to S3 Bucket:
    @Override
    public Mono<Void> uploadFile(String key, String contentType, byte[] file) {
        return Mono.fromCompletionStage(() -> {
            // Try to upload to s3:
            try {
                return s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType(contentType)
                                .build(),
                        AsyncRequestBody.fromBytes(file)

                // Handle failed attempts:
                ).whenComplete((response, exception) -> {
                    if (exception != null) {
                        // Handle exception
                        throw new RuntimeException("s3.upload.failed", exception);
                    }
                });
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }

        // Return void to signal completion:
        }).then();
    }

    // Download file from S3 Bucket:
    @Override
    public Mono<DownloadResponseDto> getObject(String key) {

        // Establish which object we're trying to pull and from which bucket:
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();


        // Send our request to S3:
        return Mono.fromCompletionStage(() -> {
            try {
                return s3Client.getObject(getObjectRequest, AsyncResponseTransformer.toBytes());
            } catch (Exception e) {
                throw new RuntimeException("s3.download.failed", e);
            }

        //TODO: Try to do this without gathering the responseBytes into an array:
        //TODO: Throw custom exception with centralized message:
        }).map(responseBytes -> {
            try (InputStream inputStream = new ByteArrayInputStream(responseBytes.asByteArray())) {
                String contentType = responseBytes.response().contentType();
                return new DownloadResponseDto(inputStream, contentType);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create input stream from S3 response", e);
            }
        });
    }

    // AWS SDK calls are all synchronous but Pre-signed Urls are lightweight and non-blocking, thus we don't
    // need to use subsribeOn(Schedulers.boundedElastic()) to reserve a synchronous thread pool. Instead we
    // can just defer the call until something subscribes to it:

    // Generate pre-signed URL to allow user upload:
    @Override
    public Mono<String> generateUploadUrl(String key, String contentType) {
        return Mono.defer(() -> {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .putObjectRequest(putRequest)
                    .signatureDuration(Duration.ofMinutes(10))
                    .build();

            String presignedUrl = presigner.presignPutObject(presignRequest).url().toString();
            return Mono.just(presignedUrl);
        });
    }

    // Generate pre-signed URL to allow user to retrieve/download file:
    @Override
    public Mono<String> generateDownloadUrl(String key) {
        return Mono.defer(() -> {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofMinutes(10))
                    .build();

            String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();
            return Mono.just(presignedUrl);
        });
    }
}
