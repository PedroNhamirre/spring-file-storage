package tech.pedronhamirre.fileapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.pedronhamirre.fileapi.model.FileEntity;

import java.util.UUID;

public interface FileUploadRepository extends JpaRepository<FileEntity, UUID> {
}
