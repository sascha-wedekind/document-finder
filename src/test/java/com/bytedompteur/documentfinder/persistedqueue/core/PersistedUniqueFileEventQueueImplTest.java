package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent.Type;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class PersistedUniqueFileEventQueueImplTest {

  @Test
  void pushAndPop10Events() throws InterruptedException {
    // Arrange
    PersistedUniqueFileEventQueueImpl sut = new PersistedUniqueFileEventQueueImpl();
    CountDownLatch totalQueueModificationsExpected = new CountDownLatch(20);
    List<FileEvent> eventsToAdd = List.of(
      new FileEvent(Type.CREATE, Path.of("4203589")),
      new FileEvent(Type.DELETE, Path.of("04")),
      new FileEvent(Type.UPDATE, Path.of("887461")),
      new FileEvent(Type.DELETE, Path.of("266")),
      new FileEvent(Type.CREATE, Path.of("1")),
      new FileEvent(Type.UPDATE, Path.of("1395")),
      new FileEvent(Type.CREATE, Path.of("363")),
      new FileEvent(Type.DELETE, Path.of("26310")),
      new FileEvent(Type.UPDATE, Path.of("18542")),
      new FileEvent(Type.UPDATE, Path.of("715"))
    );
    ArrayList<Object> eventsConsumed = new ArrayList<>();

    Thread producer = new Thread(() -> eventsToAdd
      .forEach(it -> {
        try {
          Thread.sleep(95);
          sut.pushOrOverwrite(it);
          totalQueueModificationsExpected.countDown();
        } catch (InterruptedException e) {
          fail(e);
        }
      }));

    Thread consumer = new Thread(() -> {
      int numberOfEventsToConsume = 10;
      while (numberOfEventsToConsume > 0) {
        try {
          Optional<FileEvent> event = sut.pop();
          if (event.isPresent()) {
            eventsConsumed.add(event.get());
            numberOfEventsToConsume--;
            totalQueueModificationsExpected.countDown();
          }
          Thread.sleep(75);
        } catch (InterruptedException e) {
          fail(e);
        }
      }
    });

    // Act
    consumer.start();
    producer.start();

    // Assert
    boolean allEventsAddedAndRemoved = totalQueueModificationsExpected.await(15L, TimeUnit.SECONDS);
    assertThat(allEventsAddedAndRemoved).isTrue();
    assertThat(eventsConsumed).containsExactly(eventsToAdd.toArray());
    assertThat(sut.size()).isEqualTo(0);
    assertThat(sut.isEmpty()).isTrue();
  }

  @Test
  void queueAvoidsMoreThanOneEventWithSamePath() throws InterruptedException {
    // Arrange
    PersistedUniqueFileEventQueueImpl sut = new PersistedUniqueFileEventQueueImpl();
    List<FileEvent> eventsToAdd = List.of(
      new FileEvent(Type.CREATE, Path.of("4203589")),
      new FileEvent(Type.DELETE, Path.of("4203589")),
      new FileEvent(Type.UPDATE, Path.of("4203589")),
      new FileEvent(Type.DELETE, Path.of("266")),
      new FileEvent(Type.CREATE, Path.of("1")),
      new FileEvent(Type.UPDATE, Path.of("1"))
    );
    ArrayList<Object> eventsConsumed = new ArrayList<>();

    // Act
    eventsToAdd.forEach(sut::pushOrOverwrite);

    // Assert
    while (!sut.isEmpty()) {
      sut.pop().ifPresent(eventsConsumed::add);
    }

    assertThat(eventsConsumed).hasSize(3);
    assertThat(eventsConsumed).containsExactly(
      eventsToAdd.get(2),
      eventsToAdd.get(3),
      eventsToAdd.get(5)
    );
  }

  @Test
  void queuePreservesOrder() throws InterruptedException {
    // Arrange
    PersistedUniqueFileEventQueueImpl sut = new PersistedUniqueFileEventQueueImpl();
    List<FileEvent> eventsToAdd = List.of(
      new FileEvent(Type.CREATE, Path.of("1")),
      new FileEvent(Type.DELETE, Path.of("2")),
      new FileEvent(Type.UPDATE, Path.of("3"))
    );
    ArrayList<Object> eventsConsumed = new ArrayList<>();

    // Act
    eventsToAdd.forEach(sut::pushOrOverwrite);

    // Assert
    while (!sut.isEmpty()) {
      sut.pop().ifPresent(eventsConsumed::add);
    }

    assertThat(eventsConsumed).hasSize(3);
    assertThat(eventsConsumed).containsExactly(eventsToAdd.toArray());
  }
}
