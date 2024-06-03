package com.skillstorm.services;

import reactor.core.publisher.Mono;

import java.io.InputStream;

public interface S3Service {

    // Upload file to S3 Bucket:
    Mono<Void> uploadFile(String key, byte[] file);

    // Download file from S3 Bucket:
    Mono<InputStream> getObject(String key);
}
