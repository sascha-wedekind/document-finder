package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import com.bytedompteur.documentfinder.directorywatcher.adapter.in.FileWatchEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.ReactiveAdapter;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class StartDirectoryWatcherCommand implements Runnable {

  public static final int MAX_RETRIES = 20;
  public static final int DELAY_IN_MILLIS = 250;
  private final DirectoryWatcher directoryWatcher;
  private final SettingsService settingsService;
  private final PersistedUniqueFileEventQueue queue;

  @Override
  public void run() {
    if (!directoryWatcher.isWatching()) {
      directoryWatcher.startWatching();
      waitUntilWatcherIsWatching();

      ReactiveAdapter.subscribe(
        this::map,
        directoryWatcher.fileEvents(),
        queue
      );

      configureWatcherWithDirectoriesFromSettings();
    }
  }

  protected FileEvent map(FileWatchEvent value) {
    FileEvent.Type type;
    switch (value.getType()) {
      case UPDATE -> type = FileEvent.Type.UPDATE;
      case CREATE -> type = FileEvent.Type.CREATE;
      default -> type = FileEvent.Type.DELETE;
    }
    return new FileEvent(type, value.getPath());
  }


  protected void configureWatcherWithDirectoriesFromSettings() {
    settingsService
      .read()
      .orElse(settingsService.getDefaultSettings())
      .getFolders()
      .stream()
      .map(Path::of)
      .forEach(this::watchIncludingSubdirectories);
  }

  protected void watchIncludingSubdirectories(Path value) {
    try {
      log.info("Watching '{}' including subdirectories", value);
      directoryWatcher.watchIncludingSubdirectories(value);
    } catch (IOException e) {
      log.error("Failed to watch directory '{}'", value, e);
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
