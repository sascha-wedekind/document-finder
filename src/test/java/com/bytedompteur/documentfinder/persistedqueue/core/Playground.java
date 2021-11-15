package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent.Type;
import java.nio.file.Path;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Slf4j
public class Playground {


  @Test
  void name() {
    Flux
      .fromStream(IntStream.range(0, 1000).boxed())
      .buffer(100)
      .subscribe(it -> {
        System.out.println(it.size());
      });

//    Flux<Object> empty = Flux.empty();
//
//    empty.subscribeWith()

  }

  @Test
  void name2() throws InterruptedException {
    PersistedUniqueFileEventQueueImpl queue = new PersistedUniqueFileEventQueueImpl();
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

}
