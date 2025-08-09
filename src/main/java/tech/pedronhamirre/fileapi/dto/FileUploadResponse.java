package tech.pedronhamirre.fileapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileUploadResponse(
        @JsonIgnore
        UUID id,
        String originalName,
        String size,
        String contentType,
        String extension,
        String url,
        LocalDateTime uploadTime
) {
}
