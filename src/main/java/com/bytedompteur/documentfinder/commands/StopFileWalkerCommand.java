package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalker;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.time.Duration;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StopFileWalkerCommand implements Runnable {

  public static final int MAX_RETRIES = 20;
  public static final int DELAY_IN_MILLIS = 250;
  private final FileWalker fileWalker;

  @Override
  public void run() {
    if (fileWalker.isRunning()) {
      stopAndWait();
    }
  }

  protected void stopAndWait() {
    fileWalker.stop();

    var policy = RetryPolicy
      .builder()
      .handleResult(true) // retry as long as FileWalker.isRunning returns given result ('true')
      .withMaxRetries(MAX_RETRIES)
      .withDelay(Duration.ofMillis(DELAY_IN_MILLIS))
      .build();
    Failsafe
      .with(policy)
      .get(fileWalker::isRunning);
  }
}
