package tech.pedronhamirre.fileapi.exceptions.files;

public class FileSizeLimitExceededException extends RuntimeException {

    public FileSizeLimitExceededException(String message) {
        super(message);
    }

}
