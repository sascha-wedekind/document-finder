package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.out.FilesReadWriteAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Slf4j
public class QueueRepositoryImpl implements AutoCloseable, com.bytedompteur.documentfinder.persistedqueue.adapter.in.QueueRepository {

  /*
   * JVM Object destruction - START
   */
  private static final Cleaner CLEANER = Cleaner.create();
  static class State implements Runnable {

    private final QueueRepositoryImpl reference;

    State(QueueRepositoryImpl reference) {
      this.reference = reference;
    }

    public void run() {
      try {
        reference.close();
      } catch (Exception e) {
        // IGNORE - not sure if it's possible to log something in this JVM state.
      }
    }
  }
  /*
   * JVM Object destruction - END
   */

  public static final String REPOSITORY_FILE_NAME = "persisted_queue.log";

  private final PersistedQueueItemCompactor compactor;
  private final String applicationHomeDirectory;
  private final FilesReadWriteAdapter filesReadWriteAdapter;
  private final Clock clock;
  private BufferedWriter writer;

  public void save(FileEvent value, QueueModificationType queueModificationType) throws IOException {
    initWriterIfRequired();
    var fileLine = new PersistedQueueItem(
      clock.millis(),
      queueModificationType,
      value.getType(),
      value.getPath().toString()
    ).toFileLine();
    if (fileLine.isPresent()) {
      writer.write(fileLine.get());
      writer.newLine();
    }
  }

  public List<FileEvent> readCompactedQueueLog() {
    var filePath = createFilePath();
    log.info("Reading queue log from '{}'", filePath);
    var result = compactor
      .compact(readItemsFromFile(filePath))
      .stream()
      .sorted(Comparator.comparingLong(PersistedQueueItem::getTimestamp))
      .map(this::mapToFileEvent)
      .toList();
    log.info("Read {} items from '{}'", result.size(), filePath);
    deleteLog(filePath);
    return result;
  }

  @Override
  public void close() throws Exception {
    if (nonNull(writer)) {
      writer.flush();
      writer.close();
      writer = null;
    }
  }

  public void flush() throws IOException {
    if (nonNull(writer)) {
      writer.flush();
    }
  }

  private void deleteLog(Path filePath) {
    try {
      filesReadWriteAdapter.deleteIfExists(filePath);
      log.info("Deleted '{}'", filePath);
    } catch (IOException e) {
      log.error("Could not delete '{}'", filePath, e);
    }
  }

  protected List<PersistedQueueItem> readItemsFromFile(Path filePath) {
    List<PersistedQueueItem> persistedQueueItems = List.of();
    try {
      persistedQueueItems = filesReadWriteAdapter
        .readAllLines(filePath)
        .stream()
        .map(PersistedQueueItem::fromFileLine)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
    } catch (NoSuchFileException e) {
      log.warn("Queue log '{}' does not exist.", filePath);
    } catch (IOException e) {
      log.error("While reading queue log '{}'", filePath, e);
    }
    return persistedQueueItems;
  }

  protected FileEvent mapToFileEvent(PersistedQueueItem it) {
    return new FileEvent(it.getFileEventType(), Path.of(it.getPath()));
  }

  private void initWriterIfRequired() throws IOException {
    if (isNull(writer)) {
      writer = filesReadWriteAdapter.newBufferedWriter(
        createFilePath(),
        StandardOpenOption.WRITE,
        StandardOpenOption.APPEND,
        StandardOpenOption.CREATE
      );
      CLEANER.register(this, new State(this));
    }
  }

  private Path createFilePath() {
    return Path.of(applicationHomeDirectory, REPOSITORY_FILE_NAME);
  }
}
