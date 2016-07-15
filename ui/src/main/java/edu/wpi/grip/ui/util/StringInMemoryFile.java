package edu.wpi.grip.ui.util;

import net.schmizz.sshj.xfer.InMemorySourceFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A "file" that can be transfered uses SSHJ's scp routines, but is backed by an in-memory string
 * instead of an actual file.  This is used to deploy projects, which may or may not be saved to a
 * file.
 */
public class StringInMemoryFile extends InMemorySourceFile {
  private final String name;
  private final byte[] contentsBytes;
  private final int permissions;

  /**
   * @param name        The name of the file.
   * @param contents    The contents of the file.
   * @param permissions The permissions to use for the file.
   */
  public StringInMemoryFile(String name, String contents, int permissions) {
    super();
    this.name = name;
    this.contentsBytes = contents.getBytes(StandardCharsets.UTF_8);
    this.permissions = permissions;
  }

  @SuppressWarnings("PMD.AvoidUsingOctalValues")
  public StringInMemoryFile(String name, String contents) {
    this(name, contents, 0644);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public long getLength() {
    return contentsBytes.length;
  }

  @Override
  public int getPermissions() {
    return permissions;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(contentsBytes);
  }
}
