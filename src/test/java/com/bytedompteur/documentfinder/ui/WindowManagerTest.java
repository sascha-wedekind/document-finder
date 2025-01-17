package com.bytedompteur.documentfinder.ui;

import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowComponent;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowComponent;
import com.bytedompteur.documentfinder.ui.systemtray.dagger.SystemTrayComponent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WindowManagerTest {

  @Mock
  Function<Parent, Scene> mockedSceneFactory;

  @Mock
  JavaFxPlatformAdapter mockedPlatformAdapter;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  MainWindowComponent.Builder mockedMainWindowComponentBuilder;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  OptionsWindowComponent.Builder mockedOptionsWindowComponentBuilder;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  SystemTrayComponent.Builder mockedSystemTrayComponentBuilder;

  @InjectMocks
  WindowManager sut;

  @BeforeEach
  void setUp() {
    when(mockedSceneFactory.apply(any())).thenReturn(mock(Scene.class));
  }

  @Test
  void showMainWindow_setsCurrentControllerToMainWindowController() {
    // Act
    sut.showMainWindow();

    // Assert
    var currentController = sut.getCurrentController();
    var mainWindowController = mockedMainWindowComponentBuilder.build().mainWindowController();
    assertThat(currentController).isEqualTo(mainWindowController);
  }

  @Test
  void showMainWindow_callsAfterViewShowMethodOnMainWindowController() {
    // Act
    sut.showMainWindow();

    // Assert
    var mainWindowController = mockedMainWindowComponentBuilder.build().mainWindowController();
    var runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(mockedPlatformAdapter, atLeastOnce()).runLater(runnableArgumentCaptor.capture());
    executeLastRunnableInList(runnableArgumentCaptor.getAllValues());
    verify(mainWindowController).afterViewShown();
  }

  @Test
  void showMainWindow_wrapsMainWindowsNodeInScene() {
    // Act
    sut.showMainWindow();

    // Assert
    var parent = mockedMainWindowComponentBuilder.build().mainViewNode().get();
    verify(mockedSceneFactory).apply(parent);
  }

  @Test
  void showMainWindow_setsCurrentControllerToOptionsWindowController() {
    // Act
    sut.showOptionsWindow();

    // Assert
    var currentController = sut.getCurrentController();
    var optionsWindowController = mockedOptionsWindowComponentBuilder.build().optionsWindowController();
    assertThat(currentController).isEqualTo(optionsWindowController);
  }

  @Test
  void showMainWindow_callsAfterViewShowMethodOnOptionsWindowController() {
    // Act
    sut.showOptionsWindow();

    // Assert
    var optionsWindowController = mockedOptionsWindowComponentBuilder.build().optionsWindowController();
    var runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(mockedPlatformAdapter, atLeastOnce()).runLater(runnableArgumentCaptor.capture());
    executeLastRunnableInList(runnableArgumentCaptor.getAllValues());
    verify(optionsWindowController).afterViewShown();
  }

  @Test
  void showMainWindow_wrapsOptionsWindowsNodeInScene() {
    // Act
    sut.showOptionsWindow();

    // Assert
    var parent = mockedOptionsWindowComponentBuilder.build().optionsViewNode().get();
    verify(mockedSceneFactory).apply(parent);
  }


  private void executeLastRunnableInList(List<Runnable> allValues) {
    Collections.reverse(allValues);
    allValues.get(0).run();
  }
}
