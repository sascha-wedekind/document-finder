package com.bytedompteur.documentfinder.commands;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
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
