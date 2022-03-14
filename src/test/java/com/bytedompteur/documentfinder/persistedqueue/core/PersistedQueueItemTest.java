package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersistedQueueItemTest {

  @Test
  void fromFileLine_returnsItem_whenLineCouldBeParsedWithoutErrors() {
    // Arrange
    var fileLine = "1646754415183,ADDED,CREATE,'a\\b\\c\\d'";

    // Act
    var result = PersistedQueueItem.fromFileLine(fileLine);

    // Assert
    var expectedItem = new PersistedQueueItem(1646754415183L, QueueModificationType.ADDED, FileEvent.Type.CREATE, "a\\b\\c\\d");
    assertThat(result).contains(expectedItem);
  }

  @Test
  void fromFileLine_returnsEmptyOptional_whenLinePathDoesNotStartAndEndWithAnApostrophe() {
    // Arrange
    var fileLine = "1646754415183,ADDED,CREATE,'a";

    // Act
    var result = PersistedQueueItem.fromFileLine(fileLine);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void fromFileLine_returnsEmptyOptional_whenLineHasLessThanFourSectionsSeparatedByComma() {
    // Arrange
    var fileLine = "1646754415183,ADDED,CREATE";

    // Act
    var result = PersistedQueueItem.fromFileLine(fileLine);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void fromFileLine_returnsEmptyOptional_whenLineIsEmpty() {
    // Arrange
    var fileLine = "";

    // Act
    var result = PersistedQueueItem.fromFileLine(fileLine);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void fromFileLine_returnsEmptyOptional_whenLineIsNull() {
    // Act
    var result = PersistedQueueItem.fromFileLine(null);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void toFileLine_returnsLine_whenItemHasNonNullFields() {
    // Arrange
    var item = new PersistedQueueItem(123L, QueueModificationType.ADDED, FileEvent.Type.CREATE, "a");

    // Act
    var result = item.toFileLine();

    // Assert
    assertThat(result).contains("123,ADDED,CREATE,'a'");
  }

  @Test
  void toFileLine_returnsEmptyOptional_whenItemHasAnyNullField() {
    // Arrange
    var item1 = new PersistedQueueItem(123L, null, FileEvent.Type.CREATE, "a");
    var item2 = new PersistedQueueItem(123L, QueueModificationType.ADDED, null, "a");
    var item3 = new PersistedQueueItem(123L, QueueModificationType.ADDED, FileEvent.Type.CREATE, null);

    // Act
    var result1 = item1.toFileLine();
    var result2 = item2.toFileLine();
    var result3 = item3.toFileLine();

    // Assert
    assertThat(result1).isEmpty();
    assertThat(result2).isEmpty();
    assertThat(result3).isEmpty();
  }
}
