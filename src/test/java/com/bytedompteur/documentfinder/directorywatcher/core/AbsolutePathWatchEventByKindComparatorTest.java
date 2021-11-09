package com.bytedompteur.documentfinder.directorywatcher.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.nio.file.StandardWatchEventKinds;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbsolutePathWatchEventByKindComparatorTest {

  private AbsolutePathWatchEventByKindComparator sut = new AbsolutePathWatchEventByKindComparator();

  @Mock
  AbsolutePathWatchEvent e1;

  @Mock
  AbsolutePathWatchEvent e2;

  @Test
  void compare_returns0_whenBothKindsAreCreate() {
    // Arrange
    when(e1.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
    when(e2.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);

    // Act
    int result = sut.compare(e1, e2);

    // Assert
    assertThat(result).isEqualTo(0);
  }

  @Test
  void compare_returns0_whenBothKindsAreModify() {
    // Arrange
    when(e1.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
    when(e2.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);

    // Act
    int result = sut.compare(e1, e2);

    // Assert
    assertThat(result).isEqualTo(0);
  }

  @Test
  void compare_returns0_whenBothKindsAreDelete() {
    // Arrange
    when(e1.kind()).thenReturn(StandardWatchEventKinds.ENTRY_DELETE);
    when(e2.kind()).thenReturn(StandardWatchEventKinds.ENTRY_DELETE);

    // Act
    int result = sut.compare(e1, e2);

    // Assert
    assertThat(result).isEqualTo(0);
  }

  @Test
  void compare_returnsNegativeNumber_whenFirstKindIsCreateAndSecondIsModify() {
    // Arrange
    when(e1.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
    when(e2.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);

    // Act
    int result = sut.compare(e1, e2);

    // Assert
    assertThat(result).isNegative();
  }

  @Test
  void compare_returnsNegativeNumber_whenFirstKindIsCreateAndSecondIsDelete() {
    // Arrange
    when(e1.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
    when(e2.kind()).thenReturn(StandardWatchEventKinds.ENTRY_DELETE);

    // Act
    int result = sut.compare(e1, e2);

    // Assert
    assertThat(result).isNegative();
  }

  @Test
  void compare_returnsNegativeNumber_whenFirstKindIsModifyAndSecondIsDelete() {
    // Arrange
    when(e1.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
    when(e2.kind()).thenReturn(StandardWatchEventKinds.ENTRY_DELETE);

    // Act
    int result = sut.compare(e1, e2);

    // Assert
    assertThat(result).isNegative();
  }

  @Test
  void compare_returnsPositiveNumber_whenFirstKindIsModifyAndSecondIsCCreate() {
    // Arrange
    when(e1.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
    when(e2.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);

    // Act
    int result = sut.compare(e1, e2);

    // Assert
    assertThat(result).isPositive();
  }

  @Test
  void compare_returnsPositiveNumber_whenFirstKindIsDeleteAndSecondIsCreate() {
    // Arrange
    when(e1.kind()).thenReturn(StandardWatchEventKinds.ENTRY_DELETE);
    when(e2.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);

    // Act
    int result = sut.compare(e1, e2);

    // Assert
    assertThat(result).isPositive();
  }

  @Test
  void compare_returnsPositiveNumber_whenFirstKindIsDeleteAndSecondIsModify() {
    // Arrange
    when(e1.kind()).thenReturn(StandardWatchEventKinds.ENTRY_DELETE);
    when(e2.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);

    // Act
    int result = sut.compare(e1, e2);

    // Assert
    assertThat(result).isPositive();
  }
}
