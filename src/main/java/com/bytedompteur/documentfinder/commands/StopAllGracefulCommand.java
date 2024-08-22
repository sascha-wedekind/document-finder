package com.bytedompteur.documentfinder.commands;

import lombok.RequiredArgsConstructor;

import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StopAllGracefulCommand implements Runnable {

  private final ExecutorService executorService;
  private final StopDirectoryWatcherCommand stopDirectoryWatcherCommand;
  private final StopFileWalkerCommand stopFileWalkerCommand;
  private final StopFulltextSearchServiceCommand stopFulltextSearchServiceCommand;
  private final WaitUntilPersistedQueueIsEmptyCommand waitUntilPersistedQueueIsEmptyCommand;
  private final WaitUntilFulltextSearchServiceProcessedAllEventsCommand waitUntilFulltextSearchServiceProcessedAllEventsCommand;
  private final StopIPCServerCommand stopIPCServerCommand;

  @Override
  public void run() {
    CompletableFuture
      .allOf(
        CompletableFuture.runAsync(stopDirectoryWatcherCommand, executorService),
        CompletableFuture.runAsync(stopFileWalkerCommand, executorService),
        CompletableFuture.runAsync(stopIPCServerCommand, executorService)
      )
      .thenRunAsync(waitUntilPersistedQueueIsEmptyCommand, executorService)
      .thenRunAsync(waitUntilFulltextSearchServiceProcessedAllEventsCommand, executorService)
      .thenRunAsync(stopFulltextSearchServiceCommand, executorService)
      .join();

  }
}
