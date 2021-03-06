package com.ak.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.util.Builder;

public class LocalFileIO<E extends Enum<E> & LocalFileIO.OSDirectory> implements LocalIO {
  public interface OSDirectory {
    Path getDirectory();
  }

  private enum LogOSDirectory implements OSDirectory {
    WINDOWS {
      @Override
      public Path getDirectory() {
        File appDataDir = null;
        try {
          String appDataEV = Optional.ofNullable(System.getenv("APPDATA")).orElse("");
          if (!appDataEV.isEmpty()) {
            appDataDir = new File(appDataEV);
          }
        }
        catch (SecurityException e) {
          Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
        }

        if (appDataDir != null && appDataDir.isDirectory()) {
          return Paths.get(appDataDir.getPath(), VENDOR_ID);
        }
        else {
          return Paths.get(USER_HOME_PATH, "Application Data", VENDOR_ID);
        }
      }
    },
    MAC {
      @Override
      public Path getDirectory() {
        return Paths.get(USER_HOME_PATH, "Library", "Application Support", VENDOR_ID);
      }
    },
    UNIX {
      @Override
      public Path getDirectory() {
        return Paths.get(USER_HOME_PATH);
      }
    }
  }

  private static final String USER_HOME_PATH = AccessController.doPrivileged(
      (PrivilegedAction<String>) () -> Optional.ofNullable(System.getProperty("user.home")).orElse(""));
  private static final String VENDOR_ID = Stream.of(LocalFileIO.class.getPackage().getName().split("\\.")).limit(2).
      collect(Collectors.joining("."));

  private final Path path;
  private final String fileName;
  private final E osIdEnum;

  private LocalFileIO(AbstractBuilder b, Class<E> enumClass) {
    path = b.relativePath;
    fileName = Optional.ofNullable(b.fileName).orElse("");
    osIdEnum = Enum.valueOf(enumClass, OS.get().name());
  }

  @Override
  public Path getPath() throws IOException {
    Path path = osIdEnum.getDirectory().resolve(this.path);
    Files.createDirectories(path);
    if (!fileName.isEmpty()) {
      path = path.resolve(fileName);
    }
    return path;
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return Files.newInputStream(getPath());
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    return Files.newOutputStream(getPath());
  }

  public abstract static class AbstractBuilder implements Builder<LocalIO> {
    private final String fileExtension;
    private Path relativePath;
    private String fileName;

    AbstractBuilder(String fileExtension) {
      this.fileExtension = fileExtension;
    }

    public final AbstractBuilder addPath(String part) {
      if (relativePath == null) {
        relativePath = Paths.get(part);
      }
      else {
        relativePath.resolve(part);
      }
      return this;
    }

    public final AbstractBuilder fileName(String fileName) {
      this.fileName = fileName;
      if (!fileExtension.isEmpty()) {
        this.fileName += "." + fileExtension;
      }
      return this;
    }
  }

  public static final class LogBuilder extends AbstractBuilder {
    public LogBuilder() {
      super("");
    }

    /**
     * Open file (for <b>background logging</b>) in directory
     * <ul>
     * <li>
     * Windows - ${userHome}/Application Data/${vendorId}/${applicationId}
     * </li>
     * <li>
     * MacOS - ${userHome}/Library/Application Support/${vendorId}/${applicationId}
     * </li>
     * <li>
     * Unix and other - ${userHome}/.${applicationId}
     * </li>
     * </ul>
     *
     * @return interface for input/output file creation.
     */
    @Override
    public LocalIO build() {
      return new LocalFileIO<>(this, LogOSDirectory.class);
    }
  }

  public static final class LocalStorageBuilder extends AbstractBuilder {
    public LocalStorageBuilder() {
      super("xml");
    }

    @Override
    public LocalIO build() {
      return new LocalFileIO<>(this, LogOSDirectory.class);
    }
  }
}
