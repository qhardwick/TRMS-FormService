package com.skillstorm.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;

@Data
@AllArgsConstructor
public class DownloadResponse {

    private final InputStream inputStream;
    private final String contentType;
}
