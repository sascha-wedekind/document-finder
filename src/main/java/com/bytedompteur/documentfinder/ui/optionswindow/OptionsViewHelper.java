package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import javafx.scene.Parent;
import lombok.*;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@ToString
public abstract class OptionsViewHelper {

  public enum Name {FILE_TYPES_VIEW, FOLDER_VIEW, ABOUT_VIEW,GENERAL_OPTIONS_VIEW}

  @Getter
  private final Parent viewInstance;

  @Getter
  private final Name name;

  private final OptionsController controller;


  Flux<Object> cancelButtonClicked() {
    return controller.cancelButtonClicked();
  }


  Flux<Object> okButtonClicked() {
    return controller.okButtonClicked();
  }

  void beforeViewHide() {
    controller.beforeViewHide();
  }

  abstract void insertSettingsInController(Settings settings);

  abstract Settings extractSettingsFromController(Settings settings);
}
