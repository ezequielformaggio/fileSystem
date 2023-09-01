package Exceptions;

public class FileIsAlreadyOpenException extends RuntimeException {
    public FileIsAlreadyOpenException(String cause) {
      super(cause);
    }
}

