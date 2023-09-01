package Exceptions;

  public class CanNotReadFileException extends RuntimeException {
    public CanNotReadFileException(String cause) {
      super(cause);
    }
}


