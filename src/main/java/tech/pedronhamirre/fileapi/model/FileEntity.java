package tech.pedronhamirre.fileapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false, unique = true)
    private String storedName;

    @PositiveOrZero
    private long size;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String extension;


    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadTime;

    @Column(nullable = false)
    private String storagePath;

    @PrePersist
    protected void onCreate() {
        if (this.uploadTime == null) {
            this.uploadTime = LocalDateTime.now();
        }
    }

}
