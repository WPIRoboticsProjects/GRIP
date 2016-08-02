package edu.wpi.grip.core.operations.python;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static edu.wpi.grip.core.operations.python.PythonOperationUtils.DIRECTORY;
import static edu.wpi.grip.core.operations.python.PythonOperationUtils.checkDirExists;
import static edu.wpi.grip.core.operations.python.PythonOperationUtils.read;
import static edu.wpi.grip.core.operations.python.PythonOperationUtils.tryCreate;
import static edu.wpi.grip.core.operations.python.PythonScriptFile.TEMPLATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PythonOperationUtilsTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNotCorrectFile() {
    PythonOperationUtils.read(new File(System.getProperty("user.home")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotPythonFile() {
    PythonOperationUtils.read(new File(DIRECTORY, "not-a-python-file.txt"));
  }

  @Test
  public void testReadFile() throws IOException {
    File file = new File(DIRECTORY, "a-python-file.py");
    file.deleteOnExit();
    checkDirExists();
    file.createNewFile();
    Files.write(file.toPath(), TEMPLATE.getBytes());
    String read = read(file);
    assertEquals(TEMPLATE, read);
  }

  @Test
  public void testTryCreate() {
    assertNotNull(tryCreate(TEMPLATE));
    assertNull(tryCreate("not executable python code"));
  }

}
