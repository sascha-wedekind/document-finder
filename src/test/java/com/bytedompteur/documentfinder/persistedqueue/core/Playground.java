package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent.Type;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
public class Playground {

  @Test
  void name2() throws InterruptedException {
    var mockedRepository = Mockito.mock(QueueRepository.class);
    Mockito
      .when(mockedRepository.readCompactedQueueLog())
      .thenReturn(List.of());

    PersistedUniqueFileEventQueueImpl queue = new PersistedUniqueFileEventQueueImpl(mockedRepository);
    Flux
      .fromStream(IntStream.range(0, 2 * 60000).boxed())
      .map(it -> new FileEvent(Type.CREATE, Path.of(it.toString())))
      .buffer(1000)
      .subscribe(value -> {
        log.info("Adding 1000 events");
        queue.pushOrOverwrite(value);
        log.info("1000 events added");
      });

    while (queue.size() != 2 * 60000) {
      Thread.sleep(100);
    }
    log.info("All events added");
    System.out.println();
  }

  @Test
  void name() {

    var itemsList = Flux
      .fromStream(IntStream.range(0, 10).boxed())
      .map(it -> "/a/b/c/" + it)
      .switchMap(it -> Flux.fromIterable(List.of(
        new PersistedQueueItem(Clock.systemDefaultZone().millis(), QueueModificationType.ADDED, Type.CREATE, it),
        new PersistedQueueItem(Clock.systemDefaultZone().millis(), QueueModificationType.ADDED, Type.UPDATE, it),
        new PersistedQueueItem(Clock.systemDefaultZone().millis(), QueueModificationType.ADDED, Type.DELETE, it)
      )))
      .collectList()
      .block();

    Collections.shuffle(itemsList);


    itemsList
      .stream()
      .map(PersistedQueueItem::toFileLine)
      .map(Optional::get)
      .forEach(System.out::println);



//    var map = new TreeMap<Long, PersistedQueueItem>();
//    itemsList.forEach(it -> addOrReplaceItem(it, map));


    System.out.println();

//    var persistedQueueItemToProcess = itemsList.get(0);
//    addOrReplaceItem(persistedQueueItemToProcess, map);

  }


}
