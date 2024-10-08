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
public class WaitUntilFulltextSearchServiceProcessedAllEventsCommand implements Runnable {

  public static final int DELAY_IN_MILLIS = 250;
  private final FulltextSearchService searchService;

  @Override
  public void run() {
    waitUntilAllEventsAreProcessed();
  }

  protected void waitUntilAllEventsAreProcessed() {
    log.info("Waiting until fulltext search service processed all events");
    var policy = RetryPolicy
      .<Long>builder()
      .handleResultIf(it -> it > 0)
      .withMaxRetries(-1)
      .withDelay(Duration.ofMillis(DELAY_IN_MILLIS))
      .build();
    Failsafe
      .with(policy)
      .get(searchService::getNumberOfEventsNotYetProcessed);
    log.info("Fulltext search service processed all events");
  }
}
