package com.skillstorm.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;

@Data
@AllArgsConstructor
public class DownloadResponseDto {

    private final InputStream inputStream;
    private final String contentType;
}
