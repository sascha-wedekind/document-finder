package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.QueueRepository;
import com.bytedompteur.documentfinder.ui.WindowManager;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
@Singleton // see alreadyExecuted property
public class ExitApplicationCommand implements  Runnable{

  private final StopAllGracefulCommand stopAllGracefulCommand;
  private final ExecutorService executorService;
  private final QueueRepository queueRepository;

  private final WindowManager windowManager;

  private boolean alreadyExecuted = false;

  @Override
  public void run() {
    if (!alreadyExecuted) {
      stopAllServices();
      stopRunningThreads();
      closeQueueRepository();
      destroyUIComponents();

      log.info("Terminating JavaFx");
      alreadyExecuted = true;
      Platform.exit();
    }
  }

  private void destroyUIComponents() {
    windowManager.notifyCurrentControllerBeforeViewHide();
    windowManager.hideSystemTrayIcon();
  }

  private void closeQueueRepository() {
    log.info("Closing persisted queue repository");
    try {
      queueRepository.close();
    } catch (Exception e) {
      log.error("Failed to close queue repository", e);
    }
  }

  @SuppressWarnings("unused")
  private void stopRunningThreads() {
    log.info("Stopping running threads. Waiting max 30 seconds.");
    try {
      executorService.shutdown();
      var ignore = executorService.awaitTermination(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error("Failed to stop running threads", e);
      Thread.currentThread().interrupt();
    }
  }

  private void stopAllServices() {
    log.info("Stopping application");
    log.info("Stopping all services");
    stopAllGracefulCommand.run();
  }

}
