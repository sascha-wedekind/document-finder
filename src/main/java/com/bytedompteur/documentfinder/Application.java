package com.bytedompteur.documentfinder;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent.Type;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.ReactiveAdapter;
import java.nio.file.Path;
import java.util.Set;

public class Application {

  public static void main(String[] args) throws InterruptedException {
    var applicationComponent = DaggerApplicationComponent
      .builder()
      .numberOfThreads(4)
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

}
