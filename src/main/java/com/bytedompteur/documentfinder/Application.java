package com.bytedompteur.documentfinder;

import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Application {

  public static void main(String[] args) throws Exception {
    var applicationHomeDirectory = determineApplicationHomeDirectory();
    System.setProperty("APPLICATION_HOME_DIRECTORY", applicationHomeDirectory);

    var log = LoggerFactory.getLogger(Application.class);
    log.info("Starting Document Finder");
    log.info("Determined index directory '{}'", applicationHomeDirectory);

    var applicationComponent = DaggerApplicationComponent
      .builder()
      .numberOfThreads(0)
      .applicationHomeDirectory(applicationHomeDirectory)
      .build();

    applicationComponent.clearAllCommand().run();
    applicationComponent.startAllCommand().run();

    var fulltextSearchService = applicationComponent.fulltextSearchService();
    var queue = applicationComponent.queue();

    // Ramp up phase
    Thread.sleep(2000);

    CompletableFuture
      .runAsync(applicationComponent.waitUntilPersistedQueueIsEmptyCommand(), applicationComponent.executorService())
      .thenRunAsync(applicationComponent.waitUntilFulltextSearchServiceProcessedAllEventsCommand(), applicationComponent.executorService())
      .join();


    log.info("Added to queue:" + queue.getNumberOfFilesAdded() + ", removed: " + queue.getNumberOfFilesRemoved());
    log.info("Number of scanned files is {}", fulltextSearchService.getScannedFiles());

    log.info("STOPPING ALL GRACEFUL");
    applicationComponent.stopAllGracefulCommand().run();
    log.info("Shutdown Executor service");
    applicationComponent.executorService().shutdown();
    log.info("Executor service shut down");
    System.exit(0);
  }

  public static String determineApplicationHomeDirectory() {
    return Optional
      .ofNullable(System.getProperty("documentfinder.homedir"))
      .orElseGet(() -> {
        var indexDirName = ".documentfinder";
        return Path.of(System.getProperty("user.home"), indexDirName).toString();
      });
  }

}
