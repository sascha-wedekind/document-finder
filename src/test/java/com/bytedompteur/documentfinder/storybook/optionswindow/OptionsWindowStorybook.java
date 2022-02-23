package com.bytedompteur.documentfinder.storybook.optionswindow;

import com.bytedompteur.documentfinder.storybook.mainwindow.DaggerMainWindowStorybookComponent;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.testfx.api.FxToolkit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class OptionsWindowStorybook {

  static CountDownLatch countDownLatch = new CountDownLatch(1);

  public static void main(String[] args) throws TimeoutException, InterruptedException {
    FxToolkit.registerPrimaryStage();
    FxToolkit.setupApplication(OptionsWindowPlaybookApplication::new);
    countDownLatch.await();
  }

  public static class OptionsWindowPlaybookApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
      primaryStage.initStyle(StageStyle.UNIFIED);
      primaryStage.setTitle(getClass().getSimpleName());

      var playbookComponent = DaggerOptionsWindowStorybookComponent.builder().build();
      var lazyNode = playbookComponent.optionsViewNode();
      var node = lazyNode.get();

      var button = new Button("Close Storybook Window");
      button.setStyle("-fx-background-color: #ff9100; ");
      button.setOnAction(e -> countDownLatch.countDown());
      button.setMaxWidth(Double.MAX_VALUE);

      var vBox = new VBox();
      vBox.getChildren().add(button);
      vBox.setFillWidth(true);

      var borderPane = new BorderPane();
      borderPane.setBottom(vBox);
      borderPane.setCenter(node);
      borderPane.autosize();

      Scene scene = new Scene(borderPane);
      primaryStage.setScene(scene);
      primaryStage.show();
    }

  }
}
