package com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent.Type;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.ReactiveAdapter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class PersistedUniqueFileEventQueueAdapter {

  private final PersistedUniqueFileEventQueue queue;

  public Flux<FileEvent> subscribe() {
    return ReactiveAdapter
      .subscribe(queue)
      .filter(it -> it.getType() != Type.UNKNOWN)
      .map(this::map);
  }

  protected FileEvent map(com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent value) {
    if (value.getType() == Type.DELETE) {
      return new FileEvent(FileEvent.Type.DELETE, value.getPath());
    } else {
      return new FileEvent(FileEvent.Type.OTHER, value.getPath());
    }
  }

}
