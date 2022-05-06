package com.bytedompteur.documentfinder.interprocesscommunication.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class SocketAddressService {

  public static final String SOCKET_NAME = "document-finder.socket";

  private final String applicationHomeDirectory;

  public SocketAddressService(String applicationHomeDirectory) {
    Validate.notBlank(applicationHomeDirectory, "'applicationHomeDirectory' must not be blank");
    this.applicationHomeDirectory = applicationHomeDirectory;
  }

  UnixDomainSocketAddress createSocketAddress() {
    Path socketFile = getSocketAddressFile();
    log.debug("Creating socket address with path '{}'", socketFile);
    return UnixDomainSocketAddress.of(socketFile);
  }

  void deleteSocketAddressFileIfExists() throws IOException {
    Path socketFile = getSocketAddressFile();
    log.debug("Deleting file at path '{}'", socketFile);
    Files.deleteIfExists(socketFile);
  }

  Path getSocketAddressFile() {
    return FileSystems.getDefault().getPath(applicationHomeDirectory, SOCKET_NAME);
  }

  boolean socketAddressFileExists() {
    return Files.exists(getSocketAddressFile());
  }
}
