package fs;

public class Buffer {
  private final byte[] bufferBytes;
  private final int bufferStart;
  private int bufferEnd;

  public Buffer(int size) {
    bufferStart = 0;
    bufferEnd = size -1;
    bufferBytes = new byte[size];
  }

  public byte[] getBufferBytes() {
    return bufferBytes;
  }

  public int getBufferStart() {
    return bufferStart;
  }

  public int getBufferEnd() {
    return bufferEnd;
  }

  public int getBufferCurrentSize() {
    return bufferEnd + 1 - bufferStart;
  }

  public int getBufferMaxSize() {
    return this.bufferBytes.length;
  }

  public void limit(int offset) {
    bufferEnd = bufferStart + offset - 1;
  }

  public boolean finDeBuffer() {
    return getBufferCurrentSize() == getBufferMaxSize();
  }
}
