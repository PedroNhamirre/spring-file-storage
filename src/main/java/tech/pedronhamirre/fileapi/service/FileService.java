package tech.pedronhamirre.fileapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.pedronhamirre.fileapi.dto.FileUploadRequest;
import tech.pedronhamirre.fileapi.dto.FileUploadResponse;
import tech.pedronhamirre.fileapi.exceptions.files.FileNotFoundException;
import tech.pedronhamirre.fileapi.exceptions.files.FileSizeLimitExceededException;
import tech.pedronhamirre.fileapi.exceptions.files.FileStorageException;
import tech.pedronhamirre.fileapi.model.FileEntity;
import tech.pedronhamirre.fileapi.repository.FileUploadRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final FileUploadRepository fileUploadRepository;
    private final Path storageDir;

    private final long MAX_SIZE;

    @Autowired
    public FileService(FileUploadRepository fileUploadRepository,
                       @Value("${file.upload-dir}") String uploadDir,
                       @Value("${spring.servlet.multipart.max-file-size}") String maxFileSize) {
        this.fileUploadRepository = fileUploadRepository;
        this.storageDir = Path.of(uploadDir).toAbsolutePath().normalize();
        this.MAX_SIZE = DataSize.parse(maxFileSize).toBytes();
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar diretório de uploads", e);
        }
    }

    public FileUploadResponse upload(FileUploadRequest fileUploadRequest) {
        MultipartFile file = fileUploadRequest.file();
        validateFile(file);
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String contentType = file.getContentType();
        String storedName = generateStoredFileName(extension);
        Path storagePath = storageDir.resolve(storedName);

        if (!Objects.equals(storagePath.getParent(), storageDir)) {
            throw new FileStorageException("Nome de ficheiro não suportado");
        }

        try {
            Files.copy(file.getInputStream(), storagePath, StandardCopyOption.REPLACE_EXISTING);
            FileEntity fileEntity = FileEntity.builder()
                    .originalName(originalName)
                    .storedName(storedName)
                    .size(file.getSize())
                    .contentType(contentType)
                    .extension(extension)
                    .storagePath(storagePath.toString())
                    .build();
            fileUploadRepository.save(fileEntity);
            return mapToFileUploadResponse(fileEntity);
        } catch (IOException e) {
            throw new FileStorageException("Erro ao salvar o arquivo", e);
        }
    }

    public FileUploadResponse getFileData(UUID id) {
        FileEntity fileEntity = findFileById(id);
        return mapToFileUploadResponse(fileEntity);
    }

    public File getDownloadFile(UUID id) {
        Path filePath = getFilePath(id);
        return filePath.toFile();
    }

    public List<FileUploadResponse> getAllFiles() {
        return fileUploadRepository.findAll().stream()
                .map(this::mapToFileUploadResponse)
                .collect(Collectors.toList());
    }

    public Path getFilePath(UUID id) {
        FileEntity fileEntity = findFileById(id);
        Path filePath = Path.of(fileEntity.getStoragePath());
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Arquivo não encontrado no armazenamento");
        }
        return filePath;
    }


    private FileEntity findFileById(UUID id) {
        return fileUploadRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Arquivo não encontrado: " + id));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Arquivo está vazio");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new FileSizeLimitExceededException("Tamanho máximo permitido é " + formatSizeInMB(MAX_SIZE));
        }
    }


    private String getExtension(String filename) {
        if (filename == null) {
            throw new FileStorageException("Nome do arquivo inválido ou sem extensão");
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            throw new FileStorageException("Nome do arquivo inválido ou sem extensão");
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }


    private String generateStoredFileName(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    private String buildFileUrl(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(id.toString())
                .path("/download")
                .toUriString();
    }

    private String formatSizeInMB(long sizeInBytes) {
        double sizeInMB = sizeInBytes / (1024.0 * 1024.0);
        return String.format("%.1f MB", sizeInMB);
    }

    private FileUploadResponse mapToFileUploadResponse(FileEntity fileEntity) {
        String url = buildFileUrl(fileEntity.getId());
        return new FileUploadResponse(
                fileEntity.getId(),
                fileEntity.getOriginalName(),
                formatSizeInMB(fileEntity.getSize()),
                fileEntity.getContentType(),
                "." + fileEntity.getExtension(),
                url,
                fileEntity.getUploadTime()
        );
    }
}
