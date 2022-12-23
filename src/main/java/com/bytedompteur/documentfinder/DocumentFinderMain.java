package com.bytedompteur.documentfinder;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCServerNotRunningException;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages.ShowMainWindowMessage;
import com.bytedompteur.documentfinder.interprocesscommunication.dagger.DaggerIPCClientComponent;
import com.bytedompteur.documentfinder.interprocesscommunication.dagger.IPCClientComponent;
import com.bytedompteur.documentfinder.ui.DocumentFinderUIMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;

public class DocumentFinderMain {

  public static void main(String[] args) {
    String applicationHomeDirectory = determineApplicationHomeDirectoryExitOnError();

    var log = LoggerFactory.getLogger(DocumentFinderMain.class);
    log.info("Starting Document Finder");
    log.info("Determined application home directory '{}'", applicationHomeDirectory);

    log.debug("Creating IPCClient component");
    var ipcClientComponent = DaggerIPCClientComponent
      .builder()
      .applicationHomeDirectory(applicationHomeDirectory)
      .build();

    SendMessageResult sendMessageResult = SendMessageResult.SERVER_NOT_RUNNING;
    if (ipcClientComponent.ipcService().isIPCServerAlreadyRunningInDifferentProcess()) {
      sendMessageResult = sendOpenMainWindowMessageToRunningProcess(log, ipcClientComponent);
    }

    switch (sendMessageResult) {
      case FAILED -> System.exit(1);
      case SUCCESS -> System.exit(0);
      default -> launchUI(args, log);
    }
  }

  @SuppressWarnings("java:S3252")
  private static void launchUI(String[] args, Logger log) {
    try {
      DocumentFinderUIMain.launch(DocumentFinderUIMain.class, args);
    } catch (Exception e) {
      log.error("While launching UI", e);
      System.exit(1);
    }
  }

  private static SendMessageResult sendOpenMainWindowMessageToRunningProcess(Logger log, IPCClientComponent ipcClientComponent) {
    log.debug("A different instance is already running. Sending ShowMainWindowCommand and stopping this instance");

    SendMessageResult result = SendMessageResult.SUCCESS;
    try {
      ipcClientComponent.ipcService().sendMessageToServer(new ShowMainWindowMessage());
    } catch (IPCServerNotRunningException ignore) {
      result = SendMessageResult.SERVER_NOT_RUNNING;
    } catch (Exception e) {
      result = SendMessageResult.FAILED;
    }
    return result;
  }

  @SuppressWarnings("java:S106")
  private static String determineApplicationHomeDirectoryExitOnError() {
    String applicationHomeDirectory = null;
    try {
      applicationHomeDirectory = determineApplicationHomeDirectory();
      System.setProperty("APPLICATION_HOME_DIRECTORY_FOR_LOGGING", applicationHomeDirectory);
    } catch (Exception e) {
      System.err.println("Could not determine application home directory");
      e.printStackTrace();
      System.exit(1);
    }
    return applicationHomeDirectory;
  }

  public static String determineApplicationHomeDirectory() {
    return Optional
      .ofNullable(System.getProperty("documentfinder.homedir"))
      .orElseGet(() -> {
        var indexDirName = ".documentfinder";
        return Path.of(System.getProperty("user.home"), indexDirName).toString();
      });
  }
}
