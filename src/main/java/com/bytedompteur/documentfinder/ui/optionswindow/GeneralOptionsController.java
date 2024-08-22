package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowScope;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;

@Slf4j
@OptionsWindowScope
@SuppressWarnings("java:S1172")
public class GeneralOptionsController extends BaseOptionsController {

  private boolean forceIndexRebuild;

  @FXML
  public CheckBox debugLoggingEnabledCheckbox;
  @FXML
  public CheckBox runOnStartupCheckbox;

  @Inject
  public GeneralOptionsController(OkCancelButtonHandler okCancelButtonHandler) {
    super(okCancelButtonHandler);
  }

  @FXML
  public void initialize() {
    // ignore
  }


  public void setIsDebugLoggingEnabled(boolean isDebugLoggingEnabled) {
    debugLoggingEnabledCheckbox.setSelected(isDebugLoggingEnabled);
  }

  public boolean isDebugLoggingEnabled() {
    return debugLoggingEnabledCheckbox.isSelected();
  }


  public boolean isRunOnStartup() {
    return runOnStartupCheckbox.isSelected();
  }

  public boolean isForceIndexRebuild() {
    return forceIndexRebuild;
  }

  public void setRunOnStartup(boolean value) {
    runOnStartupCheckbox.setSelected(value);
  }

  public void handleOkButtonClick(ActionEvent ignore) {
    emitOkButtonClicked();
  }

  public void handleCancelButtonClick(ActionEvent ignore) {
    emitCancelButtonClicked();
  }

  public void handleRebuildSearchIndexButtonClick(ActionEvent ignore) {
    this.forceIndexRebuild = true;
    emitOkButtonClicked();
  }
}
