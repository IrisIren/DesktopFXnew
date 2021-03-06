package com.ak.util;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;

public final class LocalFileHandler extends FileHandler {
  public LocalFileHandler() throws IOException, SecurityException {
    super(String.format("%s.%%u.%%g.log",
            new LocalFileIO.LogBuilder().addPath(
                LogManager.getLogManager().getProperty(LocalFileHandler.class.getName() + ".name")).fileName(
                DateTimeFormatter.ofPattern("yyyy-MMM-dd HH-mm-ss").format(ZonedDateTime.now())).build().
                getPath().toString()), true
    );
  }
}
