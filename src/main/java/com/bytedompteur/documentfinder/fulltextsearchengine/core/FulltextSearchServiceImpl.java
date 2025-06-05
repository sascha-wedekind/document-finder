package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.SearchResult;
import com.bytedompteur.documentfinder.searchhistory.SearchHistoryManager; // Added import
// import lombok.RequiredArgsConstructor; // Will be removed
import reactor.core.publisher.Flux;

import jakarta.inject.Inject; // Keep for constructor injection
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

// @RequiredArgsConstructor will be removed as constructor is now manually defined.
public class FulltextSearchServiceImpl implements FulltextSearchService {

  private final FileEventHandler fileEventHandler;
  private final IndexRepository indexRepository;
  private final SearchHistoryManager searchHistoryManager; // Added field
  private final AtomicBoolean eventHandlingStarted = new AtomicBoolean(false);

  @Inject // Dagger uses this constructor
  public FulltextSearchServiceImpl(FileEventHandler fileEventHandler,
                                   IndexRepository indexRepository,
                                   SearchHistoryManager searchHistoryManager) {
    this.fileEventHandler = fileEventHandler;
    this.indexRepository = indexRepository;
    this.searchHistoryManager = searchHistoryManager;
  }

  @Override
  public void startInboundFileEventProcessing() {
    if (eventHandlingStarted.compareAndSet(false, true)) {
      fileEventHandler.startEventHandling();
    }
  }

  @Override
  public void stopInboundFileEventProcessing() {
    if (eventHandlingStarted.compareAndSet(true, false)) {
      fileEventHandler.stopEventHandling();
    }
  }

  @Override
  public boolean inboundFileEventProcessingRunning() {
    return eventHandlingStarted.get();
  }

  @Override
  public int getScannedFiles() {
    return indexRepository.count();
  }

  @Override
  public void commitScannedFiles() {
    indexRepository.commit();
  }

  @Override
  public Flux<Path> getCurrentPathProcessed() {
    return this.fileEventHandler.getCurrentPathProcessed();
  }

  @Override
  public Flux<SearchResult> findFilesWithNamesOrContentMatching(CharSequence charSequence) {
    if (charSequence != null) {
      String query = charSequence.toString().trim(); // Trim the query
      if (!query.isEmpty()) {
        this.searchHistoryManager.addSearchQuery(query);
      }
    }
    return indexRepository.findByFileNameOrContent(charSequence);
  }

  @Override
  public Flux<SearchResult> findLastUpdated() {
    return indexRepository.findLastUpdated();
  }

  @Override
  public void clearIndex() {
    indexRepository.clear();
  }

  @Override
  public long getNumberOfEventsNotYetProcessed() {
    return fileEventHandler.getNumberOfEventsNotYetProcessed();
  }
}
