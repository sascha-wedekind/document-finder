package com.bytedompteur.documentfinder.ui.dagger;

import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class FxmlLoaderFactory {

  private final Map<Class<? extends FxController>, Provider<FxController>> controllerFactoriesByClassMap;

  @Inject
  public FxmlLoaderFactory(Map<Class<? extends FxController>, Provider<FxController>> value) {
    this.controllerFactoriesByClassMap = value;
  }

  public Parent createParentNode(FxmlFile fxmlFile) {
    var fxmlLoader = createFxmlLoaderFor(fxmlFile.getResourcePathString());
    try {
      return fxmlLoader.load();
    } catch (IOException e) {
      log.error("Unable to load FXML resource '{}'", fxmlFile.getResourcePathString(), e);
      throw new RuntimeException(e);
    }
  }

  public Scene createScene(FxmlFile fxmlFile) {
    return createScene(fxmlFile.getResourcePathString());
  }

  protected Scene createScene(String fxmlResourceName) {
    var fxmlLoader = createFxmlLoaderFor(fxmlResourceName);
    try {
      fxmlLoader.load();
    } catch (IOException e) {
      log.error("Unable to load FXML resource '{}'", fxmlResourceName, e);
      throw new RuntimeException(e);
    }
    return new Scene(fxmlLoader.getRoot());
  }

  protected FXMLLoader createFxmlLoaderFor(String fxmlResourceName) {
    var url = getClass().getResource(fxmlResourceName);
    return new FXMLLoader(url, null, null, this::getControllerOrThrow);
  }

  private FxController getControllerOrThrow(Class<?> clazz) {
    if (controllerFactoriesByClassMap.containsKey(clazz)) {
      return controllerFactoriesByClassMap.get(clazz).get();
    } else {
      var msg = String.format("Requested FxController '%s' not contained in map", clazz);
      log.error(msg);
      throw new IllegalArgumentException(msg);
    }
  }
}
