package tech.pedronhamirre.fileapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.pedronhamirre.fileapi.dto.FileUploadRequest;
import tech.pedronhamirre.fileapi.dto.FileUploadResponse;
import tech.pedronhamirre.fileapi.service.FileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping()
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestPart("file") MultipartFile file) {
        FileUploadRequest request = new FileUploadRequest(file);
        FileUploadResponse response = fileService.upload(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileUploadResponse> fileData(@PathVariable("id") UUID id) {
        FileUploadResponse response = fileService.getFileData(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") UUID id) throws IOException {
        File file = fileService.getDownloadFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(new FileSystemResource(file));
    }

}
