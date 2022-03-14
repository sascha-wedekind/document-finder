package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class PersistedQueueItemCompactorTest {

  PersistedQueueItemCompactor sut = new PersistedQueueItemCompactor();

  @Test
  void compact_returnsEmptyCollection_whenParameterIsNull() {
    // Act
    var result = sut.compact(null);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void compact_selectItemWithHighestTimestamp_whenPathOfAllItemsIsEqual() {
    // Arrange
    var items = List.of(
      new PersistedQueueItem(1L, QueueModificationType.ADDED, FileEvent.Type.DELETE, "/a/b/c"),
      new PersistedQueueItem(2L, QueueModificationType.ADDED, FileEvent.Type.UPDATE, "/a/b/c"),
      new PersistedQueueItem(3L, QueueModificationType.ADDED, FileEvent.Type.CREATE, "/a/b/c")
    );

    // Act
    var result = sut.compact(items);

    // Assert
    assertThat(result).containsExactly(
      new PersistedQueueItem(3L, QueueModificationType.ADDED, FileEvent.Type.CREATE, "/a/b/c")
    );
  }

  @Test
  void compact_selectItemWithMostImportantFileEventType_whenPathAndTimestampOfAllItemsIsEqual() {
    // Arrange
    var items = List.of(
      new PersistedQueueItem(1L, QueueModificationType.ADDED, FileEvent.Type.DELETE, "/a/b/c"),
      new PersistedQueueItem(1L, QueueModificationType.ADDED, FileEvent.Type.UPDATE, "/a/b/c"),
      new PersistedQueueItem(1L, QueueModificationType.ADDED, FileEvent.Type.CREATE, "/a/b/c")
    );

    // Act
    var result = sut.compact(items);

    // Assert
    assertThat(result).containsExactly(
      new PersistedQueueItem(1L, QueueModificationType.ADDED, FileEvent.Type.DELETE, "/a/b/c")
    );
  }

  @Test
  void compact_removesLatestItem_whenPathIsEqualTimestampIsTheHighestAndModificationTypeIsRemoved() {
    // Arrange
    var items = List.of(
      new PersistedQueueItem(1L, QueueModificationType.ADDED, FileEvent.Type.DELETE, "/a/b/c"),
      new PersistedQueueItem(2L, QueueModificationType.REMOVED, FileEvent.Type.UPDATE, "/a/b/c"),
      new PersistedQueueItem(3L, QueueModificationType.ADDED, FileEvent.Type.CREATE, "/a/b/c"),
      new PersistedQueueItem(4L, QueueModificationType.REMOVED, FileEvent.Type.CREATE, "/a/b/c")
    );

    // Act
    var result = sut.compact(items);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void compact_doesNotRemovesLatestItem_whenPathIsEqualTimestampIsLowerAndModificationTypeIsRemoved() {
    // Arrange
    var items = List.of(
      new PersistedQueueItem(1L, QueueModificationType.ADDED, FileEvent.Type.DELETE, "/a/b/c"),
      new PersistedQueueItem(2L, QueueModificationType.REMOVED, FileEvent.Type.UPDATE, "/a/b/c"),
      new PersistedQueueItem(4L, QueueModificationType.ADDED, FileEvent.Type.CREATE, "/a/b/c"),
      new PersistedQueueItem(3L, QueueModificationType.REMOVED, FileEvent.Type.CREATE, "/a/b/c")
    );

    // Act
    var result = sut.compact(items);

    // Assert
    assertThat(result).containsExactly(
      new PersistedQueueItem(4L, QueueModificationType.ADDED, FileEvent.Type.CREATE, "/a/b/c")
    );
  }
}
