package fs;

import Exceptions.CanNotReadFileException;
import java.util.function.Consumer;

public class File {
  public String path;
  public int ID;
  private final LowLevelFileSystem lowLevelFileSystem;

  public File(int fileID, String path, LowLevelFileSystem lowLevelFileSystem) {
    this.ID = fileID;
    this.path = path;
    this.lowLevelFileSystem = lowLevelFileSystem;
  }

  public void close() {
    lowLevelFileSystem.closeFile(ID);
  }

  public int read(Buffer buffer) {
    int readBytes = lowLevelFileSystem.syncReadFile(ID,
        buffer.getBufferBytes(),
        buffer.getBufferStart(),
        buffer.getBufferEnd());
    buffer.limit(readBytes);
    validateRead(readBytes);
    return readBytes;
  }

    public void asyncRead(Buffer buffer,Consumer<Integer> callback) {
      lowLevelFileSystem.asyncReadFile(ID,
          buffer.getBufferBytes(),
          buffer.getBufferStart(),
          buffer.getBufferEnd(),
          readBytes -> {
            buffer.limit(readBytes);
            callback.accept(readBytes);
          });
  }

  public void write(Buffer buffer) {
    lowLevelFileSystem.syncWriteFile(ID,
        buffer.getBufferBytes(),
        buffer.getBufferStart(),
        buffer.getBufferEnd());
  }

  public void asyncWrite(Buffer buffer, Runnable callback) {
    lowLevelFileSystem.asyncWriteFile(ID,
        buffer.getBufferBytes(),
        buffer.getBufferStart(),
        buffer.getBufferEnd(),
        callback);
  }

  void validateRead(int readBytes) {
    if(readBytes == -1) {
      throw new CanNotReadFileException("No es posible leer el archivo");
    }
  }

}
