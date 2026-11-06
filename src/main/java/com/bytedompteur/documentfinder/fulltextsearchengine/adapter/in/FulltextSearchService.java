package com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in;

import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.time.LocalDate;

public interface FulltextSearchService {

  /**
   * Starts the service
   */
  void startInboundFileEventProcessing();

  /**
   * Stops the service
   */
  void stopInboundFileEventProcessing();

  boolean inboundFileEventProcessingRunning();

  int getScannedFiles();

  void commitScannedFiles();

  Flux<Path> getCurrentPathProcessed();

  Flux<SearchResult> findFilesWithNamesOrContentMatching(CharSequence charSequence, LocalDate updatedFrom, LocalDate updatedTo);

  Flux<SearchResult> findLastUpdated();

  void clearIndex();

  long getNumberOfEventsNotYetProcessed();
}
