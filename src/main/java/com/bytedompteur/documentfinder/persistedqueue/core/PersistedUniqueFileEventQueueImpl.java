package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class PersistedUniqueFileEventQueueImpl implements PersistedUniqueFileEventQueue {

  public static final int FLUSH_REPOSITORY_THRESHOLD = 100;

  private final LinkedList<FileEvent> inMemoryEventsQueue = new LinkedList<>();
  private final Set<Long> knownPaths = new TreeSet<>();
  private final QueueRepositoryImpl queueRepository;
  private Long numberOfFilesAdded = 0L;
  private Long numberOfFilesRemoved = 0L;
  private int flushRepoCounter = 0;
  private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);

  @SuppressWarnings("UnstableApiUsage")
  private final HashFunction hashFunction = Hashing.murmur3_128();

  @Inject
  public PersistedUniqueFileEventQueueImpl(QueueRepositoryImpl queueRepository) {
    this.queueRepository = queueRepository;
    importPersistedQueueItems();
  }

  @Override
  public void pushOrOverwrite(FileEvent value) {
    Lock writeLock = rwLock.writeLock();
    try {
      writeLock.lock();
      pushOrOverwriteSingle(value);
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void pushOrOverwrite(List<FileEvent> value) {
    Lock writeLock = rwLock.writeLock();
    try {
      writeLock.lock();
      value.forEach(this::pushOrOverwriteSingle);
    } finally {
      writeLock.unlock();
    }
  }

  private void pushOrOverwriteSingle(FileEvent value) {
    if (!isPathValid(value)) {
      return;
    }

    long pathHash = getPathHash(value);
    if (knownPaths.contains(pathHash)) {
      log.debug("Replacing '{}' int queue", value);
      replaceEvent(value);
    } else {
      log.debug("Adding '{}' to queue", value);
      inMemoryEventsQueue.add(value);
      knownPaths.add(pathHash);
      numberOfFilesAdded++;

      try {
        queueRepository.save(value, QueueModificationType.ADDED);
      } catch (IOException e) {
        log.error("While saving '{}' in repository ", value, e);
      }
    }

    flushRepoWhenCounterExceedThreshold();
  }

  private boolean isPathValid(FileEvent value) {
    var isPathValid = true;
    try {
      Path.of(value.getPath().toString());
    } catch (Exception e) {
      isPathValid = false;
    }
    return isPathValid;
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
    var writeLock = rwLock.writeLock();
    try {
      writeLock.lock();
      if (!inMemoryEventsQueue.isEmpty()) {
        FileEvent event = inMemoryEventsQueue.pop();
        result = Optional.of(event);
        knownPaths.remove(getPathHash(event));
        queueRepository.save(event, QueueModificationType.REMOVED);
        numberOfFilesRemoved++;
        flushRepoWhenCounterExceedThreshold();
      }
    } catch (IOException e) {
      log.error("While marking '{}' as removed in repository ", result.get(), e);
    } catch (NoSuchElementException e) {
      log.error("", e);
    } catch (Exception e) {
      log.error("While pop from in memory queue", e);
    } finally {
      writeLock.unlock();
    }
    return result;
  }

  @Override
  public boolean isEmpty() throws InterruptedException {
    var readLock = rwLock.readLock();
    try {
      readLock.lock();
      var result = Objects.equals(getNumberOfFilesAdded(), getNumberOfFilesRemoved());
      log.debug("Is empty? Items added {} == items removed {} -> result {} -> items in actual in list {}", getNumberOfFilesAdded(), getNumberOfFilesRemoved(), result, inMemoryEventsQueue.size());
      flushRepository();
      return result;
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public int size() throws InterruptedException {
    var readLock = rwLock.readLock();
    try {
      readLock.lock();
      flushRepository();
      return inMemoryEventsQueue.size();
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void clear() {
    var writeLock = rwLock.writeLock();
    try {
      writeLock.lock();
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
      numberOfFilesAdded = 0L;
      numberOfFilesRemoved = 0L;
      flushRepository();
      log.info("Removed {} items from the queue", queueItemsRemoved);
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public Long getNumberOfFilesAdded() {
    return numberOfFilesAdded;
  }

  @Override
  public Long getNumberOfFilesRemoved() {
    return numberOfFilesRemoved;
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

  protected void flushRepoWhenCounterExceedThreshold() {
    flushRepoCounter++;
    if (flushRepoCounter > FLUSH_REPOSITORY_THRESHOLD) {
      flushRepository();
      flushRepoCounter = 0;
    }
  }

  private void flushRepository() {
    try {
      queueRepository.flush();
    } catch (IOException e) {
      log.error("While periodically flushing repository", e);
    }
  }
}
