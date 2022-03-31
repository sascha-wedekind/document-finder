package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalker;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.ReactiveAdapter;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Duration;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StartFileWalkerCommand implements Runnable {

  public static final int MAX_RETRIES = 20;
  public static final int DELAY_IN_MILLIS = 250;
  private final FileWalker fileWalker;
  private final PersistedUniqueFileEventQueue queue;
  private final SettingsService settingsService;

  @Override
  public void run() {
    if (!fileWalker.isRunning()) {
      startAndWait();
    }
  }

  protected void startAndWait() {
    startWalkerAndConnectWithQueue();
    waitUntilWalkerIsRunning();
  }

  private void waitUntilWalkerIsRunning() {
    var policy = RetryPolicy
      .builder()
      .handleResult(false) // retry as long as FileWalker.isRunning returns given result ('true')
      .withMaxRetries(MAX_RETRIES)
      .withDelay(Duration.ofMillis(DELAY_IN_MILLIS))
      .build();

    Failsafe
      .with(policy)
      .get(fileWalker::isRunning);
  }

  private void startWalkerAndConnectWithQueue() {
    var settings = settingsService.read().orElse(settingsService.getDefaultSettings());
    var fileTypes = settings.getFileTypes().stream().collect(Collectors.toUnmodifiableSet());
    var paths = settings.getFolders().stream().map(Path::of).collect(Collectors.toUnmodifiableSet());

    ReactiveAdapter.subscribe(
      path -> new FileEvent(FileEvent.Type.CREATE, path),
      fileWalker.findFilesWithEndings(fileTypes, paths),
      queue
    );
  }
}
