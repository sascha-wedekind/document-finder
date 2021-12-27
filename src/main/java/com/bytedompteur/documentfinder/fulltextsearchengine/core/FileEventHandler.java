package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.FileEvent;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.FileEvent.Type;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.PersistedUniqueFileEventQueueAdapter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;

@RequiredArgsConstructor
@Slf4j
public class FileEventHandler {

  private final ExecutorService executorService;
  private final IndexRepository indexRepository;
  private final PersistedUniqueFileEventQueueAdapter adapter;
  private Disposable subscription;

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

  protected void handleFileEvent(FileEvent event) {
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
      var adapter = new FileParserRepositoryAdapter(indexRepository, parserTask, path);
      executorService.submit(adapter); // Consumer
    } catch (IOException e) {
      log.error("Error while processing file create or update of '{}'. File ignored", path, e);
    }
  }

  protected void handleFileDelete(Path path) {
    try {
      indexRepository.delete(path);
    } catch (IOException e) {
      log.error("Error while deleting '{}' from index", path, e);
    }
  }

}
