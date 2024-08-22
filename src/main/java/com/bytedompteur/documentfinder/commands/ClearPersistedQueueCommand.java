package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import lombok.RequiredArgsConstructor;

import jakarta.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClearPersistedQueueCommand implements Runnable {

  private final PersistedUniqueFileEventQueue queue;

  @Override
  public void run() {
    queue.clear();
  }
}
