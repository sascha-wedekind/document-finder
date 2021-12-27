package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FulltextSearchServiceImpl implements FulltextSearchService {

  private final FileEventHandler fileEventHandler;
  private final IndexRepository indexRepository;
  private AtomicBoolean eventHandlingStarted = new AtomicBoolean(false);

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
}
