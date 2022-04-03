package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.SearchResult;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FulltextSearchServiceImpl implements FulltextSearchService {

  private final FileEventHandler fileEventHandler;
  private final IndexRepository indexRepository;
  private final AtomicBoolean eventHandlingStarted = new AtomicBoolean(false);

  @Override
  public void startInboundFileEventProcessing() {
    if (eventHandlingStarted.compareAndSet(false, true)) {
      fileEventHandler.startEventHandling();
    }
  }

  @Override
  public void stopInboundFileEventProcessing() {
    if (eventHandlingStarted.compareAndSet(true, false)) {
      fileEventHandler.stopEventHandling();
    }
  }

  @Override
  public boolean inboundFileEventProcessingRunning() {
    return eventHandlingStarted.get();
  }

  @Override
  public int getScannedFiles() {
    return indexRepository.count();
  }

  @Override
  public void commitScannedFiles() {
    indexRepository.commit();
  }

  @Override
  public Flux<Path> getCurrentPathProcessed() {
    return this.fileEventHandler.getCurrentPathProcessed();
  }

  @Override
  public Flux<SearchResult> findFilesWithNamesOrContentMatching(CharSequence charSequence) {
    return indexRepository.findByFileNameOrContent(charSequence);
  }

  @Override
  public void clearIndex() {
    indexRepository.clear();
  }

  @Override
  public long getNumberOfEventsNotYetProcessed() {
    return fileEventHandler.getNumberOfEventsNotYetProcessed();
  }
}
