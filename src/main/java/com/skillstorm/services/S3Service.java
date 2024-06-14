package com.skillstorm.services;

import com.skillstorm.utils.DownloadResponse;
import reactor.core.publisher.Mono;

public interface S3Service {

    // Upload file to S3 Bucket:
    Mono<Void> uploadFile(String key, String contentType, byte[] file);

    // Download file from S3 Bucket:
    Mono<DownloadResponse> getObject(String key);
}
