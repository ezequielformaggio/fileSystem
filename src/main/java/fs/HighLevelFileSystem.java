package fs;

import Exceptions.CanNotOpenFileException;

public class HighLevelFileSystem {
  private final LowLevelFileSystem lowLevelFileSystem;

  public HighLevelFileSystem(LowLevelFileSystem lowLevelFileSystem) {
    this.lowLevelFileSystem = lowLevelFileSystem;
  }

  public File open(String path) {
    int fileID = lowLevelFileSystem.openFile(path);
    validateCorrectOpening(fileID);
    return new File(fileID, path, lowLevelFileSystem);
  }

  void validateCorrectOpening(int fileID) {
    if(fileID == -1) {
      throw new CanNotOpenFileException("No es posible abrir el archivo");
    }
  }

  boolean exists(String path) {
    return lowLevelFileSystem.exists(path);
  }

  boolean isRegularFile(String path) {
    return lowLevelFileSystem.isRegularFile(path);
  }

  boolean isDirectory(String path) {
    return lowLevelFileSystem.isDirectory(path);
  }

}
