package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCServer;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.time.Duration;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StopIPCServerCommand implements Runnable{

  public static final int MAX_RETRIES = 20;
  public static final int DELAY_IN_MILLIS = 250;

  private final IPCServer server;

  @Override
  public void run() {
    if (server.isRunning()) {
      stopAndWait();
    }
  }

  protected void stopAndWait() {
    server.stop();

    var policy = RetryPolicy
      .builder()
      .handleResult(true) // retry as long as isRunning returns given result ('true')
      .withMaxRetries(MAX_RETRIES)
      .withDelay(Duration.ofMillis(DELAY_IN_MILLIS))
      .build();
    Failsafe
      .with(policy)
      .get(server::isRunning);
  }
}
