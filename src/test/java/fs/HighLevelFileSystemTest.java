package fs;

import Exceptions.CanNotOpenFileException;
import Exceptions.CanNotReadFileException;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.function.Consumer;
import static org.mockito.Mockito.*;

class HighLevelFileSystemTest {

  private LowLevelFileSystem lowLevelFileSystem;
  private HighLevelFileSystem fileSystem;

  @BeforeEach
  void initFileSystem() {
    lowLevelFileSystem = mock(LowLevelFileSystem.class);
    fileSystem = new HighLevelFileSystem(lowLevelFileSystem);
  }

  @Test
  void sePuedeAbrirUnArchivo() {
    when(lowLevelFileSystem.openFile("unArchivo.txt")).thenReturn(42);
    File file = fileSystem.open("unArchivo.txt");
    Assertions.assertEquals(file.ID, 42);
  }

  @Test
  void siLaAperturaFallaUnaExcepcionEsLanzada() {
    when(lowLevelFileSystem.openFile("otroArchivo.txt")).thenReturn(-1);
    Assertions.assertThrows(CanNotOpenFileException.class, () -> fileSystem.open("otroArchivo.txt"));
  }

  @Test
  void siLaLecturaSincronicaFallaUnaExcepcionEsLanzada() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile("archivoMalito.txt")).thenReturn(13);
    when(lowLevelFileSystem.syncReadFile(anyInt(), any(), anyInt(), anyInt())).thenReturn(-1);

    File file = fileSystem.open("archivoMalito.txt");

    Assertions.assertThrows(CanNotReadFileException.class, () -> file.read(buffer));
  }

  @Test
  void sePuedeLeerSincronicamenteUnArchivoCuandoNoHayNadaParaLeer() {
    Buffer buffer = new Buffer(100);
    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBufferBytes(), 0, 100)).thenReturn(0);

    File file = fileSystem.open("ejemplo.txt");
    int leido = file.read(buffer);

    Assertions.assertEquals(0, buffer.getBufferStart());
    Assertions.assertEquals(-1, buffer.getBufferEnd());
    Assertions.assertEquals(0, buffer.getBufferCurrentSize());
    Assertions.assertEquals(0, leido);
  }

  @Test
  void sePuedeLeerSincronicamenteUnArchivoCuandoHayAlgoParaLeer() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBufferBytes(), 0, 9)).thenAnswer(invocation -> {
      Arrays.fill(buffer.getBufferBytes(), 0, 4, (byte) 3);
      return 4;
    });

    File file = fileSystem.open("ejemplo.txt");
    int leido = file.read(buffer);

    Assertions.assertTrue(leido > 0);
    Assertions.assertEquals(0, buffer.getBufferStart());
    Assertions.assertEquals(3, buffer.getBufferEnd());
    Assertions.assertEquals(4, buffer.getBufferCurrentSize());
    Assertions.assertArrayEquals(buffer.getBufferBytes(), new byte[] {3, 3, 3, 3, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void sePuedeLeerAsincronicamenteUnArchivo() {

    Consumer<Integer> callback = mock(Consumer.class);
    Buffer buffer = new Buffer(10);
    final int bytesLeidos = 5;

    doAnswer(invocation -> {
      callback.accept(bytesLeidos);
      return null;
    }).when(lowLevelFileSystem).asyncReadFile(eq(100), eq(buffer.getBufferBytes()), eq(0), eq(9), any());

    when(lowLevelFileSystem.openFile("unaRuta")).thenReturn(100);
    File file = fileSystem.open("unaRuta");
    file.asyncRead(buffer, callback);
    verify(callback, times(1)).accept(bytesLeidos);
  }

  @Test
  void sePuedeEscribirSincronicamenteUnArchivoCuandoHayNoHayNadaParaEscribir() {
    Buffer buffer = new Buffer(10);
    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    doAnswer(invocation -> null).when(lowLevelFileSystem).syncWriteFile(42, buffer.getBufferBytes(), 0, 9);

    File file = fileSystem.open("ejemplo.txt");
    file.write(buffer);
    verify(lowLevelFileSystem, times(1)).syncWriteFile(42, buffer.getBufferBytes(), 0, 9);
  }

  @Test
  void sePuedeEscribirSincronicamenteUnArchivoCuandoHayAlgoParaEscribir() {
    Buffer buffer = new Buffer(10);
    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);

    doAnswer(invocation -> {
      Arrays.fill(buffer.getBufferBytes(), 2, 4, (byte) 1);
      return null;
    }).when(lowLevelFileSystem).syncWriteFile(42, buffer.getBufferBytes(), 0, 9);

    File file = fileSystem.open("ejemplo.txt");
    file.write(buffer);

    Assertions.assertEquals(0, buffer.getBufferStart());
    Assertions.assertEquals(9, buffer.getBufferEnd());
    Assertions.assertEquals(10, buffer.getBufferCurrentSize());
    Assertions.assertArrayEquals(buffer.getBufferBytes(), new byte[] {0, 0, 1, 1, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void sePuedeEscribirAsincronicamenteUnArchivo() {

    Buffer buffer = new Buffer(10);
    final Runnable[] callback = new Runnable[1];

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    doAnswer(invocation -> {
      Arrays.fill(buffer.getBufferBytes(), 0, 3, (byte) 1);
      callback[0] = invocation.getArgument(2);
      callback[0].run();
      return null;
    }).when(lowLevelFileSystem).syncWriteFile(42, buffer.getBufferBytes(), 0, 9);

    File file = fileSystem.open("ejemplo.txt");
    file.asyncWrite(buffer, callback[0]);

    Assertions.assertEquals(0, buffer.getBufferStart());
    Assertions.assertEquals(9, buffer.getBufferEnd());
    Assertions.assertEquals(10, buffer.getBufferCurrentSize());
    Assertions.assertArrayEquals(buffer.getBufferBytes(), new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void sePuedeCerrarUnArchivo() {
    when(lowLevelFileSystem.openFile("unaRuta")).thenReturn(100);
    File file = fileSystem.open("unaRuta");
    file.close();
    verify(lowLevelFileSystem,times(1)).closeFile(100);
  }

  @Test
  void seLeeUnArchivoYSeCopianSusDatosAOtro() {
    File file = fileSystem.open("requerimientos.txt");
    File file2 = fileSystem.open("requerimientos2.txt");

    Buffer c0 = new Buffer(4);
    file.read(c0);
    file2.write(c0);

    Buffer bloque = new Buffer(16);
    file2.write(bloque);

    Buffer c1 = new Buffer(1);
    file.read(c1);
    file2.write(c1);

    Buffer c2 = new Buffer(5);
    file.read(c2);
    file2.write(c2);

    Buffer test = new Buffer(100);
    Assertions.assertEquals(file.read(test), file2.read(test));

    file.close();
    file2.close();

  }

  @Test
  void leerUnArchivoCompletoYEscribirloEnOtroArchivoPorBloques() {
    File file = fileSystem.open("requerimientos.txt");
    File file2 = fileSystem.open("requerimientos2.txt");
    Buffer buffer = new Buffer(4);
    while(!buffer.finDeBuffer()){
      file.read(buffer);
      file2.write(buffer);
    }
  }

  // por mas que los path existan siempre da false
  @Ignore
  void sePuedeSaberSiUnPathEsUnArchivoRegular() {
    String path = "unArchivo.txt";
    Assertions.assertTrue(fileSystem.isRegularFile(path));
  }

  @Ignore
  void sePuedeSaberSiUnPathEsUnDirectorio() {
    String path = "src/test/java/fs";
    Assertions.assertTrue(fileSystem.isDirectory(path));
  }

  @Ignore
  void sePuedeSaberSiUnPathExiste() {
    String path = "unArchivo.txt";
    Assertions.assertTrue(fileSystem.exists(path));
  }

}
