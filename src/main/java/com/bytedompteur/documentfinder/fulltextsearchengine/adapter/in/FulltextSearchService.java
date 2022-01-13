package com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in;

import reactor.core.publisher.Flux;

import java.nio.file.Path;

public interface FulltextSearchService {

  void startInboundFileEventProcessing();

  void stopInboundFileEventProcessing();

  boolean inboundFileEventProcessingRunning();

  int getScannedFiles();

  void commitScannedFiles();

  Flux<Path> getCurrentPathProcessed();
}
