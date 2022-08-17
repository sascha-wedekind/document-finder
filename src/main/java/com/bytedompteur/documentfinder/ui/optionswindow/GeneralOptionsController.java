package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowScope;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
@OptionsWindowScope
@SuppressWarnings("java:S1172")
public class GeneralOptionsController extends BaseOptionsController {

  @FXML
  public CheckBox debugLoggingEnabledCheckbox;

  @Inject
  public GeneralOptionsController(OkCancelButtonHandler okCancelButtonHandler) {
    super(okCancelButtonHandler);
  }

  @FXML
  public void initialize() {

  }


  public void setIsDebugLoggingEnabled(boolean isDebugLoggingEnabled) {
    debugLoggingEnabledCheckbox.setSelected(isDebugLoggingEnabled);
  }

  public boolean isDebugLoggingEnabled() {
    return debugLoggingEnabledCheckbox.isSelected();
  }


  public void handleOkButtonClick(ActionEvent ignore) {
    emitOkButtonClicked();
  }

  public void handleCancelButtonClick(ActionEvent ignore) {
    emitCancelButtonClicked();
  }
}
