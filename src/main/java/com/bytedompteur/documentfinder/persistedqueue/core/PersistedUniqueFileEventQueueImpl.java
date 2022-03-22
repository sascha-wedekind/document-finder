package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

@Slf4j
public class PersistedUniqueFileEventQueueImpl implements PersistedUniqueFileEventQueue {

  private final Semaphore writeSemaphore = new Semaphore(1, true);
  private final Semaphore readSemaphore = new Semaphore(1, true);
  private final LinkedList<FileEvent> inMemoryEventsQueue = new LinkedList<>();
  private final Set<Long> knownPaths = new TreeSet<>();
  private final QueueRepository queueRepository;

  @SuppressWarnings("UnstableApiUsage")
  private final HashFunction hashFunction = Hashing.murmur3_128();

  @Inject
  public PersistedUniqueFileEventQueueImpl(QueueRepository queueRepository) {
    this.queueRepository = queueRepository;
    importPersistedQueueItems();
  }

  @Override
  public void pushOrOverwrite(FileEvent value) {
    try {
      readSemaphore.acquire();
      writeSemaphore.acquire();
      pushOrOverwriteSingle(value);
    } catch (InterruptedException e) {
      log.error("While pushing {} to in memory queue", value, e);
    } finally {
      writeSemaphore.release();
      readSemaphore.release();
    }
  }

  @Override
  public void pushOrOverwrite(List<FileEvent> value) {
    try {
      readSemaphore.acquire();
      writeSemaphore.acquire();
      value.forEach(this::pushOrOverwriteSingle);
    } catch (InterruptedException e) {
      log.error("While pushing {} to in memory queue", value, e);
    } finally {
      writeSemaphore.release();
      readSemaphore.release();
    }
  }

  private void pushOrOverwriteSingle(FileEvent value) {
    long pathHash = getPathHash(value);
    if (knownPaths.contains(pathHash)) {
      log.debug("Replacing '{}' int queue", value);
      replaceEvent(value);
    } else{
      log.debug("Adding '{}' to queue", value);
      inMemoryEventsQueue.add(value);
      knownPaths.add(pathHash);
    }

    try {
      queueRepository.save(value, QueueModificationType.ADDED);
    } catch (IOException e) {
      log.error("While saving '{}' in repository ", value, e);
    }
  }

  private void replaceEvent(FileEvent value) {
    boolean replaced = false;
    ListIterator<FileEvent> iterator = inMemoryEventsQueue.listIterator();
    while (iterator.hasNext() && !replaced) {
      FileEvent fileEvent = iterator.next();
      if (fileEvent.getPath().equals(value.getPath())) {
        iterator.set(value);
        replaced = true;
      }
    }
  }

  @Override
  public Optional<FileEvent> pop() {
    Optional<FileEvent> result = Optional.empty();
    try {
      readSemaphore.acquire();
      writeSemaphore.acquire();
      FileEvent event = inMemoryEventsQueue.pop();
      result = Optional.of(event);
      knownPaths.remove(getPathHash(event));
      queueRepository.save(event, QueueModificationType.REMOVED);
    } catch (IOException e) {
        log.error("While marking '{}' as removed in repository ", result.get(), e);
    } catch (NoSuchElementException e) {
      // IGNORE - thrown when queue is empty
    } catch (Exception e) {
      log.error("While pop from in memory queue", e);
    } finally {
      writeSemaphore.release();
      readSemaphore.release();
    }
    return result;
  }

  @Override
  public boolean isEmpty() throws InterruptedException {
    try {
      readSemaphore.acquire();
      return inMemoryEventsQueue.isEmpty();
    } finally {
      readSemaphore.release();
    }
  }

  @Override
  public int size() throws InterruptedException {
    try {
      readSemaphore.acquire();
      return inMemoryEventsQueue.size();
    } finally {
      readSemaphore.release();
    }
  }

  @Override
  public void clear() {
    try {
      readSemaphore.acquire();
      writeSemaphore.acquire();
      log.info("Start clearing the queue");
      var queueItemsRemoved = inMemoryEventsQueue.size();
      inMemoryEventsQueue.forEach(fileEvent -> {
        try {
          queueRepository.save(fileEvent, QueueModificationType.REMOVED);
        } catch (IOException e) {
          // Ignore
        }
      });
      inMemoryEventsQueue.clear();
      log.info("Removed {} items from the queue", queueItemsRemoved);
    } catch (InterruptedException e) {
      log.error("While clearing the queue", e);
    } finally {
      writeSemaphore.release();
      readSemaphore.release();
    }
  }

  private long getPathHash(FileEvent value) {
    return hashFunction.hashUnencodedChars(value.getPath().toString()).asLong();
  }

  protected void importPersistedQueueItems() {
    var fileEvents = queueRepository.readCompactedQueueLog();
    if (!fileEvents.isEmpty()) {
      pushOrOverwrite(fileEvents);
    }
  }
}
