package com.bytedompteur.documentfinder.commands;

import lombok.RequiredArgsConstructor;

import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClearAllCommand implements Runnable {

  private final ExecutorService executorService;
  private final ClearFulltextSearchServiceIndexCommand clearFulltextSearchServiceIndexCommand;
  private final ClearPersistedQueueCommand clearPersistedQueueCommand;

  @Override
  public void run() {
    CompletableFuture
      .allOf(
        CompletableFuture.runAsync(clearFulltextSearchServiceIndexCommand, executorService),
        CompletableFuture.runAsync(clearPersistedQueueCommand, executorService)
      )
      .join();
  }
}
