package tech.pedronhamirre.fileapi.dto;

import java.time.LocalDateTime;

public record FileUploadResponse(
        String originalName,
        String size,
        String contentType,
        String extension,
        String url,
        LocalDateTime uploadTime
) {
}
