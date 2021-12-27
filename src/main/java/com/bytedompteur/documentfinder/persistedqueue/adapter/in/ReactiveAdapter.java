package com.bytedompteur.documentfinder.persistedqueue.adapter.in;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReactiveAdapter {

  /**
   * @param <S> the source type
   * @param mapperFunction mapper function, that maps a source type into a FileEvent (Flux.map(...))
   * @param publisher a source type publisher to subscribe to.
   * @param queue the queue to add the file events to.
   * @return
   */
  public static <S> Disposable subscribe(
    Function<S, FileEvent> mapperFunction,
    Flux<S> publisher,
    PersistedUniqueFileEventQueue queue
  ) {
    return publisher
      .map(mapperFunction)
      .buffer(Duration.of(250, ChronoUnit.MILLIS))
      .subscribe(queue::pushOrOverwrite);
  }

  /**
   * @param queue the queue to obtain the events from.
   * @param emitInterval the interval to check if there are new entries in the queue.
   */
  public static Flux<FileEvent> subscribe(
    PersistedUniqueFileEventQueue queue,
    Duration emitInterval
  ) {
    return Flux
      .interval(emitInterval)
      .map(it -> queue.pop())
      .filter(Optional::isPresent)
      .map(Optional::get);
  }

  /**
   * @param queue the queue to obtain the events from.
   */
  public static Flux<FileEvent> subscribe(
    PersistedUniqueFileEventQueue queue
  ) {
    return subscribe(queue, Duration.of(150, ChronoUnit.MILLIS));
  }

}
