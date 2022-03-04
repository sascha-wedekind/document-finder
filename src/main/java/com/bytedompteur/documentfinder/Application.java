package com.bytedompteur.documentfinder;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent.Type;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.ReactiveAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class Application {

  public static void main(String[] args) throws InterruptedException {

    var applicationComponent = DaggerApplicationComponent
      .builder()
      .numberOfThreads(4)
      .applicationHomeDirectory(determineApplicationHomeDirectory())
      .build();

    var fulltextSearchService = applicationComponent.fulltextSearchService();
    fulltextSearchService.startInboundFileEventProcessing();

    var fileWalker = applicationComponent.fileWalker();
    var queue = applicationComponent.queue();

    var disposable = ReactiveAdapter.subscribe(
      path -> new FileEvent(Type.CREATE, path),
      fileWalker.findFilesWithEndings(Set.of("pdf"),
        Set.of(Path.of("X:\\OneDrive\\Dokumente\\DokumentePrivate\\Sureness_Networking"))),
      queue
    );

    Thread.sleep(30000);

    fulltextSearchService.commitScannedFiles();
    disposable.dispose();
    fulltextSearchService.stopInboundFileEventProcessing();
    System.out.println("FileWalker running: " + fileWalker.isRunning());
    System.out.println(fulltextSearchService.getScannedFiles());
  }

  public static String determineApplicationHomeDirectory() {
    var homeDirectory = Optional
      .ofNullable(System.getProperty("documentfinder.homedir"))
      .orElseGet(() -> {
        var indexDirName = ".documentfinder";
        return Path.of(System.getProperty("user.home"), indexDirName).toString();
      });
    log.info("Determined index directory '{}'", homeDirectory);
    return homeDirectory;
  }

}
