package tech.pedronhamirre.fileapi.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record FileUploadRequest(
        @NotNull(message = "O arquivo é obrigatório")
        MultipartFile file
) {
}
