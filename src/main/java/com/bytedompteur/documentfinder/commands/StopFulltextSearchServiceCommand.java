package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StopFulltextSearchServiceCommand implements Runnable {

  public static final int MAX_RETRIES = 20;
  public static final int DELAY_IN_MILLIS = 250;
  private final FulltextSearchService searchService;

  @Override
  public void run() {
    if (searchService.inboundFileEventProcessingRunning()) {
      log.info("Stopping fulltext search service");
      stopAndWait();
      log.info("Fulltext search service stopped");
    }
  }

  protected void stopAndWait() {
    searchService.stopInboundFileEventProcessing();

    var policy = RetryPolicy
      .builder()
      .handleResult(true) // retry as long as inboundFileEventProcessingRunning returns given result ('true')
      .withMaxRetries(MAX_RETRIES)
      .withDelay(Duration.ofMillis(DELAY_IN_MILLIS))
      .build();
    Failsafe
      .with(policy)
      .get(searchService::inboundFileEventProcessingRunning);
  }
}
