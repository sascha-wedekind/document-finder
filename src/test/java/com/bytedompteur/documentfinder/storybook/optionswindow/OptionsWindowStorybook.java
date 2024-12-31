package com.bytedompteur.documentfinder.storybook.optionswindow;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    System.exit(0);
  }

  public static class OptionsWindowPlaybookApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
      primaryStage.initStyle(StageStyle.DECORATED);
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
      scene.getStylesheets().add(getClass().getResource("/css/default-dark.css").toExternalForm());
//      scene.getStylesheets().add(getClass().getResource("/css/default-light.css").toExternalForm());
      primaryStage.setScene(scene);
      primaryStage.show();
    }

  }
}
