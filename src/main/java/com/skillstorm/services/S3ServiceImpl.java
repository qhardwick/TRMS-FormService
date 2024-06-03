package com.skillstorm.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

@Service
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final String bucket;

    @Autowired
    public S3ServiceImpl(S3Client s3Client, @Value("${BUCKET}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    // Upload file to S3 Bucket:
    @Override
    public Mono<Void> uploadFile(String key, byte[] file) {
        return Mono.fromRunnable(() -> {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build(), RequestBody.fromBytes(file));
        });
    }

    // Download file from S3 Bucket:
    @Override
    public Mono<InputStream> getObject(String key) {
        return Mono.fromCallable(() -> s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()));
    }
}
