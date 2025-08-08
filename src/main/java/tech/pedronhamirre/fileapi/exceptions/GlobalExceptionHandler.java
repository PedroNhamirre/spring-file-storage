package tech.pedronhamirre.fileapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.pedronhamirre.fileapi.dto.ErrorResponseDTO;
import tech.pedronhamirre.fileapi.exceptions.files.FileNotFoundException;
import tech.pedronhamirre.fileapi.exceptions.files.FileSizeLimitExceededException;
import tech.pedronhamirre.fileapi.exceptions.files.FileStorageException;
import tech.pedronhamirre.fileapi.exceptions.files.InvalidFileTypeException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponseDTO> handleStorageException(FileStorageException ex) {
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidFileTypeException(InvalidFileTypeException ex) {
        return errorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<?> handleSizeLimitException(FileSizeLimitExceededException ex) {
        return errorResponse(HttpStatus.PAYLOAD_TOO_LARGE, ex.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileNotFoundException(FileNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOtherExceptions(Exception ex) {
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado");
    }

    private ResponseEntity<ErrorResponseDTO> errorResponse(HttpStatus status, String message) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                message,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponseDTO, status);
    }
}
