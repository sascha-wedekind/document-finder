package com.bytedompteur.documentfinder.directorywatcher.core;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
class WatchServicePollHandler {

  private final Map<WatchKey, Path> pathByWatchKey;
  private final DirectoryWatcherImpl directoryWatcher;
  private final AbsolutePathWatchEventByKindComparator comparator = new AbsolutePathWatchEventByKindComparator();

  WatchServicePollHandler(DirectoryWatcherImpl directoryWatcher) {
    this.directoryWatcher = directoryWatcher;
    this.pathByWatchKey = directoryWatcher.getPathByWatchKey();
  }

  List<AbsolutePathWatchEvent> handlePoll(WatchKey key) {
    List<AbsolutePathWatchEvent> deduplicatedAndOrderedFileEvents;
    if (nonNull(key) && key.isValid()) {
      deduplicatedAndOrderedFileEvents = processWatchKey(key);
    } else {
      log.debug("No watch events because watch key is null");
      deduplicatedAndOrderedFileEvents = List.of();
    }
    logEventsReturned(deduplicatedAndOrderedFileEvents);
    return deduplicatedAndOrderedFileEvents;
  }

  /**
   * Ensures only the 'latest' event kind for a path is contained in the list. Also the events are ordered by kind. The
   * kinds are ordered like: CREATED < MODIFIED < DELETED
   */
  protected List<AbsolutePathWatchEvent> deduplicateAndOrderFileEvents(
    List<AbsolutePathWatchEvent> absolutePathEvents) {
    return absolutePathEvents
      .stream()
      .filter(AbsolutePathWatchEvent::isFileEvent)
      .sorted(comparator)
      .collect(Collectors.toMap(
        AbsolutePathWatchEvent::context,
        Function.identity(),
        (existing, replacement) -> replacement)
      )
      .values()
      .stream()
      .sorted(comparator)
      .collect(Collectors.toList());
  }

  /**
   * Adds or removes directories to / from DirectoryWatcher when they are created / removed while the watcher is
   * running.
   */
  protected void notifyWatcherAboutDirectoryEvents(List<AbsolutePathWatchEvent> absolutePathEvents) {
    List<AbsolutePathWatchEvent> directoryEvents = absolutePathEvents
      .stream()
      .filter(AbsolutePathWatchEvent::isDirectoryEvent)
      .collect(Collectors.toList());

    directoryEvents.forEach(it -> {
      if (StandardWatchEventKinds.ENTRY_CREATE.equals(it.kind())) {
        try {
          directoryWatcher.watchIncludingSubdirectories(it.context());
        } catch (IOException e) {
          log.error("Could not add '{}' to watcher", it.context(), e);
        }
      } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(it.kind())) {
        try {
          directoryWatcher.unwatchIncludingSubdirectories(it.context());
        } catch (IOException e) {
          log.error("Could not remove '{}' from watcher", it.context(), e);
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  protected List<AbsolutePathWatchEvent> mapToAbsolutePathEvents(WatchKey key) {
    List<WatchEvent<?>> events = key.pollEvents();
    if (log.isDebugEnabled()) {
      events.forEach(e -> log.debug("Processing raw watch event {} - {}", e.kind(), e.context()));
    }
    return events
      .stream()
      .filter(it -> it.kind().type().isAssignableFrom(Path.class))
      .map(it -> (WatchEvent<Path>) it)
      .filter(it -> pathByWatchKey.containsKey(key))
      .map(it -> new AbsolutePathWatchEvent(pathByWatchKey.get(key).resolve(it.context()), it))
      .collect(Collectors.toList());
  }

  private List<AbsolutePathWatchEvent> processWatchKey(WatchKey key) {
    List<AbsolutePathWatchEvent> deduplicatedAndOrderedFileEvents;
    List<AbsolutePathWatchEvent> absolutePathEvents = mapToAbsolutePathEvents(key);
    notifyWatcherAboutDirectoryEvents(absolutePathEvents);
    deduplicatedAndOrderedFileEvents = deduplicateAndOrderFileEvents(absolutePathEvents);
    key.reset();
    return deduplicatedAndOrderedFileEvents;
  }

  private void logEventsReturned(List<AbsolutePathWatchEvent> deduplicatedAndOrderedFileEvents) {
    if (log.isDebugEnabled()) {
      deduplicatedAndOrderedFileEvents.forEach(e -> log.debug("Returning absolute path watch event {} - {}", e.kind(), e.context()));
    }
  }
}
