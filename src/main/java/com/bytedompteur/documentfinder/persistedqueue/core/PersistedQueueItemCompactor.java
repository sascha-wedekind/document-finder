package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.*;

import static java.util.Objects.nonNull;

public class PersistedQueueItemCompactor {

  private final Map<Long, PersistedQueueItem> itemsByPathHashMap = new TreeMap<>();
  @SuppressWarnings("UnstableApiUsage")
  private final HashFunction hashFunction = Hashing.murmur3_128();

  public Collection<PersistedQueueItem> compact(List<PersistedQueueItem> value) {
    itemsByPathHashMap.clear();
    Optional
      .ofNullable(value)
      .orElse(List.of())
      .forEach(this::processItem);
    return itemsByPathHashMap.values();
  }

  private void processItem(PersistedQueueItem persistedQueueItemToProcess) {
    if (persistedQueueItemToProcess.getQueueModificationType() == QueueModificationType.ADDED) {
      addOrReplaceItem(persistedQueueItemToProcess);
    } else {
      removeItemIfNewer(persistedQueueItemToProcess);
    }
  }

  private void removeItemIfNewer(PersistedQueueItem persistedQueueItemToProcess) {
    var pathHash = hashFunction.hashUnencodedChars(persistedQueueItemToProcess.getPath()).asLong();
    var containedItem = itemsByPathHashMap.get(pathHash);
    if (nonNull(containedItem) && containedItem.getTimestamp() <= persistedQueueItemToProcess.getTimestamp()) {
      itemsByPathHashMap.remove(pathHash);
    }
  }

  private void addOrReplaceItem(PersistedQueueItem persistedQueueItemToProcess) {
    var pathHash = hashFunction.hashUnencodedChars(persistedQueueItemToProcess.getPath()).asLong();
    var containedItem = itemsByPathHashMap.get(pathHash);
    if (nonNull(containedItem)) {
      if (containedItem.getTimestamp() < persistedQueueItemToProcess.getTimestamp()) {
        itemsByPathHashMap.put(pathHash, persistedQueueItemToProcess);
      } else if (containedItem.getTimestamp() == persistedQueueItemToProcess.getTimestamp() && isTypeLeftMoreImportantThanRight(persistedQueueItemToProcess, containedItem)) {
        itemsByPathHashMap.put(pathHash, persistedQueueItemToProcess);
      }
    } else {
      itemsByPathHashMap.put(pathHash, persistedQueueItemToProcess);
    }
  }


  private boolean isTypeLeftMoreImportantThanRight(PersistedQueueItem itemLeft, PersistedQueueItem itemRight) {
    FileEvent.Type left = itemLeft.getFileEventType();
    FileEvent.Type right = itemRight.getFileEventType();
    var result = false;
    if (left != FileEvent.Type.UNKNOWN) {
      switch (right) {
        case CREATE -> result = true;
        case UPDATE -> result = left == FileEvent.Type.DELETE;
      }
    }
    return result;
  }
}
