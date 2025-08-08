package tech.pedronhamirre.fileapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileUploadRepository fileUploadRepository;
    private final Path storageDir;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/png", "image/jpeg", "image/gif", "application/pdf"
    );

    private final long MAX_SIZE  ;

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

            String url = buildFileUrl(fileEntity.getId());


            return new FileUploadResponse(
                    fileEntity.getOriginalName(),
                    formatSizeInMB(fileEntity.getSize()),
                    fileEntity.getContentType(),
                    "." + fileEntity.getExtension(),
                    url,
                    fileEntity.getUploadTime()
            );

        } catch (IOException e) {
            throw new FileStorageException("Erro ao salvar o arquivo", e);
        }
    }

    public FileUploadResponse getFileData(UUID id) {
        FileEntity fileEntity = findFileById(id);

        String url = buildFileUrl(fileEntity.getId());

        return new FileUploadResponse(
                fileEntity.getOriginalName(),
                formatSizeInMB(fileEntity.getSize()),
                fileEntity.getContentType(),
                "." + fileEntity.getExtension(),
                url,
                fileEntity.getUploadTime()
        );
    }

    public Path getFilePath(UUID id) {
        FileEntity fileEntity = findFileById(id);

        Path filePath = Path.of(fileEntity.getStoragePath());
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Arquivo não encontrado no armazenamento");
        }

        return filePath;
    }

    public Resource loadAsResource(UUID id) {
        try {
            Path filePath = getFilePath(id);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new FileNotFoundException("Arquivo não está disponível para leitura");
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new FileStorageException("Erro ao carregar arquivo como recurso", e);
        }
    }

    public String getContentType(UUID id) {
        return findFileById(id).getContentType();
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

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new FileStorageException("Tipo de arquivo não suportado: " + contentType);
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

}
