package com.bytedompteur.documentfinder.ui;

import com.bytedompteur.documentfinder.ui.dagger.FxmlLoaderFactory;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UITestInitHelper {

  public static void addNodeUnderTestToStage(FxmlFile fxmlFile, FxController controller, Stage stage) {
    var fxmlLoaderFactory = new FxmlLoaderFactory(Map.of(controller.getClass(), () -> controller));
    var node = fxmlLoaderFactory.createParentNode(fxmlFile);
    stage.setScene(new Scene(node, 300, 200));
  }

}
