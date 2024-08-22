package com.bytedompteur.documentfinder.commands;

import lombok.RequiredArgsConstructor;

import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StopAndClearAll implements Runnable {

  private final StopAllCommand stopAllCommand;
  private final ClearAllCommand clearAllCommand;

  @Override
  public void run() {
    CompletableFuture
      .runAsync(stopAllCommand)
      .thenRun(clearAllCommand)
      .join();
  }
}
