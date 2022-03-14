package com.bytedompteur.documentfinder;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent.Type;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.ReactiveAdapter;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

//@Slf4j
public class Application {

  public static void main(String[] args) throws InterruptedException {
    var applicationHomeDirectory = determineApplicationHomeDirectory();
    System.setProperty("APPLICATION_HOME_DIRECTORY", applicationHomeDirectory);

//    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//    var joranConfigurator = new JoranConfigurator();
//    joranConfigurator.setContext(context);

    var log = LoggerFactory.getLogger(Application.class);
    log.info("Starting Document Finder");
    log.info("Determined index directory '{}'", applicationHomeDirectory);

    var applicationComponent = DaggerApplicationComponent
      .builder()
      .numberOfThreads(4)
      .applicationHomeDirectory(applicationHomeDirectory)
      .build();

    var fulltextSearchService = applicationComponent.fulltextSearchService();
    fulltextSearchService.startInboundFileEventProcessing();

    var fileWalker = applicationComponent.fileWalker();
    var queue = applicationComponent.queue();
    var settingsService = applicationComponent.settingsService();

    var disposable = ReactiveAdapter.subscribe(
      path -> new FileEvent(Type.CREATE, path),
      fileWalker.findFilesWithEndings(new HashSet<>(settingsService.getDefaultSettings().getFileTypes()),
//        Set.of(Path.of("X:\\OneDrive\\Dokumente\\DokumentePrivate\\Sureness_Networking"))),
        Set.of(Path.of("X:\\OneDrive - Byte Dompteur\\DocumentsSecured_encrypted"))),
      queue
    );

    while (fileWalker.isRunning() || !queue.isEmpty()) {
      Thread.sleep(1000);
      fulltextSearchService.commitScannedFiles();
    }


//    Thread.sleep(Duration.ofMinutes(10).toMillis());


    fulltextSearchService.commitScannedFiles();
    disposable.dispose();
    fulltextSearchService.stopInboundFileEventProcessing();

    System.out.println("FileWalker running: " + fileWalker.isRunning());
    System.out.println(fulltextSearchService.getScannedFiles());
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
