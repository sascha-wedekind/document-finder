package com.bytedompteur.documentfinder.ui.optionswindow;

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

  abstract void insertModificationContextInController(OptionsModificationContext context);

  abstract OptionsModificationContext extractModificationContextFromController(OptionsModificationContext context);
}
