package com.bytedompteur.documentfinder.persistedqueue.adapter.in;

import java.util.List;
import java.util.Optional;

public interface PersistedUniqueFileEventQueue {

  void pushOrOverwrite(FileEvent value);

  void pushOrOverwrite(List<FileEvent> value);

  Optional<FileEvent> pop();

  boolean isEmpty() throws InterruptedException;

  int size() throws InterruptedException;

  void clear();

  Long getNumberOfFilesAdded();

  Long getNumberOfFilesRemoved();
}
