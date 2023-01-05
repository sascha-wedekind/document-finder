package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StopDirectoryWatcherCommand implements Runnable {

  public static final int MAX_RETRIES = 20;
  public static final int DELAY_IN_MILLIS = 250;
  private final DirectoryWatcher directoryWatcher;

  @Override
  public void run() {
    if (directoryWatcher.isWatching()) {
      log.info("Stopping directory watcher");
      stopAndWait();
      log.info("Directory watcher stopped");
    }
  }

  protected void stopAndWait() {
    directoryWatcher.stopWatching();

    var policy = RetryPolicy
      .builder()
      .handleResult(true) // retry as long as Watcher.isWatching returns given result ('true')
      .withMaxRetries(MAX_RETRIES)
      .withDelay(Duration.ofMillis(DELAY_IN_MILLIS))
      .build();
    Failsafe
      .with(policy)
      .get(directoryWatcher::isWatching);
  }
}
