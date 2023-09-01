package Exceptions;

public class CanNotOpenFileException extends RuntimeException {
  public CanNotOpenFileException(String cause) {
    super(cause);
  }
}
