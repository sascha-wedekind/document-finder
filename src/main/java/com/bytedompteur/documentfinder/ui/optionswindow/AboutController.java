package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowScope;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import static java.util.function.Predicate.not;

@Slf4j
@OptionsWindowScope
@SuppressWarnings("java:S1172")
public class AboutController extends BaseOptionsController {

  @Getter
  enum VersionInfoType {
    COMMIT_DATE("commit-date"),
    COMMIT("commit"),
    VERSION("version");

    private final String typeString;

    VersionInfoType(String typeString) {
      this.typeString = typeString;
    }
  }

  /**
   * Resource will be created during the build process. See 'build.gradle -> writeProperties()'
   */
  static final String RESOURCE_NAME = "/version-info.properties";

  @FXML
  public Label versionValueLabel;

  @FXML
  public Label commitValueLabel;

  @FXML
  public Label commitDateValueLabel;

  @Inject
  public AboutController(OkCancelButtonHandler okCancelButtonHandler) {
    super(okCancelButtonHandler);
  }

  @FXML
  public void initialize() {
    log.info("Trying to obtain stream for resource '{}'", RESOURCE_NAME);
    var stream = getClass().getResourceAsStream(RESOURCE_NAME);
    Properties properties = Optional
      .ofNullable(stream)
      .map(this::createPropertiesFromResource)
      .filter(not(Properties::isEmpty))
      .orElseGet(this::createDefaultProperties);

    versionValueLabel.setText(properties.getProperty(VersionInfoType.VERSION.getTypeString()));
    commitValueLabel.setText(properties.getProperty(VersionInfoType.COMMIT.getTypeString()));
    commitDateValueLabel.setText(properties.getProperty(VersionInfoType.COMMIT_DATE.getTypeString()));
  }

  public void handleOkButtonClick(ActionEvent ignored) {
    emitOkButtonClicked();
  }

  public void handleCancelButtonClick(ActionEvent ignored) {
    emitCancelButtonClicked();
  }

  private Properties createDefaultProperties() {
    var result = new Properties();
    result.setProperty(VersionInfoType.COMMIT_DATE.getTypeString(), createNoDataMessage(VersionInfoType.COMMIT_DATE));
    result.setProperty(VersionInfoType.COMMIT.getTypeString(), createNoDataMessage(VersionInfoType.COMMIT));
    result.setProperty(VersionInfoType.VERSION.getTypeString(), createNoDataMessage(VersionInfoType.VERSION));
    return result;
  }

  private String createNoDataMessage(VersionInfoType type) {
    return String.format("No %s in version-info or development build", type.getTypeString());
  }

  private Properties createPropertiesFromResource(InputStream it) {
    var result = new Properties();
    try {
      result.load(it);
    } catch (IOException e) {
      log.error("Unable to load resource '{}'. Using default properties", RESOURCE_NAME, e);
    }
    return result;
  }
}
