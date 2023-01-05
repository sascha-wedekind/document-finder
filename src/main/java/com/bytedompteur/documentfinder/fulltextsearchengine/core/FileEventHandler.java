package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.FileEvent;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.FileEvent.Type;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.FilesAdapter;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.PersistedUniqueFileEventQueueAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@Slf4j
public class FileEventHandler {

  private final ExecutorService executorService;
  private final IndexRepository indexRepository;
  private final PersistedUniqueFileEventQueueAdapter adapter;
  private final FilesAdapter filesAdapter;
  private final AtomicLong filesToProcess = new AtomicLong(0);
  private final Sinks.Many<Path> currentPathProcessedSink = Sinks.many().multicast().directBestEffort();
  private Disposable subscription;
  private Flux<Path> publish;

  public Flux<Path> getCurrentPathProcessed() {
    if (publish == null) {
      publish = currentPathProcessedSink.asFlux();
    }
    return publish;
  }

  public void startEventHandling() {
    subscription = adapter
      .subscribe()
      .subscribe(this::handleFileEvent);
  }

  public void stopEventHandling() {
    Optional
      .ofNullable(subscription)
      .ifPresent(Disposable::dispose);
  }

  public long getNumberOfEventsNotYetProcessed() {
    var result = filesToProcess.get();
    log.info("Number of events to process is {}", result);
    return result;
  }

  protected void handleFileEvent(FileEvent event) {
    var path = event.getPath();
    if (isPathInvalid(path)) {
      return;
    }
    log.debug("Emitting {}", path);
    currentPathProcessedSink.tryEmitNext(path);
    filesToProcess.incrementAndGet();
    if (event.getType() == Type.DELETE) {
      handleFileDelete(path);
    } else {
      handleFileCreateOrUpdate(path);
    }
  }

  protected void handleFileCreateOrUpdate(Path path) {
    try {
      executorService.submit(new FileParserRepositoryAdapter(
        indexRepository,
        FileParserTask.create(path),
        path,
        filesToProcess
      ));
      log.debug("Created producer / consumer parser tasks for {}", path);
    } catch (Throwable e) {
      filesToProcess.decrementAndGet();
      log.error("Error while processing file create or update of '{}'. File ignored", path, e);
    }
  }

  protected void handleFileDelete(Path path) {
    try {
      log.debug("Remove '{}' from index", path);
      indexRepository.delete(path);
    } catch (Throwable e) {
      log.error("Error while deleting '{}' from index", path, e);
    } finally {
      filesToProcess.decrementAndGet();
    }
  }

  boolean isPathInvalid(Path path) {
    var pathInvalid = false;
    if (filesAdapter.notExists(path)) {
      log.warn("Path '{}' does not exist", path);
      pathInvalid = true;
    }

    if (filesAdapter.isNotReadable(path)) {
      log.warn("Path '{}' is not readable", path);
      pathInvalid = true;
    }

    if (filesAdapter.isEmpty(path)) {
      log.warn("Path {} is empty, ignoring", path);
      pathInvalid = true;
    }
    return pathInvalid;
  }
}
