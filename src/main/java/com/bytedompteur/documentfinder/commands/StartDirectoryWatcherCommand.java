package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class StartDirectoryWatcherCommand implements Runnable {

  public static final int MAX_RETRIES = 20;
  public static final int DELAY_IN_MILLIS = 250;
  private final DirectoryWatcher directoryWatcher;

  @Override
  public void run() {
    if (!directoryWatcher.isWatching()) {
      directoryWatcher.startWatching();
      waitUntilWatcherIsWatching();
    }
  }

  private void waitUntilWatcherIsWatching() {
    var policy = RetryPolicy
      .builder()
      .handleResult(false) // retry as long as FileWalker.isRunning returns given result ('true')
      .withMaxRetries(MAX_RETRIES)
      .withDelay(Duration.ofMillis(DELAY_IN_MILLIS))
      .build();

    Failsafe
      .with(policy)
      .get(directoryWatcher::isWatching);
  }
}
