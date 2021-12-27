package com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in;

public interface FulltextSearchService {

  void startInboundFileEventProcessing();

  void stopInboundFileEventProcessing();

  boolean inboundFileEventProcessingRunning();

  int getScannedFiles();

  void commitScannedFiles();
}
