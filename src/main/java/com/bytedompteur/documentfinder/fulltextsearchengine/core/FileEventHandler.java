package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.FileEvent;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.FileEvent.Type;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.PersistedUniqueFileEventQueueAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
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
    log.debug("Number of events to process is {}", result);
    return result;
  }

  protected void handleFileEvent(FileEvent event) {
    log.debug("Emitting {}", event.getPath());
    currentPathProcessedSink.tryEmitNext(event.getPath());
    filesToProcess.incrementAndGet();
    if (event.getType() == Type.DELETE) {
      handleFileDelete(event.getPath());
    } else {
      handleFileCreateOrUpdate(event.getPath());
    }
  }

  protected void handleFileCreateOrUpdate(Path path) {
    try {
      var parserTask = FileParserTask.create(path);
      executorService.submit(parserTask);// Producer
      executorService.submit(new FileParserRepositoryAdapter(indexRepository, parserTask, path, filesToProcess)); // Consumer
    } catch (IOException e) {
      log.error("Error while processing file create or update of '{}'. File ignored", path, e);
    }
  }

  protected void handleFileDelete(Path path) {
    try {
      indexRepository.delete(path);
      filesToProcess.decrementAndGet();
    } catch (IOException e) {
      log.error("Error while deleting '{}' from index", path, e);
    }
  }

}
