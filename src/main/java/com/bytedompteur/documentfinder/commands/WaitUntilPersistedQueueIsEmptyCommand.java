package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.time.Duration;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WaitUntilPersistedQueueIsEmptyCommand implements Runnable {

  public static final int DELAY_IN_MILLIS = 250;
  private final PersistedUniqueFileEventQueue queue;

  @Override
  public void run() {
    var policy = RetryPolicy
      .builder()
      .handleResult(false)
      .withMaxRetries(-1)
      .withDelay(Duration.ofMillis(DELAY_IN_MILLIS))
      .build();
    Failsafe
      .with(policy)
      .get(queue::isEmpty);
  }
}
