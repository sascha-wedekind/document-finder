package com.bytedompteur.documentfinder.commands;

import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StopAllCommand implements Runnable {

  private final ExecutorService executorService;
  private final StopDirectoryWatcherCommand stopDirectoryWatcherCommand;
  private final StopFileWalkerCommand stopFileWalkerCommand;
  private final StopFulltextSearchServiceCommand stopFulltextSearchServiceCommand;


  @Override
  public void run() {
    CompletableFuture
      .allOf(
        CompletableFuture.runAsync(stopDirectoryWatcherCommand, executorService),
        CompletableFuture.runAsync(stopFileWalkerCommand, executorService),
        CompletableFuture.runAsync(stopFulltextSearchServiceCommand, executorService)
      )
      .join();
  }
}
