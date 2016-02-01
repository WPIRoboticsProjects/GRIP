package edu.wpi.grip.ui.util;

import net.schmizz.sshj.xfer.InMemorySourceFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

/**
 * A "file" that can be transfered uses SSHJ's scp routines, but is backed by an in-memory string instead of an actual
 * file.  This is used to deploy projects, which may or may not be saved to a file.
 */
public class StringInMemoryFile extends InMemorySourceFile {
    private final String name, contents;
    private final int permissions;

    public StringInMemoryFile(String name, String contents, int permissions) {
        super();
        this.name = name;
        this.contents = contents;
        this.permissions = permissions;
    }

    public StringInMemoryFile(String name, String contents) {
        this(name, contents, 0644);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getLength() {
        return contents.getBytes().length;
    }

    @Override
    public int getPermissions() {
        return permissions;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new StringBufferInputStream(contents);
    }
}
