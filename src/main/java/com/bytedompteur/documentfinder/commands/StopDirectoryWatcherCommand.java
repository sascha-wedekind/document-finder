package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class StopDirectoryWatcherCommand implements Runnable {

  public static final int MAX_RETRIES = 20;
  public static final int DELAY_IN_MILLIS = 250;
  private final DirectoryWatcher directoryWatcher;

  @Override
  public void run() {
    if (directoryWatcher.isWatching()) {
      stopAndWait();
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
